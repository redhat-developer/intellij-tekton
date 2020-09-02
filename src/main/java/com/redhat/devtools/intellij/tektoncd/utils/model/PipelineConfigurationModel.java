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
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import static com.redhat.devtools.intellij.tektoncd.Constants.FLAG_INPUTRESOURCEPIPELINE;
import static com.redhat.devtools.intellij.tektoncd.Constants.FLAG_PARAMETER;

public class PipelineConfigurationModel extends ConfigurationModel {
    Logger logger = LoggerFactory.getLogger(PipelineConfigurationModel.class);
    private List<Input> params;
    private List<Input> inputResource;
    private List<String> workspaces;

    public PipelineConfigurationModel(String configuration) {
        super(configuration);
        this.params = findParams(configuration);
        this.inputResource = findResources(configuration);
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

    private List<Input> findResources(String configuration) {
        List<Input> inputs = new ArrayList<>();

        try {
            JsonNode inputsNode = YAMLHelper.getValueFromYAML(configuration, new String[] {"spec", "resources"});
            if (inputsNode != null) {
                inputs.addAll(getInputsFromNode(inputsNode, FLAG_INPUTRESOURCEPIPELINE));
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
                try {
                    result.add(new Input().fromJson(item, kind));
                } catch (Exception e) {
                    logger.warn(e.getLocalizedMessage());
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
        return this.inputResource;
    }

    @Override
    public List<Output> getOutputResources() {
        return Collections.emptyList();
    }

    @Override
    public List<String> getWorkspaces() {
        return this.workspaces;
    }
}
