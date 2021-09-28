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

public class TaskRunNode extends RunNode<ParentableNode, TaskRun> {
    public TaskRunNode(TektonRootNode root, ParentableNode parent, TaskRun run) {
        super(root, parent, run);
    }

    public String getDisplayName() {
        TaskRun run = getRun();
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
        TaskRun run = getRun();
        List<Condition> conditionsList = run.getStatus() != null ? run.getStatus().getConditions() : Collections.emptyList();
        return getFailedReason(conditionsList);
    }

    @Override
    public Instant getStartTime() {
        TaskRun run = getRun();
        return getStartTime(run);
    }

    public static Instant getStartTime(TaskRun run) {
        String startTimeText = run.getStatus() == null ? null : run.getStatus().getStartTime();
        return getStartTime(startTimeText);
    }

    @Override
    public Optional<Boolean> isCompleted() {
        TaskRun run = getRun();
        List<Condition> conditionsList = run.getStatus() != null ? run.getStatus().getConditions() : Collections.emptyList();
        return isCompleted(conditionsList);
    }

    @Override
    public Instant getCompletionTime() {
        TaskRun run = getRun();
        String completionTimeText = run.getStatus() == null ? null : run.getStatus().getCompletionTime();
        return getCompletionTime(completionTimeText);
    }

    public boolean isStartedOnDebug() {
        TaskRun run = getRun();
        return run.getSpec() != null && (run.getSpec().getAdditionalProperties() != null && run.getSpec().getAdditionalProperties().get("debug") != null);
    }

    public List<TaskRun> getConditionsAsTaskRunChildren(PipelineRun pipelineRunParent) {
        List<TaskRun> conditions = new ArrayList<>();
        TaskRun run = getRun();
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
