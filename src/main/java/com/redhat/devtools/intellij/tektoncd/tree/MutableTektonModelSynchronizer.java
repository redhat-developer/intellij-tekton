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

import com.intellij.ide.util.treeView.AbstractTreeStructure;
import com.intellij.ui.tree.StructureTreeModel;
import com.redhat.devtools.intellij.common.tree.MutableModel;
import com.redhat.devtools.intellij.common.tree.MutableModelSynchronizer;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

public class MutableTektonModelSynchronizer<T> extends MutableModelSynchronizer<T> {

    public MutableTektonModelSynchronizer(StructureTreeModel treeModel,
                                    AbstractTreeStructure structure,
                                    MutableModel<T> mutableModel) {
        super(treeModel, structure, mutableModel);
    }

    @Override
    protected TreePath getTreePath(T element) {
        TreePath path;
        if (isRootNode(element)) {
            path = new TreePath(treeModel.getRoot());
        } else {
            path = findTreePath(element);
        }
        return path!=null?path:new TreePath(treeModel.getRoot());
    }

    private TreePath findTreePath(T element) {
        if (element == null ||
                !(element instanceof ParentableNode)) {
            return null;
        }

        ParentableNode pNode = (ParentableNode) element;
        List<ParentableNode> pNodes = getNodesToNamespace(pNode);
        return findTreePath(pNodes, 0, (DefaultMutableTreeNode)treeModel.getRoot());
    }

    private List<ParentableNode> getNodesToNamespace(ParentableNode node) {
        List<ParentableNode> nodes = new ArrayList<>();
        while (!(node instanceof NamespaceNode)) {
            nodes.add(0, node);
            node = (ParentableNode) node.getParent();
        }
        nodes.add(0, node);
        return nodes;
    }

    private TreePath findTreePath(List<ParentableNode> nodes, int level, DefaultMutableTreeNode start) {
        if (nodes.size() <= level
                || start == null) {
            return null;
        }
        Enumeration children = start.children();
        while (children.hasMoreElements()) {
            Object child = children.nextElement();
            if (!(child instanceof DefaultMutableTreeNode)) {
                continue;
            }

            TreePath path = null;
            if (child.toString().equalsIgnoreCase(nodes.get(level).getName())) {
                if (level == nodes.size() - 1) {
                    return new TreePath(((DefaultMutableTreeNode)child).getPath());
                } else {
                    path = findTreePath(nodes, level + 1, (DefaultMutableTreeNode) child);
                }
            }

            if (path != null) {
                return path;
            }
        }
        return null;
    }
}

