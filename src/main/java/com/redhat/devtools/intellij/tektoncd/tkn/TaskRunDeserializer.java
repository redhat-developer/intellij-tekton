/*******************************************************************************
 * Copyright (c) 2019 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package com.redhat.devtools.intellij.tektoncd.tkn;

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdNodeBasedDeserializer;
import com.fasterxml.jackson.databind.type.TypeFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

public class TaskRunDeserializer extends StdNodeBasedDeserializer<List<TaskRun>> {
    public TaskRunDeserializer() {
        super(TypeFactory.defaultInstance().constructCollectionType(List.class, TaskRun.class));
    }

    @Override
    public List<TaskRun> convert(JsonNode root, DeserializationContext ctxt) throws IOException {
        List<TaskRun> result = new ArrayList<>();
        JsonNode items = root.get("items");
        if (items != null) {
            for (Iterator<JsonNode> it = items.iterator(); it.hasNext(); ) {
                JsonNode item = it.next();
                String name = item.get("metadata").get("name").asText();
                Optional<Boolean> completed = Optional.empty();
                JsonNode conditions = item.get("status").get("conditions");
                if (conditions.isArray() && conditions.size() > 0) {
                    String status = conditions.get(0).get("status").asText();
                    if (status.equalsIgnoreCase("true")) {
                        completed = Optional.of(true);
                    } else if (status.equalsIgnoreCase("false")) {
                        completed = Optional.of(false);
                    }
                }
                result.add(TaskRun.of(name, completed));
            }
        }
        return result;
    }
}
