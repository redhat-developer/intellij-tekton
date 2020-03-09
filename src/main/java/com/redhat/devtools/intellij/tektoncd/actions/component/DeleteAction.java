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
package com.redhat.devtools.intellij.tektoncd.actions.component;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;
import com.redhat.devtools.intellij.common.tree.LazyMutableTreeNode;
import com.redhat.devtools.intellij.common.ui.delete.DeleteDialog;
import com.redhat.devtools.intellij.common.utils.UIHelper;
import com.redhat.devtools.intellij.tektoncd.actions.TektonAction;
import com.redhat.devtools.intellij.tektoncd.tkn.Tkn;
import com.redhat.devtools.intellij.tektoncd.tree.PipelineNode;
import com.redhat.devtools.intellij.tektoncd.tree.ResourceNode;
import com.redhat.devtools.intellij.tektoncd.tree.TaskNode;

import javax.swing.tree.TreePath;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public class DeleteAction extends TektonAction {
    public DeleteAction() { super(TaskNode.class, PipelineNode.class, ResourceNode.class); }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent, TreePath path, Object selected, Tkn tkncli) throws IOException {
        CompletableFuture.runAsync(() -> {
            try {
                DeleteDialog deleteDialog = UIHelper.executeInUI(() -> {
                    String kind = selected.getClass().getSimpleName().toLowerCase().replace("node", "");
                    DeleteDialog dialog = new DeleteDialog(null,
                            "Delete " + selected.toString(),
                            "Are you sure you want to delete " + kind + " " + selected.toString() + " ?");
                    dialog.show();
                    return dialog;
                });
                if (deleteDialog.isOK()) {
                    String namespace = ((LazyMutableTreeNode)selected).getParent().getParent().toString();
                    switch (selected.getClass().getSimpleName()) {
                        case "PipelineNode":
                            tkncli.deletePipeline(namespace, selected.toString());
                            break;
                        case "ResourceNode":
                            tkncli.deleteResource(namespace, selected.toString());
                            break;
                        case "TaskNode":
                            tkncli.deleteTask(namespace, selected.toString());
                            break;
                        default:
                            break;
                    }
                    ((LazyMutableTreeNode)((LazyMutableTreeNode) selected).getParent()).reload();
                }
            } catch (IOException e) {
                UIHelper.executeInUI(() -> Messages.showErrorDialog("Error: " + e.getLocalizedMessage(), "Login"));
            }
        });

    }
}