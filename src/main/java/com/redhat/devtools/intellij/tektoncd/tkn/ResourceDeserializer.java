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

public class ResourceDeserializer extends StdNodeBasedDeserializer<Resource> {
    public ResourceDeserializer() {
        super(Resource.class);
    }

    @Override
    public Resource convert(JsonNode root, DeserializationContext ctxt) {
        String name = root.get("metadata").get("name").asText();
        String type = root.get("spec").get("type").asText();
        return new Resource(name, type);
    }
}
