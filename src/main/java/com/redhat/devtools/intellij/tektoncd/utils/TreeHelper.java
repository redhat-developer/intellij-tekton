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
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.treeStructure.Tree;
import com.redhat.devtools.intellij.common.actions.StructureTreeAction;
import com.redhat.devtools.intellij.common.utils.UIHelper;
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
import com.redhat.devtools.intellij.tektoncd.tree.TriggerBindingNode;
import com.redhat.devtools.intellij.tektoncd.tree.TriggerTemplateNode;
import kotlin.Triple;

import javax.swing.tree.TreePath;
import java.io.IOException;

import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_CLUSTERTASKS;
import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_CLUSTERTRIGGERBINDINGS;
import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_CONDITIONS;
import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_EVENTLISTENER;
import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_PIPELINERUN;
import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_PIPELINES;
import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_RESOURCES;
import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_TASKRUN;
import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_TASKS;
import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_TRIGGERBINDINGS;
import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_TRIGGERTEMPLATES;

public class TreeHelper {

    public static Tree getTree(Project project) {
        ToolWindow window = ToolWindowManager.getInstance(project).getToolWindow("Tekton");
        JBScrollPane pane = (JBScrollPane) window.getContentManager().findContent("").getComponent();
        return (Tree) pane.getViewport().getView();
    }

    /**
     * Get YAML and Tekton kind from Tekton tree node.
     *
     * @param node the Tekton tree node
     * @return Pair where 'first' is YAML content and 'second' is Tekton kind
     * @throws IOException
     */
    public static Triple<String, String, Boolean> getYAMLAndKindFromNode(ParentableNode<?> node) throws IOException {
        String namespace = node.getNamespace();
        Tkn tkncli = node.getRoot().getTkn();
        String content = "";
        String kind = "";
        boolean readonly = false;
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
        } else if (node instanceof TaskRunNode) {
            content = tkncli.getTaskRunYAML(namespace, node.getName());
            kind = KIND_TASKRUN;
            readonly = true;
        } else if (node instanceof PipelineRunNode){
            content = tkncli.getPipelineRunYAML(namespace, node.getName());
            kind = KIND_PIPELINERUN;
            readonly = true;
        }

        return new Triple<>(content, kind, readonly);
    }

    public static void openTektonResourceInEditor(TreePath path) {
        if (path == null) {
            return;
        }

        Object node = path.getLastPathComponent();
        ParentableNode<? extends ParentableNode<?>> element = StructureTreeAction.getElement(node);

        Triple<String, String, Boolean> yamlAndKind = null;
        try {
            yamlAndKind = getYAMLAndKindFromNode(element);
        } catch (IOException e) {
            UIHelper.executeInUI(() -> Messages.showErrorDialog("Error: " + e.getLocalizedMessage(), "Error"));
        }

        if (yamlAndKind != null && !yamlAndKind.getFirst().isEmpty()) {
            Project project = element.getRoot().getProject();
            String namespace = element.getNamespace();
            VirtualFileHelper.openVirtualFileInEditor(project, namespace, element.getName(), yamlAndKind.getFirst(), yamlAndKind.getSecond(), yamlAndKind.getThird());
        }
    }
}
