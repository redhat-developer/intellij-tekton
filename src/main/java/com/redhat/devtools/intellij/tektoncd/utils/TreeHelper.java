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
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.treeStructure.Tree;
import com.redhat.devtools.intellij.common.actions.StructureTreeAction;
import com.redhat.devtools.intellij.common.utils.UIHelper;
import com.redhat.devtools.intellij.tektoncd.Constants;
import com.redhat.devtools.intellij.tektoncd.telemetry.TelemetryService;
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
import com.redhat.devtools.intellij.telemetry.core.service.TelemetryMessageBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.tree.TreePath;


import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_CLUSTERTASK;
import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_CLUSTERTASKS;
import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_CLUSTERTRIGGERBINDING;
import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_CLUSTERTRIGGERBINDINGS;
import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_CONDITION;
import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_CONDITIONS;
import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_EVENTLISTENER;
import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_EVENTLISTENERS;
import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_PIPELINE;
import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_PIPELINERUN;
import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_PIPELINERUNS;
import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_PIPELINES;
import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_RESOURCE;
import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_RESOURCES;
import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_TASK;
import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_TASKRUN;
import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_TASKRUNS;
import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_TASKS;
import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_TRIGGERBINDING;
import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_TRIGGERBINDINGS;
import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_TRIGGERTEMPLATE;
import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_TRIGGERTEMPLATES;
import static com.redhat.devtools.intellij.tektoncd.telemetry.TelemetryService.NAME_PREFIX_ACTION;
import static com.redhat.devtools.intellij.telemetry.core.util.AnonymizeUtils.anonymizeResource;

public class TreeHelper {

    private static final Logger logger = LoggerFactory.getLogger(TreeHelper.class);

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

    /**
     * Get YAML and Tekton kind from Tekton tree node.
     *
     * @param node the Tekton tree node
     * @return Pair where 'first' is YAML content and 'second' is Tekton kind
     */
    public static Pair<String, String> getYAMLAndKindFromNode(ParentableNode<?> node) {
        Pair<String, String> yamlAndKind = null;
        try {
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
                content = tkncli.getClusterTriggerBindingYAML(node.getName());
                kind = KIND_CLUSTERTRIGGERBINDINGS;
            } else if (node instanceof EventListenerNode) {
                content = tkncli.getEventListenerYAML(namespace, node.getName());
                kind = KIND_EVENTLISTENERS;
            } else if (node instanceof TaskRunNode) {
                content = tkncli.getTaskRunYAML(namespace, node.getName());
                kind = KIND_TASKRUN;
            } else if (node instanceof PipelineRunNode) {
                content = tkncli.getPipelineRunYAML(namespace, node.getName());
                kind = KIND_PIPELINERUN;
            }
            yamlAndKind = Pair.create(content, kind);
        } catch (IOException e) {
            UIHelper.executeInUI(() -> Messages.showErrorDialog("Error: " + e.getLocalizedMessage(), "Error"));
        }
        return yamlAndKind;
    }

    public static void openTektonResourceInEditor(TreePath path) {
        if (path == null) {
            return;
        }

        Object node = path.getLastPathComponent();
        ParentableNode<? extends ParentableNode<?>> element = StructureTreeAction.getElement(node);
        Pair<String, String> yamlAndKind = getYAMLAndKindFromNode(element);
        if (yamlAndKind == null
                || yamlAndKind.getFirst().isEmpty()) {
            return;
        }
        Project project = element.getRoot().getProject();
        String namespace = element.getNamespace();
        String name = element.getName();
        String content = yamlAndKind.getFirst();
        String kind = yamlAndKind.getSecond();
        TelemetryMessageBuilder.ActionMessage telemetry = TelemetryService.instance().action(NAME_PREFIX_ACTION + ": open resource in editor")
                .property(TelemetryService.PROP_RESOURCE_KIND, yamlAndKind.second);
        try {
            VirtualFileHelper.openVirtualFileInEditor(project, namespace, name, content, kind, false);
            telemetry.send();
        } catch (IOException e) {
            String errorMessage = "Could not open resource in editor: " + e.getLocalizedMessage();
            telemetry
                    .error(anonymizeResource(name, namespace, errorMessage))
                    .send();
            logger.warn(errorMessage, e);
        }
    }

    public static String getPluralKind(String kind) {
        switch(kind.toLowerCase()) {
            case KIND_PIPELINE: {
                return KIND_PIPELINES;
            }
            case KIND_PIPELINERUN: {
                return KIND_PIPELINERUNS;
            }
            case KIND_TASK: {
                return KIND_TASKS;
            }
            case KIND_TASKRUN: {
                return KIND_TASKRUNS;
            }
            case KIND_CLUSTERTASK: {
                return KIND_CLUSTERTASKS;
            }
            case KIND_CONDITION: {
                return KIND_CONDITIONS;
            }
            case KIND_RESOURCE: {
                return KIND_RESOURCES;
            }
            case KIND_TRIGGERTEMPLATE: {
                return KIND_TRIGGERTEMPLATES;
            }
            case KIND_TRIGGERBINDING: {
                return KIND_TRIGGERBINDINGS;
            }
            case KIND_CLUSTERTRIGGERBINDING: {
                return KIND_CLUSTERTRIGGERBINDINGS;
            }
            case KIND_EVENTLISTENER: {
                return KIND_EVENTLISTENERS;
            }
            default: {
                return kind;
            }
        }
    }

    public static Map<Class, List<ParentableNode>> getResourcesByClass(ParentableNode[] elements) {
        Map<Class, List<ParentableNode>> resourcesByClass = new HashMap<>();
        Arrays.stream(elements).forEach(element ->
                resourcesByClass.computeIfAbsent(element.getClass(), value -> new ArrayList<>())
                        .add(element));
        return resourcesByClass;
    }
}
