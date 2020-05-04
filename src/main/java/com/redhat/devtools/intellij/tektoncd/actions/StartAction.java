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
import com.redhat.devtools.intellij.common.utils.ExecHelper;
import com.redhat.devtools.intellij.common.utils.UIHelper;
import com.redhat.devtools.intellij.tektoncd.Constants;
import com.redhat.devtools.intellij.tektoncd.tkn.Resource;
import com.redhat.devtools.intellij.tektoncd.tkn.Tkn;
import com.redhat.devtools.intellij.tektoncd.tree.NamespaceNode;
import com.redhat.devtools.intellij.tektoncd.tree.ParentableNode;
import com.redhat.devtools.intellij.tektoncd.tree.PipelineNode;
import com.redhat.devtools.intellij.tektoncd.tree.TaskNode;
import com.redhat.devtools.intellij.tektoncd.tree.TektonTreeStructure;
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
    private static final Logger logger = LoggerFactory.getLogger(StartAction.class);

    public StartAction() { super(PipelineNode.class, TaskNode.class); }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent, TreePath path, Object selected, Tkn tkncli) {
        ParentableNode<? extends ParentableNode<NamespaceNode>> element = getElement(selected);
        String namespace = element.getParent().getParent().getName();
        ExecHelper.submit(() -> {
            String configuration = null;
            Notification notification;
            List<Resource> resources;
            try {
                if (element instanceof PipelineNode) {
                    configuration = tkncli.getPipelineYAML(namespace, element.getName());
                } else if (element instanceof TaskNode) {
                    configuration = tkncli.getTaskYAML(namespace, element.getName());
                }
                resources = tkncli.getResources(namespace);
            } catch (IOException e) {
                UIHelper.executeInUI(() ->
                        Messages.showErrorDialog(
                                element.getName() + " in namespace " + namespace + " failed to start. An error occurred while retrieving information.\n" + e.getLocalizedMessage(),
                                "Error"));
                logger.warn("Error: " + e.getLocalizedMessage());
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
                    if (element instanceof PipelineNode) {
                        tkncli.startPipeline(namespace, element.getName(), params, inputResources);
                    } else if (element instanceof TaskNode) {
                        tkncli.startTask(namespace, element.getName(), params, inputResources, outputResources);
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
