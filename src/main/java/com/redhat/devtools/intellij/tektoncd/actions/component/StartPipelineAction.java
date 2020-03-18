package com.redhat.devtools.intellij.tektoncd.actions.component;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.ProjectManager;
import com.redhat.devtools.intellij.tektoncd.actions.TektonAction;
import com.redhat.devtools.intellij.tektoncd.tkn.Tkn;
import com.redhat.devtools.intellij.tektoncd.tree.PipelineNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.tree.TreePath;
import java.io.IOException;

public class StartPipelineAction extends TektonAction {
    Logger logger = LoggerFactory.getLogger(StartPipelineAction.class);

    public StartPipelineAction() { super(PipelineNode.class); }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent, TreePath path, Object selected, Tkn tkncli) {
        String namespace = ((PipelineNode)selected).getParent().getParent().toString();
        try {
            tkncli.startPipeline(namespace, selected.toString());
            ((PipelineNode)selected).reload();
        } catch (IOException e) {
            // give a visual notification to user if an error occurs
            Notification notification = new Notification("SaveNotification", "Error", e.getLocalizedMessage(), NotificationType.ERROR);
            notification.notify(ProjectManager.getInstance().getDefaultProject());
            logger.error("Error: " + e.getLocalizedMessage());
        }
    }
}
