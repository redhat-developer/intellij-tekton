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
import com.redhat.devtools.intellij.common.utils.ExecHelper;
import com.redhat.devtools.intellij.common.utils.UIHelper;
import com.redhat.devtools.intellij.tektoncd.telemetry.TelemetryService;
import com.redhat.devtools.intellij.tektoncd.tkn.Tkn;
import com.redhat.devtools.intellij.tektoncd.tree.ClusterTasksNode;
import com.redhat.devtools.intellij.tektoncd.tree.ParentableNode;
import com.redhat.devtools.intellij.tektoncd.tree.PipelinesNode;
import com.redhat.devtools.intellij.tektoncd.tree.TasksNode;
import com.redhat.devtools.intellij.tektoncd.ui.hub.HubDialog;
import com.redhat.devtools.intellij.tektoncd.ui.hub.HubModel;
import javax.swing.tree.TreePath;


import static com.redhat.devtools.intellij.tektoncd.telemetry.TelemetryService.NAME_PREFIX_CRUD;
import static com.redhat.devtools.intellij.telemetry.core.service.TelemetryMessageBuilder.ActionMessage;

public class TektonHubAction extends TektonAction {

    public TektonHubAction() { super(TasksNode.class, ClusterTasksNode.class, PipelinesNode.class); }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent, TreePath path, Object selected, Tkn tkncli) {
        ActionMessage telemetry = TelemetryService.instance().action(NAME_PREFIX_CRUD + "tekton hub");
        ExecHelper.submit(() -> {
            ParentableNode element = getElement(selected);
            Project project = getEventProject(anActionEvent);
            HubModel model = new HubModel(project, tkncli, element);
            telemetry.send();
            UIHelper.executeInUI(() -> {
                HubDialog wizard = new HubDialog(project, model);
                wizard.setModal(false);
                wizard.show();
                return wizard;
            });
        });

    }
}