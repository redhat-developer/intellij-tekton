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
package com.redhat.devtools.intellij.common.utils;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.treeStructure.Tree;
import com.redhat.devtools.intellij.common.tree.LazyMutableTreeNode;
import com.redhat.devtools.intellij.tektoncd.tree.PipelinesNode;
import com.redhat.devtools.intellij.tektoncd.tree.ResourcesNode;
import com.redhat.devtools.intellij.tektoncd.tree.TasksNode;

import static com.redhat.devtools.intellij.tektoncd.Constants.*;

public class TreeHelper {

    public static Tree getTree(Project project) {
        ToolWindow window = ToolWindowManager.getInstance(project).getToolWindow("Tekton");
        JBScrollPane pane = (JBScrollPane) window.getContentManager().findContent("").getComponent();
        return (Tree) pane.getViewport().getView();
    }

    /**
     *
     * Refresh node with name and kind on tree.
     * If name is empty refresh first node of that kind
     *
     * @param tree tree of node to be refreshed
     * @param kind kind of node to be refreshed
     * @param name name of node to be refreshed
     */
    public static void refreshNode(Tree tree, String kind, String name) {
        if (tree == null) {
            return;
        }

        Class nodeClass = retrieveNodeClassByKind(kind);

        if (nodeClass == null) return;
        LazyMutableTreeNode[] nodes = (LazyMutableTreeNode[]) tree.getSelectedNodes(nodeClass, null);
        if (nodes != null && nodes.length > 0) {
            nodes[0].reload();
        }
    }

    public static Class retrieveNodeClassByKind(String kind) {
        switch(kind) {
            case KIND_PIPELINES: return PipelinesNode.class;
            case KIND_RESOURCES: return ResourcesNode.class;
            case KIND_TASKS: return TasksNode.class;
            default: return null;
        }
    }
}
