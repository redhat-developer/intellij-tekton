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
import com.redhat.devtools.intellij.common.utils.ExecHelper;
import com.redhat.devtools.intellij.tektoncd.tkn.Tkn;
import com.redhat.devtools.intellij.tektoncd.tree.ParentableNode;
import com.redhat.devtools.intellij.tektoncd.tree.PipelineRunNode;
import com.redhat.devtools.intellij.tektoncd.tree.TaskRunNode;
import javax.swing.tree.TreePath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShowDiagnosticDataAction extends TektonAction {
    Logger logger = LoggerFactory.getLogger(ShowDiagnosticDataAction.class);

    public ShowDiagnosticDataAction() { super(PipelineRunNode.class, TaskRunNode.class); }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent, TreePath path, Object selected, Tkn tkncli) {
        ExecHelper.submit(() -> {
            ParentableNode element = getElement(selected);
            String namespace = element.getNamespace();
            if (element instanceof PipelineRunNode) {
                tkncli.getDiagnosticData(namespace, "tekton.dev/pipelineRun", element.getName());
            } else if (element instanceof TaskRunNode) {
                tkncli.getDiagnosticData(namespace, "tekton.dev/taskRun", element.getName());
            }
        });
    }
}
