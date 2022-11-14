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
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TaskConfigurationModelTest extends BaseTest {

    @Test
    public void checkEmptyTaskModel() throws IOException {
        String configuration = load("task1.yaml");
        TaskConfigurationModel model = (TaskConfigurationModel) ConfigurationModelFactory.getModel(configuration);
        assertEquals(model.getName(), "foo");
        assertEquals(model.getNamespace(), "tekton");
        assertEquals(model.getKind(), "Task");
        assertTrue(model.getParams().isEmpty());
        assertTrue(model.getWorkspaces().isEmpty());
    }

    @Test
    public void checkTaskModelWithParams() throws IOException {
        String configuration = load("task2.yaml");
        TaskConfigurationModel model = (TaskConfigurationModel) ConfigurationModelFactory.getModel(configuration);
        assertEquals(model.getName(), "foo");
        assertEquals(model.getNamespace(), "tekton");
        assertEquals(model.getKind(), "Task");
        assertTrue(model.getParams().size() == 1);
        assertEquals(model.getParams().get(0).name(), "parm1");
        assertTrue(model.getWorkspaces().isEmpty());
    }

    @Test
    public void checkTaskModelWithInputResources() throws IOException {
        String configuration = load("task7.yaml");
        TaskConfigurationModel model = (TaskConfigurationModel) ConfigurationModelFactory.getModel(configuration);
        assertEquals(model.getName(), "foo");
        assertEquals(model.getNamespace(), "tekton");
        assertEquals(model.getKind(), "Task");
        assertTrue(model.getParams().isEmpty());
        assertTrue(model.getWorkspaces().isEmpty());
    }

    @Test
    public void checkTaskModelWithOutputResources() throws IOException {
        String configuration = load("task9.yaml");
        TaskConfigurationModel model = (TaskConfigurationModel) ConfigurationModelFactory.getModel(configuration);
        assertEquals(model.getName(), "foo");
        assertEquals(model.getNamespace(), "tekton");
        assertEquals(model.getKind(), "Task");
        assertTrue(model.getParams().isEmpty());
        assertTrue(model.getWorkspaces().isEmpty());
    }

    @Test
    public void checkTaskModelWithWorkspaces() throws IOException {
        String configuration = load("task10.yaml");
        TaskConfigurationModel model = (TaskConfigurationModel) ConfigurationModelFactory.getModel(configuration);
        assertEquals(model.getName(), "foo");
        assertEquals(model.getNamespace(), "tekton");
        assertEquals(model.getKind(), "Task");
        assertTrue(model.getParams().isEmpty());
        assertTrue(model.getWorkspaces().size() == 2);
        assertEquals(model.getWorkspaces().get(0).getName(), "write-allowed");
        assertEquals(model.getWorkspaces().get(1).getName(), "write-disallowed");
    }

    @Test
    public void checkTaskModelWithMultipleInputs() throws IOException {
        String configuration = load("task14.yaml");
        TaskConfigurationModel model = (TaskConfigurationModel) ConfigurationModelFactory.getModel(configuration);
        assertEquals(model.getName(), "foo");
        assertEquals(model.getNamespace(), "tekton");
        assertEquals(model.getKind(), "Task");
        assertTrue(model.getParams().size() == 1);
        assertEquals(model.getParams().get(0).name(), "parm1");
        assertTrue(model.getWorkspaces().size() == 2);
        assertEquals(model.getWorkspaces().get(0).getName(), "write-allowed");
        assertEquals(model.getWorkspaces().get(1).getName(), "write-disallowed");
    }

}
