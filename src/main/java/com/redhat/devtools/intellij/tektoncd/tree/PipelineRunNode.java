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
import io.fabric8.tekton.pipeline.v1beta1.PipelineRun;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
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
        return getStartTime(run);
    }

    public static Instant getStartTime(PipelineRun run) {
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

}
