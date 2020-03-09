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
package com.redhat.devtools.intellij.common.utils;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.redhat.devtools.intellij.tektoncd.tkn.Resource;
import com.redhat.devtools.intellij.tektoncd.tkn.component.field.Input;
import com.redhat.devtools.intellij.tektoncd.tkn.component.field.Output;

import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.Optional;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

public class JSONHelper {

    private static final ObjectMapper JSON_MAPPER = new ObjectMapper(new JsonFactory());

    public static JsonNode getJSONFromURL(URL file) throws IOException {
        return JSON_MAPPER.readTree(file);
    }

    public static JsonNode MapToJSON(Map<String, Object> map) throws IOException {
        return JSON_MAPPER.readTree(JSON_MAPPER.writeValueAsString(map));
    }

    public static String getName(String json) throws IOException {
        return JSON_MAPPER.readTree(json).get("metadata").get("name").asText();
    }

    public static List<Input> getInputs(String json) throws IOException {
        List<Input> result = new ArrayList<>();
        List<JsonNode> params = JSON_MAPPER.readTree(json).get("spec").get("inputs").findValues("params");
        List<JsonNode> resources = JSON_MAPPER.readTree(json).get("spec").get("inputs").findValues("resources");

        if (params != null) {
            for (Iterator<JsonNode> it = params.iterator(); it.hasNext(); ) {
                JsonNode item = it.next();
                result.add(getInputFromJSON(item, Input.Kind.PARAMETER));
            }
        }

        if (resources != null) {
            for (Iterator<JsonNode> it = resources.iterator(); it.hasNext(); ) {
                JsonNode item = it.next();
                result.add(getInputFromJSON(item, Input.Kind.RESOURCE));
            }
        }

        return result;
    }

    private static Input getInputFromJSON(JsonNode item, Input.Kind kind) {
        String name = item.get(0).get("name").asText();
        String type = "string";
        Optional<String> description = Optional.empty();
        Optional<String> defaultValue = Optional.empty();
        JsonNode typeItem = item.get(0).get("type");
        if (typeItem != null) {
            type = typeItem.asText();
        }
        JsonNode descriptionItem = item.get(0).get("description");
        if (descriptionItem != null) {
            description = Optional.of(descriptionItem.asText());
        }
        JsonNode defaultItem = item.get(0).get("default");
        if (defaultItem != null) {
            defaultValue = Optional.of(defaultItem.asText());
        }
        return new Input(name, type, kind, description, defaultValue);
    }

    public static String createPreviewJson(List<Input> inputs, List<Output> outputs, List<Resource> resources) throws IOException {
        JsonNode rootNode = JSON_MAPPER.createObjectNode();
        JsonNode inputsNode = JSONHelper.createInputJson(inputs, resources);
        JsonNode outputsNode = JSONHelper.createOutputJson(outputs, resources);
        ((ObjectNode) rootNode).set("inputs", inputsNode);
        ((ObjectNode) rootNode).set("outputs", outputsNode);
        return JSON_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(rootNode);
    }

    public static JsonNode createInputJson(List<Input> inputs, List<Resource> resources) {
        JsonNode inputsNode = JSON_MAPPER.createObjectNode();
        ArrayNode paramsNode = JSON_MAPPER.createArrayNode();
        ArrayNode resourcesNode = JSON_MAPPER.createArrayNode();
        JsonNode inputNode = null;
        for (Input input: inputs) {
            inputNode = JSON_MAPPER.createObjectNode();
            ((ObjectNode) inputNode).put("name", input.name());
            if (input.kind() == Input.Kind.PARAMETER) {
                ((ObjectNode) inputNode).put("value", input.value() == null ? input.defaultValue().orElse("") : input.value());
                paramsNode.add(inputNode);
            } else {
                // paths node
                if (input.value() != null) {
                    for (Resource resource: resources) {
                        if (resource.name().equals(input.value())) {
                            if (resource.paths() == null) {
                                break;
                            }
                            ArrayNode pathsNode = JSON_MAPPER.createArrayNode();
                            String[] paths = resource.paths().split(",");
                            for (String path: paths) {
                                pathsNode.add(path);
                            }
                            ((ObjectNode) inputNode).set("paths", pathsNode);
                            break;
                        }
                    }
                }

                // resourceRef node
                JsonNode resourceRefNode = JSON_MAPPER.createObjectNode();
                ((ObjectNode) resourceRefNode).put("name", input.value() == null ? "Resource has not yet been inserted" : input.value());
                ((ObjectNode) inputNode).set("resourceRef", resourceRefNode);
                resourcesNode.add(inputNode);
            }
        }

        if (paramsNode.size() > 0) ((ObjectNode) inputsNode).set("params", paramsNode);
        if (resourcesNode.size() > 0) ((ObjectNode) inputsNode).set("resources", resourcesNode);
        return inputsNode;
    }

    public static JsonNode createOutputJson(List<Output> outputs, List<Resource> resources) {
        JsonNode outputsNode = JSON_MAPPER.createObjectNode();
        ArrayNode resourcesNode = JSON_MAPPER.createArrayNode();
        JsonNode outputNode = null;
        for (Output output: outputs) {
            outputNode = JSON_MAPPER.createObjectNode();
            ((ObjectNode) outputNode).put("name", output.name());
            // paths node
            if (output.value() != null) {
                for (Resource resource: resources) {
                    if (resource.name().equals(output.value())) {
                        if (resource.paths() == null) {
                            break;
                        }
                        ArrayNode pathsNode = JSON_MAPPER.createArrayNode();
                        String[] paths = resource.paths().split(",");
                        for (String path: paths) {
                            pathsNode.add(path);
                        }
                        ((ObjectNode) outputNode).set("paths", pathsNode);
                        break;
                    }
                }
            }

            // resourceRef node
            JsonNode resourceRefNode = JSON_MAPPER.createObjectNode();
            ((ObjectNode) resourceRefNode).put("name", output.value() == null ? "Resource has not yet been inserted" : output.value());
            ((ObjectNode) outputNode).set("resourceRef", resourceRefNode);
            resourcesNode.add(outputNode);
        }

        if (resourcesNode.size() > 0) ((ObjectNode) outputsNode).set("resources", resourcesNode);
        return outputsNode;
    }

    public static List<Output> getOutputs(String json) throws IOException {
        List<Output> result = new ArrayList<>();
        List<JsonNode> resources = JSON_MAPPER.readTree(json).get("spec").get("outputs").findValues("resources");

        if (resources != null) {
            for (Iterator<JsonNode> it = resources.iterator(); it.hasNext(); ) {
                JsonNode item = it.next();
                result.add(getOutputFromJSON(item));
            }
        }

        return result;
    }

    private static Output getOutputFromJSON(JsonNode item) {
        String name = item.get(0).get("name").asText();
        String type = "string"; // which is default value for a resource ??
        Optional<String> description = Optional.empty();
        Optional<Boolean> optional = Optional.empty();
        JsonNode typeItem = item.get(0).get("type");
        if (typeItem != null) {
            type = typeItem.asText();
        }
        JsonNode descriptionItem = item.get(0).get("description");
        if (descriptionItem != null) {
            description = Optional.of(descriptionItem.asText());
        }
        JsonNode optionalItem = item.get(0).get("optional");
        if (optionalItem != null) {
            optional = Optional.of(optionalItem.asBoolean());
        }
        return new Output(name, type, description, optional);
    }

}
