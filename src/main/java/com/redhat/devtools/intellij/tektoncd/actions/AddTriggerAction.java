/*******************************************************************************
 * Copyright (c) 2020 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc.
 ******************************************************************************/
package com.redhat.devtools.intellij.tektoncd.actions;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;
import com.redhat.devtools.intellij.common.utils.ExecHelper;
import com.redhat.devtools.intellij.common.utils.UIHelper;
import com.redhat.devtools.intellij.common.utils.YAMLHelper;
import com.redhat.devtools.intellij.tektoncd.telemetry.TelemetryService;
import com.redhat.devtools.intellij.tektoncd.tkn.Resource;
import com.redhat.devtools.intellij.tektoncd.tkn.Tkn;
import com.redhat.devtools.intellij.tektoncd.tree.ParentableNode;
import com.redhat.devtools.intellij.tektoncd.tree.PipelineNode;
import com.redhat.devtools.intellij.tektoncd.tree.TaskNode;
import com.redhat.devtools.intellij.tektoncd.ui.wizard.addtrigger.AddTriggerWizard;
import com.redhat.devtools.intellij.tektoncd.utils.CRDHelper;
import com.redhat.devtools.intellij.tektoncd.utils.SnippetHelper;
import com.redhat.devtools.intellij.tektoncd.utils.TreeHelper;
import com.redhat.devtools.intellij.tektoncd.utils.Utils;
import com.redhat.devtools.intellij.tektoncd.utils.YAMLBuilder;
import com.redhat.devtools.intellij.tektoncd.utils.model.actions.AddTriggerModel;
import io.fabric8.kubernetes.api.model.GenericKubernetesResource;
import io.fabric8.kubernetes.api.model.GenericKubernetesResourceList;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.tree.TreePath;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_EVENTLISTENERS;
import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_TRIGGERBINDINGS;
import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_TRIGGERTEMPLATES;
import static com.redhat.devtools.intellij.tektoncd.Constants.NOTIFICATION_ID;
import static com.redhat.devtools.intellij.tektoncd.telemetry.TelemetryService.NAME_PREFIX_CRUD;
import static com.redhat.devtools.intellij.tektoncd.telemetry.TelemetryService.PROP_RESOURCE_KIND;
import static com.redhat.devtools.intellij.tektoncd.telemetry.TelemetryService.VALUE_ABORTED;
import static com.redhat.devtools.intellij.telemetry.core.service.TelemetryMessageBuilder.ActionMessage;
import static com.redhat.devtools.intellij.telemetry.core.util.AnonymizeUtils.anonymizeResource;

public class AddTriggerAction extends TektonAction {
    private static final Logger logger = LoggerFactory.getLogger(AddTriggerAction.class);

    public AddTriggerAction() { super(PipelineNode.class, TaskNode.class); }

    @Override
    public boolean isVisible(Object selected) {
        // if triggers are not installed, don't show this action
        return ((selected instanceof PipelineNode || selected instanceof TaskNode)
                && ((ParentableNode)selected).getRoot().getTkn().isTektonTriggersAware());
    }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent, TreePath path, Object selected, Tkn tkncli) {
        ActionMessage telemetry = TelemetryService.instance().action(NAME_PREFIX_CRUD + "add trigger");
        ParentableNode element = getElement(selected);
        String namespace = element.getNamespace();
        ExecHelper.submit(() -> {
            try {
                String triggerVersion = tkncli.getTektonTriggersApiVersion();
                Map<String, String> triggerBindingTemplates = SnippetHelper.getTriggerBindingTemplates(triggerVersion);
                AddTriggerModel model = createModel(element, namespace, tkncli);

                if (!model.isValid()) {
                    UIHelper.executeInUI(() -> Messages.showErrorDialog(model.getErrorMessage(), "Error"));
                    return;
                }

                String kind = (element instanceof PipelineNode) ? "Pipeline " : "Task ";
                telemetry.property(PROP_RESOURCE_KIND, kind);
                AddTriggerWizard addTriggerWizard = openTriggerBindingWizard(anActionEvent, element, triggerBindingTemplates, model, kind);
                if (!addTriggerWizard.isOK()) {
                    telemetry.result(VALUE_ABORTED)
                            .send();
                    return;
                }
                createNewVolumes(model.getWorkspaces(), tkncli);
                // take/create all triggerBindings
                Map<String, String> triggerBindingsSelected = model.getBindingsSelectedByUser();
                saveTriggerBindings(triggerBindingsSelected, namespace, tkncli);

                // get all params from bindings
                Set<String> paramsFromBindings = model.extractVariablesFromSelectedBindings();
                // interpolate the variables correctly $variable to $(tt.params.variable)
                normalizeVariablesInterpolation(model, paramsFromBindings);
                // create the triggerTemplate
                String randomString = Utils.getRandomString(6);
                String triggerTemplateName = element.getName() + "-template-" + randomString;
                ObjectNode run = createNode(element, model);
                String triggerApiVersion = tkncli.getTektonTriggersApiVersion();
                ObjectNode triggerTemplate = YAMLBuilder.createTriggerTemplate(triggerTemplateName, triggerApiVersion, new ArrayList<>(paramsFromBindings), Arrays.asList(run));
                saveResource(YAMLHelper.JSONToYAML(triggerTemplate), namespace, KIND_TRIGGERTEMPLATES, tkncli);
                notifySuccessOperation("TriggerTemplate " + triggerTemplateName);

                // create the eventListener
                String eventListenerName = element.getName() + "-listener-" + randomString;
                // TODO we are using the default pipeline serviceAccount but we should allow users to select the one they prefer
                ObjectNode eventListener = YAMLBuilder.createEventListener(eventListenerName, triggerApiVersion, "pipeline", triggerBindingsSelected.keySet().stream()
                        .map(binding -> binding.replace(" NEW", ""))
                        .collect(Collectors.toList()), triggerTemplateName);
                saveResource(YAMLHelper.JSONToYAML(eventListener), namespace, KIND_EVENTLISTENERS, tkncli);
                telemetry.result("bindings and resources created")
                        .send();
                notifySuccessOperation("EventListener " + eventListenerName);
                TreeHelper.refresh(getEventProject(anActionEvent), (ParentableNode) ((ParentableNode) element.getParent()).getParent());
            } catch (IOException e) {
                String errorMessage = "Failed to add a trigger to " + element.getName() + " in namespace " + namespace + "\n" + e.getLocalizedMessage();
                telemetry
                        .error(anonymizeResource(element.getName(), namespace, errorMessage))
                        .send();
                Notification notification = new Notification(NOTIFICATION_ID,
                        "Error",
                        errorMessage,
                        NotificationType.ERROR);
                Notifications.Bus.notify(notification);
                logger.warn(errorMessage, e);
            }
        });
    }

    private AddTriggerWizard openTriggerBindingWizard(AnActionEvent anActionEvent, ParentableNode element, Map<String, String> triggerBindingTemplates, AddTriggerModel model, String kind) {
        return UIHelper.executeInUI(() -> {
            String titleDialog = "Add Trigger to " + kind + " " + element.getName();
            AddTriggerWizard wizard = new AddTriggerWizard(titleDialog, getEventProject(anActionEvent), model, triggerBindingTemplates);
            wizard.show();
            return wizard;
        });
    }

    private void saveTriggerBindings(Map<String, String> triggerBindingsSelected, String namespace, Tkn tkncli) {
        triggerBindingsSelected.keySet().stream().filter(binding -> binding.endsWith(" NEW")).forEach(binding -> {
            try {
                String bindingBody = triggerBindingsSelected.get(binding);
                saveResource(bindingBody, namespace, KIND_TRIGGERBINDINGS, tkncli);
                String nameBinding = YAMLHelper.getStringValueFromYAML(bindingBody, new String[] {"metadata", "name"});
                notifySuccessOperation("TriggerBinding " + nameBinding);
            } catch (IOException e) {
                logger.warn(e.getLocalizedMessage(), e);
            }
        });
    }

    private ObjectNode createNode(ParentableNode element, AddTriggerModel model) {
        ObjectNode run;
        if (element instanceof PipelineNode) {
            run = YAMLBuilder.createPipelineRun(model);
        } else {
            run = YAMLBuilder.createTaskRun(model);
        }
        return run;
    }

    protected AddTriggerModel createModel(ParentableNode element, String namespace, Tkn tkncli) throws IOException {
        List<String> serviceAccounts = tkncli.getServiceAccounts(namespace);
        List<String> secrets = tkncli.getSecrets(namespace);
        List<String> configMaps = tkncli.getConfigMaps(namespace);
        List<String> persistentVolumeClaims = tkncli.getPersistentVolumeClaim(namespace);
        Map<String, String> triggerBindings = getTriggerBindings(namespace, tkncli);
        String configuration = getConfiguration(element, namespace, tkncli);
        return new AddTriggerModel(configuration, serviceAccounts, secrets, configMaps, persistentVolumeClaims, triggerBindings);
    }

    private String getConfiguration(ParentableNode element, String namespace, Tkn tkncli) throws IOException {
        if (element instanceof PipelineNode) {
            return tkncli.getPipelineYAML(namespace, element.getName());
        } else if (element instanceof TaskNode) {
            return tkncli.getTaskYAML(namespace, element.getName());
        } else {
            return null;
        }
    }

    protected void saveResource(String resourceBody, String namespace, String kind_plural, Tkn tkncli) throws IOException {
        String name = YAMLHelper.getStringValueFromYAML(resourceBody, new String[]{"metadata", "name"});
        String apiVersion = YAMLHelper.getStringValueFromYAML(resourceBody, new String[]{"apiVersion"});
        JsonNode spec = YAMLHelper.getValueFromYAML(resourceBody, new String[]{"spec"});
        CustomResourceDefinitionContext crdContext = CRDHelper.getCRDContext(apiVersion, kind_plural);

        try {
            String resourceNamespace = CRDHelper.isClusterScopedResource(kind_plural) ? "" : namespace;
            GenericKubernetesResource resource = tkncli.getCustomResource(resourceNamespace, name, crdContext);
            if (resource == null) {
                tkncli.createCustomResource(resourceNamespace, crdContext, resourceBody);
            } else {
                JsonNode customResource = CRDHelper.convertToJsonNode(resource);
                ((ObjectNode) customResource).set("spec", spec);
                tkncli.editCustomResource(resourceNamespace, name, crdContext, customResource.toString());
            }
        } catch (KubernetesClientException e) {
            throw new IOException(e);
        }
    }

    private Map<String, String> getTriggerBindings(String namespace, Tkn tkncli) {
        Map<String, String> triggerBindingsOnCluster = new HashMap<>();
        GenericKubernetesResourceList allTriggerBindings = tkncli.getCustomResources(namespace, CRDHelper.getCRDContext("triggers.tekton.dev/v1beta1", "triggerbindings"));
        if (allTriggerBindings == null) {
            return triggerBindingsOnCluster;
        }
        allTriggerBindings.getItems().forEach(binding -> {
            try {
                String bindingAsYAML = YAMLBuilder.writeValueAsString(binding);
                triggerBindingsOnCluster.put(binding.getMetadata().getName(), bindingAsYAML);
            } catch (IOException e) {
                logger.warn(e.getLocalizedMessage(), e);
            }
        });
        return triggerBindingsOnCluster;
    }

    private void normalizeVariablesInterpolation(AddTriggerModel model, Set<String> variables) {
        model.getParams().forEach(param -> {
            if (param.value().startsWith("$")
                && (variables.contains(param.value().substring(1)))) {
                    param.setValue("$(tt.params." + param.value().substring(1) + ")");
            }
        });
    }

    private void notifySuccessOperation(String nameResourceCreated) {
        Notification notification = new Notification(NOTIFICATION_ID, "Save Successful", nameResourceCreated + " has been successfully created!", NotificationType.INFORMATION);
        Notifications.Bus.notify(notification);
    }
}
