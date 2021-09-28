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
        return getFailedReason(conditionsList);
    }

    @Override
    public Instant getStartTime() {
        PipelineRun run = getRun();
        String startTimeText = run.getStatus() == null ? null : run.getStatus().getStartTime();
        return getStartTime(startTimeText);
    }

    @Override
    public Optional<Boolean> isCompleted() {
        PipelineRun run = getRun();
        List<Condition> conditionsList = run.getStatus() != null ? run.getStatus().getConditions() : Collections.emptyList();
        return isCompleted(conditionsList);
    }

    @Override
    public Instant getCompletionTime() {
        PipelineRun run = getRun();
        String completionTimeText = run.getStatus() == null ? null : run.getStatus().getCompletionTime();
        return getCompletionTime(completionTimeText);
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
