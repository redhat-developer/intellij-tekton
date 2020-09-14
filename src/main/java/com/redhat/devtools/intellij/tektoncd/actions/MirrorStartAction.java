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

import com.redhat.devtools.intellij.common.utils.YAMLHelper;
import com.redhat.devtools.intellij.tektoncd.tkn.Resource;
import com.redhat.devtools.intellij.tektoncd.tkn.Run;
import com.redhat.devtools.intellij.tektoncd.tkn.Tkn;
import com.redhat.devtools.intellij.tektoncd.tree.ParentableNode;
import com.redhat.devtools.intellij.tektoncd.tree.PipelineRunNode;
import com.redhat.devtools.intellij.tektoncd.tree.TaskRunNode;
import com.redhat.devtools.intellij.tektoncd.utils.StartResourceModel;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MirrorStartAction extends StartAction {

    public MirrorStartAction() { super(PipelineRunNode.class, TaskRunNode.class); }

    @Override
    protected StartResourceModel getModel(ParentableNode element, String namespace, Tkn tkncli, List<Resource> resources, List<String> serviceAccounts, List<String> secrets, List<String> configMaps, List<String> persistentVolumeClaims) throws IOException {
        String configuration = "", runConfiguration = "";
        List<? extends Run> runs = new ArrayList<>();
        if (element instanceof PipelineRunNode) {
            runConfiguration = tkncli.getPipelineRunYAML(namespace, element.getName());
            String pipeline = YAMLHelper.getStringValueFromYAML(runConfiguration, new String[] {"metadata", "labels", "tekton.dev/pipeline"});
            configuration = tkncli.getPipelineYAML(namespace, pipeline);
            runs = tkncli.getPipelineRuns(namespace, pipeline);
        } else if (element instanceof TaskRunNode) {
            runConfiguration = tkncli.getTaskRunYAML(namespace, element.getName());
            String task = YAMLHelper.getStringValueFromYAML(runConfiguration, new String[] {"metadata", "labels", "tekton.dev/task"});
            configuration = tkncli.getTaskYAML(namespace, task);
            runs = tkncli.getTaskRuns(namespace, task);
        }

        StartResourceModel model = new StartResourceModel(configuration, resources, serviceAccounts, secrets, configMaps, persistentVolumeClaims, runs);
        model.adaptsToRun(runConfiguration);
        return model;
    }
}
