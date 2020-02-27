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

public class NamespaceNode extends LazyMutableTreeNode implements IconTreeNode {
    public NamespaceNode(String name) {
        super(name);
    }
    @Override
    public String getIconName() {
        return "/images/project.png";
    }

    @Override
    public void load() {
        super.load();
        this.add(new PipelinesNode());
        this.add(new TasksNode());
        this.add(new ClusterTasksNode());
        this.add(new ResourcesNode());
    }
}
