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
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.treeStructure.Tree;
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
import com.redhat.devtools.intellij.tektoncd.tree.TektonRootNode;
import com.redhat.devtools.intellij.tektoncd.tree.TektonTreeStructure;
import com.redhat.devtools.intellij.tektoncd.tree.TriggerBindingNode;
import com.redhat.devtools.intellij.tektoncd.tree.TriggerTemplateNode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_CLUSTERTASK;
import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_CLUSTERTASKS;
import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_CLUSTERTRIGGERBINDING;
import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_CLUSTERTRIGGERBINDINGS;
import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_CONDITION;
import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_CONDITIONS;
import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_EVENTLISTENER;
import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_EVENTLISTENERS;
import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_PIPELINE;
import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_PIPELINERESOURCE;
import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_PIPELINERUN;
import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_PIPELINERUNS;
import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_PIPELINES;
import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_RESOURCES;
import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_TASK;
import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_TASKRUN;
import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_TASKRUNS;
import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_TASKS;
import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_TRIGGERBINDING;
import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_TRIGGERBINDINGS;
import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_TRIGGERTEMPLATE;
import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_TRIGGERTEMPLATES;

public class TreeHelper {

    public static Tree getTree(Project project) {
        ToolWindow window = ToolWindowManager.getInstance(project).getToolWindow("Tekton");
        JBScrollPane pane = (JBScrollPane) window.getContentManager().findContent("").getComponent();
        return (Tree) pane.getViewport().getView();
    }

    public static Tkn getTkn(Project project) {
        try {
            TektonTreeStructure treeStructure = (TektonTreeStructure) getTree(project).getClientProperty(Constants.STRUCTURE_PROPERTY);
            TektonRootNode root = (TektonRootNode) treeStructure.getRootElement();
            return root.getTkn();
        } catch(Exception ex) {
            return null;
        }
    }

    public static void refresh(Project project, ParentableNode node) {
        TektonTreeStructure structure = (TektonTreeStructure) getTree(project).getClientProperty(Constants.STRUCTURE_PROPERTY);
        structure.fireModified(node);
    }

    public static String getKindByNode(ParentableNode<?> node) {
        String kind = "";

        if (node instanceof PipelineNode) {
            kind = KIND_PIPELINE;
        } else if (node instanceof PipelineRunNode) {
            kind = KIND_PIPELINERUN;
        } else if (node instanceof ResourceNode) {
            kind = KIND_PIPELINERESOURCE;
        } else if (node instanceof TaskNode) {
            kind = KIND_TASK;
        } else if (node instanceof TaskRunNode) {
            kind = KIND_TASKRUN;
        } else if (node instanceof ClusterTaskNode) {
            kind = KIND_CLUSTERTASK;
        } else if (node instanceof ConditionNode) {
            kind = KIND_CONDITION;
        } else if (node instanceof TriggerBindingNode) {
            kind = KIND_TRIGGERBINDING;
        } else if (node instanceof TriggerTemplateNode) {
            kind = KIND_TRIGGERTEMPLATE;
        } else if (node instanceof ClusterTriggerBindingNode) {
            kind = KIND_CLUSTERTRIGGERBINDING;
        } else if (node instanceof EventListenerNode) {
            kind = KIND_EVENTLISTENER;
        }
        return kind;
    }

    public static String getPluralKindByNode(ParentableNode<?> node) {
        String kind = "";

        if (node instanceof PipelineNode) {
            kind = KIND_PIPELINES;
        } else if (node instanceof PipelineRunNode) {
            kind = KIND_PIPELINERUNS;
        } else if (node instanceof ResourceNode) {
            kind = KIND_RESOURCES;
        } else if (node instanceof TaskNode) {
            kind = KIND_TASKS;
        } else if (node instanceof TaskRunNode) {
            kind = KIND_TASKRUNS;
        } else if (node instanceof ClusterTaskNode) {
            kind = KIND_CLUSTERTASKS;
        } else if (node instanceof ConditionNode) {
            kind = KIND_CONDITIONS;
        } else if (node instanceof TriggerBindingNode) {
            kind = KIND_TRIGGERBINDINGS;
        } else if (node instanceof TriggerTemplateNode) {
            kind = KIND_TRIGGERTEMPLATES;
        } else if (node instanceof ClusterTriggerBindingNode) {
            kind = KIND_CLUSTERTRIGGERBINDINGS;
        } else if (node instanceof EventListenerNode) {
            kind = KIND_EVENTLISTENERS;
        }
        return kind;
    }

    public static Map<Class, List<ParentableNode>> getResourcesByClass(ParentableNode[] elements) {
        Map<Class, List<ParentableNode>> resourcesByClass = new HashMap<>();
        Arrays.stream(elements).forEach(element ->
                resourcesByClass.computeIfAbsent(element.getClass(), value -> new ArrayList<>())
                        .add(element));
        return resourcesByClass;
    }

    public static String getNamespaceFromResourcePath(String path) {
        String[] attributes = path.split("/");
        if (attributes.length == 3) {
            return attributes[0];
        }
        return "";
    }

    public static String getKindFromResourcePath(String path) {
        String[] attributes = path.split("/");
        if (attributes.length < 3) {
            return attributes[0];
        }
        return attributes[1];
    }

    public static String getNameFromResourcePath(String path) {
        String[] attributes = path.split("/");
        return attributes[attributes.length - 1];
    }

    /**
     * Create resource path based on node
     * Returned path is in form tekton://namespace/kind/name for non cluster-scoped resources, tekton://kind/name otherwise
     *
     * @param node node to calculate the path
     * @return
     */
    public static String getTektonResourceUrl(ParentableNode node, boolean hasProtocol) {
        String kind = TreeHelper.getKindByNode(node);
        return getTektonResourceUrl(node.getNamespace(), kind, node.getName(), hasProtocol);
    }

    public static String getTektonResourceUrl(String namespace, String kind, String name, boolean hasProtocol) {
        String path = kind + "/" + name;
        if (!(kind.equalsIgnoreCase(KIND_CLUSTERTASK)) && !(kind.equalsIgnoreCase(KIND_CLUSTERTRIGGERBINDING))) {
            path = namespace + "/" + path;
        }

        if (!hasProtocol) {
            return path;
        }

        return "tekton://" + path;
    }
}
