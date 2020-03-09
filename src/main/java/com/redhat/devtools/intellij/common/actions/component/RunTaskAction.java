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
package com.redhat.devtools.intellij.common.actions.component;

import com.intellij.openapi.actionSystem.AnActionEvent;

import com.intellij.openapi.ui.Messages;
import com.redhat.devtools.intellij.common.ui.component.RunTaskDialog;
import com.redhat.devtools.intellij.common.utils.UIHelper;
import com.redhat.devtools.intellij.tektoncd.actions.TektonAction;
import com.redhat.devtools.intellij.tektoncd.tkn.Tkn;
import com.redhat.devtools.intellij.tektoncd.tree.TaskNode;

import javax.swing.tree.TreePath;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public class RunTaskAction extends TektonAction {
    public RunTaskAction() { super(TaskNode.class); }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent, TreePath path, Object selected, Tkn tkncli) {
        CompletableFuture.runAsync(() -> {
            try {
                RunTaskDialog loginDialog = UIHelper.executeInUI(() -> {
                    RunTaskDialog dialog = null;
                    try {
                        String namespace = ((TaskNode)selected).getParent().getParent().toString();
                        dialog = new RunTaskDialog(null,
                                                    tkncli.getTaskJSON(namespace, selected.toString()),
                                                    tkncli.getResources(namespace));
                        dialog.show();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return dialog;
                });
                if (loginDialog.isOK()) {
                    tkncli.runTask(((TaskNode)selected).getParent().getParent().toString(), selected.toString());
                    ((TaskNode) selected).reload();
                }
            } catch (IOException e) {
                UIHelper.executeInUI(() -> Messages.showErrorDialog("Error: " + e.getLocalizedMessage(), "Login"));
            }
        });

    }
}
