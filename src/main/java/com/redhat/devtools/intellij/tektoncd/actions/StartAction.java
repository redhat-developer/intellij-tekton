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
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;
import com.redhat.devtools.intellij.common.tree.LazyMutableTreeNode;
import com.redhat.devtools.intellij.common.utils.ExecHelper;
import com.redhat.devtools.intellij.common.utils.UIHelper;
import com.redhat.devtools.intellij.tektoncd.tkn.Resource;
import com.redhat.devtools.intellij.tektoncd.tkn.Tkn;
import com.redhat.devtools.intellij.tektoncd.tree.PipelineNode;
import com.redhat.devtools.intellij.tektoncd.tree.TaskNode;
import com.redhat.devtools.intellij.tektoncd.ui.StartDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.tree.TreePath;
import java.io.IOException;
import java.util.List;

import static com.redhat.devtools.intellij.tektoncd.Constants.NOTIFICATION_ID;

public class StartAction extends TektonAction {
    Logger logger = LoggerFactory.getLogger(StartAction.class);

    public StartAction() { super(PipelineNode.class, TaskNode.class); }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent, TreePath path, Object selected, Tkn tkncli) {
        String namespace = ((LazyMutableTreeNode)selected).getParent().getParent().toString();
        Class<?> nodeClass = selected.getClass();
        ExecHelper.submit(() -> {
            String configuration = null;
            Notification notification;
            List<Resource> resources;
            try {
                if (PipelineNode.class.equals(nodeClass)) {
                    configuration = tkncli.getPipelineYAML(namespace, selected.toString());
                } else if (TaskNode.class.equals(nodeClass)) {
                    configuration = tkncli.getTaskYAML(namespace, selected.toString());
                }
                resources = tkncli.getResources(namespace);
            } catch (IOException e) {
                UIHelper.executeInUI(() ->
                        Messages.showErrorDialog(
                                selected.toString() + " in namespace " + namespace + " failed to start. An error occurred while retrieving information.\n" + e.getLocalizedMessage(),
                                "Error"));
                logger.error("Error: " + e.getLocalizedMessage());
                return;
            }
            String finalConfiguration = configuration;
            StartDialog stdialog = UIHelper.executeInUI(() -> {
                StartDialog dialog = new StartDialog(null, finalConfiguration, resources);
                dialog.show();
                return dialog;
            });
            if (stdialog.isOK()) {
                try {
                    if (PipelineNode.class.equals(nodeClass)) {
                        tkncli.startPipeline(namespace, selected.toString(), stdialog.getParameters(), stdialog.getInputResources());
                    } else if (TaskNode.class.equals(nodeClass)) {
                        tkncli.startTask(namespace, selected.toString(), stdialog.getParameters(), stdialog.getInputResources(), stdialog.getOutputResources());
                    }
                    ((LazyMutableTreeNode)selected).reload();
                } catch (IOException e) {
                    notification = new Notification(NOTIFICATION_ID,
                            "Error",
                            selected.toString() + " in namespace " + namespace + " failed to start\n" + e.getLocalizedMessage(),
                            NotificationType.ERROR);
                    Notifications.Bus.notify(notification);
                    logger.error("Error: " + e.getLocalizedMessage());
                }
            }
        });
    }
}
