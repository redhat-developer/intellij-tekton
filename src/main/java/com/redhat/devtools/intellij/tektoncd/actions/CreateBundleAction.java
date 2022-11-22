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
import com.intellij.openapi.project.DumbAwareAction;
import com.redhat.devtools.intellij.tektoncd.tkn.Tkn;
import com.redhat.devtools.intellij.tektoncd.ui.bundle.CreateBundleDialog;
import com.redhat.devtools.intellij.tektoncd.utils.TreeHelper;
import com.redhat.devtools.intellij.telemetry.core.service.TelemetryMessageBuilder;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.redhat.devtools.intellij.tektoncd.telemetry.TelemetryService.NAME_PREFIX_CRUD;
import static com.redhat.devtools.intellij.tektoncd.telemetry.TelemetryService.instance;

public class CreateBundleAction extends DumbAwareAction {
    private static final Logger logger = LoggerFactory.getLogger(AddTriggerAction.class);

    public CreateBundleAction() { }

    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        Tkn tkncli = TreeHelper.getTkn(anActionEvent.getProject());
        if (tkncli == null) {
            return;
        }

        TelemetryMessageBuilder.ActionMessage telemetry = instance().action(NAME_PREFIX_CRUD + "create bundle");

        CreateBundleDialog dialog = new CreateBundleDialog(anActionEvent.getProject(), tkncli, telemetry);
        dialog.setModal(false);
        dialog.show();
    }
}
