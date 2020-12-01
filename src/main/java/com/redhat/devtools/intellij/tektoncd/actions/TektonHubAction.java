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
package com.redhat.devtools.intellij.tektoncd.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;
import com.redhat.devtools.intellij.common.utils.ExecHelper;
import com.redhat.devtools.intellij.common.utils.UIHelper;
import com.redhat.devtools.intellij.tektoncd.tkn.Tkn;
import com.redhat.devtools.intellij.tektoncd.tree.ParentableNode;
import com.redhat.devtools.intellij.tektoncd.tree.TasksNode;
import com.redhat.devtools.intellij.tektoncd.ui.hub.HubDialog;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.tree.TreePath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TektonHubAction extends TektonAction {
    Logger logger = LoggerFactory.getLogger(TektonHubAction.class);

    public TektonHubAction() { super(TasksNode.class); }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent, TreePath path, Object selected, Tkn tkncli) {

        ExecHelper.submit(() -> {

            ParentableNode element = getElement(selected);
            String namespace = element.getNamespace();
            List<String> tasks = Collections.emptyList();
            try {
                tasks = tkncli.getTasks(namespace).stream().map(task -> task.getMetadata().getName()).collect(Collectors.toList());
            } catch (IOException e) {
                UIHelper.executeInUI(() ->
                        Messages.showErrorDialog(
                                "Failed to retrieve data for " + element.getName() + " in namespace " + namespace + ". An error occurred while retrieving them.\n" + e.getLocalizedMessage(),
                                "Error"));
                logger.warn("Error: " + e.getLocalizedMessage());
                return;
            }

            List<String> finalTasks = tasks;
            HubDialog hubDialog = UIHelper.executeInUI(() -> {
                HubDialog wizard = new HubDialog(getEventProject(anActionEvent), namespace, finalTasks);
                wizard.show();
                return wizard;
            });
        });

    }
}