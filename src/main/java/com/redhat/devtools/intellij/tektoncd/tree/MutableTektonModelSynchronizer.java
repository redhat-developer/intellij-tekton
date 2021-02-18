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
import com.intellij.ide.util.treeView.NodeDescriptor;
import com.intellij.ui.tree.StructureTreeModel;
import com.redhat.devtools.intellij.common.tree.MutableModel;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.function.Supplier;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

public class MutableTektonModelSynchronizer<T> implements MutableModel.Listener<T> {
    private final StructureTreeModel treeModel;
    private final AbstractTreeStructure structure;
    private final MutableModel<T> mutableModel;

    public MutableTektonModelSynchronizer(StructureTreeModel treeModel,
                                    AbstractTreeStructure structure,
                                    MutableModel<T> mutableModel) {
        this.treeModel = treeModel;
        this.structure = structure;
        this.mutableModel = mutableModel;
        this.mutableModel.addListener(this);
    }

    private void invalidatePath(Supplier<TreePath> pathSupplier) {
        treeModel.getInvoker().runOrInvokeLater(() -> {
            TreePath path = pathSupplier.get();
            if (path.getLastPathComponent() == treeModel.getRoot()) {
                invalidateRoot();
            }
            treeModel.invalidate(path, true);
        });
    }

    private void invalidateRoot() {
        treeModel.invalidate();
    }

    private T getParentElement(T element) {
        return (T) structure.getParentElement(element);
    }

    private TreePath getTreePath(T element) {
        TreePath path;
        if (isRootNode(element)) {
            path = new TreePath(treeModel.getRoot());
        } else {
            path = findTreePath(element); //, (DefaultMutableTreeNode)treeModel.getRoot());
        }
        return path!=null?path:new TreePath(treeModel.getRoot());
    }

    private boolean isRootNode(T element) {
        NodeDescriptor descriptor = (NodeDescriptor) ((DefaultMutableTreeNode)treeModel.getRoot()).getUserObject();
        return descriptor != null && descriptor.getElement() == element;
    }

    private TreePath findTreePath(T element) {
        if (element == null) {
            return null;
        }
        ParentableNode pNode;
        if (element instanceof ParentableNode) {
            pNode = (ParentableNode) element;
        } else {
            return null; //usa original method
        }
        List<ParentableNode> pNodes = getParentableNodesToNamespace(pNode);
        return findTreePath(pNodes, 0, (DefaultMutableTreeNode)treeModel.getRoot());
    }

    private List<ParentableNode> getParentableNodesToNamespace(ParentableNode node) {
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

            if (level == nodes.size() - 1 && child.toString().equalsIgnoreCase(nodes.get(level).getName())) {
                return new TreePath(((DefaultMutableTreeNode)child).getPath());
            }

            //se è elemento del livello
            TreePath path = null;
            if (child.toString().equalsIgnoreCase(nodes.get(level).getName())) { //hasElement(nodes, level + 1, nodes[level])) {
                path = findTreePath(nodes, level + 1, (DefaultMutableTreeNode) child);
            }

            if (path != null) {
                return path;
            }
        }
        return null;
    }

    private TreePath findTreePath(T element, DefaultMutableTreeNode start) {
        if (element == null
                || start == null) {
            return null;
        }
        Enumeration children = start.children();
        while (children.hasMoreElements()) {
            Object child = children.nextElement();
            if (!(child instanceof DefaultMutableTreeNode)) {
                continue;
            }
            if (hasElement(element, (DefaultMutableTreeNode) child)) {
                return new TreePath(((DefaultMutableTreeNode)child).getPath());
            }
            TreePath path = findTreePath(element, (DefaultMutableTreeNode) child);
            if (path != null) {
                return path;
            }
        }
        return null;
    }

    private boolean hasElement(T element, DefaultMutableTreeNode node) {
        NodeDescriptor descriptor = (NodeDescriptor) node.getUserObject();
        return descriptor != null && descriptor.getElement() == element;
    }


    @Override
    public void onAdded(T element) {
        invalidatePath(() -> getTreePath(getParentElement(element)));
    }

    @Override
    public void onModified(T element) {
        invalidatePath(() -> getTreePath(element));
    }

    @Override
    public void onRemoved(T element) {
        invalidatePath(() -> getTreePath(getParentElement(element)));
    }
}

