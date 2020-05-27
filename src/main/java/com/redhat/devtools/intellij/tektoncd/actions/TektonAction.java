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

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.treeStructure.Tree;
import com.redhat.devtools.intellij.common.actions.StructureTreeAction;
import com.redhat.devtools.intellij.tektoncd.Constants;
import com.redhat.devtools.intellij.tektoncd.tkn.Tkn;
import com.redhat.devtools.intellij.tektoncd.tree.TektonRootNode;
import com.redhat.devtools.intellij.tektoncd.tree.TektonTreeStructure;
import com.redhat.devtools.intellij.tektoncd.utils.SnippetHelper;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.tree.TreePath;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

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

    public String getSnippet(String snippet) {
        String content = null;
        try {
            content = SnippetHelper.getBody(snippet);
        } catch (IOException e) {
            logger.warn("Error: " + e.getLocalizedMessage(), e);
        }
        return content;
    }

    public void createAndOpenVirtualFile(Project project, String namespace, String name, String content, String kind) {
        try {
            VirtualFile vf = createTempFile(name, content);
            vf.putUserData(KIND_PLURAL, kind);
            vf.putUserData(PROJECT, project);
            vf.putUserData(NAMESPACE, namespace);
            File fileToDelete = new File(vf.getPath());
            fileToDelete.deleteOnExit();
            FileEditorManager.getInstance(project).openFile(vf, true);
        } catch (IOException e) {
            logger.warn(e.getLocalizedMessage(), e);
        }
    }

    private VirtualFile createTempFile(String name, String content) throws IOException {
        File file = new File(System.getProperty("java.io.tmpdir"), name);
        FileUtils.write(file, content, StandardCharsets.UTF_8);
        return LocalFileSystem.getInstance().refreshAndFindFileByIoFile(file);
    }
}