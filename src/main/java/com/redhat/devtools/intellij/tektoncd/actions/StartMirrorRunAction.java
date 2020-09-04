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

import com.google.common.base.Strings;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;
import com.redhat.devtools.intellij.common.utils.ExecHelper;
import com.redhat.devtools.intellij.common.utils.UIHelper;
import com.redhat.devtools.intellij.common.utils.YAMLHelper;
import com.redhat.devtools.intellij.tektoncd.Constants;
import com.redhat.devtools.intellij.tektoncd.actions.logs.FollowLogsAction;
import com.redhat.devtools.intellij.tektoncd.tkn.Resource;
import com.redhat.devtools.intellij.tektoncd.tkn.Tkn;
import com.redhat.devtools.intellij.tektoncd.tkn.component.field.Workspace;
import com.redhat.devtools.intellij.tektoncd.tree.ParentableNode;
import com.redhat.devtools.intellij.tektoncd.tree.PipelineNode;
import com.redhat.devtools.intellij.tektoncd.tree.PipelineRunNode;
import com.redhat.devtools.intellij.tektoncd.tree.TaskNode;
import com.redhat.devtools.intellij.tektoncd.tree.TaskRunNode;
import com.redhat.devtools.intellij.tektoncd.tree.TektonTreeStructure;
import com.redhat.devtools.intellij.tektoncd.ui.wizard.StartWizard;
import com.redhat.devtools.intellij.tektoncd.utils.StartResourceModel;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import javax.swing.tree.TreePath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import static com.redhat.devtools.intellij.tektoncd.Constants.NOTIFICATION_ID;

public class StartMirrorRunAction extends TektonAction {
    private static final Logger logger = LoggerFactory.getLogger(StartMirrorRunAction.class);

    public StartMirrorRunAction() { super(PipelineRunNode.class, TaskRunNode.class); }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent, TreePath path, Object selected, Tkn tkncli) {
        ParentableNode element = getElement(selected);
        String namespace = element.getNamespace();
        ExecHelper.submit(() -> {
            String configuration = null;
            String runConfiguration = null;
            Notification notification;
            List<Resource> resources;
            List<String> serviceAccounts, secrets, configMaps, persistentVolumeClaims;
            try {
                if (element instanceof PipelineRunNode) {
                    runConfiguration = tkncli.getPipelineRunYAML(namespace, element.getName());
                    String pipeline = YAMLHelper.getStringValueFromYAML(runConfiguration, new String[] {"metadata", "labels", "tekton.dev/pipeline"});
                    if (Strings.isNullOrEmpty(pipeline)) {
                        return;
                    }
                    configuration = tkncli.getPipelineYAML(namespace, pipeline);
                } else if (element instanceof TaskRunNode) {
                    runConfiguration = tkncli.getTaskRunYAML(namespace, element.getName());
                    String task = YAMLHelper.getStringValueFromYAML(runConfiguration, new String[] {"metadata", "labels", "tekton.dev/task"});
                    if (Strings.isNullOrEmpty(task)) {
                        return;
                    }
                    configuration = tkncli.getTaskYAML(namespace, task);
                }
                resources = tkncli.getResources(namespace);
                serviceAccounts = tkncli.getServiceAccounts(namespace);
                secrets = tkncli.getSecrets(namespace);
                configMaps = tkncli.getConfigMaps(namespace);
                persistentVolumeClaims = tkncli.getPersistentVolumeClaim(namespace);
            } catch (IOException e) {
                UIHelper.executeInUI(() ->
                        Messages.showErrorDialog(
                                element.getName() + " in namespace " + namespace + " failed to start. An error occurred while retrieving information.\n" + e.getLocalizedMessage(),
                                "Error"));
                logger.warn("Error: " + e.getLocalizedMessage());
                return;
            }

            StartResourceModel model = new StartResourceModel(configuration, resources, serviceAccounts, secrets, configMaps, persistentVolumeClaims);

            if (!model.isValid()) {
                UIHelper.executeInUI(() -> Messages.showErrorDialog(model.getErrorMessage(), "Error"));
                return;
            }

            boolean noInputsAndOuputs = model.getInputs().isEmpty() && model.getOutputs().isEmpty() && model.getWorkspaces().isEmpty();
            StartWizard startWizard = null;

            if (!noInputsAndOuputs) {
                startWizard = UIHelper.executeInUI(() -> {
                    String titleDialog;
                    if (element instanceof PipelineNode) {
                        titleDialog = "Pipeline " + element.getName();
                    } else {
                        titleDialog = "Task " + element.getName();
                    }
                    StartWizard wizard = new StartWizard(titleDialog, getEventProject(anActionEvent), model);
                    wizard.show();
                    return wizard;
                });
            }
            if (noInputsAndOuputs || startWizard.isOK()) {
                try {
                    String serviceAccount = model.getServiceAccount();
                    Map<String, String> taskServiceAccount = model.getTaskServiceAccounts();
                    Map<String, String> params = model.getParameters();
                    Map<String, Workspace> workspaces = model.getWorkspaces();
                    Map<String, String> inputResources = model.getInputResources();
                    Map<String, String> outputResources = model.getOutputResources();
                    String runName = null;
                    if (element instanceof PipelineNode) {
                        runName = tkncli.startPipeline(namespace, element.getName(), params, inputResources, serviceAccount, taskServiceAccount, workspaces);

                    } else if (element instanceof TaskNode) {
                        runName = tkncli.startTask(namespace, element.getName(), params, inputResources, outputResources, serviceAccount, workspaces);
                    }
                    if(runName != null) {
                        FollowLogsAction followLogsAction = (FollowLogsAction) ActionManager.getInstance().getAction("FollowLogsAction");
                        followLogsAction.actionPerformed(namespace, runName, element.getClass(), tkncli);
                    }
                    ((TektonTreeStructure)getTree(anActionEvent).getClientProperty(Constants.STRUCTURE_PROPERTY)).fireModified(element);
                } catch (IOException e) {
                    notification = new Notification(NOTIFICATION_ID,
                            "Error",
                            element.getName() + " in namespace " + namespace + " failed to start\n" + e.getLocalizedMessage(),
                            NotificationType.ERROR);
                    Notifications.Bus.notify(notification);
                    logger.warn("Error: " + e.getLocalizedMessage());
                }
            }
        });
    }

}
