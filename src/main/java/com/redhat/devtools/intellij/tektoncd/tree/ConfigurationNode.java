/*******************************************************************************
 * Copyright (c) 2021 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc.
 ******************************************************************************/
package com.redhat.devtools.intellij.tektoncd.tree;

public class ConfigurationNode extends ParentableNode<ConfigurationsNode> {
    private String name, displayName;
    protected ConfigurationNode(TektonRootNode root, ConfigurationsNode parent, String name, String displayName) {
        super(root, parent, displayName);
        this.name = name;
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getName() {
        return name;
    }
}
