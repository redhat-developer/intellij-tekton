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
import com.redhat.devtools.intellij.tektoncd.actions.task.DebugModel;
import com.redhat.devtools.intellij.tektoncd.actions.task.State;
import com.redhat.devtools.intellij.tektoncd.tkn.Tkn;
import com.redhat.devtools.intellij.tektoncd.ui.toolwindow.debug.DebugPanelBuilder;
import java.io.IOException;
import java.util.function.Supplier;
import javax.swing.Icon;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DebugToolbarTerminateAction extends DebugToolbarAction {

    private static final Logger logger = LoggerFactory.getLogger(DebugToolbarTerminateAction.class);

    public DebugToolbarTerminateAction(String text, String description, Icon icon, Tkn tkn, Supplier<DebugModel> model) {
        super(text, description, icon, tkn, model);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        try {
            DebugModel debugModel = model.get();
            tkn.cancelTaskRun(debugModel.getPod().getMetadata().getNamespace(), debugModel.getResource());
            debugModel.setResourceStatus(State.COMPLETE_FAILED);
            DebugPanelBuilder.instance(tkn).addContent(debugModel);
        } catch (IOException ex) {
            logger.warn(ex.getLocalizedMessage(), ex);
        }
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        boolean isCompleted = model.get().getResourceStatus().equals(State.COMPLETE_SUCCESS) ||
                model.get().getResourceStatus().equals(State.COMPLETE_FAILED);
        e.getPresentation().setEnabled(!isCompleted);
    }
}
