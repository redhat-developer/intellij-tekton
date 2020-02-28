/*******************************************************************************
 * Copyright (c) 2020 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package com.redhat.devtools.intellij.tektoncd.tree;

import com.redhat.devtools.intellij.common.tree.IconTreeNode;
import com.redhat.devtools.intellij.common.tree.LazyMutableTreeNode;

import javax.swing.tree.DefaultMutableTreeNode;
import java.io.IOException;

public class ClusterTasksNode extends LazyMutableTreeNode implements IconTreeNode {
    public ClusterTasksNode() {
        super("ClusterTasks");
    }

    @Override
    public void load() {
        super.load();
        try {
            NamespaceNode namespaceNode = (NamespaceNode) getParent();
            ((TektonRootNode)getRoot()).getTkn().getClusterTasks(namespaceNode.toString()).forEach(task -> add(new TaskNode(task)));
        } catch (IOException e) {
            add(new DefaultMutableTreeNode("Failed to load clustertasks"));
        }
    }

    @Override
    public String getIconName() {
        return "/images/clustertask.png";
    }
}
