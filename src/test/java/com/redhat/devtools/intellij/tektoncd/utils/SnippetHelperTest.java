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
package com.redhat.devtools.intellij.tektoncd.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.redhat.devtools.intellij.common.utils.JSONHelper;
import com.redhat.devtools.intellij.tektoncd.completion.TknDictionary;
import org.junit.Test;
import org.mockito.MockedStatic;

import java.io.IOException;
import java.net.URL;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;

public class SnippetHelperTest {

    @Test
    public void checkRightFileIsCalledOnGetSnippet() throws IOException {
        URL snippetsUrl = TknDictionary.class.getResource("/tknsnippets.json");
        JsonNode preResultNode = JSONHelper.getJSONFromURL(snippetsUrl);

        MockedStatic<JSONHelper> jsonHelper = mockStatic(JSONHelper.class);
        jsonHelper.when(() -> JSONHelper.getJSONFromURL(any())).thenReturn(preResultNode);

        JsonNode result = SnippetHelper.getSnippetJSON();

        jsonHelper.verify(() -> JSONHelper.getJSONFromURL(eq(snippetsUrl)));
        jsonHelper.close();
        assertEquals(result, preResultNode);
    }

    @Test
    public void checkRightFileIsCalledOnGetSnippetWithParam() throws IOException {
        URL triggerBindingSnippetsUrl = TknDictionary.class.getResource("/triggerBindingSnippets.json");
        JsonNode preResultNode = JSONHelper.getJSONFromURL(triggerBindingSnippetsUrl);

        MockedStatic<JSONHelper> jsonHelper = mockStatic(JSONHelper.class);
        jsonHelper.when(() -> JSONHelper.getJSONFromURL(any())).thenReturn(preResultNode);

        JsonNode result = SnippetHelper.getSnippetJSON(triggerBindingSnippetsUrl);

        jsonHelper.verify(() -> JSONHelper.getJSONFromURL(eq(triggerBindingSnippetsUrl)));
        jsonHelper.close();
        assertEquals(result, preResultNode);
    }

    @Test
    public void checkAllBindingTemplatesAreRetrieved() throws IOException {
        URL triggerBindingSnippetsUrl = TknDictionary.class.getResource("/triggerBindingSnippets.json");
        JsonNode resultNode = JSONHelper.getJSONFromURL(triggerBindingSnippetsUrl);

        MockedStatic<JSONHelper> jsonHelper = mockStatic(JSONHelper.class);
        jsonHelper.when(() -> JSONHelper.getJSONFromURL(any())).thenReturn(resultNode);

        Map<String, String> bindingTemplates = SnippetHelper.getTriggerBindingTemplates("triggers.tekton.dev/v1beta1");

        assertTrue(bindingTemplates.containsKey("github-pullreq"));
        assertTrue(bindingTemplates.containsKey("github-push"));
        assertTrue(bindingTemplates.containsKey("github-pullreq-review-comment"));
        assertTrue(bindingTemplates.containsKey("gitlab-pullreq"));
        assertTrue(bindingTemplates.containsKey("gitlab-push"));
        assertTrue(bindingTemplates.containsKey("gitlab-pullreq-review-comment"));
        assertTrue(bindingTemplates.containsKey("message-binding"));
        assertTrue(bindingTemplates.containsKey("pipeline-binding"));
        assertTrue(bindingTemplates.containsKey("empty-binding"));
        assertEquals(bindingTemplates.size(), 9);
        jsonHelper.verify(() -> JSONHelper.getJSONFromURL(eq(triggerBindingSnippetsUrl)));
        jsonHelper.close();
    }

    @Test
    public void checkNoBindingTemplatesAreRetrievedIfFileNotExists() throws IOException {
        MockedStatic<JSONHelper> jsonHelper = mockStatic(JSONHelper.class);
        jsonHelper.when(() -> JSONHelper.getJSONFromURL(any())).thenReturn(null);

        Map<String, String> bindingTemplates = SnippetHelper.getTriggerBindingTemplates("triggers.tekton.dev/v1beta1");

        assertTrue(bindingTemplates.isEmpty());
        jsonHelper.close();
    }
}
