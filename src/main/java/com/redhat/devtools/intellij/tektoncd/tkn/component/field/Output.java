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

import java.util.Optional;

public class Output {

    private String name;
    private String type;
    private Optional<String> description;
    private Optional<Boolean> optional;
    private String value;

    public Output() {}

    public String name() {
        return name;
    }

    public String type() {
        return type;
    }

    public Optional<String> description() {
        return description;
    }

    public Optional<Boolean> optional() {
        return optional;
    }

    public String value() { return value; }

    public void setValue(String value) {
        this.value = value;
    }

    public Output fromJson(JsonNode outputNode) {
        if (!outputNode.has("name") ||
                outputNode.get("name").isNull()) {
            return null;
        }
        String name = outputNode.get("name").asText("");
        String type = "string"; // which is default value for a resource ??
        Optional<String> description = Optional.empty();
        Optional<Boolean> optional = Optional.empty();
        JsonNode typeItem = outputNode.get("type");
        if (typeItem != null) {
            type = typeItem.asText();
        }
        JsonNode descriptionItem = outputNode.get("description");
        if (descriptionItem != null) {
            description = Optional.of(descriptionItem.asText());
        }
        JsonNode optionalItem = outputNode.get("optional");
        if (optionalItem != null) {
            optional = Optional.of(optionalItem.asBoolean());
        }
        this.name = name;
        this.type = type;
        this.description = description;
        this.optional = optional;
        return this;
    }

    @Override
    public String toString() {
        return name;
    }

}
