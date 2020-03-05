package com.redhat.devtools.intellij.common.utils;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Map;

public class ParserHelper {

    private static final ObjectMapper JSON_MAPPER = new ObjectMapper(new JsonFactory());

    public static JsonNode getSpecJSON(String json) throws IOException {
        return JSON_MAPPER.readTree(json).get("spec");
    }

    public static JsonNode MapToJSON(Map<String, Object> map) throws IOException {
        return JSON_MAPPER.readTree(JSON_MAPPER.writeValueAsString(map));
    }
}

