package com.redhat.devtools.intellij.common.actions.component;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.redhat.devtools.intellij.common.actions.TknAction;
import com.redhat.devtools.intellij.common.utils.ExecHelper;
import com.redhat.devtools.intellij.tektoncd.tree.TaskNode;
import com.redhat.devtools.intellij.tektoncd.tree.TasksNode;

import javax.swing.tree.TreePath;
import java.io.IOException;

public class RunTaskAction extends TknAction {
    public RunTaskAction() { super(TaskNode.class); }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent, TreePath path, Object selected, String tkncli) throws IOException {
        ExecHelper.execute(tkncli, "task", "start", selected.toString(), "-n", ((TaskNode)selected).getParent().getParent().toString());
    }
}
