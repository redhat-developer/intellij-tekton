package com.redhat.devtools.intellij.tektoncd.utils;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Map;

public class JSONHelper {

    private static final ObjectMapper JSON_MAPPER = new ObjectMapper(new JsonFactory());

    public static JsonNode MapToJSON(Map<String, Object> map) throws IOException {
        return JSON_MAPPER.readTree(JSON_MAPPER.writeValueAsString(map));
    }
}

