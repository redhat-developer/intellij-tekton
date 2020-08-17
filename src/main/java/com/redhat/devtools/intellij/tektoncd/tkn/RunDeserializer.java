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

import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_TASKRUN;

public class RunDeserializer extends StdNodeBasedDeserializer<Run> {
    public RunDeserializer() {
        super(Run.class);
    }

    @Override
    public Run convert(JsonNode item, DeserializationContext ctxt) {
        String name = item.get("metadata").get("name").asText();
        // this is a temporary fix for a bug in tkn 0.9.0. TaskRunList doesn't mention the kind for its children
        String kind = item.has("kind") ? item.get("kind").asText() : KIND_TASKRUN;
        return createRun(item, name, "", kind);
    }

    private Run createRun(JsonNode item, String name, String triggeredBy, String kind) {
        Optional<Boolean> completed = isCompleted(item);
        Instant completionTime = getCompletionTime(item);
        Instant startTime = getStartTime(item);
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
            JsonNode conditionChecks = item.get("conditionChecks");
            JsonNode conditions = item.get("status").get("conditions");
            String failedReason = "";
            if (conditions != null && conditions.isArray() && conditions.size() > 0) {
                failedReason = conditions.get(0).get("reason").asText("");
            }

            return createTaskRun(name, triggeredBy, stepName, completed, startTime, completionTime, conditionChecks, failedReason);
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

    private Run createTaskRun(String name, String triggeredBy, String stepName, Optional<Boolean> completed, Instant startTime, Instant completionTime, JsonNode conditionChecksNode, String failedReason) {
        List<TaskRun> conditionChecks = new ArrayList<>();
        if (conditionChecksNode != null) {
            for (Iterator<Map.Entry<String,JsonNode>> it = conditionChecksNode.fields(); it.hasNext(); ) {
                Map.Entry<String,JsonNode> entry = it.next();
                String taskRunName = entry.getKey();
                JsonNode conditionCheckNode = entry.getValue();
                String conditionName = conditionCheckNode.get("conditionName").asText("");
                Optional<Boolean> isConditionCompleted = isCompleted(conditionCheckNode);
                Instant conditionCompletionTime = getCompletionTime(conditionCheckNode);
                Instant conditionStartTime = getStartTime(conditionCheckNode);
                conditionChecks.add(new TaskRun(taskRunName, "", conditionName, isConditionCompleted, conditionStartTime, conditionCompletionTime, new ArrayList<>(), ""));
            }
        }
        return new TaskRun(name, triggeredBy, stepName, completed, startTime, completionTime, conditionChecks, failedReason);
    }

    private Instant getCompletionTime(JsonNode item) {
        Instant completionTime = null;
        try {
            String completionTimeText = item.get("status").get("completionTime") == null ? null : item.get("status").get("completionTime").asText(null);
            if (completionTimeText != null) completionTime = Instant.parse(completionTimeText);
        } catch (NullPointerException ne) { }
        return completionTime;
    }

    private Instant getStartTime(JsonNode item) {
        Instant startTime = null;
        try {
            String startTimeText = item.get("status").get("startTime") == null ? null : item.get("status").get("startTime").asText(null);
            if (startTimeText != null) startTime = Instant.parse(startTimeText);
        } catch (NullPointerException ne) { }
        return startTime;
    }

    private Optional<Boolean> isCompleted(JsonNode item) {
        Optional<Boolean> completed = Optional.empty();
        try {
            JsonNode conditions = item.get("status").get("conditions");
            if (conditions.isArray() && conditions.size() > 0) {
                String status = conditions.get(0).get("status").asText();
                if (status.equalsIgnoreCase("true")) {
                    completed = Optional.of(true);
                } else if (status.equalsIgnoreCase("false")) {
                    completed = Optional.of(false);
                }
            }
        } catch (Exception e) {}
        return completed;
    }
}
