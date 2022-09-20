/*******************************************************************************
 *  Copyright (c) 2022 Red Hat, Inc.
 *  Distributed under license by Red Hat, Inc. All rights reserved.
 *  This program is made available under the terms of the
 *  Eclipse Public License v2.0 which accompanies this distribution,
 *  and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 *  Contributors:
 *  Red Hat, Inc.
 ******************************************************************************/
package com.redhat.devtools.intellij.tektoncd.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.redhat.devtools.intellij.tektoncd.tkn.Tkn;
import com.redhat.devtools.intellij.tektoncd.tree.PipelineNode;
import com.redhat.devtools.intellij.tektoncd.tree.TaskNode;
import com.redhat.devtools.intellij.tektoncd.ui.bundle.CreateBundleDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.tree.TreePath;

public class CreateBundleAction extends TektonAction {
    private static final Logger logger = LoggerFactory.getLogger(AddTriggerAction.class);

    public CreateBundleAction() { super(PipelineNode.class, TaskNode.class); }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent, TreePath path, Object selected, Tkn tkncli) {
        //TelemetryMessageBuilder.ActionMessage telemetry = TelemetryService.instance().action(NAME_PREFIX_CRUD + "add trigger");

        CreateBundleDialog dialog = new CreateBundleDialog(anActionEvent.getProject(), tkncli);
        dialog.show();
    }
}
