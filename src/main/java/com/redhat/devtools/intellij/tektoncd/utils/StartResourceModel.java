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
package com.redhat.devtools.intellij.tektoncd.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Strings;
import com.redhat.devtools.intellij.common.utils.YAMLHelper;
import com.redhat.devtools.intellij.tektoncd.tkn.Resource;
import com.redhat.devtools.intellij.tektoncd.tkn.component.field.Input;
import com.redhat.devtools.intellij.tektoncd.tkn.component.field.Output;
import com.redhat.devtools.intellij.tektoncd.tkn.component.field.Workspace;

import com.redhat.devtools.intellij.tektoncd.utils.model.RunConfigurationModel;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.redhat.devtools.intellij.tektoncd.Constants.FLAG_INPUTRESOURCEPIPELINE;
import static com.redhat.devtools.intellij.tektoncd.Constants.FLAG_INPUTRESOURCETASK;
import static com.redhat.devtools.intellij.tektoncd.Constants.FLAG_PARAMETER;
import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_PIPELINE;

public class StartResourceModel {

    private String namespace, name, kind;
    private String serviceAccountName;
    private List<Input> inputs;
    private List<Output> outputs;
    private List<Resource> resources;
    private List<String> serviceAccounts, secrets, configMaps, persistenceVolumeClaims;
    private boolean isValid = true;
    private String errorMessage;
    private Map<String, String> parameters, inputResources, outputResources, taskServiceAccountNames;
    private Map<String, Workspace> workspaces;

    public StartResourceModel(String configuration, List<Resource> resources, List<String> serviceAccounts, List<String> secrets, List<String> configMaps, List<String> persistenceVolumeClaims) {
        this.errorMessage = "Tekton configuration has an invalid format:\n";
        this.inputs = Collections.emptyList();
        this.outputs = Collections.emptyList();
        this.resources = resources;
        this.serviceAccountName = "";
        this.parameters = Collections.emptyMap();
        this.inputResources = Collections.emptyMap();
        this.outputResources = Collections.emptyMap();
        this.taskServiceAccountNames = Collections.emptyMap();
        this.serviceAccounts = serviceAccounts;
        this.workspaces = Collections.emptyMap();
        this.secrets = secrets;
        this.configMaps = configMaps;
        this.persistenceVolumeClaims = persistenceVolumeClaims;

        buildModel(configuration);
    }

    private void buildModel(String configuration) {
        try {
            this.namespace = YAMLHelper.getStringValueFromYAML(configuration, new String[] {"metadata", "namespace"});
            if (Strings.isNullOrEmpty(namespace)) {
                errorMessage += " * Namespace field is missing or its value is not valid.\n";
                isValid = false;
            }
            this.name = YAMLHelper.getStringValueFromYAML(configuration, new String[] {"metadata", "name"});
            if (Strings.isNullOrEmpty(this.name)) {
                errorMessage += " * Name field is missing or its value is not valid.\n";
                isValid = false;
            }
            this.kind = YAMLHelper.getStringValueFromYAML(configuration, new String[] {"kind"});
            if (Strings.isNullOrEmpty(kind)) {
                errorMessage += " * Kind field is missing or its value is not valid.\n";
                isValid = false;
            }
            initTaskServiceAccounts(configuration);
            buildWorkspaces(configuration);
            buildInputs(configuration);
            buildOutputs(configuration);

            // if for a specific input/output type (git, image, ...) only a resource exists, set that resource as default value for input/output
            setDefaultValueResources();
        } catch (IOException e) {
            errorMessage += " Error: " + e.getLocalizedMessage() + "\n";
            isValid = false;
        }
    }

    private void initTaskServiceAccounts(String configuration) throws IOException {
        if (isPipeline()) {
            JsonNode tasksNode = YAMLHelper.getValueFromYAML(configuration, new String[] {"spec", "tasks"});
            if (tasksNode != null) {
                this.taskServiceAccountNames = new HashMap<>();
                for(JsonNode item : tasksNode) {
                    String task = item.get("name").asText();
                    this.taskServiceAccountNames.put(task, "");
                }
            }
        }
    }

    private void buildInputs(String configuration) throws IOException {
        this.inputs = new ArrayList<>();
        JsonNode paramsNode = YAMLHelper.getValueFromYAML(configuration, new String[]{"spec", "params"});
        if (paramsNode != null) {
            this.inputs.addAll(getInputsFromNode(paramsNode, FLAG_PARAMETER));
        }

        if (isPipeline()) {
            JsonNode inputsNode = YAMLHelper.getValueFromYAML(configuration, new String[] {"spec", "resources"});
            if (inputsNode != null) {
                this.inputs.addAll(getInputsFromNode(inputsNode, FLAG_INPUTRESOURCEPIPELINE));
            }
        } else {
            JsonNode resourceInputsNode = YAMLHelper.getValueFromYAML(configuration, new String[]{"spec", "resources", "inputs"});
            if (resourceInputsNode != null) {
                this.inputs.addAll(getInputsFromNode(resourceInputsNode, FLAG_INPUTRESOURCETASK));
            }
        }
    }

    private void buildOutputs(String configuration) throws IOException {
        if (!isPipeline()) {
            JsonNode outputsNode = YAMLHelper.getValueFromYAML(configuration, new String[] {"spec", "resources", "outputs"});
            if (outputsNode != null) {
                this.outputs = getOutputs(outputsNode);
            }
        }
    }

    private void buildWorkspaces(String configuration) throws IOException {
        JsonNode workspacesNode = YAMLHelper.getValueFromYAML(configuration, new String[] {"spec", "workspaces"});
        if (workspacesNode != null) {
            workspaces = new HashMap<>();
            for(JsonNode item : workspacesNode) {
                workspaces.put(item.get("name").asText(), null);
            }
        }
    }

    private void setDefaultValueResources() {
        List<Input> resourceInputs = null;
        if (this.inputs != null) {
            resourceInputs = inputs.stream().filter(input -> input.kind() == Input.Kind.RESOURCE).collect(Collectors.toList());
        }

        if (resourceInputs == null && this.outputs == null) {
            return;
        }

        if (this.resources == null) {
            errorMessage += " * The " + this.kind + " requires resources to be started but no resources were found in the cluster.\n";
            isValid = false;
        }

        // set the first resource for a specific type (git, image, ...) as the default value for input/output
        Map<String, List<Resource>> resourceGroupedByType = resources.stream().collect(Collectors.groupingBy(Resource::type));

        if (resourceInputs != null) {
            for (Input input: resourceInputs) {
                List<Resource> resourcesByInputType = resourceGroupedByType.get(input.type());
                if (resourcesByInputType == null) {
                    errorMessage += " * The input " + input.name() + " requires a resource of type " + input.type() + " but no resource of that type was found in the cluster.\n";
                    isValid = false;
                    continue;
                }
                input.setValue(resourcesByInputType.get(0).name());
            }
        }

        if (outputs != null) {
            for (Output output: outputs) {
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

    private List<Input> getInputsFromNode(JsonNode inputsNode, String flag) {
        List<Input> result = new ArrayList<>();

        if (flag.equals(FLAG_PARAMETER)) {
            result.addAll(getInputsFromNodeInternal(inputsNode, Input.Kind.PARAMETER));
        } else {
            result.addAll(getInputsFromNodeInternal(inputsNode, Input.Kind.RESOURCE));
        }

        return result;
    }

    private List<Input> getInputsFromNodeInternal(JsonNode node, Input.Kind kind) {
        List<Input> result = new ArrayList<>();
        if (node != null) {
            for (JsonNode item : node) {
                result.add(new Input().fromJson(item, kind));
            }
        }
        return result;
    }

    private List<Output> getOutputs(JsonNode outputsNode) {
        List<Output> result = new ArrayList<>();

        if (outputsNode != null) {
            for (JsonNode item : outputsNode) {
                result.add(new Output().fromJson(item));
            }
        }

        return result;
    }

    private boolean isPipeline() {
        return KIND_PIPELINE.equalsIgnoreCase(this.kind);
    }

    public boolean isValid() {
        return this.isValid;
    }

    public String getNamespace() {
        return this.namespace;
    }

    public String getName() {
        return this.name;
    }

    public String getKind() {
        return this.kind;
    }

    public List<Input> getInputs() {
        return this.inputs;
    }

    public List<Output> getOutputs() {
        return this.outputs;
    }

    public List<Resource> getResources() {
        return this.resources;
    }

    public List<String> getServiceAccounts() {
        return this.serviceAccounts;
    }

    public String getErrorMessage() {
        return this.errorMessage;
    }

    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public void setInputResources(Map<String, String> inputResources) {
        this.inputResources = inputResources;
    }

    public Map<String, String> getInputResources() {
        return inputResources;
    }

    public void setOutputResources(Map<String, String> outputResources) {
        this.outputResources = outputResources;
    }

    public Map<String, String> getOutputResources() {
        return outputResources;
    }

    public void setServiceAccount(String serviceAccountName) { this.serviceAccountName = serviceAccountName; }

    public String getServiceAccount() { return this.serviceAccountName; }

    public void setTaskServiceAccounts(Map<String, String> taskServiceAccountNames) { this.taskServiceAccountNames = taskServiceAccountNames; }

    public Map<String, String> getTaskServiceAccounts() { return this.taskServiceAccountNames; }

    public Map<String, Workspace> getWorkspaces() {
        return this.workspaces;
    }

    public List<String> getSecrets() {
        return this.secrets;
    }

    public List<String> getConfigMaps() {
        return this.configMaps;
    }

    public List<String> getPersistenceVolumeClaims() {
        return this.persistenceVolumeClaims;
    }

    public void adaptsToRun(String configuration) {
        // TODO once #187 is merged we will use the factory to get the model
        RunConfigurationModel conf = new RunConfigurationModel(configuration);
        /* TODO
        if (!(conf instanceof RunConfigurationModel)) return;
         */

        // update params/input resources
        this.inputs.stream().forEach(input -> {
            // for each input, update its defaultValue/Value with the value taken from the *run model
            if (input.kind().equals(Input.Kind.PARAMETER)) {
                if (conf.getParameters().containsKey(input.name())) {
                    String value = conf.getParameters().get(input.name());
                    input.setDefaultValue(value);
                }
            } else {
                String value = conf.getResources().get(input.name());
                input.setValue(value);
            }
        });

        // update workspaces
        this.workspaces.keySet().forEach(workspaceName -> {
            this.workspaces.put(workspaceName, conf.getWorkspacesValues().get(workspaceName));
        });

        //this.serviceAccountName = conf.getServiceAccount();

        /*this.taskServiceAccountNames.keySet().forEach(task -> {
            if (conf.getTaskServiceAccountNames().has(task)) {
                this.taskServiceAccountNames.put(task, conf.getTaskServiceAccountNames.get(task));
            }
        });*/

    }
}
