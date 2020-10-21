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

import com.intellij.openapi.vfs.VirtualFileManager;
import com.redhat.devtools.intellij.common.utils.YAMLHelper;
import com.redhat.devtools.intellij.tektoncd.tkn.Resource;
import com.redhat.devtools.intellij.tektoncd.tkn.Run;
import com.redhat.devtools.intellij.tektoncd.tkn.Tkn;
import com.redhat.devtools.intellij.tektoncd.tree.ParentableNode;
import com.redhat.devtools.intellij.tektoncd.tree.PipelineRunNode;
import com.redhat.devtools.intellij.tektoncd.tree.TaskRunNode;
import com.redhat.devtools.intellij.tektoncd.utils.TektonVirtualFile;
import com.redhat.devtools.intellij.tektoncd.utils.TreeHelper;
import com.redhat.devtools.intellij.tektoncd.utils.model.actions.StartResourceModel;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MirrorStartAction extends StartAction {

    public MirrorStartAction() { super(PipelineRunNode.class, TaskRunNode.class); }

    @Override
    protected StartResourceModel getModel(ParentableNode element, String namespace, Tkn tkncli, List<Resource> resources, List<String> serviceAccounts, List<String> secrets, List<String> configMaps, List<String> persistentVolumeClaims) throws IOException {
        String configuration = "", runConfiguration = "";
        List<? extends Run> runs = new ArrayList<>();
        String urlRun = TreeHelper.getTektonResourcePath(element, true);
        runConfiguration = ((TektonVirtualFile) VirtualFileManager.getInstance().findFileByUrl(urlRun)).getContent().toString();
        if (element instanceof PipelineRunNode) {
            String pipeline = YAMLHelper.getStringValueFromYAML(runConfiguration, new String[] {"metadata", "labels", "tekton.dev/pipeline"});
            configuration = ((TektonVirtualFile) VirtualFileManager.getInstance().findFileByUrl("tekton://" + element.getNamespace() + "/pipeline/" + pipeline)).getContent().toString();
            runs = tkncli.getPipelineRuns(namespace, pipeline);
        } else if (element instanceof TaskRunNode) {
            String task = YAMLHelper.getStringValueFromYAML(runConfiguration, new String[] {"metadata", "labels", "tekton.dev/task"});
            configuration = ((TektonVirtualFile) VirtualFileManager.getInstance().findFileByUrl("tekton://" + element.getNamespace() + "/task/" + task)).getContent().toString();
            runs = tkncli.getTaskRuns(namespace, task);
        }

        StartResourceModel model = new StartResourceModel(configuration, resources, serviceAccounts, secrets, configMaps, persistentVolumeClaims, runs);
        model.adaptsToRun(runConfiguration);
        return model;
    }
}
