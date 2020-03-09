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

import com.intellij.ide.scratch.ScratchRootType;
import com.intellij.lang.Language;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.redhat.devtools.intellij.common.listener.DeleteFileListener;
import com.redhat.devtools.intellij.common.tree.LazyMutableTreeNode;
import com.redhat.devtools.intellij.tektoncd.actions.TektonAction;
import com.redhat.devtools.intellij.tektoncd.tkn.Tkn;
import com.redhat.devtools.intellij.tektoncd.tree.PipelineNode;
import com.redhat.devtools.intellij.tektoncd.tree.ResourceNode;
import com.redhat.devtools.intellij.tektoncd.tree.TaskNode;

import javax.swing.tree.TreePath;
import java.io.IOException;
import java.util.Arrays;

import static com.redhat.devtools.intellij.common.CommonConstants.DELETEFLAG;

public class OpenEditorTaskAction extends TektonAction {
    public OpenEditorTaskAction() { super(TaskNode.class, PipelineNode.class, ResourceNode.class); }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent, TreePath path, Object selected, Tkn tkncli) throws IOException {
        String content = "";
        String namespace = ((LazyMutableTreeNode)selected).getParent().getParent().toString();
        switch (selected.getClass().getSimpleName()) {
            case "PipelineNode":
                content = tkncli.getPipelineJSON(namespace, selected.toString());
                break;
            case "ResourceNode":
                content = tkncli.getResourceJSON(namespace, selected.toString());
                break;
            case "TaskNode":
                content = tkncli.getTaskJSON(namespace, selected.toString());
                break;
            default:
                break;
        }

        if (!content.isEmpty()) {
            Project project = anActionEvent.getProject();
            boolean fileAlreadyOpened = Arrays.stream(FileEditorManager.getInstance(project).getAllEditors()).
                                               anyMatch(fileEditor -> fileEditor.getFile().getName().startsWith(namespace + "-" + selected.toString()) &&
                                                                      fileEditor.getFile().getExtension().equals("json"));
            if (!fileAlreadyOpened) {
                VirtualFile fv = ScratchRootType.getInstance().createScratchFile(project, namespace + "-" + selected.toString() + ".json", Language.ANY, content);
                fv.putUserData(DELETEFLAG, "file");
                project.getMessageBus().connect().subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, new DeleteFileListener());
                FileEditorManager.getInstance(project).openFile(fv, true);
            }
        }
    }
}
