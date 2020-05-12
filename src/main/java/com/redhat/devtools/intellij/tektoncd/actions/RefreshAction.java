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
import com.redhat.devtools.intellij.common.actions.StructureTreeAction;
import com.redhat.devtools.intellij.tektoncd.Constants;
import com.redhat.devtools.intellij.tektoncd.tree.ClusterTasksNode;
import com.redhat.devtools.intellij.tektoncd.tree.ConditionsNode;
import com.redhat.devtools.intellij.tektoncd.tree.ClusterTriggerBindingsNode;
import com.redhat.devtools.intellij.tektoncd.tree.EventListenersNode;
import com.redhat.devtools.intellij.tektoncd.tree.PipelineNode;
import com.redhat.devtools.intellij.tektoncd.tree.PipelineRunsNode;
import com.redhat.devtools.intellij.tektoncd.tree.PipelinesNode;
import com.redhat.devtools.intellij.tektoncd.tree.ResourcesNode;
import com.redhat.devtools.intellij.tektoncd.tree.TaskNode;
import com.redhat.devtools.intellij.tektoncd.tree.TaskRunsNode;
import com.redhat.devtools.intellij.tektoncd.tree.TasksNode;
import com.redhat.devtools.intellij.tektoncd.tree.TektonTreeStructure;
import com.redhat.devtools.intellij.tektoncd.tree.TriggerBindingsNode;
import com.redhat.devtools.intellij.tektoncd.tree.TriggerTemplatesNode;

import javax.swing.tree.TreePath;

public class RefreshAction extends StructureTreeAction {
    public RefreshAction() {
        super(PipelinesNode.class, TasksNode.class, ClusterTasksNode.class, ResourcesNode.class, PipelineNode.class, TaskNode.class, PipelineRunsNode.class, TaskRunsNode.class, ConditionsNode.class,
                TriggerTemplatesNode.class, TriggerBindingsNode.class, ClusterTriggerBindingsNode.class, EventListenersNode.class);
    }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent, TreePath path, Object selected) {
        selected = StructureTreeAction.getElement(selected);
        TektonTreeStructure structure = (TektonTreeStructure) getTree(anActionEvent).getClientProperty(Constants.STRUCTURE_PROPERTY);
        structure.fireModified(selected);
    }
}
