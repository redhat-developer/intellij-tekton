package com.redhat.devtools.intellij.tektoncd.tkn;

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdNodeBasedDeserializer;
import com.fasterxml.jackson.databind.type.TypeFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ResourceDeserializer extends StdNodeBasedDeserializer<List<Resource>> {
    public ResourceDeserializer() {
        super(TypeFactory.defaultInstance().constructCollectionType(List.class, Resource.class));
    }

    @Override
    public List<Resource> convert(JsonNode root, DeserializationContext ctxt) throws IOException {
        List<Resource> result = new ArrayList<>();
        JsonNode items = root.get("items");
        if (items != null) {
            for (Iterator<JsonNode> it = items.iterator(); it.hasNext(); ) {
                JsonNode item = it.next();
                String name = item.get("metadata").get("name").asText();
                String type = item.get("spec").get("type").asText();
                result.add(new Resource(name, type));
            }
        }
        return result;
    }
}
