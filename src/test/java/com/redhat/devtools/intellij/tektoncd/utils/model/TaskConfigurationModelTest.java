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
import org.junit.Test;


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
        assertTrue(model.getInputResources().isEmpty());
        assertTrue(model.getOutputResources().isEmpty());
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
        assertTrue(model.getInputResources().isEmpty());
        assertTrue(model.getOutputResources().isEmpty());
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
        assertTrue(model.getInputResources().size() == 2);
        assertEquals(model.getInputResources().get(0).name(), "resource1");
        assertEquals(model.getInputResources().get(0).type(), "git");
        assertEquals(model.getInputResources().get(1).name(), "resource2");
        assertEquals(model.getInputResources().get(1).type(), "git");
        assertTrue(model.getOutputResources().isEmpty());
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
        assertTrue(model.getInputResources().isEmpty());
        assertTrue(model.getOutputResources().size() == 2);
        assertEquals(model.getOutputResources().get(0).name(), "resource1");
        assertEquals(model.getOutputResources().get(0).type(), "image");
        assertEquals(model.getOutputResources().get(1).name(), "resource2");
        assertEquals(model.getOutputResources().get(1).type(), "image");
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
        assertTrue(model.getInputResources().isEmpty());
        assertTrue(model.getOutputResources().isEmpty());
        assertTrue(model.getWorkspaces().size() == 2);
        assertEquals(model.getWorkspaces().get(0), "write-allowed");
        assertEquals(model.getWorkspaces().get(1), "write-disallowed");
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
        assertTrue(model.getInputResources().size() == 1);
        assertEquals(model.getInputResources().get(0).name(), "resource1");
        assertEquals(model.getInputResources().get(0).type(), "git");
        assertTrue(model.getOutputResources().size() == 2);
        assertEquals(model.getOutputResources().get(0).name(), "resource1");
        assertEquals(model.getOutputResources().get(0).type(), "image");
        assertEquals(model.getOutputResources().get(1).name(), "resource2");
        assertEquals(model.getOutputResources().get(1).type(), "image");
        assertTrue(model.getWorkspaces().size() == 2);
        assertEquals(model.getWorkspaces().get(0), "write-allowed");
        assertEquals(model.getWorkspaces().get(1), "write-disallowed");
    }

}
