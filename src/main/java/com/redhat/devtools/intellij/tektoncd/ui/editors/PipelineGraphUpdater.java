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
import io.fabric8.tekton.pipeline.v1beta1.Pipeline;
import io.fabric8.tekton.pipeline.v1beta1.PipelineTask;
import io.fabric8.tekton.pipeline.v1beta1.PipelineTaskCondition;
import io.fabric8.tekton.pipeline.v1beta1.PipelineTaskInputResource;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

public class PipelineGraphUpdater implements GraphUpdater<Pipeline> {
    enum Type {
        TASK,
        CONDITION;
    }

    static class Node {
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
    private final ObjectMapper MAPPER  = new ObjectMapper(new YAMLFactory());

    @Override
    public Pipeline adapt(String content) {
        try {
            return MAPPER.readValue(content, Pipeline.class);
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public void update(Pipeline content, mxGraph graph) {
        List<PipelineTask> tasks = content.getSpec().getTasks();
        if (tasks != null && !tasks.isEmpty()) {
            List<PipelineTask> finallyTasks = content.getSpec().getFinally();
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
            generateGraph(graph, tree.values());
        }
    }

    private void generateGraph(mxGraph graph, Collection<Node> nodes) {
        Map<String, Object> context = new HashMap<>();
        generateGraph(graph, nodes, context);
    }

    private void generateGraph(mxGraph graph, Collection<Node> nodes, Map<String,Object> context) {
        for (Node node : nodes) {
            Object vertex = createVertex(graph, node, context);
            generateGraph(graph, node.childs, context);
            for (Node child : node.childs) {
                if (!context.containsKey(node.id + "->" + child.id)) {
                    context.put(node.id + "->" + child.id, graph.insertEdge(null, node.id + "->" + child.id, "", vertex, createVertex(graph, child, context)));
                }
            }
        }
    }

    private Object createVertex(mxGraph graph, Node node, Map<String, Object> vertexes) {
        Object vertex = vertexes.get(node.id);
        if (vertex == null) {
            switch (node.type) {
                case TASK:
                    vertex = graph.insertVertex(null, node.id, node.name, 0, 200, WIDTH, HEIGHT, "editable=false;");
                    break;
                case CONDITION:
                    vertex = graph.insertVertex(null, node.id, node.name, 0, 200, WIDTH, HEIGHT, "shape=rhombus;");
                    break;
            }
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
                        String conditionId = idPrefix + CONDITION_PREFIX + condition.getConditionRef();
                        Node conditionNode = new Node(Type.CONDITION, conditionId,condition.getConditionRef());
                        conditionNode.childs.add(taskNode);
                        tree.put(conditionId, conditionNode);
                        for(PipelineTaskInputResource resource : condition.getResources()) {
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
