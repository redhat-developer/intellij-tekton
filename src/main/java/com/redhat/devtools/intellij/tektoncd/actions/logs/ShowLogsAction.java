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
import com.redhat.devtools.intellij.tektoncd.settings.SettingsState;
import com.redhat.devtools.intellij.tektoncd.telemetry.TelemetryService;
import com.redhat.devtools.intellij.tektoncd.tkn.Tkn;
import com.redhat.devtools.intellij.tektoncd.tree.EventListenerNode;
import com.redhat.devtools.intellij.tektoncd.tree.PipelineNode;
import com.redhat.devtools.intellij.tektoncd.tree.PipelineRunNode;
import com.redhat.devtools.intellij.tektoncd.tree.TaskNode;
import com.redhat.devtools.intellij.tektoncd.tree.TaskRunNode;
import java.io.IOException;

import com.redhat.devtools.intellij.telemetry.core.service.TelemetryMessageBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_EVENTLISTENER;
import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_PIPELINERUN;
import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_TASKRUN;
import static com.redhat.devtools.intellij.tektoncd.telemetry.TelemetryService.NAME_PREFIX_DIAG;
import static com.redhat.devtools.intellij.telemetry.core.util.AnonymizeUtils.anonymizeResource;

public class ShowLogsAction extends LogsBaseAction {

    private static final Logger logger = LoggerFactory.getLogger(ShowLogsAction.class);

    public ShowLogsAction() {
        super(EventListenerNode.class);
    }

    protected void doActionPerformed(String namespace, String resourceName, Class nodeClass, Tkn tkncli) {
        try {
            boolean toEditor = SettingsState.getInstance().displayLogsInEditor;
            if (PipelineNode.class.equals(nodeClass) || PipelineRunNode.class.equals(nodeClass)) {
                telemetry.property(TelemetryService.PROP_RESOURCE_KIND, KIND_PIPELINERUN);
                tkncli.showLogsPipelineRun(namespace, resourceName, toEditor);
            } else if (TaskNode.class.equals(nodeClass) || TaskRunNode.class.equals(nodeClass)) {
                telemetry.property(TelemetryService.PROP_RESOURCE_KIND, KIND_TASKRUN);
                tkncli.showLogsTaskRun(namespace, resourceName, toEditor);
            } else if (EventListenerNode.class.equals(nodeClass)) {
                telemetry.property(TelemetryService.PROP_RESOURCE_KIND, KIND_EVENTLISTENER);
                tkncli.showLogsEventListener(namespace, resourceName);
            }
            telemetry.send();
        } catch (IOException e) {
            String errorMessage = "Could not show logs for " + resourceName + "\n" + e.getLocalizedMessage();
            telemetry
                    .error(anonymizeResource(resourceName, namespace, errorMessage))
                    .send();
            UIHelper.executeInUI(() ->
                    Messages.showErrorDialog(
                            errorMessage,
                            "Error"));
            logger.warn(errorMessage, e);
        }
    }

    @Override
    protected TelemetryMessageBuilder.ActionMessage createTelemetry() {
        return TelemetryService.instance().action(NAME_PREFIX_DIAG + ": show logs");
    }
}
