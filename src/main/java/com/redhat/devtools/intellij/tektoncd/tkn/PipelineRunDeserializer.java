/*******************************************************************************
 * Copyright (c) 2020 Red Hat, Inc.
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
import com.redhat.devtools.intellij.common.utils.DateHelper;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

public class PipelineRunDeserializer extends StdNodeBasedDeserializer<List<PipelineRun>> {
    public PipelineRunDeserializer() {
        super(TypeFactory.defaultInstance().constructCollectionType(List.class, PipelineRun.class));
    }

    @Override
    public List<PipelineRun> convert(JsonNode root, DeserializationContext ctxt) {
        List<PipelineRun> result = new ArrayList<>();
        JsonNode items = root.get("items");
        if (items != null) {
            for (Iterator<JsonNode> it = items.iterator(); it.hasNext(); ) {
                JsonNode item = it.next();
                String name = item.get("metadata").get("name").asText();
                Optional<Boolean> completed = Optional.empty();
                JsonNode conditions = item.get("status").get("conditions");
                String startTimeText = "running " + DateHelper.humanizeDate(item.get("status").get("startTime").asText());
                String type = item.get("status").get("conditions").get(0).get("type").asText();
                String typeStatus = item.get("status").get("conditions").get(0).get("status").asText();
                String completionTimeText = "";
                if (type.equals("Succeeded")  && typeStatus.equals("True")) {
                    completionTimeText = ", finished in " + DateHelper.humanizeDate(item.get("status").get("completionTime").asText());
                    startTimeText = startTimeText.replace("running", "started") + " ago";
                } else if (type.equals("Succeeded") && typeStatus.equals("False")) {
                    startTimeText = startTimeText.replace("running", "started") + " ago";
                }
                if (conditions.isArray() && conditions.size() > 0) {
                    String status = conditions.get(0).get("status").asText();
                    if (status.equalsIgnoreCase("true")) {
                        completed = Optional.of(true);
                    } else if (status.equalsIgnoreCase("false")) {
                        completed = Optional.of(false);
                    }
                }
                result.add(PipelineRun.of(name, completed, startTimeText, completionTimeText));
            }
        }
        return result;
    }
}
