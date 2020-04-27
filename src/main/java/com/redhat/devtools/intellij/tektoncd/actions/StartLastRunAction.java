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
import com.redhat.devtools.intellij.common.tree.LazyMutableTreeNode;
import com.redhat.devtools.intellij.common.utils.ExecHelper;
import com.redhat.devtools.intellij.tektoncd.tkn.Tkn;
import com.redhat.devtools.intellij.tektoncd.tree.PipelineNode;
import com.redhat.devtools.intellij.tektoncd.tree.TaskNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.tree.TreePath;
import java.io.IOException;

import static com.redhat.devtools.intellij.tektoncd.Constants.NOTIFICATION_ID;

public class StartLastRunAction extends TektonAction {
    Logger logger = LoggerFactory.getLogger(StartLastRunAction.class);

    public StartLastRunAction() { super(PipelineNode.class, TaskNode.class); }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent, TreePath path, Object selected, Tkn tkncli) {
        ExecHelper.submit(() -> {
            String namespace = ((LazyMutableTreeNode)selected).getParent().getParent().toString();
            Class<?> nodeClass = selected.getClass();
            try {
                if (PipelineNode.class.equals(nodeClass)) {
                    tkncli.startLastPipeline(namespace, selected.toString());
                } else if (TaskNode.class.equals(nodeClass)) {
                    tkncli.startLastTask(namespace, selected.toString());
                }
                ((LazyMutableTreeNode)selected).reload();
            } catch (IOException e) {
                Notification notification = new Notification(NOTIFICATION_ID,
                        "Error",
                        selected.toString() + " in namespace " + namespace + " failed to start\n" + e.getLocalizedMessage(),
                        NotificationType.ERROR);
                Notifications.Bus.notify(notification);
                logger.warn("Error: " + e.getLocalizedMessage(), e);
            }
        });
    }

}
