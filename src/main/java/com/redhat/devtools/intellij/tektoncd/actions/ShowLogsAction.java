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
import com.redhat.devtools.intellij.common.tree.LazyMutableTreeNode;
import com.redhat.devtools.intellij.common.utils.ExecHelper;
import com.redhat.devtools.intellij.common.utils.UIHelper;
import com.redhat.devtools.intellij.tektoncd.tkn.PipelineRun;
import com.redhat.devtools.intellij.tektoncd.tkn.TaskRun;
import com.redhat.devtools.intellij.tektoncd.tkn.Tkn;
import com.redhat.devtools.intellij.tektoncd.tree.PipelineNode;
import com.redhat.devtools.intellij.tektoncd.tree.PipelineRunNode;
import com.redhat.devtools.intellij.tektoncd.tree.TaskNode;
import com.redhat.devtools.intellij.tektoncd.tree.TaskRunNode;
import com.redhat.devtools.intellij.tektoncd.ui.RunPickerDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.tree.TreePath;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class ShowLogsAction extends TektonAction {
    Logger logger = LoggerFactory.getLogger(ShowLogsAction.class);

    public ShowLogsAction() { super(PipelineRunNode.class, TaskRunNode.class, TaskNode.class, PipelineNode.class); }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent, TreePath path, Object selected, Tkn tkncli) {
        ExecHelper.submit(() -> {
            String namespace;
            String resourceName = selected.toString();
            List<String> resourceRunsName = null;
            Class<?> nodeClass = selected.getClass();
            String kindLabel = "";
            Notification notification;

            // if node selected is a Pipeline or a Task we need to find its runs
            String finalResourceName = resourceName;
            try {
                namespace = ((LazyMutableTreeNode) selected).getParent().getParent().toString();
                if (PipelineNode.class.equals(nodeClass)) {
                    List<PipelineRun> pipelineRuns = tkncli.getPipelineRuns(namespace, resourceName);
                    if (pipelineRuns == null || pipelineRuns.size() == 0) {
                        UIHelper.executeInUI(() -> Messages.showWarningDialog("Pipeline " + finalResourceName + "doesn't have any pipelineRun to be selected", "Show Logs"));
                        return;
                    }
                    resourceRunsName = pipelineRuns.stream().map(PipelineRun::getName).collect(Collectors.toList());
                    nodeClass = PipelineRunNode.class;
                    kindLabel = "pipelinerun";
                } else if (TaskNode.class.equals(nodeClass)) {
                    List<TaskRun> taskRuns = tkncli.getTaskRuns(namespace, resourceName);
                    if (taskRuns == null || taskRuns.size() == 0) {
                        UIHelper.executeInUI(() -> Messages.showWarningDialog("Task " + finalResourceName + "doesn't have any taskRun to be selected", "Show Logs"));
                        return;
                    }
                    resourceRunsName = taskRuns.stream().map(TaskRun::getName).collect(Collectors.toList());
                    nodeClass = TaskRunNode.class;
                    kindLabel = "taskrun";
                }
            } catch (IOException e) {
                UIHelper.executeInUI(() ->
                        Messages.showErrorDialog(
                                "An error occurred while requesting logs for " + finalResourceName + "\n" + e.getLocalizedMessage(),
                                "Error"));
                logger.error("Error: " + e.getLocalizedMessage(), e);
                return;
            }

            if (resourceRunsName != null) {
                if (resourceRunsName.size() == 1) {
                    // there is only 1 item, user doesn't have to pick one
                    resourceName = resourceRunsName.get(0);
                } else {
                    // if there are more than 1 runs the user has to pick the one he wants the logs for
                    String finalKindLabel = kindLabel;
                    List<String> finalResourceRunsName = resourceRunsName;
                    RunPickerDialog dialog = UIHelper.executeInUI(() -> {
                        RunPickerDialog rpdialog = new RunPickerDialog(null, finalKindLabel, finalResourceRunsName);
                        rpdialog.show();
                        return rpdialog;
                    });

                    if (dialog.isOK()) {
                        resourceName = dialog.getSelected();
                    } else {
                        return;
                    }
                }
            } else {
                namespace = ((LazyMutableTreeNode) selected).getParent().getParent().getParent().toString();
            }

            try {
                if (PipelineRunNode.class.equals(nodeClass)) {
                    tkncli.showLogsPipelineRun(namespace, resourceName);
                } else if (TaskRunNode.class.equals(nodeClass)) {
                    tkncli.showLogsTaskRun(namespace, resourceName);
                }
            } catch (IOException e) {
                String finalResourceName1 = resourceName;
                UIHelper.executeInUI(() ->
                        Messages.showErrorDialog(
                                "An error occurred while requesting logs for " + finalResourceName1 + "\n" + e.getLocalizedMessage(),
                                "Error"));
                logger.error("Error: " + e.getLocalizedMessage(), e);
            }
        });
    }
}
