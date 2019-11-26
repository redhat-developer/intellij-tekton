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
package com.redhat.devtools.intellij.tektoncd.tree;

import com.redhat.devtools.intellij.common.tree.IconTreeNode;
import com.redhat.devtools.intellij.common.tree.LazyMutableTreeNode;

import javax.swing.tree.DefaultMutableTreeNode;
import java.io.IOException;

public class TasksNode extends LazyMutableTreeNode implements IconTreeNode {
    public TasksNode() {
        super("Tasks");
    }

    @Override
    public void load() {
        super.load();
        try {
            NamespaceNode namespaceNode = (NamespaceNode) getParent();
            ((TektonRootNode)getRoot()).getTkn().getTasks(namespaceNode.toString()).forEach(task -> add(new TaskNode(task)));
        } catch (IOException e) {
            add(new DefaultMutableTreeNode("Failed to load tasks"));
        }
    }

    @Override
    public String getIconName() {
        return "/images/task.png";
    }
}
