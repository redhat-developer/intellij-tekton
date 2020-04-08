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
package com.redhat.devtools.intellij.tektoncd.actions.logs;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;
import com.redhat.devtools.intellij.common.tree.LazyMutableTreeNode;
import com.redhat.devtools.intellij.common.utils.ExecHelper;
import com.redhat.devtools.intellij.common.utils.UIHelper;
import com.redhat.devtools.intellij.tektoncd.tkn.Tkn;
import com.redhat.devtools.intellij.tektoncd.tree.PipelineNode;
import com.redhat.devtools.intellij.tektoncd.tree.PipelineRunNode;
import com.redhat.devtools.intellij.tektoncd.tree.TaskNode;
import com.redhat.devtools.intellij.tektoncd.tree.TaskRunNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.tree.TreePath;
import java.io.IOException;

public class FollowLogsAction extends LogsBaseAction {
    Logger logger = LoggerFactory.getLogger(FollowLogsAction.class);

    @Override
    public void actionPerformed(AnActionEvent anActionEvent, TreePath path, Object selected, Tkn tkncli) {
        ExecHelper.submit(() -> {
            String namespace = getNamespace((LazyMutableTreeNode) selected);
            Class<?> nodeClass = selected.getClass();
            String resourceName = pickRun(namespace, selected.toString(), nodeClass, "Follow Logs", tkncli);
            if (resourceName == null) return;

            try {
                if (PipelineRunNode.class.equals(nodeClass) || PipelineNode.class.equals(nodeClass)) {
                    tkncli.followLogsPipelineRun(namespace, resourceName);
                } else if (TaskRunNode.class.equals(nodeClass) || TaskNode.class.equals(nodeClass)) {
                    tkncli.followLogsTaskRun(namespace, resourceName);
                }
            } catch (IOException e) {
                String finalResourceName = resourceName;
                UIHelper.executeInUI(() ->
                        Messages.showErrorDialog(
                                "An error occurred while requesting logs for " + finalResourceName + "\n" + e.getLocalizedMessage(),
                                "Error"));
                logger.error("Error: " + e.getLocalizedMessage(), e);
            }
        });
    }
}
