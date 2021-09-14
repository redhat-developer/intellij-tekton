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
import com.redhat.devtools.intellij.tektoncd.tkn.Tkn;
import java.io.IOException;
import java.util.function.Supplier;
import javax.swing.Icon;
import org.jetbrains.annotations.NotNull;

public class DebugToolbarTerminateAction extends DebugToolbarAction {

    public DebugToolbarTerminateAction(String text, String description, Icon icon, Tkn tkn, Supplier<DebugModel> model) {
        super(text, description, icon, tkn, model);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        try {
            DebugModel debugModel = model.get();
            tkn.cancelTaskRun(debugModel.getPod().getMetadata().getNamespace(), debugModel.getResource());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
