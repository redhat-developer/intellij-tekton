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
    public void actionPerformed(AnActionEvent anActionEvent, TreePath path, Object selected, Tkn tkncli) {
        int resultDialog = UIHelper.executeInUI(() -> {
            String kind = selected.getClass().getSimpleName().toLowerCase().replace("node", "");
            return Messages.showYesNoDialog("Are you sure you want to delete " + kind + " " + selected.toString() + " ?",
                    "Delete " + selected.toString(),
                    null
            );
        });

        CompletableFuture.runAsync(() -> {
            try {
                if (resultDialog == Messages.OK) {
                    String namespace = ((LazyMutableTreeNode)selected).getParent().getParent().toString();
                    Class<?> nodeClass = selected.getClass();
                    if (PipelineNode.class.equals(nodeClass)) {
                        tkncli.deletePipeline(namespace, selected.toString());
                    } else if (ResourceNode.class.equals(nodeClass)) {
                        tkncli.deleteResource(namespace, selected.toString());
                    } else if (TaskNode.class.equals(nodeClass)) {
                        tkncli.deleteTask(namespace, selected.toString());
                    }
                    ((LazyMutableTreeNode)((LazyMutableTreeNode) selected).getParent()).reload();
                }
            } catch (IOException e) {
                UIHelper.executeInUI(() -> Messages.showErrorDialog("Error: " + e.getLocalizedMessage(), "Error"));
            }
        });

    }
}