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


import static com.redhat.devtools.intellij.tektoncd.Constants.FLAG_PARAMETER;

public abstract class ConfigurationModel {
    Logger logger = LoggerFactory.getLogger(ConfigurationModel.class);
    private String namespace, name, kind;

    public ConfigurationModel() {
        this.name = "";
        this.kind = "";
        this.namespace = "";
    }

    public ConfigurationModel(String configuration) {
        this();

        try {
            this.name = YAMLHelper.getStringValueFromYAML(configuration, new String[] {"metadata", "name"});
            this.kind = YAMLHelper.getStringValueFromYAML(configuration, new String[] {"kind"});
            this.namespace = YAMLHelper.getStringValueFromYAML(configuration, new String[] {"metadata", "namespace"});
        } catch (IOException e) {
            logger.warn(e.getLocalizedMessage());
        }
    }

    public String getName() {
        return name;
    }

    public String getKind() {
        return kind;
    }

    public String getNamespace() {
        return namespace;
    }

    public abstract List<Input> getParams();

    public abstract List<Input> getInputResources();

    public abstract List<Output> getOutputResources();

    public abstract List<String> getWorkspaces();

    protected List<Input> getInputsFromNode(JsonNode inputsNode, String flag) {
        List<Input> result = new ArrayList<>();

        if (flag.equals(FLAG_PARAMETER)) {
            result.addAll(getInputsFromNodeInternal(inputsNode, Input.Kind.PARAMETER));
        } else {
            result.addAll(getInputsFromNodeInternal(inputsNode, Input.Kind.RESOURCE));
        }

        return result;
    }

    protected List<Input> getInputsFromNodeInternal(JsonNode node, Input.Kind kind) {
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
}
