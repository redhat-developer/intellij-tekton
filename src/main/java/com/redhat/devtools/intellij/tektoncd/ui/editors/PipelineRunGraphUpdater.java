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

import com.mxgraph.view.mxGraph;
import com.redhat.devtools.intellij.common.utils.DateHelper;
import io.fabric8.tekton.pipeline.v1beta1.PipelineRun;
import io.fabric8.tekton.pipeline.v1beta1.PipelineRunTaskRunStatus;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.time.Instant;
import java.util.Optional;

public class PipelineRunGraphUpdater extends AbstractPipelineGraphUpdater<PipelineRun> {

    @Override
    public PipelineRun adapt(String content) {
        try {
            return MAPPER.readValue(content, PipelineRun.class);
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public void update(PipelineRun content, mxGraph graph) {
        if (content.getStatus() != null) {
            update(content, content.getStatus().getPipelineSpec(), graph);
        }
    }

    @Override
    protected String getNodeStyle(PipelineRun content, Node node) {
        String style = super.getNodeStyle(content, node);
        if (node.type == Type.TASK) {
            boolean succeeded = isTaskSucceeded(content, node.name);
            if (succeeded) {
                style += "fillColor=green;";
            } else {
                style += "fillColor=red;";
            }
        }
        return style;
    }

    @Override
    protected String getNodeLabel(PipelineRun content, Node node) {
        String label = super.getNodeLabel(content, node);
        if (node.type == Type.TASK) {
            Optional<PipelineRunTaskRunStatus> taskStatus = getTaskStatus(content, node.name);
            if (taskStatus.isPresent() && taskStatus.get().getStatus() != null &&
                    StringUtils.isNotBlank(taskStatus.get().getStatus().getStartTime()) &&
                    StringUtils.isNotBlank(taskStatus.get().getStatus().getCompletionTime())) {
                label += "\n" + DateHelper.humanizeDate(Instant.parse(taskStatus.get().getStatus().getStartTime()),
                        Instant.parse(taskStatus.get().getStatus().getCompletionTime()));
            }
        }
        return label;
    }

    private boolean isTaskSucceeded(PipelineRun content, String name) {
        boolean succeeded = false;
        Optional<PipelineRunTaskRunStatus> taskStatus = getTaskStatus(content, name);
        if (taskStatus.isPresent()) {
            succeeded = taskStatus.get().getStatus().getConditions().stream().filter(condition -> "True".equals(condition.getStatus())).findFirst().isPresent();
        }
        return succeeded;
    }

    @NotNull
    private Optional<PipelineRunTaskRunStatus> getTaskStatus(PipelineRun content, String name) {
        return content.getStatus().getTaskRuns().values().stream().filter(prs -> name.equals(prs.getPipelineTaskName())).findFirst();
    }
}
