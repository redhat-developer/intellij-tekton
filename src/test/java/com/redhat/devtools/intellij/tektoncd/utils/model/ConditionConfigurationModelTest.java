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
package com.redhat.devtools.intellij.tektoncd.utils.model;

import com.redhat.devtools.intellij.tektoncd.BaseTest;
import com.redhat.devtools.intellij.tektoncd.utils.model.resources.ConditionConfigurationModel;
import java.io.IOException;
import org.junit.Test;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ConditionConfigurationModelTest extends BaseTest {

    @Test
    public void checkEmptyConditionModel() throws IOException {
        String configuration = load("condition.yaml");
        ConditionConfigurationModel model = (ConditionConfigurationModel) ConfigurationModelFactory.getModel(configuration);
        assertEquals(model.getName(), "foo");
        assertEquals(model.getNamespace(), "tekton");
        assertEquals(model.getKind(), "Condition");
        assertTrue(model.getParams().isEmpty());
        assertTrue(model.getInputResources().isEmpty());
        assertTrue(model.getOutputResources().isEmpty());
        assertTrue(model.getWorkspaces().isEmpty());
    }

    @Test
    public void checkConditionModelWithParams() throws IOException {
        String configuration = load("condition1.yaml");
        ConditionConfigurationModel model = (ConditionConfigurationModel) ConfigurationModelFactory.getModel(configuration);
        assertEquals(model.getName(), "foo");
        assertEquals(model.getNamespace(), "tekton");
        assertEquals(model.getKind(), "Condition");
        assertTrue(model.getParams().size() == 1);
        assertEquals(model.getParams().get(0).name(), "path");
        assertTrue(model.getInputResources().isEmpty());
        assertTrue(model.getOutputResources().isEmpty());
        assertTrue(model.getWorkspaces().isEmpty());
    }

    @Test
    public void checkConditionModelWithInputResources() throws IOException {
        String configuration = load("condition2.yaml");
        ConditionConfigurationModel model = (ConditionConfigurationModel) ConfigurationModelFactory.getModel(configuration);
        assertEquals(model.getName(), "foo");
        assertEquals(model.getNamespace(), "tekton");
        assertEquals(model.getKind(), "Condition");
        assertTrue(model.getParams().isEmpty());
        assertTrue(model.getInputResources().size() == 1);
        assertEquals(model.getInputResources().get(0).name(), "workspace");
        assertEquals(model.getInputResources().get(0).type(), "git");
        assertTrue(model.getOutputResources().isEmpty());
        assertTrue(model.getWorkspaces().isEmpty());
    }

    @Test
    public void checkConditionModelWithMultipleInputs() throws IOException {
        String configuration = load("condition3.yaml");
        ConditionConfigurationModel model = (ConditionConfigurationModel) ConfigurationModelFactory.getModel(configuration);
        assertEquals(model.getName(), "foo");
        assertEquals(model.getNamespace(), "tekton");
        assertEquals(model.getKind(), "Condition");
        assertTrue(model.getParams().size() == 1);
        assertEquals(model.getParams().get(0).name(), "path");
        assertTrue(model.getInputResources().size() == 1);
        assertEquals(model.getInputResources().get(0).name(), "workspace");
        assertEquals(model.getInputResources().get(0).type(), "git");
        assertTrue(model.getOutputResources().isEmpty());
        assertTrue(model.getWorkspaces().isEmpty());
    }
}
