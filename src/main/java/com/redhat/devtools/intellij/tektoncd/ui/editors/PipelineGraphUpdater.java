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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PipelineGraphUpdater implements GraphUpdater {
    private final ObjectMapper MAPPER  = new ObjectMapper(new YAMLFactory());

    @Override
    public void update(String content, mxGraph graph) {
        try {
            Pipeline pipeline = MAPPER.readValue(content, Pipeline.class);
            List<PipelineTask> tasks = pipeline.getSpec().getTasks();
            if (tasks != null) {
                Map<String, List<String>> relations = new HashMap<>();
                Map<String, Object> nodes = new HashMap<>();
                Map<String, PipelineTaskCondition> conditions = new HashMap<>();
                for (PipelineTask task : tasks) {
                    if (task.getName() != null) {
                        String name = task.getName();
                        String taskId = "Task:" + name;
                        nodes.put(taskId, graph.insertVertex(null, taskId, name, 0, 200, WIDTH, HEIGHT));
                        if (task.getRunAfter() != null) {
                            for (String parentName : task.getRunAfter()) {
                                String parentTaskId = "Task:" + parentName;
                                relations.computeIfAbsent(parentTaskId, k -> new ArrayList<>());
                                relations.get(parentTaskId).add(taskId);
                            }
                        }
                        if (task.getConditions() != null) {
                            for(PipelineTaskCondition condition : task.getConditions()) {
                                String conditionId = "Condition:" + condition.getConditionRef();
                                conditions.put(conditionId, condition);
                                relations.computeIfAbsent(conditionId, k -> new ArrayList<>());
                                relations.get(conditionId).add(taskId);
                            }
                        }
                    }
                }
                for(Map.Entry<String, PipelineTaskCondition> entry : conditions.entrySet()) {
                    nodes.put(entry.getKey(), graph.insertVertex(null, entry.getKey(), entry.getValue().getConditionRef(), 0, 200, WIDTH, HEIGHT, "shape=rhombus;"));
                }
                for (Map.Entry<String, List<String>> entry : relations.entrySet()) {
                    if (nodes.containsKey(entry.getKey())) {
                        for (String target : entry.getValue()) {
                            graph.insertEdge(null, entry.getKey() + "->" + target, "", nodes.get(entry.getKey()), nodes.get(target));
                        }
                    }
                }
            }

        } catch (IOException e) {}
    }
}
