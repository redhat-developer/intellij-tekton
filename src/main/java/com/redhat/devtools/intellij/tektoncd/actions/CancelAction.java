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
import com.redhat.devtools.intellij.common.utils.ExecHelper;
import com.redhat.devtools.intellij.tektoncd.tkn.Tkn;
import com.redhat.devtools.intellij.tektoncd.tree.ParentableNode;
import com.redhat.devtools.intellij.tektoncd.tree.PipelineRunNode;
import com.redhat.devtools.intellij.tektoncd.tree.RunNode;
import com.redhat.devtools.intellij.tektoncd.tree.TaskRunNode;
import java.io.IOException;
import java.util.Arrays;
import javax.swing.tree.TreePath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import static com.redhat.devtools.intellij.tektoncd.Constants.NOTIFICATION_ID;

public class CancelAction extends TektonAction {
    Logger logger = LoggerFactory.getLogger(CancelAction.class);

    public CancelAction() { super(RunNode.class); }

    @Override
    public boolean isVisible(Object[] selected) {
        boolean isCancellable = Arrays.stream(selected).allMatch(item -> isVisible(item));
        return isCancellable;
    }

    @Override
    public boolean isVisible(Object selected) {
        ParentableNode element = getElement(selected);
        return element instanceof RunNode && ((RunNode) element).getRun().getCompletionTime() == null;
    }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent, TreePath path, Object selected, Tkn tkncli) {
        ExecHelper.submit(() -> {
            ParentableNode element = getElement(selected);
            String namespace = element.getNamespace();
            try {
                if (element instanceof PipelineRunNode) {
                    tkncli.cancelPipelineRun(namespace, element.getName());
                } else if (element instanceof TaskRunNode) {
                    tkncli.cancelTaskRun(namespace, element.getName());
                }
            } catch (IOException e) {
                Notification notification = new Notification(NOTIFICATION_ID,
                        "Error",
                        element.getName() + " in namespace " + namespace + " failed to cancel\n" + e.getLocalizedMessage(),
                        NotificationType.ERROR);
                Notifications.Bus.notify(notification);
                logger.warn("Error: " + e.getLocalizedMessage(), e);
            }
        });
    }

}
