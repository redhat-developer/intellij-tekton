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

import java.io.IOException;
import java.net.URL;
import java.util.Map;

public class JSONHelper {

    private static final ObjectMapper JSON_MAPPER = new ObjectMapper(new JsonFactory());

    public static JsonNode getJSONFromURL(URL file) throws IOException {
        return JSON_MAPPER.readTree(file);
    }

    public static JsonNode MapToJSON(Map<String, Object> map) throws IOException {
        return JSON_MAPPER.readTree(JSON_MAPPER.writeValueAsString(map));
    }
}
