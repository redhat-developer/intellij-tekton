package com.redhat.devtools.intellij.common.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;
import com.redhat.devtools.intellij.tektoncd.tkn.TknCli;

import javax.swing.tree.TreePath;
import java.io.IOException;

public class TknAction extends TreeAction {
    public TknAction(Class... filters) { super(filters); }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent, TreePath path, Object selected) {
        try {
            this.actionPerformed(anActionEvent, path, selected, ((TknCli) TknCli.get()).getCommand());
        } catch (IOException e) {
            Messages.showErrorDialog("Error: " + e.getLocalizedMessage(), "Error");
        }
    }

    public void actionPerformed(AnActionEvent anActionEvent, TreePath path, Object selected, String tkncli) throws IOException { }
}
