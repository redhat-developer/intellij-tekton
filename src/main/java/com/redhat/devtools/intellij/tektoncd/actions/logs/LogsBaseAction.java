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
import com.redhat.devtools.intellij.common.utils.ExecHelper;
import com.redhat.devtools.intellij.common.utils.UIHelper;
import com.redhat.devtools.intellij.tektoncd.Constants;
import com.redhat.devtools.intellij.tektoncd.actions.TektonAction;
import com.redhat.devtools.intellij.tektoncd.telemetry.TelemetryService;
import com.redhat.devtools.intellij.tektoncd.tkn.PipelineRun;
import com.redhat.devtools.intellij.tektoncd.tkn.Run;
import com.redhat.devtools.intellij.tektoncd.tkn.TaskRun;
import com.redhat.devtools.intellij.tektoncd.tkn.Tkn;
import com.redhat.devtools.intellij.tektoncd.tree.ParentableNode;
import com.redhat.devtools.intellij.tektoncd.tree.PipelineNode;
import com.redhat.devtools.intellij.tektoncd.tree.RunNode;
import com.redhat.devtools.intellij.tektoncd.tree.TaskNode;
import com.redhat.devtools.intellij.tektoncd.ui.RunPickerDialog;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.tree.TreePath;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.redhat.devtools.intellij.telemetry.core.service.TelemetryMessageBuilder.ActionMessageBuilder;
import static com.redhat.devtools.intellij.telemetry.core.util.AnonymizeUtils.anonymizeResource;

public abstract class LogsBaseAction extends TektonAction {

    private static final Logger logger = LoggerFactory.getLogger(LogsBaseAction.class);

    protected ActionMessageBuilder telemetry;

    public LogsBaseAction() {
        super(RunNode.class, TaskNode.class, PipelineNode.class);
    }

    public LogsBaseAction(Class clazz) {
        super(RunNode.class, TaskNode.class, PipelineNode.class, clazz);
    }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent, TreePath path, Object selected, Tkn tkncli) {
        this.telemetry = createTelemetry();
        ExecHelper.submit(() -> {
            ParentableNode element = getElement(selected);
            String namespace = element.getNamespace();
            String resourceName = pickResourceName(namespace, element, anActionEvent.getPresentation().getText(), tkncli);
            if (resourceName == null) return;

            doActionPerformed(namespace, resourceName, element.getClass(), tkncli);
        });
    }

    protected abstract void doActionPerformed(String namespace, String resourceName, Class nodeClass, Tkn tkncli);

    private String pickResourceName(String namespace, ParentableNode selected, String action, Tkn tkncli) {
        if (PipelineNode.class.equals(selected.getClass())) {
            return pickPipelineRunByPipeline(namespace, selected.getName(), action, tkncli);
        } else if (TaskNode.class.equals(selected.getClass())) {
            return pickTaskRunByTask(namespace, selected.getName(), action, tkncli);
        }
        return selected.getName();
    }

    private String pickPipelineRunByPipeline(String namespace, String name, String actionName, Tkn tkncli) {
        telemetry.property(TelemetryService.PROP_RESOURCE_KIND, Constants.KIND_PIPELINE);
        List<String> pipelineRunsNames;
        try {
            List<PipelineRun> pipelineRuns = tkncli.getPipelineRuns(namespace, name);
            if (pipelineRuns == null || pipelineRuns.isEmpty()) {
                String errorMessage = "Pipeline " + name + "doesn't have any pipelineRun to be selected";
                telemetry
                        .error(anonymizeResource(name, namespace, errorMessage))
                        .send();
                UIHelper.executeInUI(() -> {
                    telemetry
                            .error(errorMessage)
                            .send();
                    Messages.showWarningDialog(errorMessage, actionName);
                });
                return null;
            }
            pipelineRunsNames = pipelineRuns.stream().map(Run::getName).collect(Collectors.toList());
            return pickRunWithDialogHelper(pipelineRunsNames, "pipelinerun", actionName);
        } catch (IOException e) {
            String errorMessage = "An error occurred while requesting logs for " + name + "\n" + e.getLocalizedMessage();
            telemetry
                    .error(anonymizeResource(name, namespace, errorMessage))
                    .send();
            UIHelper.executeInUI(() -> {
                Messages.showErrorDialog(
                        errorMessage,
                        "Error");
            });
            logger.warn("Error: " + errorMessage, e);
            return null;
        }
    }

    private String pickTaskRunByTask(String namespace, String name, String actionName, Tkn tkncli) {
        telemetry.property(TelemetryService.PROP_RESOURCE_KIND, Constants.KIND_TASK);
        List<String> taskRunsNames;
        try {
            List<TaskRun> taskRuns = tkncli.getTaskRuns(namespace, name);
            if (taskRuns == null || taskRuns.isEmpty()) {
                UIHelper.executeInUI(() -> Messages.showWarningDialog("Task " + name + "doesn't have any taskRun to be selected", actionName));
                return null;
            }
            taskRunsNames = taskRuns.stream().map(Run::getName).collect(Collectors.toList());
            return pickRunWithDialogHelper(taskRunsNames, "taskrun", actionName);
        } catch (IOException e) {
            UIHelper.executeInUI(() ->
                    Messages.showErrorDialog(
                            "An error occurred while requesting logs for " + name + "\n" + e.getLocalizedMessage(),
                            "Error"));
            logger.warn("Error: " + e.getLocalizedMessage(), e);
            return null;
        }
    }

    private String pickRunWithDialogHelper(List<String> resourceRunsName, String kind, String actionName) {
        String runPicked = null;
        if (resourceRunsName != null) {
            if (resourceRunsName.size() == 1) {
                // there is only 1 item, user doesn't have to pick one
                runPicked = resourceRunsName.get(0);
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

    protected abstract ActionMessageBuilder createTelemetry();
}
