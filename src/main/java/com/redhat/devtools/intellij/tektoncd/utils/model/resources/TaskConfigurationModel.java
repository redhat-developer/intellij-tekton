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
package com.redhat.devtools.intellij.tektoncd.utils.model.resources;

import com.fasterxml.jackson.databind.JsonNode;
import com.redhat.devtools.intellij.common.utils.YAMLHelper;
import com.redhat.devtools.intellij.tektoncd.tkn.component.field.Input;
import com.redhat.devtools.intellij.tektoncd.tkn.component.field.Output;
import com.redhat.devtools.intellij.tektoncd.tkn.component.field.Workspace;
import com.redhat.devtools.intellij.tektoncd.utils.model.ResourceConfigurationModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TaskConfigurationModel extends ResourceConfigurationModel {
    Logger logger = LoggerFactory.getLogger(TaskConfigurationModel.class);
    private List<Input> params;
    private List<Input> inputResources;
    private List<Output> outputResources;
    private List<Workspace> workspaces;

    public TaskConfigurationModel(String configuration) {
        super(configuration);
        this.params = findParams(configuration);
        this.inputResources = findInputResources(configuration, new String[] {"spec", "resources", "inputs"});
        this.outputResources = findOutputs(configuration);
        this.workspaces = findWorkspaces(configuration);
    }

    private List<Output> findOutputs(String configuration) {
        List<Output> outputs = new ArrayList<>();
        try {
            JsonNode outputsNode = YAMLHelper.getValueFromYAML(configuration, new String[] {"spec", "resources", "outputs"});
            if (outputsNode != null) {
                outputs.addAll(getOutputs(outputsNode));
            }
        } catch (IOException e) {
            logger.warn(e.getLocalizedMessage(), e);
        }
        return outputs;
    }

    private List<Output> getOutputs(JsonNode outputsNode) {
        List<Output> result = new ArrayList<>();

        if (outputsNode != null) {
            for (JsonNode item : outputsNode) {
                Output o = new Output().fromJson(item);
                if (o != null) {
                    result.add(o);
                }
            }
        }

        return result;
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
    public List<Workspace> getWorkspaces() {
        return this.workspaces;
    }
}
