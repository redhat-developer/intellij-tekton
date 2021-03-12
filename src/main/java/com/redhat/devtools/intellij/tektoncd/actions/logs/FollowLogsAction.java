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

import com.intellij.openapi.ui.Messages;
import com.redhat.devtools.intellij.common.utils.UIHelper;
import com.redhat.devtools.intellij.tektoncd.Constants;
import com.redhat.devtools.intellij.tektoncd.settings.SettingsState;
import com.redhat.devtools.intellij.tektoncd.telemetry.TelemetryService;
import com.redhat.devtools.intellij.tektoncd.tkn.Tkn;
import com.redhat.devtools.intellij.tektoncd.tree.PipelineNode;
import com.redhat.devtools.intellij.tektoncd.tree.PipelineRunNode;
import com.redhat.devtools.intellij.tektoncd.tree.TaskNode;
import com.redhat.devtools.intellij.tektoncd.tree.TaskRunNode;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.redhat.devtools.intellij.telemetry.core.service.TelemetryMessageBuilder.ActionMessage;
import static com.redhat.devtools.intellij.telemetry.core.util.AnonymizeUtils.anonymizeResource;

public class FollowLogsAction extends LogsBaseAction {

    private static final Logger logger = LoggerFactory.getLogger(FollowLogsAction.class);
    private static final ActionMessage telemetry = TelemetryService.instance().action("follow logs");

    public void actionPerformed(String namespace, String resourceName, Class nodeClass, Tkn tkncli) {
        try {
            boolean toEditor = SettingsState.getInstance().displayLogsInEditor;
            if (PipelineNode.class.equals(nodeClass) || PipelineRunNode.class.equals(nodeClass)) {
                telemetry
                        .property(TelemetryService.PROP_RESOURCE_KIND, Constants.KIND_PIPELINERUN)
                        .success()
                        .send();
                tkncli.followLogsPipelineRun(namespace, resourceName, toEditor);
            } else if (TaskNode.class.equals(nodeClass) || TaskRunNode.class.equals(nodeClass)) {
                telemetry
                        .property(TelemetryService.PROP_RESOURCE_KIND, Constants.KIND_TASKRUN)
                        .success()
                        .send();
                tkncli.followLogsTaskRun(namespace, resourceName, toEditor);
            }
        } catch (IOException e) {
            telemetry.error(anonymizeResource(resourceName, namespace, e.getMessage())).send();
            UIHelper.executeInUI(() ->
                    Messages.showErrorDialog(
                            "An error occurred while requesting logs for " + resourceName + "\n" + e.getLocalizedMessage(),
                            "Error"));
            logger.warn("Could not follow logs: " + e.getLocalizedMessage());
        }
    }
}
