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
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.redhat.devtools.intellij.common.utils.ExecHelper;
import com.redhat.devtools.intellij.tektoncd.Constants;
import com.redhat.devtools.intellij.tektoncd.actions.logs.FollowLogsAction;
import com.redhat.devtools.intellij.tektoncd.tkn.Tkn;
import com.redhat.devtools.intellij.tektoncd.tree.NamespaceNode;
import com.redhat.devtools.intellij.tektoncd.tree.ParentableNode;
import com.redhat.devtools.intellij.tektoncd.tree.PipelineNode;
import com.redhat.devtools.intellij.tektoncd.tree.TaskNode;
import com.redhat.devtools.intellij.tektoncd.tree.TektonTreeStructure;
import com.redhat.devtools.intellij.tektoncd.utils.WatchHandler;
import java.io.IOException;
import javax.swing.tree.TreePath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_PIPELINERUN;
import static com.redhat.devtools.intellij.tektoncd.Constants.NOTIFICATION_ID;

public class StartLastRunAction extends TektonAction {
    Logger logger = LoggerFactory.getLogger(StartLastRunAction.class);

    public StartLastRunAction() { super(PipelineNode.class, TaskNode.class); }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent, TreePath path, Object selected, Tkn tkncli) {
        ExecHelper.submit(() -> {
            ParentableNode<? extends ParentableNode<NamespaceNode>> element = getElement(selected);
            String namespace = element.getParent().getParent().getName();
            Project project = element.getRoot().getProject();
            try {
                String runName = null;
                if (element instanceof PipelineNode) {
                    runName = tkncli.startLastPipeline(namespace, element.getName());
                } else if (element instanceof TaskNode) {
                    runName = tkncli.startLastTask(namespace, element.getName());
                }
                if (runName != null) {
                    FollowLogsAction followLogsAction = (FollowLogsAction) ActionManager.getInstance().getAction("FollowLogsAction");
                    followLogsAction.actionPerformed(namespace, runName, element.getClass(), tkncli);
                }

                WatchHandler.get().setWatchByKind(tkncli, project, namespace, KIND_PIPELINERUN);

                ((TektonTreeStructure) getTree(anActionEvent).getClientProperty(Constants.STRUCTURE_PROPERTY)).fireModified(element);
            } catch (IOException e) {
                Notification notification = new Notification(NOTIFICATION_ID,
                        "Error",
                        element.getName() + " in namespace " + namespace + " failed to start\n" + e.getLocalizedMessage(),
                        NotificationType.ERROR);
                Notifications.Bus.notify(notification);
                logger.warn("Error: " + e.getLocalizedMessage(), e);
            }
        });
    }

}
