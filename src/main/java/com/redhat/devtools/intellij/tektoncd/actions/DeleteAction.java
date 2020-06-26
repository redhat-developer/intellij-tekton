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
import com.redhat.devtools.intellij.common.utils.UIHelper;
import com.redhat.devtools.intellij.tektoncd.Constants;
import com.redhat.devtools.intellij.tektoncd.tkn.Tkn;
import com.redhat.devtools.intellij.tektoncd.tree.ClusterTaskNode;
import com.redhat.devtools.intellij.tektoncd.tree.ClusterTriggerBindingNode;
import com.redhat.devtools.intellij.tektoncd.tree.ConditionNode;
import com.redhat.devtools.intellij.tektoncd.tree.EventListenerNode;
import com.redhat.devtools.intellij.tektoncd.tree.ParentableNode;
import com.redhat.devtools.intellij.tektoncd.tree.PipelineNode;
import com.redhat.devtools.intellij.tektoncd.tree.PipelineRunNode;
import com.redhat.devtools.intellij.tektoncd.tree.ResourceNode;
import com.redhat.devtools.intellij.tektoncd.tree.TaskNode;
import com.redhat.devtools.intellij.tektoncd.tree.TaskRunNode;
import com.redhat.devtools.intellij.tektoncd.tree.TektonTreeStructure;
import com.redhat.devtools.intellij.tektoncd.tree.TriggerBindingNode;
import com.redhat.devtools.intellij.tektoncd.tree.TriggerTemplateNode;
import com.redhat.devtools.intellij.tektoncd.utils.TreeHelper;

import javax.swing.tree.TreePath;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class DeleteAction extends TektonAction {
    public DeleteAction() { super(true,
            TaskNode.class,
            TaskRunNode.class,
            PipelineNode.class,
            PipelineRunNode.class,
            ResourceNode.class,
            ClusterTaskNode.class,
            ConditionNode.class,
            TriggerTemplateNode.class,
            TriggerBindingNode.class,
            ClusterTriggerBindingNode.class,
            EventListenerNode.class); }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent, TreePath[] path, Object[] selected, Tkn tkncli) {
        ParentableNode[] elements = Arrays.stream(selected).map(item -> getElement(item)).toArray(ParentableNode[]::new);
        int resultDialog = UIHelper.executeInUI(() -> {
            String name = "";
            String dialogText = "";
            if (elements.length == 1) {
                name = elements[0].getName();
                dialogText = elements[0].getClass().getSimpleName().toLowerCase().replace("node", "") + " " + name + " ?";
            } else {
                dialogText = "the following items?\n";
                for (ParentableNode element: elements) {
                    dialogText += element.getName() + "\n";
                }
            }

            return Messages.showYesNoDialog("Are you sure you want to delete " + dialogText,
                    "Delete " + name,
                    null
            );
        });

        CompletableFuture.runAsync(() -> {
            if (resultDialog == Messages.OK) {
                String namespace = elements[0].getNamespace();
                Map<Class, List<ParentableNode>> resourcesByClass = TreeHelper.getResourcesByClass(elements);
                for(Class type: resourcesByClass.keySet()) {
                    try {
                        List<String> resources = resourcesByClass.get(type).stream().map(x -> x.getName()).collect(Collectors.toList());
                        if (type.equals(PipelineNode.class)) {
                            tkncli.deletePipelines(namespace, resources);
                        } else if (type.equals(PipelineRunNode.class)) {
                            tkncli.deletePipelineRuns(namespace, resources);
                        } else if (type.equals(ResourceNode.class)) {
                            tkncli.deleteResources(namespace, resources);
                        } else if (type.equals(TaskNode.class)) {
                            tkncli.deleteTasks(namespace, resources);
                        } else if (type.equals(ClusterTaskNode.class)) {
                            tkncli.deleteClusterTasks(resources);
                        } else if (type.equals(TaskRunNode.class)) {
                            tkncli.deleteTaskRuns(namespace, resources);
                        } else if (type.equals(ConditionNode.class)) {
                            tkncli.deleteConditions(namespace, resources);
                        } else if (type.equals(TriggerTemplateNode.class)) {
                            tkncli.deleteTriggerTemplates(namespace, resources);
                        } else if (type.equals(TriggerBindingNode.class)) {
                            tkncli.deleteTriggerBindings(namespace, resources);
                        } else if (type.equals(ClusterTriggerBindingNode.class)) {
                            tkncli.deleteClusterTriggerBindings(resources);
                        } else if (type.equals(EventListenerNode.class)) {
                            tkncli.deleteEventListeners(namespace, resources);
                        }
                        ((TektonTreeStructure) getTree(anActionEvent).getClientProperty(Constants.STRUCTURE_PROPERTY)).fireModified(resourcesByClass.get(type).get(0).getParent());
                    } catch (IOException e) {
                        UIHelper.executeInUI(() -> Messages.showErrorDialog("Error: " + e.getLocalizedMessage(), "Error"));
                    }
                }
            }
        });

    }
}