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
package com.redhat.devtools.intellij.tektoncd.actions;

import com.intellij.openapi.util.Pair;
import com.redhat.devtools.intellij.common.utils.YAMLHelper;
import com.redhat.devtools.intellij.tektoncd.telemetry.TelemetryService;
import com.redhat.devtools.intellij.tektoncd.tkn.Resource;
import com.redhat.devtools.intellij.tektoncd.tkn.Tkn;
import com.redhat.devtools.intellij.tektoncd.tree.ParentableNode;
import com.redhat.devtools.intellij.tektoncd.tree.PipelineRunNode;
import com.redhat.devtools.intellij.tektoncd.tree.TaskRunNode;
import com.redhat.devtools.intellij.tektoncd.utils.model.actions.StartResourceModel;
import io.fabric8.kubernetes.api.model.HasMetadata;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.redhat.devtools.intellij.tektoncd.telemetry.TelemetryService.NAME_PREFIX_START_STOP;
import static com.redhat.devtools.intellij.telemetry.core.service.TelemetryMessageBuilder.ActionMessage;

public class MirrorStartAction extends StartAction {

    public MirrorStartAction(Class... filters) { super(filters); }

    public MirrorStartAction() {
        super(PipelineRunNode.class, TaskRunNode.class);
    }

    @Override
    protected StartResourceModel createModel(ParentableNode element, String namespace, Tkn tkncli, List<Resource> resources, List<String> serviceAccounts, List<String> secrets, List<String> configMaps, List<String> persistentVolumeClaims) throws IOException {
        String configuration = "", runConfiguration = "";
        List<? extends HasMetadata> runs = new ArrayList<>();
        if (element instanceof PipelineRunNode) {
            runConfiguration = tkncli.getPipelineRunYAML(namespace, element.getName());
            String pipeline = YAMLHelper.getStringValueFromYAML(runConfiguration, new String[] {"metadata", "labels", "tekton.dev/pipeline"});
            configuration = tkncli.getPipelineYAML(namespace, pipeline);
            runs = tkncli.getPipelineRuns(namespace, pipeline);
        } else if (element instanceof TaskRunNode) {
            runConfiguration = tkncli.getTaskRunYAML(namespace, element.getName());
            Pair<String, String> taskNameAndConfiguration = getTaskConfiguration(namespace, tkncli, runConfiguration);
            if (taskNameAndConfiguration == null) {
                throw new IOException("Error: Unable to retrieve task from this taskrun");
            }
            configuration = taskNameAndConfiguration.getSecond();
            runs = tkncli.getTaskRuns(namespace, taskNameAndConfiguration.getFirst());
        }
        StartResourceModel model = new StartResourceModel(configuration, resources, serviceAccounts, secrets, configMaps, persistentVolumeClaims, runs);
        model.adaptsToRun(runConfiguration);
        return model;
    }

    private Pair<String, String> getTaskConfiguration(String namespace, Tkn tkncli, String taskRunYAML) throws IOException {
        String task = YAMLHelper.getStringValueFromYAML(taskRunYAML, new String[] {"metadata", "labels", "tekton.dev/task"});
        String clusterTask = YAMLHelper.getStringValueFromYAML(taskRunYAML, new String[] {"metadata", "labels", "tekton.dev/clusterTask"});
        if (task != null && !task.isEmpty()) {
            String configuration = tkncli.getTaskYAML(namespace, task);
            return new Pair<>(task, configuration);
        } else if (clusterTask != null && !clusterTask.isEmpty()) {
            String configuration = tkncli.getClusterTaskYAML(clusterTask);
            return new Pair<>(clusterTask, configuration);
        }
        return null;
    }

    @Override
    protected ActionMessage createTelemetry() {
        return TelemetryService.instance().action(NAME_PREFIX_START_STOP + "mirror start");
    }

}
