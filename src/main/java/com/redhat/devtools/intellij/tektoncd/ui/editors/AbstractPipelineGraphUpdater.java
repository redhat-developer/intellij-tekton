/*******************************************************************************
 * Copyright (c) 2020 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package com.redhat.devtools.intellij.tektoncd.ui.editors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.mxgraph.view.mxGraph;
import io.fabric8.tekton.pipeline.v1beta1.PipelineSpec;
import io.fabric8.tekton.pipeline.v1beta1.PipelineTask;
import io.fabric8.tekton.pipeline.v1beta1.PipelineTaskCondition;
import io.fabric8.tekton.pipeline.v1beta1.PipelineTaskInputResource;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

public abstract class AbstractPipelineGraphUpdater<T> implements GraphUpdater<T> {
    protected enum Type {
        TASK,
        CONDITION;
    }

    protected static class Node {
        Type type;
        String id;
        String name;
        Collection<Node> childs = new ArrayList();

        public Node(Type type, String id, String name) {
            this.type = type;
            this.id = id;
            this.name = name;
        }
    }
    public static final String TASK_PREFIX = "Task:";
    public static final String CONDITION_PREFIX = "Condition:";
    public static final String FINALLY_PREFIX= "Finally:";
    protected final ObjectMapper MAPPER  = new ObjectMapper(new YAMLFactory());

    protected void update(T content, PipelineSpec pipelineSpec, mxGraph graph) {
        List<PipelineTask> tasks = pipelineSpec!=null?pipelineSpec.getTasks():null;
        if (tasks != null && !tasks.isEmpty()) {
            List<PipelineTask> finallyTasks = pipelineSpec.getFinally();
            Collection<Node> finallyNodes = Collections.emptyList();
            if (finallyTasks != null && !finallyTasks.isEmpty()) {
                finallyNodes = generateTree(finallyTasks, FINALLY_PREFIX).values();
            }
            Map<String, Node> tree = generateTree(tasks, "");
            Node finallyNode = new Node(Type.CONDITION, "__finally__", "finally");
            finallyNode.childs = finallyNodes;
            if (!finallyNodes.isEmpty()) {
                tree.values().stream().flatMap(node -> Stream.concat(node.childs.stream(), Stream.of(node))).forEach(node -> {
                    if (node.childs.isEmpty()) {
                        node.childs = Collections.singletonList(finallyNode);
                    }
                });
            }
            generateGraph(content, graph, tree.values());
        }
    }

    private void generateGraph(T content, mxGraph graph, Collection<Node> nodes) {
        Map<String, Object> context = new HashMap<>();
        generateGraph(content, graph, nodes, context);
    }

    private void generateGraph(T content, mxGraph graph, Collection<Node> nodes, Map<String, Object> context) {
        for (Node node : nodes) {
            Object vertex = createVertex(content, graph, node, context);
            generateGraph(content, graph, node.childs, context);
            for (Node child : node.childs) {
                if (!context.containsKey(node.id + "->" + child.id)) {
                    context.put(node.id + "->" + child.id, graph.insertEdge(null, node.id + "->" + child.id,
                            "", vertex, createVertex(content, graph, child, context)));
                }
            }
        }
    }

    protected String getNodeStyle(T content, Node node) {
        switch (node.type) {
            case TASK:
                return "editable=false;";
            case CONDITION:
                return "shape=rhombus;editable=false;";
        }
        return null;
    }

    protected String getNodeLabel(T content, Node node) {
        return node.name;
    }

    private Object createVertex(T content, mxGraph graph, Node node, Map<String, Object> vertexes) {
        Object vertex = vertexes.get(node.id);
        if (vertex == null) {
            vertex = graph.insertVertex(null, node.id, getNodeLabel(content, node), 0, 200, WIDTH, HEIGHT, getNodeStyle(content, node));
            vertexes.put(node.id, vertex);
        }
        return vertex;
    }

    static Map<String,Node> generateTree(List<PipelineTask> tasks, String idPrefix) {
        Map<String,Node> tree = new HashMap<>();
        Map<String, List<String>> relations = new HashMap<>();
        for (PipelineTask task : tasks) {
            if (task != null && StringUtils.isNotBlank(task.getName())) {
                String name = task.getName();
                String taskId = idPrefix + TASK_PREFIX + name;
                Node taskNode = new Node(Type.TASK, taskId, name);
                tree.put(taskId, taskNode);
                if (task.getRunAfter() != null) {
                    for (String parentName : task.getRunAfter()) {
                        String parentTaskId = idPrefix + TASK_PREFIX + parentName;
                        relations.computeIfAbsent(parentTaskId, k -> new ArrayList<>());
                        relations.get(parentTaskId).add(taskId);
                    }
                }
                if (task.getConditions() != null) {
                    for(PipelineTaskCondition condition : task.getConditions()) {
                        if (condition != null && StringUtils.isNotBlank(condition.getConditionRef())) {
                            String conditionId = idPrefix + CONDITION_PREFIX + condition.getConditionRef();
                            Node conditionNode = new Node(Type.CONDITION, conditionId,condition.getConditionRef());
                            conditionNode.childs.add(taskNode);
                            tree.put(conditionId, conditionNode);
                            for(PipelineTaskInputResource resource : condition.getResources()) {
                                if (resource != null && resource.getFrom() != null) {
                                    for(String parentName : resource.getFrom()) {
                                        String parentTaskId = TASK_PREFIX + parentName;
                                        relations.computeIfAbsent(parentTaskId, k -> new ArrayList<>());
                                        relations.get(parentTaskId).add(conditionId);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        Set<String> toRemove = new HashSet<>();
        for (Map.Entry<String, List<String>> entry : relations.entrySet()) {
            if (tree.containsKey(entry.getKey())) {
                for (String target : entry.getValue()) {
                    Node source = tree.get(entry.getKey());
                    source.childs.add(tree.get(target));
                    toRemove.add(target);
                }
            }
        }
        toRemove.forEach(id -> tree.remove(id));
        return tree;
    }
}
