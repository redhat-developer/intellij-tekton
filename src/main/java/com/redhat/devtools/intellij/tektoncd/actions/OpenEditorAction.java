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
import com.intellij.openapi.util.Pair;
import com.redhat.devtools.intellij.common.utils.UIHelper;
import com.redhat.devtools.intellij.tektoncd.tkn.Tkn;
import com.redhat.devtools.intellij.tektoncd.tree.*;
import com.redhat.devtools.intellij.tektoncd.utils.TreeHelper;
import com.redhat.devtools.intellij.tektoncd.utils.VirtualDocumentHelper;

import javax.swing.tree.TreePath;
import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

public class OpenEditorAction extends TektonAction {
    public OpenEditorAction() {
        super(TaskNode.class, PipelineNode.class, ResourceNode.class, ClusterTaskNode.class, ConditionNode.class, TriggerTemplateNode.class, TriggerBindingNode.class, ClusterTriggerBindingNode.class, EventListenerNode.class);
    }


    @Override
    public void actionPerformed(AnActionEvent anActionEvent, TreePath path, Object selected, Tkn tkncli) {
        ParentableNode<? extends ParentableNode<NamespaceNode>> element = getElement(selected);
        String content = "";
        String namespace = element.getParent().getParent().getName();
        Pair<String, String> yamlAndKind = null;
        try {
            yamlAndKind = TreeHelper.getYAMLAndKindFromNode(element);
        } catch (IOException e) {
            UIHelper.executeInUI(() -> Messages.showErrorDialog("Error: " + e.getLocalizedMessage(), "Error"));
        }

        if (yamlAndKind != null && !yamlAndKind.first.isEmpty()) {
            Project project = anActionEvent.getProject();

            Optional<FileEditor> editor = Arrays.stream(FileEditorManager.getInstance(project).getAllEditors()).
                    filter(fileEditor -> fileEditor.getFile().getName().startsWith(namespace + "-" + element.getName() + ".yaml") &&
                            fileEditor.getFile().getExtension().equals("yaml")).findFirst();
            if (!editor.isPresent()) {
                VirtualDocumentHelper.createAndOpenVirtualFile(project, namespace, namespace + "-" + element.getName() + ".yaml", yamlAndKind.first, yamlAndKind.second);
            } else {
                FileEditorManager.getInstance(project).openTextEditor(new OpenFileDescriptor(project, editor.get().getFile()), true);
            }
        }
    }
}
