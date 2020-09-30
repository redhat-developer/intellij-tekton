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
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
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
import com.redhat.devtools.intellij.tektoncd.tkn.Run;
import com.redhat.devtools.intellij.tektoncd.tkn.Tkn;
import com.redhat.devtools.intellij.tektoncd.tkn.component.field.Workspace;
import com.redhat.devtools.intellij.tektoncd.tree.ParentableNode;
import com.redhat.devtools.intellij.tektoncd.tree.PipelineNode;
import com.redhat.devtools.intellij.tektoncd.tree.TaskNode;
import com.redhat.devtools.intellij.tektoncd.ui.wizard.addtrigger.AddTriggerWizard;
import com.redhat.devtools.intellij.tektoncd.utils.CRDHelper;
import com.redhat.devtools.intellij.tektoncd.utils.SnippetHelper;
import com.redhat.devtools.intellij.tektoncd.utils.YAMLBuilder;
import com.redhat.devtools.intellij.tektoncd.utils.model.actions.AddTriggerModel;
import com.redhat.devtools.intellij.tektoncd.utils.model.actions.StartResourceModel;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext;
import io.fabric8.tekton.client.TektonClient;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.swing.tree.TreePath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


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
            List<String> serviceAccounts, secrets, configMaps, persistentVolumeClaims, triggerBindings;
            Map<String, String> triggerBindingTemplates;
            AddTriggerModel model;
            try {
                resources = tkncli.getResources(namespace);
                serviceAccounts = tkncli.getServiceAccounts(namespace);
                secrets = tkncli.getSecrets(namespace);
                configMaps = tkncli.getConfigMaps(namespace);
                persistentVolumeClaims = tkncli.getPersistentVolumeClaim(namespace);
                triggerBindings = tkncli.getTriggerBindings(namespace);
                triggerBindingTemplates = SnippetHelper.getTriggerBindingTemplates();
                model = getModel(element, namespace, tkncli, resources, serviceAccounts, secrets, configMaps, persistentVolumeClaims, triggerBindings);
            } catch (IOException e) {
                UIHelper.executeInUI(() ->
                        Messages.showErrorDialog(
                                element.getName() + " in namespace " + namespace + " failed to start. An error occurred while retrieving information.\n" + e.getLocalizedMessage(),
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
                    titleDialog = "Pipeline " + element.getName();
                } else {
                    titleDialog = "Task " + element.getName();
                }
                AddTriggerWizard wizard = new AddTriggerWizard(titleDialog, getEventProject(anActionEvent), model, triggerBindingTemplates);
                wizard.show();
                return wizard;
            });

            if (addTriggerWizard.isOK()) {
               try {
                   List<String> triggerBindingsSelected = new ArrayList<>(model.getBindingsSelectedByUser().keySet());
                   String newBindingAdded = model.getNewBindingAdded();
                   if (!newBindingAdded.isEmpty()) {
                       //TODO create new binding
                       saveResource(newBindingAdded, namespace, "triggerbindings", tkncli);
                   }

                   // TODO create new triggerTemplate
                String triggerTemplateName = element.getName() + "-template";
                   ObjectNode pipelineRun = YAMLBuilder.createPipelineRun(element.getName(), model);
                   ObjectNode triggerTemplate = YAMLBuilder.createTriggerTemplate(triggerTemplateName, Collections.emptyList(), Arrays.asList(pipelineRun));
                saveResource(YAMLBuilder.writeValueAsString(triggerTemplate), namespace, "triggertemplates", tkncli);

                    // TODO create new eventListener
                ObjectNode eventListener = YAMLBuilder.createEventListener(element.getName() + "-listener", "", triggerBindingsSelected, triggerTemplateName);
                   saveResource(YAMLBuilder.writeValueAsString(eventListener), namespace, "eventlisteners", tkncli);

                } catch (IOException e) {
                    Notification notification = new Notification(NOTIFICATION_ID,
                            "Error",
                            model.getName() + " in namespace " + namespace + " failed to start\n" + e.getLocalizedMessage(),
                            NotificationType.ERROR);
                    Notifications.Bus.notify(notification);
                    logger.warn("Error: " + e.getLocalizedMessage());
                }
            }
        });
    }

    protected AddTriggerModel getModel(ParentableNode element, String namespace, Tkn tkncli, List<Resource> resources, List<String> serviceAccounts, List<String> secrets, List<String> configMaps, List<String> persistentVolumeClaims, List<String> triggerBindings) throws IOException {
        String configuration = "";
        if (element instanceof PipelineNode) {
            configuration = tkncli.getPipelineYAML(namespace, element.getName());
        }
        /* else if (element instanceof TaskNode) { // uncomment to extend to tasks
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
}
