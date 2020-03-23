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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.IOException;

public class YAMLHelper {

    private static final ObjectMapper YAML_MAPPER = new ObjectMapper(new YAMLFactory());

    public static String getStringValueFromYAML(String yamlAsString, String[] fieldnames) throws IOException {
        JsonNode nodeValue = YAMLHelper.getValueFromYAML(yamlAsString, fieldnames);
        if (nodeValue == null && !nodeValue.isTextual()) return null;
        return nodeValue.asText();
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