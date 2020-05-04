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

public class TaskNode extends ParentableNode<ParentableNode<NamespaceNode>> {
    private final boolean cluster;

    public TaskNode(TektonRootNode root, ParentableNode<NamespaceNode> parent, String name, boolean cluster) {
        super(root, parent, name);
        this.cluster = cluster;
    }

    public boolean isCluster() {
        return cluster;
    }
}
