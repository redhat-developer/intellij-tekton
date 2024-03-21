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

import com.intellij.openapi.util.text.StringUtil;
import com.mxgraph.view.mxGraph;
import com.redhat.devtools.intellij.common.utils.DateHelper;
import com.redhat.devtools.intellij.tektoncd.tkn.TknCliFactory;
import io.fabric8.tekton.pipeline.v1beta1.Pipeline;
import io.fabric8.tekton.pipeline.v1beta1.PipelineRun;
import io.fabric8.tekton.pipeline.v1beta1.PipelineRunTaskRunStatus;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URL;
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
        if (content.getStatus() != null && content.getStatus().getPipelineSpec() != null) {
            update(content, content.getStatus().getPipelineSpec(), graph);
        } else if (content.getSpec() != null && content.getSpec().getPipelineRef() != null && !StringUtil.isEmptyOrSpaces(content.getSpec().getPipelineRef().getName())) {
            TknCliFactory.getInstance().getTkn(null).thenAcceptAsync(tkn -> {
                try {
                    String pipelineYAML = tkn.getPipelineYAML(content.getMetadata().getNamespace(), content.getSpec().getPipelineRef().getName());
                    Pipeline pipeline = MAPPER.readValue(pipelineYAML, Pipeline.class);
                    update(content, pipeline.getSpec(), graph);
                } catch (IOException e) {
                }
            });
        }
    }

    private static final URL SUCCESS_ICON = PipelineRunGraphUpdater.class.getResource("/images/success.png");

    private static final URL FAILED_ICON = PipelineRunGraphUpdater.class.getResource("/images/failed.png");

    @Override
    protected String getNodeLabel(PipelineRun content, Node node) {
        String label = super.getNodeLabel(content, node);
        if (node.type == Type.TASK) {
            Optional<PipelineRunTaskRunStatus> taskStatus = getTaskStatus(content, node.name);
            if (taskStatus.isPresent() && taskStatus.get().getStatus() != null &&
                    !StringUtil.isEmptyOrSpaces(taskStatus.get().getStatus().getStartTime()) &&
                    !StringUtil.isEmptyOrSpaces(taskStatus.get().getStatus().getCompletionTime())) {
                label += "\n" + DateHelper.humanizeDate(Instant.parse(taskStatus.get().getStatus().getStartTime()),
                        Instant.parse(taskStatus.get().getStatus().getCompletionTime()));
            }
            label = "<table><tr><td align=\"center\"><img src=\"" + (isTaskSucceeded(content, node.name)?SUCCESS_ICON.toString():FAILED_ICON.toString()) + "\"/></td><td>" + label + "</td></tr></table>";
        }
        return label;
    }

    private boolean isTaskSucceeded(PipelineRun content, String name) {
        boolean succeeded = false;
        Optional<PipelineRunTaskRunStatus> taskStatus = getTaskStatus(content, name);
        if (taskStatus.isPresent() && taskStatus.get().getStatus() != null && taskStatus.get().getStatus().getConditions() != null) {
            succeeded = taskStatus.get().getStatus().getConditions().stream().filter(condition -> "True".equals(condition.getStatus())).findFirst().isPresent();
        }
        return succeeded;
    }

    @NotNull
    private Optional<PipelineRunTaskRunStatus> getTaskStatus(PipelineRun content, String name) {
        return content.getStatus() != null && content.getStatus().getTaskRuns() != null ? content.getStatus().getTaskRuns().values().stream().filter(prs -> name.equals(prs.getPipelineTaskName())).findFirst() : Optional.empty();
    }
}
