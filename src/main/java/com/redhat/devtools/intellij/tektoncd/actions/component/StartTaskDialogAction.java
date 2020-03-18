/*******************************************************************************
 * Copyright (c) 2020 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc.
 ******************************************************************************/
package com.redhat.devtools.intellij.tektoncd.actions.component;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.redhat.devtools.intellij.common.utils.ExecHelper;
import com.redhat.devtools.intellij.common.utils.UIHelper;
import com.redhat.devtools.intellij.tektoncd.actions.TektonAction;
import com.redhat.devtools.intellij.tektoncd.tkn.Resource;
import com.redhat.devtools.intellij.tektoncd.tkn.Tkn;
import com.redhat.devtools.intellij.tektoncd.tree.TaskNode;
import com.redhat.devtools.intellij.tektoncd.ui.component.StartTaskDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.tree.TreePath;
import java.io.IOException;
import java.util.List;

public class StartTaskDialogAction extends TektonAction {
    Logger logger = LoggerFactory.getLogger(StartTaskDialogAction.class);

    public StartTaskDialogAction() { super(TaskNode.class); }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent, TreePath path, Object selected, Tkn tkncli) {
        String namespace = ((TaskNode)selected).getParent().getParent().toString();
        ExecHelper.submit(() -> {
            String task;
            List<Resource> resources;
            try {
                task = tkncli.getTaskYAML(namespace, selected.toString());
                resources = tkncli.getResources(namespace);
            } catch (IOException e) {
                logger.error("Error: " + e.getLocalizedMessage());
                return;
            }
            StartTaskDialog stdialog = UIHelper.executeInUI(() -> {
                StartTaskDialog dialog = new StartTaskDialog(null, task, resources);
                dialog.show();
                return dialog;
            });
            if (stdialog.isOK()) {
                try {
                    tkncli.startTask(namespace, selected.toString(), stdialog.args());
                    ((TaskNode)selected).reload();
                } catch (IOException e) {
                    logger.error("Error: " + e.getLocalizedMessage());
                }
            }
        });
    }
}
