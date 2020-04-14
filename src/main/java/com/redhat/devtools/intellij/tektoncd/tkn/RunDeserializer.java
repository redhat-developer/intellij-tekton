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
package com.redhat.devtools.intellij.tektoncd.tkn;

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdNodeBasedDeserializer;
import com.fasterxml.jackson.databind.type.TypeFactory;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_TASKRUN;

public class RunDeserializer extends StdNodeBasedDeserializer<List<Run>> {
    public RunDeserializer() {
        super(TypeFactory.defaultInstance().constructCollectionType(List.class, Run.class));
    }

    @Override
    public List<Run> convert(JsonNode root, DeserializationContext ctxt) {
        List<Run> result = new ArrayList<>();
        JsonNode items = root.get("items");
        if (items != null) {
            for (Iterator<JsonNode> it = items.iterator(); it.hasNext(); ) {
                JsonNode item = it.next();
                String name = item.get("metadata").get("name").asText();
                String kind = item.get("kind").asText();
                result.add(createRun(item, name, "", kind));
            }
        }
        return result;
    }

    private Run createRun(JsonNode item, String name, String triggeredBy, String kind) {
        Optional<Boolean> completed = Optional.empty();
        JsonNode conditions = item.get("status").get("conditions");
        Instant completionTime = null;
        Instant startTime = null;
        String completionTimeText = item.get("status").get("completionTime") == null ? null : item.get("status").get("completionTime").asText(null);
        if (completionTimeText != null) completionTime = Instant.parse(completionTimeText);
        String startTimeText = item.get("status").get("startTime").asText();
        if (startTimeText != null) startTime = Instant.parse(startTimeText);
        if (conditions.isArray() && conditions.size() > 0) {
            String status = conditions.get(0).get("status").asText();
            if (status.equalsIgnoreCase("true")) {
                completed = Optional.of(true);
            } else if (status.equalsIgnoreCase("false")) {
                completed = Optional.of(false);
            }
        }
        if (kind.equalsIgnoreCase(KIND_TASKRUN)) {
            String stepName = "";
            JsonNode pipelineTaskName = item.get("pipelineTaskName");
            if (pipelineTaskName == null) {
                JsonNode labels = item.get("metadata") != null ? item.get("metadata").get("labels") : null;
                if (labels != null) {
                    stepName = labels.get("tekton.dev/pipelineTask") != null ? labels.get("tekton.dev/pipelineTask").asText() : "";
                    triggeredBy = labels.get("tekton.dev/pipeline") != null ? labels.get("tekton.dev/pipeline").asText() : "";
                }
            } else {
                stepName = pipelineTaskName.asText();
            }
            return createTaskRun(name, triggeredBy, stepName, completed, startTime, completionTime);
        } else {
            JsonNode taskRunsNode = item.get("status").get("taskRuns");
            return createPipelineRun(name, completed, startTime, completionTime, taskRunsNode);
        }
    }

    private Run createPipelineRun(String name, Optional<Boolean> completed, Instant startTime, Instant completionTime, JsonNode tasksRunNode) {
        List<TaskRun> tasksRun = new ArrayList<>();
        if (tasksRunNode != null) {
            for (Iterator<Map.Entry<String, JsonNode>> it = tasksRunNode.fields(); it.hasNext(); ) {
                Map.Entry<String, JsonNode> entry = it.next();
                tasksRun.add((TaskRun) createRun(entry.getValue(), entry.getKey(), name, KIND_TASKRUN));
            }
        }
        return new PipelineRun(name, completed, startTime, completionTime, tasksRun);
    }

    private Run createTaskRun(String name, String triggeredBy, String stepName, Optional<Boolean> completed, Instant startTime, Instant completionTime) {
        return new TaskRun(name, triggeredBy, stepName, completed, startTime, completionTime);
    }
}
