package com.redhat.devtools.intellij.tektoncd.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.IOException;

public class YAMLHelper {

    private static final ObjectMapper YAML_MAPPER = new ObjectMapper(new YAMLFactory());

    public static String getStringValueFromYAML(String yamlAsString, String[] fieldnames) throws IOException {
        if (yamlAsString == null) return null;
        JsonNode node = YAML_MAPPER.readTree(yamlAsString);
        for (String fieldname: fieldnames) {
            if (!node.has(fieldname)) return null;
            node = node.get(fieldname);
        }
        return node.asText();
    }

    public static JsonNode getValueFromYAML(String yamlAsString, String[] fieldnames) throws IOException {
        if (yamlAsString == null) return null;
        JsonNode node = YAML_MAPPER.readTree(yamlAsString);
        for (String fieldname: fieldnames) {
            if (!node.has(fieldname)) return null;
            node = node.get(fieldname);
        }
        return node;
    }
}
