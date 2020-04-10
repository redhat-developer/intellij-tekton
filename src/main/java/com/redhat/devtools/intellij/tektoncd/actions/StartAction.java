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
import com.redhat.devtools.intellij.tektoncd.utils.StartResourceModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.tree.TreePath;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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

            StartResourceModel model = new StartResourceModel(configuration, resources);

            if (!model.isValid()) {
                UIHelper.executeInUI(() -> Messages.showErrorDialog(model.getErrorMessage(), "Error"));
                return;
            }

            boolean noInputsAndOuputs = model.getInputs() == null && model.getOutputs() == null;
            StartDialog stdialog = null;

            if (!noInputsAndOuputs) {
                stdialog = UIHelper.executeInUI(() -> {
                    StartDialog dialog = new StartDialog(null, model.getNamespace(), model.getName(), model.getKind(), model.getInputs(), model.getOutputs(), resources);
                    dialog.show();
                    return dialog;
                });
            }
            if (noInputsAndOuputs || stdialog.isOK()) {
                try {
                    Map<String, String> params = stdialog == null ? Collections.emptyMap() : stdialog.getParameters();
                    Map<String, String> inputResources = stdialog == null ? Collections.emptyMap() : stdialog.getInputResources();
                    Map<String, String> outputResources = stdialog == null ? Collections.emptyMap() : stdialog.getOutputResources();
                    if (PipelineNode.class.equals(nodeClass)) {
                        tkncli.startPipeline(namespace, selected.toString(), params, inputResources);
                    } else if (TaskNode.class.equals(nodeClass)) {
                        tkncli.startTask(namespace, selected.toString(), params, inputResources, outputResources);
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
