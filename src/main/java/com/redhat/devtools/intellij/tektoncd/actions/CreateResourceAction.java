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
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.fileChooser.FileChooserDialog;
import com.intellij.openapi.fileChooser.FileChooserFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.redhat.devtools.intellij.common.utils.ExecHelper;
import com.redhat.devtools.intellij.common.utils.UIHelper;
import com.redhat.devtools.intellij.tektoncd.kubectl.Kubectl;
import com.redhat.devtools.intellij.tektoncd.tree.NamespaceNode;
import com.redhat.devtools.intellij.tektoncd.tree.TektonRootNode;
import org.jetbrains.annotations.NotNull;

import javax.swing.tree.TreePath;
import java.io.IOException;

public class CreateResourceAction extends KubectlAction {
    public CreateResourceAction() {
        super(NamespaceNode.class);
    }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent, TreePath path, Object selected, Kubectl kubectl) {
        TektonRootNode root = ((TektonRootNode)((NamespaceNode)selected).getRoot());
        VirtualFile[] files = selectFile(root.getModel().getProject());
        if (files.length > 0) {
            ExecHelper.submit(() -> process(kubectl, selected.toString(), files[0]));
        }
    }

    private void process(Kubectl kubectl, String namespace, VirtualFile file)  {
        try {
            kubectl.create(namespace, file.getPath());
        } catch (IOException e) {
            UIHelper.executeInUI(() -> Messages.showDialog(null, "Error create the resource " + file, "Create resource", e.getLocalizedMessage(), new String[] {Messages.OK_BUTTON}, 0, 0, Messages.getErrorIcon()));
        }
    }

    @NotNull
    private VirtualFile[] selectFile(Project project) {
        FileChooserDialog dialog = FileChooserFactory.getInstance().createFileChooser(FileChooserDescriptorFactory.createSingleFileDescriptor(), project, null);
        return dialog.choose(project);
    }
}
