/*******************************************************************************
 * Copyright (c) 2019 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package com.redhat.devtools.intellij.tektoncd.actions;

import com.google.common.base.Strings;
import com.intellij.ide.scratch.ScratchRootType;
import com.intellij.lang.Language;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.treeStructure.Tree;
import com.redhat.devtools.intellij.common.actions.StructureTreeAction;
import com.redhat.devtools.intellij.tektoncd.Constants;
import com.redhat.devtools.intellij.tektoncd.tkn.Tkn;
import com.redhat.devtools.intellij.tektoncd.tree.TektonRootNode;
import com.redhat.devtools.intellij.tektoncd.tree.TektonTreeStructure;
import com.redhat.devtools.intellij.tektoncd.utils.SnippetHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.tree.TreePath;
import java.io.File;
import java.io.IOException;

import static com.redhat.devtools.intellij.common.CommonConstants.PROJECT;
import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_PLURAL;
import static com.redhat.devtools.intellij.tektoncd.Constants.NAMESPACE;

public class TektonAction extends StructureTreeAction {
    Logger logger = LoggerFactory.getLogger(TektonAction.class);
    public TektonAction(Class... filters) {
    super(filters);
  }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent, TreePath path, Object selected) {
        try {
            this.actionPerformed(anActionEvent, path, selected, getTkn(anActionEvent));
        } catch (IOException e) {
            Messages.showErrorDialog("Error: " + e.getLocalizedMessage(), "Error");
        }
    }

    private Tkn getTkn(AnActionEvent anActionEvent) throws IOException {
        Tree tree = getTree(anActionEvent);
        return ((TektonRootNode)((TektonTreeStructure)tree.getClientProperty(Constants.STRUCTURE_PROPERTY)).getRootElement()).getTkn();
    }

    public void actionPerformed(AnActionEvent anActionEvent, TreePath path, Object selected, Tkn tkn) {}

    public String getSnippet(String namespace, String snippet) {
        String content = null;
        try {
            content = SnippetHelper.getBody(snippet);
            if (!Strings.isNullOrEmpty(content) && !Strings.isNullOrEmpty(namespace)) {
                content = content.replace("${namespace}", namespace);
            }
        } catch (IOException e) {
            logger.warn("Error: " + e.getLocalizedMessage(), e);
        }
        return content;
    }

    public void createAndOpenVirtualFile(Project project, String namespace, String name, String content, String kind) {
        VirtualFile vf = ScratchRootType.getInstance().createScratchFile(project, name, Language.ANY, content);
        vf.putUserData(KIND_PLURAL, kind);
        vf.putUserData(PROJECT, project);
        vf.putUserData(NAMESPACE, namespace);
        File fileToDelete = new File(vf.getPath());
        fileToDelete.deleteOnExit();
        FileEditorManager.getInstance(project).openFile(vf, true);
    }

}
