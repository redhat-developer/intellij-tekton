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
package com.redhat.devtools.intellij.tektoncd.utils;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.treeStructure.Tree;
import com.redhat.devtools.intellij.tektoncd.tkn.Tkn;
import com.redhat.devtools.intellij.tektoncd.tree.*;

import java.io.IOException;

import static com.redhat.devtools.intellij.tektoncd.Constants.*;
import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_TASKRUN;

public class TreeHelper {

    public static Tree getTree(Project project) {
        ToolWindow window = ToolWindowManager.getInstance(project).getToolWindow("Tekton");
        JBScrollPane pane = (JBScrollPane) window.getContentManager().findContent("").getComponent();
        return (Tree) pane.getViewport().getView();
    }

    /**
     * Get YAML and Tekton kind from Tekton tree node.
     * @param node the Tekton tree node
     * @return Pair where 'first' is YAML content and 'second' is Tekton kind
     * @throws IOException
     */
    public static Pair<String, String> getYAMLAndKindFromNode(ParentableNode<? extends ParentableNode<?>> node ) throws IOException {
        String namespace = node.getNamespace();
        Tkn tkncli = node.getRoot().getTkn();
        String content = "";
        String kind = "";
        if (node instanceof PipelineNode) {
            content = tkncli.getPipelineYAML(namespace, node.getName());
            kind = KIND_PIPELINES;
        } else if (node instanceof ResourceNode) {
            content = tkncli.getResourceYAML(namespace, node.getName());
            kind = KIND_RESOURCES;
        } else if (node instanceof TaskNode) {
            content = tkncli.getTaskYAML(namespace, node.getName());
            kind = KIND_TASKS;
        } else if (node instanceof ClusterTaskNode) {
            content = tkncli.getClusterTaskYAML(node.getName());
            kind = KIND_CLUSTERTASKS;
        } else if (node instanceof ConditionNode) {
            content = tkncli.getConditionYAML(namespace, node.getName());
            kind = KIND_CONDITIONS;
        } else if (node instanceof TriggerTemplateNode) {
            content = tkncli.getTriggerTemplateYAML(namespace, node.getName());
            kind = KIND_TRIGGERTEMPLATES;
        } else if (node instanceof TriggerBindingNode) {
            content = tkncli.getTriggerBindingYAML(namespace, node.getName());
            kind = KIND_TRIGGERBINDINGS;
        } else if (node instanceof ClusterTriggerBindingNode) {
            content = tkncli.getClusterTriggerBindingYAML(namespace, node.getName());
            kind = KIND_CLUSTERTRIGGERBINDINGS;
        } else if (node instanceof EventListenerNode) {
            content = tkncli.getEventListenerYAML(namespace, node.getName());
            kind = KIND_EVENTLISTENER;
        } else if(node instanceof TaskRunNode){
            content = tkncli.getTaskRunYAML(namespace, node.getName());
            kind = KIND_TASKRUN;
        }

        return Pair.pair(content, kind);
    }
}
