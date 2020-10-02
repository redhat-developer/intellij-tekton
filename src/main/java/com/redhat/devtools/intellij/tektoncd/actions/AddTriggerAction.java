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
import com.redhat.devtools.intellij.common.utils.JSONHelper;
import com.redhat.devtools.intellij.common.utils.UIHelper;
import com.redhat.devtools.intellij.common.utils.YAMLHelper;
import com.redhat.devtools.intellij.tektoncd.tkn.Resource;
import com.redhat.devtools.intellij.tektoncd.tkn.Tkn;
import com.redhat.devtools.intellij.tektoncd.tree.ParentableNode;
import com.redhat.devtools.intellij.tektoncd.tree.PipelineNode;
import com.redhat.devtools.intellij.tektoncd.ui.wizard.addtrigger.AddTriggerWizard;
import com.redhat.devtools.intellij.tektoncd.utils.CRDHelper;
import com.redhat.devtools.intellij.tektoncd.utils.SnippetHelper;
import com.redhat.devtools.intellij.tektoncd.utils.TektonVirtualFileManager;
import com.redhat.devtools.intellij.tektoncd.utils.Utils;
import com.redhat.devtools.intellij.tektoncd.utils.YAMLBuilder;
import com.redhat.devtools.intellij.tektoncd.utils.model.actions.AddTriggerModel;
import com.redhat.devtools.intellij.tektoncd.utils.model.resources.TriggerBindingConfigurationModel;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.tree.TreePath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_PLURAL;
import static com.redhat.devtools.intellij.tektoncd.Constants.NOTIFICATION_ID;

public class AddTriggerAction extends TektonAction {
    Logger logger = LoggerFactory.getLogger(AddTriggerAction.class);

    public AddTriggerAction() { super(PipelineNode.class); }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent, TreePath path, Object selected, Tkn tkncli) {
        ParentableNode element = getElement(selected);
        String namespace = element.getNamespace();
        ExecHelper.submit(() -> {
            List<Resource> resources;
            List<String> serviceAccounts, secrets, configMaps, persistentVolumeClaims;
            Map<String, String> triggerBindings, triggerBindingTemplates;
            AddTriggerModel model;
            try {
                resources = tkncli.getResources(namespace);
                serviceAccounts = tkncli.getServiceAccounts(namespace);
                secrets = tkncli.getSecrets(namespace);
                configMaps = tkncli.getConfigMaps(namespace);
                persistentVolumeClaims = tkncli.getPersistentVolumeClaim(namespace);
                triggerBindings = getTriggerBindings(namespace, tkncli);
                triggerBindingTemplates = SnippetHelper.getTriggerBindingTemplates();
                model = getModel(element, namespace, tkncli, resources, serviceAccounts, secrets, configMaps, persistentVolumeClaims, triggerBindings);
            } catch (IOException e) {
                UIHelper.executeInUI(() ->
                        Messages.showErrorDialog(
                                "Failed to add a trigger to " + element.getName() + " in namespace " + namespace + ". An error occurred while retrieving informations.\n" + e.getLocalizedMessage(),
                                "Error"));
                logger.warn("Error: " + e.getLocalizedMessage());
                return;
            }

            if (!model.isValid()) {
                UIHelper.executeInUI(() -> Messages.showErrorDialog(model.getErrorMessage(), "Error"));
                return;
            }

            AddTriggerWizard addTriggerWizard = UIHelper.executeInUI(() -> {
                String titleDialog;
                if (element instanceof PipelineNode) {
                    titleDialog = "Add Trigger to Pipeline " + element.getName();
                } else {
                    titleDialog = "Add Trigger to Task " + element.getName();
                }
                AddTriggerWizard wizard = new AddTriggerWizard(titleDialog, getEventProject(anActionEvent), model, triggerBindingTemplates);
                wizard.show();
                return wizard;
            });

            if (addTriggerWizard.isOK()) {
               try {
                   // take/create all triggerBindings
                   Map<String, String> triggerBindingsSelected = model.getBindingsSelectedByUser();
                   String newBindingAdded = model.getNewBindingAdded();
                   if (!newBindingAdded.isEmpty()) {
                       saveResource(newBindingAdded, namespace, "triggerbindings", tkncli);
                       String nameBinding = YAMLHelper.getStringValueFromYAML(newBindingAdded, new String[] {"metadata", "name"});
                       notifySuccessOperation("TriggerBinding " + nameBinding);
                       // add the new binding to the list of bindings
                       triggerBindingsSelected.put(nameBinding, newBindingAdded);
                   }

                   // get all params from bindings
                   Set<String> paramsFromBindings = model.extractVariablesFromSelectedBindings();

                   String randomString = Utils.getRandomString(6);
                   // create the triggerTemplate
                   String triggerTemplateName = element.getName() + "-template-" + randomString;
                   ObjectNode pipelineRun = YAMLBuilder.createPipelineRun(element.getName(), model); //TODO need to support taskrun as well
                   ObjectNode triggerTemplate = YAMLBuilder.createTriggerTemplate(triggerTemplateName, new ArrayList<>(paramsFromBindings), Arrays.asList(pipelineRun));
                   saveResource(YAMLBuilder.writeValueAsString(triggerTemplate), namespace, "triggertemplates", tkncli);
                   notifySuccessOperation("TriggerTemplate " + triggerTemplateName);

                   // create the eventListener
                   String eventListenerName = element.getName() + "-listener-" + randomString;
                   // TODO we are using the default pipeline serviceAccount but we should allow users to select the one they prefer
                   ObjectNode eventListener = YAMLBuilder.createEventListener(eventListenerName, "pipeline", new ArrayList<>(triggerBindingsSelected.keySet()), triggerTemplateName);
                   saveResource(YAMLBuilder.writeValueAsString(eventListener), namespace, "eventlisteners", tkncli);
                   notifySuccessOperation("EventListener " + eventListenerName);

               } catch (IOException e) {
                   Notification notification = new Notification(NOTIFICATION_ID,
                           "Error",
                           "Failed to add a trigger to " + element.getName() + " in namespace " + namespace + "\n" + e.getLocalizedMessage(),
                           NotificationType.ERROR);
                   Notifications.Bus.notify(notification);
                   logger.warn("Error: " + e.getLocalizedMessage());
               }
            }
        });
    }

    protected AddTriggerModel getModel(ParentableNode element, String namespace, Tkn tkncli, List<Resource> resources, List<String> serviceAccounts, List<String> secrets, List<String> configMaps, List<String> persistentVolumeClaims, Map<String, String> triggerBindings) throws IOException {
        String configuration = "";
        if (element instanceof PipelineNode) {
            configuration = tkncli.getPipelineYAML(namespace, element.getName());
        }
        /* else if (element instanceof TaskNode) { // TODO uncomment to extend to tasks
            configuration = tkncli.getTaskYAML(namespace, element.getName());
        } */
        return new AddTriggerModel(configuration, resources, serviceAccounts, secrets, configMaps, persistentVolumeClaims, triggerBindings);
    }

    protected void saveResource(String resourceBody, String namespace, String kind_plural, Tkn tkncli) throws IOException{
        String name = YAMLHelper.getStringValueFromYAML(resourceBody, new String[] {"metadata", "name"});
        String apiVersion = YAMLHelper.getStringValueFromYAML(resourceBody, new String[] {"apiVersion"});
        JsonNode spec = YAMLHelper.getValueFromYAML(resourceBody, new String[] {"spec"});
        CustomResourceDefinitionContext crdContext = CRDHelper.getCRDContext(apiVersion, kind_plural);

        try {
            String resourceNamespace = CRDHelper.isClusterScopedResource(kind_plural) ? "" : namespace;
            Map<String, Object> resource = tkncli.getCustomResource(resourceNamespace, name, crdContext);
            if (resource == null) {
                tkncli.createCustomResource(resourceNamespace, crdContext, resourceBody);
            } else {
                JsonNode customResource = JSONHelper.MapToJSON(resource);
                ((ObjectNode) customResource).set("spec", spec);
                tkncli.editCustomResource(resourceNamespace, name, crdContext, customResource.toString());
            }
        } catch (KubernetesClientException e) {
            throw new IOException(e.getLocalizedMessage());
        }
    }

    private Map<String, String> getTriggerBindings(String namespace, Tkn tkncli) {
        Map<String, String> triggerBindingsOnCluster = new HashMap<>();
        Map<String, Object> allTriggerBindings = tkncli.getCustomResources(namespace, CRDHelper.getCRDContext("triggers.tekton.dev/v1alpha1", "triggerbindings"));
        if (allTriggerBindings == null) {
            return triggerBindingsOnCluster;
        }
        List<Map<String, Object>> triggerBindingsItems = (List<Map<String, Object>>) allTriggerBindings.get("items");
        triggerBindingsItems.forEach(binding -> {
            try {
                String bindingAsYAML = YAMLBuilder.writeValueAsString(binding);
                triggerBindingsOnCluster.put(((Map<String, Object>)binding.get("metadata")).get("name").toString(), bindingAsYAML);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        return triggerBindingsOnCluster;
    }

    private void notifySuccessOperation(String nameResourceCreated) {
        Notification notification = new Notification(NOTIFICATION_ID, "Save Successful", nameResourceCreated + " has been successfully created!", NotificationType.INFORMATION);
        Notifications.Bus.notify(notification);
    }
}
