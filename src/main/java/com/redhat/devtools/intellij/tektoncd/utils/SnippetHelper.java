package com.redhat.devtools.intellij.tektoncd.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.redhat.devtools.intellij.tektoncd.completion.TknDictionary;

import java.io.IOException;
import java.net.URL;

public class SnippetHelper {
    private static final URL SNIPPETS_URL = TknDictionary.class.getResource("/tknsnippets.json");

    public static JsonNode getSnippetJSON() throws IOException {
        return JSONHelper.getJSONFromFile(SNIPPETS_URL);
    }

    public static String getBody(String snippet) throws IOException {
        String yaml = JSONHelper.JSONToYAML(JSONHelper.getJSONFromFile(SNIPPETS_URL).get(snippet).get("body"));
        return yaml.replaceAll("\"\n", "\n").replaceAll("- \"", "");
    }
}
