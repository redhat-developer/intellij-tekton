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
package com.redhat.devtools.intellij.tektoncd.utils.model.actions;

import com.redhat.devtools.intellij.tektoncd.tkn.Resource;
import com.redhat.devtools.intellij.tektoncd.utils.model.ConfigurationModel;
import com.redhat.devtools.intellij.tektoncd.utils.model.ConfigurationModelFactory;
import com.redhat.devtools.intellij.tektoncd.utils.model.RunConfigurationModel;
import com.redhat.devtools.intellij.tektoncd.utils.model.runs.PipelineRunConfigurationModel;
import com.redhat.devtools.intellij.tektoncd.utils.model.runs.TaskRunConfigurationModel;
import io.fabric8.kubernetes.api.model.HasMetadata;
import java.util.List;

public class StartResourceModel extends ActionToRunModel {

    private List<? extends HasMetadata> runs;
    private String runPrefixName;

    public StartResourceModel(String configuration, List<Resource> resources, List<String> serviceAccounts, List<String> secrets, List<String> configMaps, List<String> persistentVolumeClaims) {
        super(configuration, resources, serviceAccounts, secrets, configMaps, persistentVolumeClaims);
        this.runPrefixName = "";
    }

    public StartResourceModel(String configuration, List<Resource> resources, List<String> serviceAccounts, List<String> secrets, List<String> configMaps, List<String> persistentVolumeClaims, List<? extends HasMetadata> runs) {
        this(configuration, resources, serviceAccounts, secrets, configMaps, persistentVolumeClaims);
        this.runs = runs;
    }

    public List<? extends HasMetadata> getRuns() {
        return this.runs;
    }

    public void setRunPrefixName(String runPrefixName) {
        this.runPrefixName = runPrefixName;
    }

    public String getRunPrefixName() {
        return runPrefixName;
    }

    public void adaptsToRun(String configuration) {
        ConfigurationModel model = ConfigurationModelFactory.getModel(configuration);
        if (!(model instanceof RunConfigurationModel)) return;

        // update params/input resources
        this.resource.getParams().forEach(input -> {
            // for each input, update its defaultValue/Value with the value taken from the *run model
            if (((RunConfigurationModel) model).getParameters().containsKey(input.name())) {
                String value = ((RunConfigurationModel) model).getParameters().get(input.name());
                input.setDefaultValue(value);
            }
        });

        this.resource.getInputResources().forEach(input -> {
            // for each input, update its defaultValue/Value with the value taken from the *run model
            String value = null;
            if (model instanceof PipelineRunConfigurationModel) {
                if (((PipelineRunConfigurationModel) model).getResources().containsKey(input.name())) {
                    value = ((PipelineRunConfigurationModel) model).getResources().get(input.name());
                }
            } else {
                if (((TaskRunConfigurationModel) model).getInputResources().containsKey(input.name())) {
                    value = ((TaskRunConfigurationModel) model).getInputResources().get(input.name());
                }
            }
            if (value != null) {
                input.setValue(value);
            }
        });

        // update output resource if its a taskrun
        if (model instanceof TaskRunConfigurationModel) {
            this.resource.getOutputResources().stream().forEach(output -> {
                if (((TaskRunConfigurationModel) model).getOutputResources().containsKey(output.name())) {
                    output.setValue(((TaskRunConfigurationModel) model).getOutputResources().get(output.name()));
                }
            });
        }

        // update workspaces
        this.workspaces.keySet().forEach(workspaceName -> {
            if (((RunConfigurationModel) model).getWorkspacesValues().containsKey(workspaceName)) {
                this.workspaces.put(workspaceName, ((RunConfigurationModel) model).getWorkspacesValues().get(workspaceName));
            }
        });

        //update serviceAccount/taskServiceAccount
        String sa = ((RunConfigurationModel) model).getServiceAccountName();
        if (sa != null) {
            this.globalServiceAccount = sa;
        }

        this.taskServiceAccountNames.keySet().forEach(task -> {
            if (((RunConfigurationModel) model).getTaskServiceAccountNames().containsKey(task)) {
                this.taskServiceAccountNames.put(task, ((RunConfigurationModel) model).getTaskServiceAccountNames().get(task));
            }
        });

    }
}
