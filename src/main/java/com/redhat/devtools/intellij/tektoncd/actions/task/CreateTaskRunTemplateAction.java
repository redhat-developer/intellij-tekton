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
package com.redhat.devtools.intellij.tektoncd.actions.task;

import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;
import com.redhat.devtools.intellij.common.utils.ExecHelper;
import com.redhat.devtools.intellij.common.utils.UIHelper;
import com.redhat.devtools.intellij.tektoncd.actions.TektonAction;
import com.redhat.devtools.intellij.tektoncd.telemetry.TelemetryService;
import com.redhat.devtools.intellij.tektoncd.tkn.Tkn;
import com.redhat.devtools.intellij.tektoncd.tree.ParentableNode;
import com.redhat.devtools.intellij.tektoncd.tree.TaskNode;
import com.redhat.devtools.intellij.tektoncd.utils.VirtualFileHelper;
import com.redhat.devtools.intellij.tektoncd.utils.YAMLBuilder;
import com.redhat.devtools.intellij.tektoncd.utils.model.resources.TaskConfigurationModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.tree.TreePath;
import java.io.IOException;

import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_TASKRUN;
import static com.redhat.devtools.intellij.tektoncd.Constants.NOTIFICATION_ID;
import static com.redhat.devtools.intellij.telemetry.core.service.TelemetryMessageBuilder.ActionMessage;
import static com.redhat.devtools.intellij.telemetry.core.util.AnonymizeUtils.anonymizeResource;

public class CreateTaskRunTemplateAction extends TektonAction {

    private static final Logger logger = LoggerFactory.getLogger(CreateTaskRunTemplateAction.class);

    public CreateTaskRunTemplateAction() { super(TaskNode.class); }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent, TreePath path, Object selected, Tkn tkncli) {
        ParentableNode element = getElement(selected);
        String namespace = element.getNamespace();
        ActionMessage telemetry = TelemetryService.instance().action("create task run");
        ExecHelper.submit(() -> {
            Notification notification;
            TaskConfigurationModel model;
            try {
                model = getModel(element, namespace, tkncli);
            } catch (IOException e) {
                telemetry.error(anonymizeResource(element.getName(), namespace, e.getMessage())).send();
                UIHelper.executeInUI(() ->
                        Messages.showErrorDialog(
                                "Failed to create TaskRun templace from " + element.getName() + " in namespace " + namespace + "An error occurred while retrieving information.\n" + e.getLocalizedMessage(),
                                "Error"));
                logger.warn("Error: " + e.getLocalizedMessage());
                return;
            }

            if (!model.isValid()) {
                String errorMessage = "Failed to create a TaskRun templace from " + element.getName() + " in namespace " + namespace + ". The task is not valid.";
                telemetry.error(anonymizeResource(element.getName(), namespace, errorMessage)).send();
                UIHelper.executeInUI(() -> Messages.showErrorDialog(errorMessage, "Error"));
                return;
            }

            try {
                String contentTask = new YAMLMapper().writeValueAsString(YAMLBuilder.createTaskRun(model));
                UIHelper.executeInUI(() ->
                        VirtualFileHelper.openVirtualFileInEditor(anActionEvent.getProject(), namespace, "generate-taskrun-" + model.getName(), contentTask, KIND_TASKRUN, true));
            } catch (IOException e) {
                String errorMessage = "Failed to create TaskRun templace from" + element.getName() + " in namespace " + namespace + " \n" + e.getLocalizedMessage();
                telemetry.error(anonymizeResource(element.getName(), namespace, errorMessage))
                        .send();
                notification = new Notification(NOTIFICATION_ID,
                        "Error",
                        errorMessage,
                        NotificationType.ERROR);
                Notifications.Bus.notify(notification);
                logger.warn("Error: " + e.getLocalizedMessage());
            }

        });
    }

    protected TaskConfigurationModel getModel(ParentableNode element, String namespace, Tkn tkncli) throws IOException {
        String configuration = "";
        if (element instanceof TaskNode) {
            configuration = tkncli.getTaskYAML(namespace, element.getName());
        }
        return new TaskConfigurationModel(configuration);
    }
}
