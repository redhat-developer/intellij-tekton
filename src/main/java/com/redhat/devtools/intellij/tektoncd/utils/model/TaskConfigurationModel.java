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
package com.redhat.devtools.intellij.tektoncd.utils.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.redhat.devtools.intellij.common.utils.YAMLHelper;
import com.redhat.devtools.intellij.tektoncd.tkn.component.field.Input;
import com.redhat.devtools.intellij.tektoncd.tkn.component.field.Output;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import static com.redhat.devtools.intellij.tektoncd.Constants.FLAG_INPUTRESOURCETASK;
import static com.redhat.devtools.intellij.tektoncd.Constants.FLAG_PARAMETER;

public class TaskConfigurationModel extends ConfigurationModel {
    Logger logger = LoggerFactory.getLogger(TaskConfigurationModel.class);
    private List<Input> params;
    private List<Input> inputResources;
    private List<Output> outputResources;
    private List<String> workspaces;

    public TaskConfigurationModel(String configuration) {
        super(configuration);
        this.params = findParams(configuration);
        this.inputResources = findInputResources(configuration);
        this.outputResources = findOutputs(configuration);
        this.workspaces = findWorkspaces(configuration);
    }

    private List<String> findWorkspaces(String configuration) {
        List<String> workspaces = new ArrayList<>();
        try {
            JsonNode workspacesNode = YAMLHelper.getValueFromYAML(configuration, new String[] {"spec", "workspaces"});
            if (workspacesNode != null) {
                for(JsonNode item : workspacesNode) {
                    if (item.has("name")) {
                        workspaces.add(item.get("name").asText());
                    }
                }
            }
        } catch (IOException e) {
            logger.warn(e.getLocalizedMessage());
        }
        return workspaces;
    }

    private List<Output> findOutputs(String configuration) {
        List<Output> outputs = new ArrayList<>();
        try {
            JsonNode outputsNode = YAMLHelper.getValueFromYAML(configuration, new String[] {"spec", "resources", "outputs"});
            if (outputsNode != null) {
                outputs.addAll(getOutputs(outputsNode));
            }
        } catch (IOException e) {
            logger.warn(e.getLocalizedMessage());
        }
        return outputs;
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

    private List<Input> findInputResources(String configuration) {
        List<Input> inputs = new ArrayList<>();

        try {
            JsonNode resourceInputsNode = YAMLHelper.getValueFromYAML(configuration, new String[]{"spec", "resources", "inputs"});
            if (resourceInputsNode != null) {
                inputs.addAll(getInputsFromNode(resourceInputsNode, FLAG_INPUTRESOURCETASK));
            }
        } catch (IOException e) {
            logger.warn(e.getLocalizedMessage());
        }

        return inputs;

    }

    private List<Input> findParams(String configuration) {
        List<Input> inputs = new ArrayList<>();

        try {
            JsonNode paramsNode = YAMLHelper.getValueFromYAML(configuration, new String[]{"spec", "params"});
            if (paramsNode != null) {
                inputs.addAll(getInputsFromNode(paramsNode, FLAG_PARAMETER));
            }
        } catch (IOException e) {
            logger.warn(e.getLocalizedMessage());
        }

        return inputs;

    }

    @Override
    public List<Input> getParams() {
        return this.params;
    }

    @Override
    public List<Input> getInputResources() {
        return this.inputResources;
    }

    @Override
    public List<Output> getOutputResources() {
        return this.outputResources;
    }

    @Override
    public List<String> getWorkspaces() {
        return this.workspaces;
    }
}
