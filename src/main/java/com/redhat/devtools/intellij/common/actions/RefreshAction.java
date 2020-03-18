package com.redhat.devtools.intellij.common.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.redhat.devtools.intellij.common.tree.LazyMutableTreeNode;
import com.redhat.devtools.intellij.tektoncd.tree.ClusterTasksNode;
import com.redhat.devtools.intellij.tektoncd.tree.PipelinesNode;
import com.redhat.devtools.intellij.tektoncd.tree.ResourcesNode;
import com.redhat.devtools.intellij.tektoncd.tree.TasksNode;

import javax.swing.tree.TreePath;

public class RefreshAction extends TreeAction {
    public RefreshAction() { super(PipelinesNode.class, TasksNode.class, ClusterTasksNode.class, ResourcesNode.class); }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent, TreePath path, Object selected) {
        ((LazyMutableTreeNode) selected).reload();
    }
}
