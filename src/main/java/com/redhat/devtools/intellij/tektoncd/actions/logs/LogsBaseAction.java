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
import com.redhat.devtools.intellij.tektoncd.actions.TektonAction;
import com.redhat.devtools.intellij.tektoncd.tkn.Run;
import com.redhat.devtools.intellij.tektoncd.tkn.Tkn;
import com.redhat.devtools.intellij.tektoncd.tree.PipelineNode;
import com.redhat.devtools.intellij.tektoncd.tree.RunNode;
import com.redhat.devtools.intellij.tektoncd.tree.TaskNode;
import com.redhat.devtools.intellij.tektoncd.ui.RunPickerDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.tree.TreePath;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public abstract class LogsBaseAction extends TektonAction {
    Logger logger = LoggerFactory.getLogger(LogsBaseAction.class);

    public LogsBaseAction() { super(RunNode.class, TaskNode.class, PipelineNode.class); }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent, TreePath path, Object selected, Tkn tkncli) {
        ExecHelper.submit(() -> {
            String namespace = getNamespace((LazyMutableTreeNode) selected);
            String resourceName = pickRun(namespace, selected, anActionEvent.getPresentation().getText(), tkncli);
            if (resourceName == null) return;

            this.actionPerformed(namespace, resourceName, selected.getClass(), tkncli);
        });
    }

    public abstract void actionPerformed(String namespace, String resourceName, Class nodeClass, Tkn tkncli);

    private String pickRun(String namespace, Object selected, String action, Tkn tkncli) {
        if (PipelineNode.class.equals(selected.getClass())) {
            return this.pickPipelineRunByPipeline(namespace, selected.toString(), action, tkncli);
        } else if (TaskNode.class.equals(selected.getClass())) {
            return this.pickTaskRunByTask(namespace, selected.toString(), action, tkncli);
        }
        return ((RunNode) selected).getName();
    }

    private String pickPipelineRunByPipeline(String namespace, String name, String actionName, Tkn tkncli) {
        List<String> pipelineRunsNames;
        try {
            List<Run> pipelineRuns = tkncli.getPipelineRuns(namespace, name);
            if (pipelineRuns == null || pipelineRuns.size() == 0) {
                UIHelper.executeInUI(() -> Messages.showWarningDialog("Pipeline " + name + "doesn't have any pipelineRun to be selected", actionName));
                return null;
            }
            pipelineRunsNames = pipelineRuns.stream().map(Run::getName).collect(Collectors.toList());
        } catch (IOException e) {
            UIHelper.executeInUI(() ->
                    Messages.showErrorDialog(
                            "An error occurred while requesting logs for " + name + "\n" + e.getLocalizedMessage(),
                            "Error"));
            logger.warn("Error: " + e.getLocalizedMessage(), e);
            return null;
        }

        return this.pickRunWithDialogHelper(pipelineRunsNames, "pipelinerun", actionName);
    }

    private String pickTaskRunByTask(String namespace, String name, String actionName, Tkn tkncli) {
        List<String> taskRunsNames;
        try {
            List<Run> taskRuns = tkncli.getTaskRuns(namespace, name);
            if (taskRuns == null || taskRuns.size() == 0) {
                UIHelper.executeInUI(() -> Messages.showWarningDialog("Task " + name + "doesn't have any taskRun to be selected", actionName));
                return null;
            }
            taskRunsNames = taskRuns.stream().map(Run::getName).collect(Collectors.toList());
        } catch (IOException e) {
            UIHelper.executeInUI(() ->
                    Messages.showErrorDialog(
                            "An error occurred while requesting logs for " + name + "\n" + e.getLocalizedMessage(),
                            "Error"));
            logger.warn("Error: " + e.getLocalizedMessage(), e);
            return null;
        }

        return this.pickRunWithDialogHelper(taskRunsNames, "taskrun", actionName);
    }

    private String pickRunWithDialogHelper(List resourceRunsName, String kind, String actionName) {
        String runPicked = null;
        if (resourceRunsName != null) {
            if (resourceRunsName.size() == 1) {
                // there is only 1 item, user doesn't have to pick one
                runPicked = resourceRunsName.get(0).toString();
            } else {
                // if there are more than 1 runs the user has to pick the one he wants the logs for
                RunPickerDialog dialog = UIHelper.executeInUI(() -> {
                    RunPickerDialog rpdialog = new RunPickerDialog(null, actionName, kind, resourceRunsName);
                    rpdialog.show();
                    return rpdialog;
                });

                if (dialog.isOK()) {
                    runPicked = dialog.getSelected();
                }
            }
        }
        return runPicked;

    }

    private String getNamespace(LazyMutableTreeNode selected) {
        Class<?> nodeClass = selected.getClass();
        if (PipelineNode.class.equals(nodeClass) || TaskNode.class.equals(nodeClass)) {
            return selected.getParent().getParent().toString();
        } else {
            if (((RunNode) selected).getLevel() == 4) {
                return selected.getParent().getParent().getParent().toString();
            } else {
                return selected.getParent().getParent().getParent().getParent().toString();
            }
        }
    }

}
