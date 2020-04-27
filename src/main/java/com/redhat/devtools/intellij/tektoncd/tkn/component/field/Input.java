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
package com.redhat.devtools.intellij.tektoncd.tkn.component.field;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class Input {

    public enum Kind { PARAMETER, RESOURCE }

    private String name;
    private String type;
    private Kind kind;
    private Optional<String> description;
    private String value;
    private Optional<String> defaultValue;

    public Input() {}

    public String name() {
        return name;
    }

    public String type() {
        return type;
    }

    public Optional<String> description() {
        return description;
    }

    public Optional<String> defaultValue() {
        return defaultValue;
    }

    public String value() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Kind kind() {
        return kind;
    }

    public Input fromJson(JsonNode inputNode, Kind kind) {
        String name = inputNode.get("name").asText();
        String type = "string";
        Optional<String> description = Optional.empty();
        Optional<String> defaultValue = Optional.empty();
        JsonNode typeItem = inputNode.get("type");
        if (typeItem != null) {
            type = typeItem.asText();
        }
        JsonNode descriptionItem = inputNode.get("description");
        if (descriptionItem != null) {
            description = Optional.of(descriptionItem.asText());
        }
        JsonNode defaultItem = inputNode.get("default");
        if (defaultItem != null) {
            defaultValue = Optional.of(defaultItem.asText());
            if (defaultItem.isArray()) {
                defaultValue = Optional.of(convertArrayNodeToString((ArrayNode) defaultItem));
            }
        }
        this.name = name;
        this.type = type;
        this.defaultValue = defaultValue;
        this.description = description;
        this.kind = kind;
        return this;
    }

    private String convertArrayNodeToString(ArrayNode node) {
        return  StreamSupport.stream(node.spliterator(), false).map(JsonNode::asText).collect(Collectors.joining(","));
    }

    @Override
    public String toString() {
        return name;
    }
}
