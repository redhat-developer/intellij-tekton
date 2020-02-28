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
package com.redhat.devtools.intellij.common.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;
import com.redhat.devtools.intellij.tektoncd.tkn.Tkn;
import com.redhat.devtools.intellij.tektoncd.tkn.TknCli;

import javax.swing.tree.TreePath;
import java.io.IOException;

public class TknAction extends TreeAction {
    public TknAction(Class... filters) { super(filters); }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent, TreePath path, Object selected) {
        try {
            this.actionPerformed(anActionEvent, path, selected, TknCli.get());
        } catch (IOException e) {
            Messages.showErrorDialog("Error: " + e.getLocalizedMessage(), "Error");
        }
    }

    public void actionPerformed(AnActionEvent anActionEvent, TreePath path, Object selected, Tkn tkncli) throws IOException { }
}
