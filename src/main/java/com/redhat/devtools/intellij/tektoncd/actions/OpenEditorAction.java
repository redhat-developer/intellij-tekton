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
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.redhat.devtools.intellij.common.utils.UIHelper;
import com.redhat.devtools.intellij.tektoncd.tkn.Tkn;
import com.redhat.devtools.intellij.tektoncd.tree.NamespaceNode;
import com.redhat.devtools.intellij.tektoncd.tree.ParentableNode;
import com.redhat.devtools.intellij.tektoncd.tree.PipelineNode;
import com.redhat.devtools.intellij.tektoncd.tree.ResourceNode;
import com.redhat.devtools.intellij.tektoncd.tree.TaskNode;

import javax.swing.tree.TreePath;
import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_PIPELINES;
import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_RESOURCES;
import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_TASKS;

public class OpenEditorAction extends TektonAction {
    public OpenEditorAction() { super(TaskNode.class, PipelineNode.class, ResourceNode.class); }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent, TreePath path, Object selected, Tkn tkncli) {
        ParentableNode<? extends ParentableNode<NamespaceNode>> element = getElement(selected);
        String content = "";
        String namespace = element.getParent().getParent().getName();
        String kind = "";
        try {
            if (element instanceof PipelineNode) {
                content = tkncli.getPipelineYAML(namespace, element.getName());
                kind = KIND_PIPELINES;
            } else if (element instanceof ResourceNode) {
                content = tkncli.getResourceYAML(namespace, element.getName());
                kind = KIND_RESOURCES;
            } else if (element instanceof TaskNode) {
                content = tkncli.getTaskYAML(namespace, element.getName());
                kind = KIND_TASKS;
            }
        }
        catch (IOException e) {
            UIHelper.executeInUI(() -> Messages.showErrorDialog("Error: " + e.getLocalizedMessage(), "Error"));
        }

        if (!content.isEmpty()) {
            Project project = anActionEvent.getProject();

            Optional<FileEditor> editor = Arrays.stream(FileEditorManager.getInstance(project).getAllEditors()).
                    filter(fileEditor -> fileEditor.getFile().getName().startsWith(namespace + "-" + element.getName()) &&
                            fileEditor.getFile().getExtension().equals("yaml")).findFirst();
            if (!editor.isPresent()) {
                createAndOpenVirtualFile(project, namespace + "-" + element.getName() + ".yaml", content, kind);
            } else {
                FileEditorManager.getInstance(project).openTextEditor(new OpenFileDescriptor(project, editor.get().getFile()), true);
            }
        }
    }
}
