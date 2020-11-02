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

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Strings;
import com.redhat.devtools.intellij.common.utils.YAMLHelper;
import com.redhat.devtools.intellij.tektoncd.tkn.Resource;
import com.redhat.devtools.intellij.tektoncd.tkn.component.field.Input;
import com.redhat.devtools.intellij.tektoncd.tkn.component.field.Output;
import com.redhat.devtools.intellij.tektoncd.tkn.component.field.Workspace;
import com.redhat.devtools.intellij.tektoncd.utils.model.ConfigurationModel;
import com.redhat.devtools.intellij.tektoncd.utils.model.ConfigurationModelFactory;
import com.redhat.devtools.intellij.tektoncd.utils.model.ResourceConfigurationModel;
import com.redhat.devtools.intellij.tektoncd.utils.model.resources.PipelineConfigurationModel;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_CLUSTERTASK;

public abstract class ActionToRunModel extends ConfigurationModel {

    protected ResourceConfigurationModel resource;
    protected boolean isValid = true;
    protected String errorMessage;
    protected Map<String, Workspace> workspaces;
    protected Map<String, String> taskServiceAccountNames;
    protected String globalServiceAccount;
    protected List<Resource> pipelineResources;
    protected List<String> serviceAccounts, secrets, configMaps, persistentVolumeClaims;

    public ActionToRunModel(String configuration, List<Resource> resources, List<String> serviceAccounts, List<String> secrets, List<String> configMaps, List<String> persistentVolumeClaims) {
        super(configuration);
        this.errorMessage = "Tekton configuration has an invalid format:\n";
        this.serviceAccounts = serviceAccounts;
        this.pipelineResources = resources;
        this.secrets = secrets;
        this.configMaps = configMaps;
        this.persistentVolumeClaims = persistentVolumeClaims;
        init(configuration);
    }

    public void init(String configuration) {
        if (Strings.isNullOrEmpty(name)) {
            errorMessage += " * Name field is missing or its value is not valid.\n";
            isValid = false;
        }
        if (Strings.isNullOrEmpty(kind)) {
            errorMessage += " * Kind field is missing or its value is not valid.\n";
            isValid = false;
        }
        if (Strings.isNullOrEmpty(namespace) && !kind.equalsIgnoreCase(KIND_CLUSTERTASK)) {
            errorMessage += " * Namespace field is missing or its value is not valid.\n";
            isValid = false;
        }

        if (isValid) {
            resource = (ResourceConfigurationModel) ConfigurationModelFactory.getModel(configuration);
            if (resource == null) {
                errorMessage += "Unable to create a model for the following resource kind - " + kind;
                isValid = false;
                return;
            }

            this.globalServiceAccount = "";
            this.workspaces = new HashMap<>();
            resource.getWorkspaces().stream().forEach(ws -> this.workspaces.put(ws, null));
            this.taskServiceAccountNames = Collections.emptyMap();

            if (resource instanceof PipelineConfigurationModel) {
                initTaskServiceAccounts(configuration);
            }
            // if for a specific input/output type (git, image, ...) only a resource exists, set that resource as default value for input/output
            setDefaultValueResources();
        }
    }

    public boolean isValid() {
        return this.isValid;
    }

    public String getErrorMessage() {
        return this.errorMessage;
    }

    public List<Resource> getPipelineResources() {
        return this.pipelineResources;
    }

    public List<String> getServiceAccounts() {
        return this.serviceAccounts;
    }

    public List<String> getSecrets() {
        return this.secrets;
    }

    public List<String> getConfigMaps() {
        return this.configMaps;
    }

    public List<String> getPersistentVolumeClaims() {
        return this.persistentVolumeClaims;
    }

    public List<Input> getParams() {
        return resource.getParams();
    }

    public List<Input> getInputResources() {
        return resource.getInputResources();
    }

    public List<Output> getOutputResources() {
        return resource.getOutputResources();
    }

    public Map<String, Workspace> getWorkspaces() {
        return this.workspaces;
    }

    public Map<String, String> getTaskServiceAccounts() { return this.taskServiceAccountNames; }

    public void setServiceAccount(String serviceAccountName) { this.globalServiceAccount = serviceAccountName; }

    public String getServiceAccount() { return this.globalServiceAccount; }

    private void initTaskServiceAccounts(String configuration) {
        this.taskServiceAccountNames = new LinkedHashMap<>();
        JsonNode tasksNode = null;
        try {
            tasksNode = YAMLHelper.getValueFromYAML(configuration, new String[] {"spec", "tasks"});
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (tasksNode != null) {
            for(JsonNode item : tasksNode) {
                if (item.has("name")) {
                    String task = item.get("name").asText();
                    this.taskServiceAccountNames.put(task, "");
                }
            }
        }
        // get tasks from finally section
        JsonNode finallyNode = null;
        try {
            finallyNode = YAMLHelper.getValueFromYAML(configuration, new String[] {"spec", "finally"});
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (finallyNode != null) {
            for(JsonNode item : finallyNode) {
                if (item.has("name")) {
                    String finalTask = item.get("name").asText();
                    this.taskServiceAccountNames.put(finalTask, "");
                }
            }
        }
    }

    private void setDefaultValueResources() {
        if (this.pipelineResources == null || this.pipelineResources.isEmpty()) {
            if (!this.getInputResources().isEmpty() || !this.getOutputResources().isEmpty()) {
                errorMessage += " * The " + this.kind + " requires resources to be started but no resources were found in the cluster.\n";
                isValid = false;
            }
        }

        // set the first resource for a specific type (git, image, ...) as the default value for input/output
        Map<String, List<Resource>> resourceGroupedByType = pipelineResources.stream().collect(Collectors.groupingBy(Resource::type));

        if (!this.resource.getInputResources().isEmpty()) {
            for (Input input: this.resource.getInputResources()) {
                List<Resource> resourcesByInputType = resourceGroupedByType.get(input.type());
                if (resourcesByInputType == null) {
                    errorMessage += " * The input " + input.name() + " requires a resource of type " + input.type() + " but no resource of that type was found in the cluster.\n";
                    isValid = false;
                    continue;
                }
                input.setValue(resourcesByInputType.get(0).name());
            }
        }

        if (!this.resource.getOutputResources().isEmpty()) {
            for (Output output: this.resource.getOutputResources()) {
                List<Resource> resourcesByOutputType = resourceGroupedByType.get(output.type());
                if (resourcesByOutputType == null) {
                    errorMessage += " * The output " + output.name() + " requires a resource of type " + output.type() + " but no resource of that type was found in the cluster.\n";
                    isValid = false;
                    continue;
                }
                output.setValue(resourcesByOutputType.get(0).name());
            }
        }
    }


}
