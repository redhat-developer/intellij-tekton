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
package com.redhat.devtools.intellij.tektoncd.actions.clustertask;

import com.google.common.base.Strings;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.redhat.devtools.intellij.tektoncd.actions.TektonAction;
import com.redhat.devtools.intellij.tektoncd.tkn.Tkn;
import com.redhat.devtools.intellij.tektoncd.tree.ClusterTasksNode;
import com.redhat.devtools.intellij.tektoncd.utils.VirtualFileHelper;

import javax.swing.tree.TreePath;

import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_CLUSTERTASKS;

public class CreateClusterTaskAction extends TektonAction {

    public CreateClusterTaskAction() { super(ClusterTasksNode.class); }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent, TreePath path, Object selected, Tkn tkncli) {
        ClusterTasksNode item = getElement(selected);
        String namespace = item.getParent().getName();
        String content = getSnippet("Tekton: ClusterTask");

        if (!Strings.isNullOrEmpty(content)) {
            VirtualFileHelper.createAndOpenVirtualFile(anActionEvent.getProject(), namespace, "newclustertask.yaml", content, KIND_CLUSTERTASKS, item);
        }
    }
}