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
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.redhat.devtools.intellij.common.tree.LazyMutableTreeNode;
import com.redhat.devtools.intellij.common.utils.UIHelper;
import com.redhat.devtools.intellij.tektoncd.tkn.Tkn;
import com.redhat.devtools.intellij.tektoncd.tree.PipelineNode;
import com.redhat.devtools.intellij.tektoncd.tree.ResourceNode;
import com.redhat.devtools.intellij.tektoncd.tree.TaskNode;

import javax.swing.tree.TreePath;
import java.io.IOException;
import java.util.Arrays;

import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_PIPELINES;
import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_RESOURCES;
import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_TASKS;

public class OpenEditorTaskAction extends TektonAction {
    public OpenEditorTaskAction() { super(TaskNode.class, PipelineNode.class, ResourceNode.class); }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent, TreePath path, Object selected, Tkn tkncli) {
        String content = "";
        String namespace = ((LazyMutableTreeNode)selected).getParent().getParent().toString();
        String kind = "";
        try {
            if (PipelineNode.class.equals(selected.getClass())) {
                content = tkncli.getPipelineYAML(namespace, selected.toString());
                kind = KIND_PIPELINES;
            } else if (ResourceNode.class.equals(selected.getClass())) {
                content = tkncli.getResourceYAML(namespace, selected.toString());
                kind = KIND_RESOURCES;
            } else if (TaskNode.class.equals(selected.getClass())) {
                content = tkncli.getTaskYAML(namespace, selected.toString());
                kind = KIND_TASKS;
            }
        }
        catch (IOException e) {
            UIHelper.executeInUI(() -> Messages.showErrorDialog("Error: " + e.getLocalizedMessage(), "Error"));
        }

        if (!content.isEmpty()) {
            Project project = anActionEvent.getProject();

            boolean fileAlreadyOpened = Arrays.stream(FileEditorManager.getInstance(project).getAllEditors()).
                                               anyMatch(fileEditor -> fileEditor.getFile().getName().startsWith(namespace + "-" + selected.toString()) &&
                                                                      fileEditor.getFile().getExtension().equals("yaml"));
            if (!fileAlreadyOpened) {
                createAndOpenVirtualFile(project, namespace + "-" + selected.toString() + ".yaml", content, kind);
            }
        }
    }
}
