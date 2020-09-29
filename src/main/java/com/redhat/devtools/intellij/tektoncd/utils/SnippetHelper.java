package com.redhat.devtools.intellij.tektoncd.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.redhat.devtools.intellij.common.utils.JSONHelper;
import com.redhat.devtools.intellij.common.utils.YAMLHelper;
import com.redhat.devtools.intellij.tektoncd.completion.TknDictionary;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class SnippetHelper {
    private static final URL SNIPPETS_URL = TknDictionary.class.getResource("/tknsnippets.json");
    private static final URL TRIGGER_BINDING_SNIPPETS_URL = TknDictionary.class.getResource("/triggerBindingSnippets.json");
    private static final String[] TRIGGER_BINDING_TEMPLATES = new String[] { "github-pr-binding", "gitlab-push-binding", "message-binding", "pipeline-binding", "empty-binding" };

    public static JsonNode getSnippetJSON() throws IOException {
        return JSONHelper.getJSONFromURL(SNIPPETS_URL);
    }

    public static JsonNode getSnippetJSON(URL snippets) throws IOException {
        return JSONHelper.getJSONFromURL(snippets);
    }

    public static String getBody(String snippet) throws IOException {
        String yaml = YAMLHelper.JSONToYAML(SnippetHelper.getSnippetJSON(SNIPPETS_URL).get(snippet).get("body"));
        return yaml.replaceAll("\"\n", "\n").replaceAll("- \"", "");
    }

    private static String getBodyTriggerBindings(String snippet) throws IOException {
        String yaml = YAMLHelper.JSONToYAML(SnippetHelper.getSnippetJSON(TRIGGER_BINDING_SNIPPETS_URL).get(snippet).get("body"));
        return yaml.replaceAll("\"\n", "\n").replaceAll("- \"", "");
    }

    public static Map<String, String> getTriggerBindingTemplates() {
        Map<String, String> triggerBindingTemplates = new HashMap<>();
        Arrays.stream(TRIGGER_BINDING_TEMPLATES).forEach(triggerBinding -> {
            try {
                String body = getBodyTriggerBindings(triggerBinding);
                triggerBindingTemplates.put(triggerBinding, body);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        return triggerBindingTemplates;
    }
}
