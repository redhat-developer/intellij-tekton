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

import com.redhat.devtools.intellij.common.utils.StringHelper;
import io.fabric8.knative.internal.pkg.apis.Condition;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.tekton.pipeline.v1beta1.PipelineRun;
import io.fabric8.tekton.pipeline.v1beta1.PipelineRunConditionCheckStatus;
import io.fabric8.tekton.pipeline.v1beta1.PipelineRunTaskRunStatus;
import io.fabric8.tekton.pipeline.v1beta1.TaskRun;
import io.fabric8.tekton.pipeline.v1beta1.TaskRunStatus;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class TaskRunNode extends RunNode {
    public TaskRunNode(TektonRootNode root, ParentableNode parent, TaskRun run) {
        super(root, parent, run);
    }

    public String getDisplayName() {
        TaskRun run = (TaskRun)getRun();
        String displayName = "";
        String triggeredBy = run.getMetadata().getLabels().getOrDefault("tekton.dev/pipeline", "");
        String stepName = run.getMetadata().getLabels().getOrDefault("tekton.dev/pipelineTask", "");
        if (!triggeredBy.isEmpty()) {
            displayName += StringHelper.beautify(triggeredBy) + "/";
        }
        displayName +=  StringHelper.beautify(stepName);
        if (displayName.isEmpty()) {
            displayName = run.getMetadata().getName();
        }
        return displayName;
    }

    @Override
    public String getFailedReason() {
        TaskRun run = (TaskRun)getRun();
        List<Condition> conditionsList = run.getStatus() != null ? run.getStatus().getConditions() : Collections.emptyList();
        if (conditionsList.size() > 0) {
            return conditionsList.get(0).getReason();
        }
        return "";
    }

    @Override
    public Instant getStartTime() {
        TaskRun run = (TaskRun)getRun();
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
        TaskRun run = (TaskRun)getRun();
        try {
            List<Condition> conditionsList = run.getStatus() != null ? run.getStatus().getConditions() : Collections.emptyList();
            if (conditionsList.size() > 0) {
                if (conditionsList.get(0).getStatus().equalsIgnoreCase("True")) {
                    completed = Optional.of(true);
                } else if (conditionsList.get(0).getStatus().equalsIgnoreCase("False")) {
                    completed = Optional.of(false);
                }
            }
        } catch (Exception e) {}
        return completed;
    }

    @Override
    public Instant getCompletionTime() {
        TaskRun run = (TaskRun)getRun();
        Instant completionTime = null;
        try {
            String completionTimeText = run.getStatus() == null ? null : run.getStatus().getCompletionTime();
            if (completionTimeText != null && !completionTimeText.isEmpty()) completionTime = Instant.parse(completionTimeText);
        } catch (NullPointerException ne) { }
        return completionTime;
    }

    public boolean isStartedOnDebug() {
        TaskRun run = (TaskRun)getRun();
        return run.getSpec() != null && (run.getSpec().getAdditionalProperties() != null && run.getSpec().getAdditionalProperties().get("debug") != null);
    }

    public List<TaskRun> getConditionsAsTaskRunChildren(PipelineRun pipelineRunParent) {
        List<TaskRun> conditions = new ArrayList<>();
        TaskRun run = (TaskRun)getRun();
        PipelineRunTaskRunStatus pipelineRunTaskRunStatus = pipelineRunParent.getStatus().getTaskRuns().get(run.getMetadata().getName());
        if (pipelineRunTaskRunStatus != null && !pipelineRunTaskRunStatus.getConditionChecks().isEmpty()) {
            for (PipelineRunConditionCheckStatus pipelineRunConditionCheckStatus: pipelineRunTaskRunStatus.getConditionChecks().values()) {
                pipelineRunConditionCheckStatus.getConditionName();
                TaskRun taskRun = new TaskRun();
                TaskRunStatus taskRunStatus = new TaskRunStatus();
                taskRunStatus.setCompletionTime(pipelineRunConditionCheckStatus.getStatus().getCompletionTime());
                taskRunStatus.setConditions(pipelineRunConditionCheckStatus.getStatus().getConditions());
                taskRunStatus.setStartTime(pipelineRunConditionCheckStatus.getStatus().getStartTime());
                ObjectMeta taskRunMetadata = new ObjectMeta();
                taskRunMetadata.setName(pipelineRunConditionCheckStatus.getConditionName());
                Map<String, String> labels = new HashMap<>();
                labels.put("tekton.dev/pipeline", pipelineRunParent.getMetadata().getLabels().get("tekton.dev/pipeline"));
                labels.put("tekton.dev/pipelineRun", pipelineRunParent.getMetadata().getName());
                taskRunMetadata.setLabels(labels);
                taskRun.setMetadata(taskRunMetadata);
                taskRun.setStatus(taskRunStatus);
                conditions.add(taskRun);
            }
        }
        return conditions;
    }
}
