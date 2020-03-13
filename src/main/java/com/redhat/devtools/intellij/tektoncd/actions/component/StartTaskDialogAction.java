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
import com.redhat.devtools.intellij.tektoncd.ui.component.StartTaskDialog;
import com.redhat.devtools.intellij.common.utils.UIHelper;
import com.redhat.devtools.intellij.tektoncd.actions.TektonAction;
import com.redhat.devtools.intellij.tektoncd.tkn.Tkn;
import com.redhat.devtools.intellij.tektoncd.tree.TaskNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.tree.TreePath;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public class StartTaskDialogAction extends TektonAction {
    Logger logger = LoggerFactory.getLogger(StartTaskDialogAction.class);

    public StartTaskDialogAction() { super(TaskNode.class); }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent, TreePath path, Object selected, Tkn tkncli) {
        String namespace = ((TaskNode)selected).getParent().getParent().toString();
        StartTaskDialog stdialog = UIHelper.executeInUI(() -> {
            StartTaskDialog dialog = null;
            try {
                dialog = new StartTaskDialog(null,
                        tkncli.getTaskJSON(namespace, selected.toString()),
                        tkncli.getResources(namespace));
                dialog.show();
            } catch (IOException e) {
                logger.error("Error: " + e.getLocalizedMessage());
            }
            return dialog;
        });
        CompletableFuture.runAsync(() -> {            
            if (stdialog.isOK()) {
                try {
                    tkncli.runTask(namespace, selected.toString(), stdialog.args());
                    ((TaskNode)selected).reload();
                } catch (IOException e) {
                    logger.error("Error: " + e.getLocalizedMessage());
                }
            }
        });


    }
}
