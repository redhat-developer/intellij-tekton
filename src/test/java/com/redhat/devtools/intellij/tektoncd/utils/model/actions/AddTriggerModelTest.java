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
package com.redhat.devtools.intellij.tektoncd.utils.model.actions;

import com.redhat.devtools.intellij.tektoncd.BaseTest;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class AddTriggerModelTest extends BaseTest {

    private final static String MODEL_ACTIONS_FOLDER = "utils/model/actions/";

    public void testCheckNoVariablesIfNoBindings() throws IOException {
        Map<String, String> bindingsAvailableOnCluster = new HashMap<>();
        String content = load("task1.yaml");

        AddTriggerModel model = new AddTriggerModel(content, Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), bindingsAvailableOnCluster);
        Set<String> variables = model.extractVariablesFromSelectedBindings();

        assertTrue(model.getBindingsSelectedByUser().isEmpty());
        assertTrue(variables.isEmpty());
    }

    public void testCheckVariablesAreExtractedFromBinding() throws IOException {
        Map<String, String> bindingsAvailableOnCluster = new HashMap<>();
        String content = load("task1.yaml");
        String binding1 = load(MODEL_ACTIONS_FOLDER + "binding1.yaml");

        AddTriggerModel model = new AddTriggerModel(content, Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), bindingsAvailableOnCluster);
        model.getBindingsSelectedByUser().put("binding1", binding1);
        Set<String> variables = model.extractVariablesFromSelectedBindings();

        assertEquals(model.getBindingsSelectedByUser().size(), 1);
        assertFalse(variables.isEmpty());
        assertEquals(variables.size(), 2);
        assertTrue(variables.contains("gitrevision"));
        assertTrue(variables.contains("gitrepositoryurl"));
    }

    public void testCheckVariablesAreExtractedFromTwoBindings() throws IOException {
        Map<String, String> bindingsAvailableOnCluster = new HashMap<>();
        String content = load("task1.yaml");
        String binding1 = load(MODEL_ACTIONS_FOLDER + "binding1.yaml");
        String binding2 = load(MODEL_ACTIONS_FOLDER + "binding2.yaml");

        AddTriggerModel model = new AddTriggerModel(content, Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), bindingsAvailableOnCluster);
        model.getBindingsSelectedByUser().put("binding1", binding1);
        model.getBindingsSelectedByUser().put("binding2", binding2);
        Set<String> variables = model.extractVariablesFromSelectedBindings();

        assertEquals(model.getBindingsSelectedByUser().size(), 2);
        assertFalse(variables.isEmpty());
        assertEquals(variables.size(), 3);
        assertTrue(variables.contains("gitrevision"));
        assertTrue(variables.contains("gitrepositoryurl"));
        assertTrue(variables.contains("contenttype"));
    }

    public void testCheckVariablesAreExtractedFromTwoBindingsAndNewAdded() throws IOException {
        Map<String, String> bindingsAvailableOnCluster = new HashMap<>();
        String content = load("task1.yaml");
        String binding1 = load(MODEL_ACTIONS_FOLDER + "binding1.yaml");
        String binding2 = load(MODEL_ACTIONS_FOLDER + "binding2.yaml");
        String binding3 = load(MODEL_ACTIONS_FOLDER + "binding3.yaml");

        AddTriggerModel model = new AddTriggerModel(content, Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), bindingsAvailableOnCluster);
        model.getBindingsSelectedByUser().put("binding1", binding1);
        model.getBindingsSelectedByUser().put("binding2", binding2);
        model.getBindingsSelectedByUser().put("binding3 NEW", binding3);
        Set<String> variables = model.extractVariablesFromSelectedBindings();

        assertEquals(model.getBindingsSelectedByUser().size(), 3);
        assertFalse(variables.isEmpty());
        assertEquals(variables.size(), 4);
        assertTrue(variables.contains("gitrevision"));
        assertTrue(variables.contains("gitrepositoryurl"));
        assertTrue(variables.contains("contenttype"));
        assertTrue(variables.contains("foo"));
    }


}
