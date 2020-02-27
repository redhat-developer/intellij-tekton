package com.redhat.devtools.intellij.tektoncd.tree;

import com.redhat.devtools.intellij.common.tree.IconTreeNode;
import com.redhat.devtools.intellij.common.tree.LazyMutableTreeNode;

import javax.swing.tree.DefaultMutableTreeNode;
import java.io.IOException;

public class ResourcesNode extends LazyMutableTreeNode implements IconTreeNode {
    public ResourcesNode() {
        super("Resources");
    }

    @Override
    public void load() {
        super.load();
        try {
            NamespaceNode namespaceNode = (NamespaceNode) getParent();
            ((TektonRootNode)getRoot()).getTkn().getResources(namespaceNode.toString()).forEach(resource -> add(new ResourceNode(resource)));
        } catch (IOException e) {
            add(new DefaultMutableTreeNode("Failed to load pipeline resources"));
        }
    }

    @Override
    public String getIconName() {
        return "/images/pipeline.png";
    }
}
