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
package com.redhat.devtools.intellij.tektoncd.tree;

import io.fabric8.knative.internal.pkg.apis.Condition;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.tekton.pipeline.v1beta1.PipelineRun;
import io.fabric8.tekton.pipeline.v1beta1.PipelineRunTaskRunStatus;
import io.fabric8.tekton.pipeline.v1beta1.TaskRun;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class PipelineRunNode extends RunNode<ParentableNode, PipelineRun> {
    public PipelineRunNode(TektonRootNode root, ParentableNode parent, PipelineRun run) {
        super(root, parent, run);
    }

    @Override
    public String getFailedReason() {
        PipelineRun run = getRun();
        List<Condition> conditionsList = run.getStatus() != null ? run.getStatus().getConditions() : Collections.emptyList();
        if (conditionsList.size() > 0) {
            return conditionsList.get(0).getReason();
        }
        return "";
    }

    @Override
    public Instant getStartTime() {
        PipelineRun run = getRun();
        Instant startTime = null;
        try {
            String startTimeText = run.getStatus() == null ? null : run.getStatus().getStartTime();
            if (startTimeText != null && !startTimeText.isEmpty()) {
                startTime = Instant.parse(startTimeText);
            }
        } catch (NullPointerException ignored) { }
        return startTime;
    }

    @Override
    public Optional<Boolean> isCompleted() {
        Optional<Boolean> completed = Optional.empty();
        PipelineRun run = getRun();
        try {
            List<Condition> conditionsList = run.getStatus() != null ? run.getStatus().getConditions() : Collections.emptyList();
            if (conditionsList.size() > 0) {
                completed = Optional.of(conditionsList.get(0).getStatus().equalsIgnoreCase("true"));
            }
        } catch (Exception e) {}
        return completed;
    }

    @Override
    public Instant getCompletionTime() {
        PipelineRun run = getRun();
        Instant completionTime = null;
        try {
            String completionTimeText = run.getStatus() == null ? null : run.getStatus().getCompletionTime();
            if (completionTimeText != null && !completionTimeText.isEmpty()) completionTime = Instant.parse(completionTimeText);
        } catch (NullPointerException ne) { }
        return completionTime;
    }

    public List<TaskRun> getPipelineRunTaskRunAsTaskRun() {
        List<TaskRun> taskRuns = new ArrayList<>();
        PipelineRun run = getRun();
        Map<String, PipelineRunTaskRunStatus> pipelineRunTaskRunStatusMap = run.getStatus() != null ? run.getStatus().getTaskRuns() : Collections.emptyMap();
        for (PipelineRunTaskRunStatus pipelineRunTaskRunStatus: pipelineRunTaskRunStatusMap.values()) {
            TaskRun taskRun = new TaskRun();
            taskRun.setStatus(pipelineRunTaskRunStatus.getStatus());
            ObjectMeta taskRunMetadata = new ObjectMeta();
            taskRunMetadata.setName(pipelineRunTaskRunStatus.getPipelineTaskName());
            Map<String, String> labels = new HashMap<>();
            labels.put("tekton.dev/pipeline", run.getMetadata().getLabels().get("tekton.dev/pipeline"));
            labels.put("tekton.dev/pipelineRun", run.getMetadata().getName());
            labels.put("tekton.dev/pipelineTask", pipelineRunTaskRunStatus.getPipelineTaskName());
            taskRunMetadata.setLabels(labels);
            taskRun.setMetadata(taskRunMetadata);
            taskRuns.add(taskRun);
        }
        return taskRuns;
    }
}
