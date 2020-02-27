package com.redhat.devtools.intellij.tektoncd.tree;

import com.redhat.devtools.intellij.common.tree.IconTreeNode;
import com.redhat.devtools.intellij.common.tree.LazyMutableTreeNode;

public class ResourceNode extends LazyMutableTreeNode implements IconTreeNode {
    public ResourceNode(String name) {
        super(name);
    }

    @Override
    public String getIconName() {
        return "/images/pipeline.png";
    }
}
