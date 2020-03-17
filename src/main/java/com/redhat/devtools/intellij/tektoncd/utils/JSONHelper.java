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

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;

import java.io.IOException;
import java.net.URL;
import java.util.Map;

import static com.fasterxml.jackson.dataformat.yaml.YAMLGenerator.Feature.WRITE_DOC_START_MARKER;

public class JSONHelper {

    private static final ObjectMapper JSON_MAPPER = new ObjectMapper(new JsonFactory());

    public static JsonNode getJSONFromFile(URL file) throws IOException {
        return JSON_MAPPER.readTree(file);
    }

    public static String JSONToYAML(JsonNode json) throws JsonProcessingException {
        if (json == null) {
            return "";
        }
        return new YAMLMapper().configure(WRITE_DOC_START_MARKER, false).writeValueAsString(json);
    }

    public static JsonNode MapToJSON(Map<String, Object> map) throws IOException {
        if (map == null) {
            return null;
        }
        return JSON_MAPPER.readTree(JSON_MAPPER.writeValueAsString(map));
    }
}
