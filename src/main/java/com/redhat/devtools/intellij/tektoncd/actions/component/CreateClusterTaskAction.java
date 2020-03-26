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

import com.google.common.base.Strings;
import com.intellij.ide.scratch.ScratchRootType;
import com.intellij.lang.Language;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.redhat.devtools.intellij.tektoncd.actions.TektonAction;
import com.redhat.devtools.intellij.tektoncd.tkn.Tkn;
import com.redhat.devtools.intellij.tektoncd.tree.ClusterTasksNode;
import com.redhat.devtools.intellij.tektoncd.utils.SnippetHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.tree.TreePath;
import java.io.File;
import java.io.IOException;

import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_PLURAL;
import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_CLUSTERTASKS;

public class CreateClusterTaskAction extends TektonAction {
    Logger logger = LoggerFactory.getLogger(CreateClusterTaskAction.class);

    public CreateClusterTaskAction() { super(ClusterTasksNode.class); }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent, TreePath path, Object selected, Tkn tkncli) {
        String content = null;
        try {
            content = SnippetHelper.getBody("Tekton: ClusterTask");
        } catch (IOException e) {
            logger.error("Error: " + e.getLocalizedMessage(), e);
        }

        if (!Strings.isNullOrEmpty(content)) {
            Project project = anActionEvent.getProject();
            VirtualFile fv = ScratchRootType.getInstance().createScratchFile(project, "newclustertask.yaml", Language.ANY, content);
            // append info to the virtualFile to be used during saving
            fv.putUserData(KIND_PLURAL, KIND_CLUSTERTASKS);
            File fileToDelete = new File(fv.getPath());
            fileToDelete.deleteOnExit();
            FileEditorManager.getInstance(project).openFile(fv, true);
        }
    }
}