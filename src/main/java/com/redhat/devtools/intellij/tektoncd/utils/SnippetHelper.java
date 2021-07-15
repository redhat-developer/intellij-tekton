package com.redhat.devtools.intellij.tektoncd.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.redhat.devtools.intellij.common.utils.JSONHelper;
import com.redhat.devtools.intellij.common.utils.YAMLHelper;
import com.redhat.devtools.intellij.tektoncd.completion.TknDictionary;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class SnippetHelper {
    private static final URL SNIPPETS_URL = TknDictionary.class.getResource("/tknsnippets.json");
    private static final URL TRIGGER_BINDING_SNIPPETS_URL = TknDictionary.class.getResource("/triggerBindingSnippets.json");

    public static JsonNode getSnippetJSON() throws IOException {
        return getSnippetJSON(SNIPPETS_URL);
    }

    public static JsonNode getSnippetJSON(URL snippets) throws IOException {
        return JSONHelper.getJSONFromURL(snippets);
    }

    public static String getBody(String snippet) throws IOException {
        return convertJSONBodyToYAML(SnippetHelper.getSnippetJSON(SNIPPETS_URL).get(snippet).get("body"));
    }

    private static String convertJSONBodyToYAML(JsonNode snippetBody) throws IOException {
        String yaml = YAMLHelper.JSONToYAML(snippetBody, false);
        return yaml.replaceAll("\"\n", "\n").replaceAll("- \"", "");
    }

    public static Map<String, String> getTriggerBindingTemplates() throws IOException {
        Map<String, String> triggerBindingTemplates = new HashMap<>();
        JsonNode bindingTemplatesNode = getSnippetJSON(TRIGGER_BINDING_SNIPPETS_URL);
        if (bindingTemplatesNode != null) {
            for (Iterator<Map.Entry<String, JsonNode>> it = bindingTemplatesNode.fields(); it.hasNext(); ) {
                Map.Entry<String, JsonNode> entry = it.next();
                String value = convertJSONBodyToYAML(entry.getValue().get("body"));
                triggerBindingTemplates.put(entry.getKey(), value);
            }
        }
        return triggerBindingTemplates;
    }
}
