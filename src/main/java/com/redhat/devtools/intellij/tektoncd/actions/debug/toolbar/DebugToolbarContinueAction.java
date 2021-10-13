/*******************************************************************************
 * Copyright (c) 2021 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc.
 ******************************************************************************/
package com.redhat.devtools.intellij.tektoncd.actions.debug.toolbar;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.redhat.devtools.intellij.tektoncd.tkn.Tkn;
import com.redhat.devtools.intellij.tektoncd.utils.model.debug.DebugModel;
import com.redhat.devtools.intellij.tektoncd.utils.model.debug.DebugResourceState;
import io.fabric8.kubernetes.client.dsl.ExecWatch;
import javax.swing.Icon;
import org.jetbrains.annotations.NotNull;

public class DebugToolbarContinueAction extends DebugToolbarAction {

    public DebugToolbarContinueAction(String text, String description, Icon icon, Tkn tkn, DebugModel model) {
        super(text, description, icon, tkn, model);
        getTemplatePresentation().setEnabled(false);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        if (model.getResourceStatus().equals(DebugResourceState.DEBUG)) {
            model.updateResourceStatus(DebugResourceState.RUNNING);
            ExecWatch execWatch = tkn.execCommandInContainer(model.getPod(), model.getContainerId(), "sh", getScript());
            execWatch.close();
            tkn.getDebugTabPanelFactory().addContent(model);
        }
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setEnabled(model.getResourceStatus().equals(DebugResourceState.DEBUG));
    }

    protected String getScript() {
        return "/tekton/debug/scripts/debug-continue";
    }
}
