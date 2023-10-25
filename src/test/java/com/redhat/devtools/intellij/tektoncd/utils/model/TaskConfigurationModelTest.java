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
import com.redhat.devtools.intellij.tektoncd.utils.model.resources.TaskConfigurationModel;

import java.io.IOException;

public class TaskConfigurationModelTest extends BaseTest {

    public void testCheckEmptyTaskModel() throws IOException {
        String configuration = load("task1.yaml");
        TaskConfigurationModel model = (TaskConfigurationModel) ConfigurationModelFactory.getModel(configuration);
        assertEquals(model.getName(), "foo");
        assertEquals(model.getNamespace(), "tekton");
        assertEquals(model.getKind(), "Task");
        assertTrue(model.getParams().isEmpty());
        assertTrue(model.getWorkspaces().isEmpty());
    }

    public void testCheckTaskModelWithParams() throws IOException {
        String configuration = load("task2.yaml");
        TaskConfigurationModel model = (TaskConfigurationModel) ConfigurationModelFactory.getModel(configuration);
        assertEquals(model.getName(), "foo");
        assertEquals(model.getNamespace(), "tekton");
        assertEquals(model.getKind(), "Task");
        assertEquals(1, model.getParams().size());
        assertEquals(model.getParams().get(0).name(), "parm1");
        assertTrue(model.getWorkspaces().isEmpty());
    }

    public void testCheckTaskModelWithInputResources() throws IOException {
        String configuration = load("task7.yaml");
        TaskConfigurationModel model = (TaskConfigurationModel) ConfigurationModelFactory.getModel(configuration);
        assertEquals(model.getName(), "foo");
        assertEquals(model.getNamespace(), "tekton");
        assertEquals(model.getKind(), "Task");
        assertTrue(model.getParams().isEmpty());
        assertTrue(model.getWorkspaces().isEmpty());
    }

    public void testCheckTaskModelWithOutputResources() throws IOException {
        String configuration = load("task9.yaml");
        TaskConfigurationModel model = (TaskConfigurationModel) ConfigurationModelFactory.getModel(configuration);
        assertEquals(model.getName(), "foo");
        assertEquals(model.getNamespace(), "tekton");
        assertEquals(model.getKind(), "Task");
        assertTrue(model.getParams().isEmpty());
        assertTrue(model.getWorkspaces().isEmpty());
    }

    public void testCheckTaskModelWithWorkspaces() throws IOException {
        String configuration = load("task10.yaml");
        TaskConfigurationModel model = (TaskConfigurationModel) ConfigurationModelFactory.getModel(configuration);
        assertEquals(model.getName(), "foo");
        assertEquals(model.getNamespace(), "tekton");
        assertEquals(model.getKind(), "Task");
        assertTrue(model.getParams().isEmpty());
        assertEquals(2, model.getWorkspaces().size());
        assertEquals(model.getWorkspaces().get(0).getName(), "write-allowed");
        assertEquals(model.getWorkspaces().get(1).getName(), "write-disallowed");
    }

    public void testCheckTaskModelWithMultipleInputs() throws IOException {
        String configuration = load("task14.yaml");
        TaskConfigurationModel model = (TaskConfigurationModel) ConfigurationModelFactory.getModel(configuration);
        assertEquals(model.getName(), "foo");
        assertEquals(model.getNamespace(), "tekton");
        assertEquals(model.getKind(), "Task");
        assertEquals(1, model.getParams().size());
        assertEquals(model.getParams().get(0).name(), "parm1");
        assertEquals(2, model.getWorkspaces().size());
        assertEquals(model.getWorkspaces().get(0).getName(), "write-allowed");
        assertEquals(model.getWorkspaces().get(1).getName(), "write-disallowed");
    }

}
