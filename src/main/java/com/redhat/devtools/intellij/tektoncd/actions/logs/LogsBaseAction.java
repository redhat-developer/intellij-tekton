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
import com.redhat.devtools.intellij.common.utils.UIHelper;
import com.redhat.devtools.intellij.tektoncd.actions.TektonAction;
import com.redhat.devtools.intellij.tektoncd.tkn.PipelineRun;
import com.redhat.devtools.intellij.tektoncd.tkn.TaskRun;
import com.redhat.devtools.intellij.tektoncd.tkn.Tkn;
import com.redhat.devtools.intellij.tektoncd.tree.PipelineNode;
import com.redhat.devtools.intellij.tektoncd.tree.TaskNode;
import com.redhat.devtools.intellij.tektoncd.ui.RunPickerDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.tree.TreePath;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class LogsBaseAction extends TektonAction {
    Logger logger = LoggerFactory.getLogger(LogsBaseAction.class);

    public LogsBaseAction(Class... filters) {
        super(filters);
    }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent, TreePath path, Object selected, Tkn tkncli) {
        this.actionPerformed(anActionEvent, path, selected, tkncli);
    }

    protected String pickRunByResource(String namespace, String resource, Class node, String action, Tkn tkncli) {
        String runPicked = null;
        if (PipelineNode.class.equals(node)) {
            runPicked = this.pickPipelineRunByPipeline(namespace, resource, action, tkncli);
        } else if (TaskNode.class.equals(node)) {
            runPicked = this.pickTaskRunByTask(namespace, resource, action, tkncli);
        }
        return runPicked;
    }

    private String pickPipelineRunByPipeline(String namespace, String name, String actionName, Tkn tkncli) {
        List<String> pipelineRunsNames;
        try {
            List<PipelineRun> pipelineRuns = tkncli.getPipelineRuns(namespace, name);
            if (pipelineRuns == null || pipelineRuns.size() == 0) {
                UIHelper.executeInUI(() -> Messages.showWarningDialog("Pipeline " + name + "doesn't have any pipelineRun to be selected", actionName));
                return null;
            }
            pipelineRunsNames = pipelineRuns.stream().map(PipelineRun::getName).collect(Collectors.toList());
        } catch (IOException e) {
            UIHelper.executeInUI(() ->
                    Messages.showErrorDialog(
                            "An error occurred while requesting logs for " + name + "\n" + e.getLocalizedMessage(),
                            "Error"));
            logger.error("Error: " + e.getLocalizedMessage(), e);
            return null;
        }

        return this.pickRunWithDialogHelper(pipelineRunsNames, "pipelinerun", actionName);
    }

    private String pickTaskRunByTask(String namespace, String name, String actionName, Tkn tkncli) {
        List<String> taskRunsNames;
        try {
            List<TaskRun> taskRuns = tkncli.getTaskRuns(namespace, name);
            if (taskRuns == null || taskRuns.size() == 0) {
                UIHelper.executeInUI(() -> Messages.showWarningDialog("Task " + name + "doesn't have any taskRun to be selected", actionName));
                return null;
            }
            taskRunsNames = taskRuns.stream().map(TaskRun::getName).collect(Collectors.toList());
        } catch (IOException e) {
            UIHelper.executeInUI(() ->
                    Messages.showErrorDialog(
                            "An error occurred while requesting logs for " + name + "\n" + e.getLocalizedMessage(),
                            "Error"));
            logger.error("Error: " + e.getLocalizedMessage(), e);
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

}
