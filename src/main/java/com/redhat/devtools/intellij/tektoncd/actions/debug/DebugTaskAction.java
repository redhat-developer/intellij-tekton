/*******************************************************************************
 * Copyright (c) 2021 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc.
 ******************************************************************************/
package com.redhat.devtools.intellij.tektoncd.actions.debug;

import com.redhat.devtools.intellij.tektoncd.actions.StartAction;
import com.redhat.devtools.intellij.tektoncd.tkn.Tkn;
import com.redhat.devtools.intellij.tektoncd.tkn.component.field.Input;
import com.redhat.devtools.intellij.tektoncd.tkn.component.field.Output;
import com.redhat.devtools.intellij.tektoncd.tkn.component.field.Workspace;
import com.redhat.devtools.intellij.tektoncd.tree.ClusterTaskNode;
import com.redhat.devtools.intellij.tektoncd.tree.ParentableNode;
import com.redhat.devtools.intellij.tektoncd.tree.TaskNode;
import com.redhat.devtools.intellij.tektoncd.utils.DebugHelper;
import com.redhat.devtools.intellij.tektoncd.utils.DeployHelper;
import com.redhat.devtools.intellij.tektoncd.utils.YAMLBuilder;
import com.redhat.devtools.intellij.tektoncd.utils.model.actions.StartResourceModel;

import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;

import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_CLUSTERTASK;
import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_TASK;
import static com.redhat.devtools.intellij.tektoncd.utils.Utils.isActiveTektonVersionOlder;

public class DebugTaskAction extends StartAction {

    public DebugTaskAction() {
        super(TaskNode.class, ClusterTaskNode.class);
    }

    @Override
    protected String doStart(Tkn tkncli, String namespace, StartResourceModel model) throws IOException {
        String runAsYAML = createRun(tkncli, namespace, model);
        if (runAsYAML == null) {
            throw new IOException("Unable to debug task" + model.getName());
        }
        runAsYAML = YAMLBuilder.addBreakPointToResource(runAsYAML);
        String runName = DeployHelper.saveResource(runAsYAML, namespace, tkncli);
        DebugHelper.doDebugTaskRun(tkncli, namespace, runName);
        return null;
    }

    private String createRun(Tkn tkncli, String namespace, StartResourceModel model) throws IOException {
        String serviceAccount = model.getServiceAccount();
        Map<String, Input> params = model.getParams().stream().collect(Collectors.toMap(Input::name, param -> param));
        Map<String, Workspace> workspaces = model.getWorkspaces();
        Map<String, String> inputResources = model.getInputResources().stream().collect(Collectors.toMap(Input::name, Input::value));
        Map<String, String> outputResources = model.getOutputResources().stream().collect(Collectors.toMap(Output::name, Output::value));
        String runPrefixName = model.getRunPrefixName();
        String run = null;
        if (model.getKind().equalsIgnoreCase(KIND_TASK)) {
            run = tkncli.createRunFromTask(namespace, model.getName(), params, inputResources, outputResources, serviceAccount, workspaces, runPrefixName);
        } else if (model.getKind().equalsIgnoreCase(KIND_CLUSTERTASK)) {
            run = tkncli.createRunFromClusterTask(namespace, model.getName(), params, inputResources, outputResources, serviceAccount, workspaces, runPrefixName);
        }
        return run;
    }

    @Override
    public boolean isVisible(Object selected) {
        Object element = getElement(selected);
        if (element instanceof TaskNode
            || element instanceof ClusterTaskNode) {
            Tkn tkn = ((ParentableNode)element).getRoot().getTkn();
            return isActiveTektonVersionOlder(tkn.getTektonVersion(), "0.26.0") &&
                    tkn.isTektonAlphaFeatureEnabled();
        }
        return false;
    }
}
