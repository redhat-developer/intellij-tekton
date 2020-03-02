package com.redhat.devtools.intellij.common.actions.component;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.testFramework.LightVirtualFile;
import com.redhat.devtools.intellij.common.actions.TknAction;
import com.redhat.devtools.intellij.common.tree.LazyMutableTreeNode;
import com.redhat.devtools.intellij.tektoncd.tkn.Tkn;
import com.redhat.devtools.intellij.tektoncd.tree.PipelineNode;
import com.redhat.devtools.intellij.tektoncd.tree.ResourceNode;
import com.redhat.devtools.intellij.tektoncd.tree.TaskNode;

import javax.swing.tree.TreePath;
import java.io.IOException;

public class OpenEditorTask extends TknAction {
    public OpenEditorTask() { super(TaskNode.class, PipelineNode.class, ResourceNode.class); }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent, TreePath path, Object selected, Tkn tkncli) throws IOException {
        String content = "";
        String namespace = ((LazyMutableTreeNode)selected).getParent().getParent().toString();
        switch (selected.getClass().getSimpleName()) {
            case "PipelineNode":
                content = tkncli.openPipelineInEditor(namespace, selected.toString());
                break;
            case "ResourceNode":
                content = tkncli.openResourceInEditor(namespace, selected.toString());
                break;
            case "TaskNode":
                content = tkncli.openTaskInEditor(namespace, selected.toString());
                break;
            default:
                break;
        }

        if (!content.isEmpty()) {
            LightVirtualFile lvFile = new LightVirtualFile(selected.toString() + ".json", content);
            FileEditorManager.getInstance(anActionEvent.getProject()).openFile(lvFile, true);
        }
    }
}
