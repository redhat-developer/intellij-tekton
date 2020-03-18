package com.redhat.devtools.intellij.common.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.redhat.devtools.intellij.common.tree.LazyMutableTreeNode;
import com.redhat.devtools.intellij.tektoncd.tree.*;

import javax.swing.tree.TreePath;

public class RefreshAction extends TreeAction {
    public RefreshAction() {
        super(PipelinesNode.class, TasksNode.class, ClusterTasksNode.class, ResourcesNode.class, PipelineNode.class, TaskNode.class);
    }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent, TreePath path, Object selected) {
        ((LazyMutableTreeNode) selected).reload();
    }
}
