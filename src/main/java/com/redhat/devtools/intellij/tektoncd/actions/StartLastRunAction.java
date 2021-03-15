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
import com.redhat.devtools.intellij.tektoncd.Constants;
import com.redhat.devtools.intellij.tektoncd.actions.logs.FollowLogsAction;
import com.redhat.devtools.intellij.tektoncd.telemetry.TelemetryService;
import com.redhat.devtools.intellij.tektoncd.tkn.Tkn;
import com.redhat.devtools.intellij.tektoncd.tree.NamespaceNode;
import com.redhat.devtools.intellij.tektoncd.tree.ParentableNode;
import com.redhat.devtools.intellij.tektoncd.tree.PipelineNode;
import com.redhat.devtools.intellij.tektoncd.tree.TaskNode;
import com.redhat.devtools.intellij.tektoncd.tree.TektonTreeStructure;
import java.io.IOException;
import javax.swing.tree.TreePath;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.redhat.devtools.intellij.tektoncd.Constants.NOTIFICATION_ID;
import static com.redhat.devtools.intellij.tektoncd.telemetry.TelemetryService.PROP_RESOURCE_KIND;
import static com.redhat.devtools.intellij.telemetry.core.service.TelemetryMessageBuilder.*;
import static com.redhat.devtools.intellij.telemetry.core.util.AnonymizeUtils.anonymizeResource;

public class StartLastRunAction extends TektonAction {

    private static final Logger logger = LoggerFactory.getLogger(StartLastRunAction.class);

    private ActionMessageBuilder telemetry;

    public StartLastRunAction() { super(PipelineNode.class, TaskNode.class); }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent, TreePath path, Object selected, Tkn tkncli) {
        telemetry = TelemetryService.instance().action("start last run");
        ExecHelper.submit(() -> {
            ParentableNode<? extends ParentableNode<NamespaceNode>> element = getElement(selected);
            String namespace = element.getParent().getParent().getName();
            try {
                String runName = startRun(tkncli, element, namespace);
                FollowLogsAction.run(namespace, runName, element.getClass(), tkncli);
                ((TektonTreeStructure) getTree(anActionEvent).getClientProperty(Constants.STRUCTURE_PROPERTY)).fireModified(element);
                telemetry.send();
            } catch (IOException e) {
                String errorMessage = element.getName() + " in namespace " + namespace + " failed to start\n" + e.getLocalizedMessage();
                telemetry
                        .error(anonymizeResource(element.getName(), namespace, errorMessage))
                        .send();
                Notification notification = new Notification(NOTIFICATION_ID,
                        "Error",
                        errorMessage,
                        NotificationType.ERROR);
                Notifications.Bus.notify(notification);
                logger.warn("Error: " + e.getLocalizedMessage());
            }
        });
    }

    private String startRun(Tkn tkncli, ParentableNode<? extends ParentableNode<NamespaceNode>> element, String namespace) throws IOException {
        String runName = null;
        if (element instanceof PipelineNode) {
            telemetry.property(PROP_RESOURCE_KIND, Constants.KIND_PIPELINE);
            runName = tkncli.startLastPipeline(namespace, element.getName());
        } else if (element instanceof TaskNode) {
            telemetry.property(PROP_RESOURCE_KIND, Constants.KIND_TASK);
            runName = tkncli.startLastTask(namespace, element.getName());
        }
        return runName;
    }

}
