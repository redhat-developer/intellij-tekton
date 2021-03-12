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
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.redhat.devtools.intellij.common.utils.ExecHelper;
import com.redhat.devtools.intellij.common.utils.UIHelper;
import com.redhat.devtools.intellij.tektoncd.telemetry.TelemetryService;
import com.redhat.devtools.intellij.tektoncd.tkn.Tkn;
import com.redhat.devtools.intellij.tektoncd.tree.ClusterTasksNode;
import com.redhat.devtools.intellij.tektoncd.tree.ParentableNode;
import com.redhat.devtools.intellij.tektoncd.tree.TasksNode;
import com.redhat.devtools.intellij.tektoncd.ui.hub.HubDialog;
import com.redhat.devtools.intellij.tektoncd.ui.hub.HubModel;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.tree.TreePath;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import static com.redhat.devtools.intellij.tektoncd.Constants.HUB_CATALOG_TAG;
import static com.redhat.devtools.intellij.telemetry.core.service.TelemetryMessageBuilder.ActionMessage;
import static com.redhat.devtools.intellij.telemetry.core.util.AnonymizeUtils.anonymizeResource;

public class TektonHubAction extends TektonAction {
    Logger logger = LoggerFactory.getLogger(TektonHubAction.class);

    public TektonHubAction() { super(TasksNode.class, ClusterTasksNode.class); }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent, TreePath path, Object selected, Tkn tkncli) {
        ActionMessage telemetry = TelemetryService.instance().action("tekton hub");
        ExecHelper.submit(() -> {
            ParentableNode element = getElement(selected);
            String namespace = element.getNamespace();
            List<String> tasks, clusterTasks;
            try {
                tasks = tkncli.getTasks(namespace).stream()
                        .filter(task -> task.getMetadata().getLabels() != null && task.getMetadata().getLabels().containsKey(HUB_CATALOG_TAG))
                        .map(task -> task.getMetadata().getName())
                        .collect(Collectors.toList());
                clusterTasks = tkncli.getClusterTasks().stream()
                        .filter(ct -> ct.getMetadata().getLabels() != null && ct.getMetadata().getLabels().containsKey(HUB_CATALOG_TAG))
                        .map(ct -> ct.getMetadata().getName())
                        .collect(Collectors.toList());
                Project project = getEventProject(anActionEvent);
                HubModel model = new HubModel(project, tkncli, namespace, tasks, clusterTasks, element instanceof TasksNode);
                telemetry.send();
                UIHelper.executeInUI(() -> {
                    HubDialog wizard = new HubDialog(project, model);
                    wizard.setModal(false);
                    wizard.show();
                    return wizard;
                });
            } catch (IOException e) {
                String errorMessage = "Failed to retrieve data for " + element.getName() + " in namespace " + namespace + ". An error occurred while retrieving them.\n" + e.getLocalizedMessage();
                telemetry.error(anonymizeResource(element.getName(), namespace, errorMessage)).send();
                UIHelper.executeInUI(() ->
                        Messages.showErrorDialog(
                                errorMessage,
                                "Error"));
                logger.warn("Error: " + e.getLocalizedMessage());
            }
        });

    }
}