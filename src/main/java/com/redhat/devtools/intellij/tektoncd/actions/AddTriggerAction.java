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

import com.intellij.notification.Notification;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;
import com.redhat.devtools.intellij.common.utils.ExecHelper;
import com.redhat.devtools.intellij.common.utils.UIHelper;
import com.redhat.devtools.intellij.tektoncd.tkn.Resource;
import com.redhat.devtools.intellij.tektoncd.tkn.Run;
import com.redhat.devtools.intellij.tektoncd.tkn.Tkn;
import com.redhat.devtools.intellij.tektoncd.tree.ParentableNode;
import com.redhat.devtools.intellij.tektoncd.tree.PipelineNode;
import com.redhat.devtools.intellij.tektoncd.tree.TaskNode;
import com.redhat.devtools.intellij.tektoncd.ui.wizard.addtrigger.AddTriggerWizard;
import com.redhat.devtools.intellij.tektoncd.utils.StartResourceModel;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.tree.TreePath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
            StartResourceModel model;
            try {
                resources = tkncli.getResources(namespace);
                serviceAccounts = tkncli.getServiceAccounts(namespace);
                secrets = tkncli.getSecrets(namespace);
                configMaps = tkncli.getConfigMaps(namespace);
                persistentVolumeClaims = tkncli.getPersistentVolumeClaim(namespace);
                triggerBindings = tkncli.getTriggerBindings(namespace);
                model = getModel(element, namespace, tkncli, resources, serviceAccounts, secrets, configMaps, persistentVolumeClaims);
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

            boolean noInputsAndOuputs = model.getInputs().isEmpty() && model.getOutputs().isEmpty() && model.getWorkspaces().isEmpty();
            AddTriggerWizard addTriggerWizard = null;

            if (!noInputsAndOuputs) {
                addTriggerWizard = UIHelper.executeInUI(() -> {
                    String titleDialog;
                    if (element instanceof PipelineNode) {
                        titleDialog = "Pipeline " + element.getName();
                    } else {
                        titleDialog = "Task " + element.getName();
                    }
                    AddTriggerWizard wizard = new AddTriggerWizard(titleDialog, element, getEventProject(anActionEvent), model, triggerBindings);
                    wizard.show();
                    return wizard;
                });
            }
            if (noInputsAndOuputs || addTriggerWizard.isOK()) {
               /* try {
                    String serviceAccount = model.getServiceAccount();
                    Map<String, String> taskServiceAccount = model.getTaskServiceAccounts();
                    Map<String, String> params = model.getParameters();
                    Map<String, Workspace> workspaces = model.getWorkspaces();
                    Map<String, String> inputResources = model.getInputResources();
                    Map<String, String> outputResources = model.getOutputResources();
                    String runPrefixName = model.getRunPrefixName();
                    String runName = null;
                    if (model.getKind().equalsIgnoreCase(KIND_PIPELINE)) {
                        runName = tkncli.startPipeline(namespace, model.getName(), params, inputResources, serviceAccount, taskServiceAccount, workspaces, runPrefixName);

                    } else if (model.getKind().equalsIgnoreCase(KIND_TASK)) {
                        runName = tkncli.startTask(namespace, model.getName(), params, inputResources, outputResources, serviceAccount, workspaces, runPrefixName);
                    }
                    if(runName != null) {
                        FollowLogsAction followLogsAction = (FollowLogsAction) ActionManager.getInstance().getAction("FollowLogsAction");
                        followLogsAction.actionPerformed(namespace, runName, element.getClass(), tkncli);
                    }

                    ParentableNode nodeToRefresh = element;
                    if (element instanceof PipelineRunNode || element instanceof TaskRunNode) {
                        nodeToRefresh = (ParentableNode) element.getParent();
                    }
                    ((TektonTreeStructure)getTree(anActionEvent).getClientProperty(Constants.STRUCTURE_PROPERTY)).fireModified(nodeToRefresh);
                } catch (IOException e) {
                    notification = new Notification(NOTIFICATION_ID,
                            "Error",
                            model.getName() + " in namespace " + namespace + " failed to start\n" + e.getLocalizedMessage(),
                            NotificationType.ERROR);
                    Notifications.Bus.notify(notification);
                    logger.warn("Error: " + e.getLocalizedMessage());
                }*/
            }
        });
    }

    protected StartResourceModel getModel(ParentableNode element, String namespace, Tkn tkncli, List<Resource> resources, List<String> serviceAccounts, List<String> secrets, List<String> configMaps, List<String> persistentVolumeClaims) throws IOException {
        String configuration = "";
        List<? extends Run> runs = new ArrayList<>();
        if (element instanceof PipelineNode) {
            configuration = tkncli.getPipelineYAML(namespace, element.getName());
            runs = tkncli.getPipelineRuns(namespace, element.getName());
        } else if (element instanceof TaskNode) {
            configuration = tkncli.getTaskYAML(namespace, element.getName());
            runs = tkncli.getTaskRuns(namespace, element.getName());
        }
        return new StartResourceModel(configuration, resources, serviceAccounts, secrets, configMaps, persistentVolumeClaims, runs);
    }
}
