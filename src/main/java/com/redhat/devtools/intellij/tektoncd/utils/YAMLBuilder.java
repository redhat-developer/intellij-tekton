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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.redhat.devtools.intellij.tektoncd.tkn.component.field.Input;
import com.redhat.devtools.intellij.tektoncd.tkn.component.field.Output;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class YAMLBuilder {
    private static final ObjectMapper YAML_MAPPER = new ObjectMapper(new com.fasterxml.jackson.dataformat.yaml.YAMLFactory());

    public static String createPreview(String serviceAccount, Map<String, String> taskServiceAccounts, List<Input> inputs, List<Output> outputs) throws IOException {
        ObjectNode rootNode = YAML_MAPPER.createObjectNode();

        if (!serviceAccount.isEmpty()) {
            rootNode.put("serviceAccountName", serviceAccount);
        }

        ArrayNode tsaNode = createTaskServiceAccountNode(taskServiceAccounts);
        if (tsaNode.size() > 0) {
            rootNode.set("serviceAccountNames", tsaNode);
        }

        ObjectNode inputsNode = createInputNode(inputs);
        rootNode.set("inputs", inputsNode);

        ObjectNode outputsNode = createOutputNode(outputs);
        rootNode.set("outputs", outputsNode);

        return new YAMLMapper().writeValueAsString(rootNode);
    }

    private static ArrayNode createTaskServiceAccountNode(Map<String, String> tsa) {
        ArrayNode serviceAccountNamesNode = YAML_MAPPER.createArrayNode();
        for (String task: tsa.keySet()) {
            if (!tsa.get(task).isEmpty()) {
                ObjectNode tsaNode = YAML_MAPPER.createObjectNode();
                tsaNode.put("taskName", task);
                tsaNode.put("serviceAccountName", tsa.get(task));
                serviceAccountNamesNode.add(tsaNode);
            }
        }
        return serviceAccountNamesNode;
    }

    private static ObjectNode createInputNode(List<Input> inputs) {
        ObjectNode inputsNode = YAML_MAPPER.createObjectNode();
        ArrayNode paramsNode = YAML_MAPPER.createArrayNode();
        ArrayNode resourcesNode = YAML_MAPPER.createArrayNode();
        ObjectNode inputNode;
        for (Input input: inputs) {
            inputNode = YAML_MAPPER.createObjectNode();
            inputNode.put("name", input.name());
            if (input.kind() == Input.Kind.PARAMETER) {
                String value = input.value() == null ? input.defaultValue().orElse("") : input.value();
                if (input.type().equals("array")) {
                    ArrayNode paramValuesNode = YAML_MAPPER.valueToTree(value.split(","));
                    inputNode.set("value", paramValuesNode);
                } else {
                    inputNode.put("value", value);
                }
                paramsNode.add(inputNode);
            } else {
                // resourceRef node
                ObjectNode resourceRefNode = YAML_MAPPER.createObjectNode();
                resourceRefNode.put("name", input.value() == null ? "Resource has not yet been inserted" : input.value());
                inputNode.set("resourceRef", resourceRefNode);
                resourcesNode.add(inputNode);
            }
        }

        if (paramsNode.size() > 0) inputsNode.set("params", paramsNode);
        if (resourcesNode.size() > 0) inputsNode.set("resources", resourcesNode);
        return inputsNode;
    }

    private static ObjectNode createOutputNode(List<Output> outputs) {
        ObjectNode outputsNode = YAML_MAPPER.createObjectNode();
        ArrayNode resourcesNode = YAML_MAPPER.createArrayNode();
        ObjectNode outputNode;
        for (Output output: outputs) {
            outputNode = YAML_MAPPER.createObjectNode();
            outputNode.put("name", output.name());

            // resourceRef node
            ObjectNode resourceRefNode = YAML_MAPPER.createObjectNode();
            resourceRefNode.put("name", output.value() == null ? "Resource has not yet been inserted" : output.value());
            outputNode.set("resourceRef", resourceRefNode);
            resourcesNode.add(outputNode);
        }

        if (resourcesNode.size() > 0) outputsNode.set("resources", resourcesNode);
        return outputsNode;
    }
}
