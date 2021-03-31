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

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;
import com.redhat.devtools.intellij.common.utils.ExecHelper;
import com.redhat.devtools.intellij.common.utils.UIHelper;
import com.redhat.devtools.intellij.tektoncd.Constants;
import com.redhat.devtools.intellij.tektoncd.tkn.Tkn;
import com.redhat.devtools.intellij.tektoncd.tree.ParentableNode;
import com.redhat.devtools.intellij.tektoncd.tree.PipelineRunNode;
import com.redhat.devtools.intellij.tektoncd.tree.TaskRunNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.tree.TreePath;
import java.io.IOException;

import static com.redhat.devtools.intellij.tektoncd.telemetry.TelemetryService.NAME_PREFIX_ACTION;
import static com.redhat.devtools.intellij.tektoncd.telemetry.TelemetryService.PROP_RESOURCE_KIND;
import static com.redhat.devtools.intellij.tektoncd.telemetry.TelemetryService.instance;
import static com.redhat.devtools.intellij.telemetry.core.service.TelemetryMessageBuilder.ActionMessage;
import static com.redhat.devtools.intellij.telemetry.core.util.AnonymizeUtils.anonymizeResource;

public class ShowDiagnosticDataAction extends TektonAction {
    Logger logger = LoggerFactory.getLogger(ShowDiagnosticDataAction.class);

    public ShowDiagnosticDataAction() { super(PipelineRunNode.class, TaskRunNode.class); }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent, TreePath path, Object selected, Tkn tkncli) {
        ActionMessage telemetry = instance().action(NAME_PREFIX_ACTION + ": show diagnostic data");
        ExecHelper.submit(() -> {
            ParentableNode element = getElement(selected);
            String namespace = element.getNamespace();
            boolean hasDataToShow = false;
            try {
                if (element instanceof PipelineRunNode) {
                    telemetry.property(PROP_RESOURCE_KIND, Constants.KIND_PIPELINERUN);
                    hasDataToShow = tkncli.getDiagnosticData(namespace, "tekton.dev/pipelineRun", element.getName());
                } else if (element instanceof TaskRunNode) {
                    telemetry.property(PROP_RESOURCE_KIND, Constants.KIND_TASKRUN);
                    hasDataToShow = tkncli.getDiagnosticData(namespace, "tekton.dev/taskRun", element.getName());
                }
                if (!hasDataToShow) {
                    String errorMessage = "No data available for " + element.getName() + " in namespace " + namespace + ".";
                    telemetry
                            .error(anonymizeResource(element.getName(), namespace, errorMessage))
                            .send();
                    UIHelper.executeInUI(() ->
                            Messages.showWarningDialog(
                                    "No data available for " + element.getName() + " in namespace " + namespace + ".",
                                    "Diagnostic Data"));
                } else {
                    telemetry
                            .success()
                            .send();
                }
            } catch (IOException e) {
                telemetry
                        .error(anonymizeResource(element.getName(), namespace, e.getMessage()))
                        .send();
                UIHelper.executeInUI(() ->
                        Messages.showErrorDialog(
                                "Failed to retrieve data for " + element.getName() + " in namespace " + namespace + ". An error occurred while retrieving them.\n" + e.getLocalizedMessage(),
                                "Error"));
                logger.warn("Error: " + e.getLocalizedMessage(), e);
                return;
            }
        });
    }
}
