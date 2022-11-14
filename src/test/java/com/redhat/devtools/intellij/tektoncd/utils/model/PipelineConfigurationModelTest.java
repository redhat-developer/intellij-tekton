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
import com.redhat.devtools.intellij.tektoncd.utils.model.resources.PipelineConfigurationModel;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PipelineConfigurationModelTest extends BaseTest {

    @Test
    public void checkEmptyPipelineModel() throws IOException {
        String configuration = load("pipeline1.yaml");
        PipelineConfigurationModel model = (PipelineConfigurationModel) ConfigurationModelFactory.getModel(configuration);
        assertEquals(model.getName(), "foo");
        assertEquals(model.getNamespace(), "tekton");
        assertEquals(model.getKind(), "Pipeline");
        assertTrue(model.getParams().isEmpty());
        assertTrue(model.getWorkspaces().isEmpty());
    }

    @Test
    public void checkPipelineModelWithParams() throws IOException {
        String configuration = load("pipeline3.yaml");
        PipelineConfigurationModel model = (PipelineConfigurationModel) ConfigurationModelFactory.getModel(configuration);
        assertEquals(model.getName(), "foo");
        assertEquals(model.getNamespace(), "tekton");
        assertEquals(model.getKind(), "Pipeline");
        assertTrue(model.getParams().size() == 1);
        assertEquals(model.getParams().get(0).name(), "param1");
        assertTrue(model.getWorkspaces().isEmpty());
    }

    @Test
    public void checkPipelineModelWithInputResources() throws IOException {
        String configuration = load("pipeline4.yaml");
        PipelineConfigurationModel model = (PipelineConfigurationModel) ConfigurationModelFactory.getModel(configuration);
        assertEquals(model.getName(), "foo");
        assertEquals(model.getNamespace(), "tekton");
        assertEquals(model.getKind(), "Pipeline");
        assertTrue(model.getParams().isEmpty());
        assertTrue(model.getWorkspaces().isEmpty());
    }

    @Test
    public void checkPipelineModelWithWorkspaces() throws IOException {
        String configuration = load("pipeline5.yaml");
        PipelineConfigurationModel model = (PipelineConfigurationModel) ConfigurationModelFactory.getModel(configuration);
        assertEquals(model.getName(), "foo");
        assertEquals(model.getNamespace(), "tekton");
        assertEquals(model.getKind(), "Pipeline");
        assertTrue(model.getParams().isEmpty());
        assertTrue(model.getWorkspaces().size() == 2);
        assertEquals(model.getWorkspaces().get(0).getName(), "password-vault");
        assertEquals(model.getWorkspaces().get(1).getName(), "recipe-store");
    }

    @Test
    public void checkPipelineModelWithMultipleInputs() throws IOException {
        String configuration = load("pipeline6.yaml");
        PipelineConfigurationModel model = (PipelineConfigurationModel) ConfigurationModelFactory.getModel(configuration);
        assertEquals(model.getName(), "foo");
        assertEquals(model.getNamespace(), "tekton");
        assertEquals(model.getKind(), "Pipeline");
        assertTrue(model.getParams().size() == 1);
        assertEquals(model.getParams().get(0).name(), "path");
        assertTrue(model.getWorkspaces().size() == 2);
        assertEquals(model.getWorkspaces().get(0).getName(), "password-vault");
        assertEquals(model.getWorkspaces().get(1).getName(), "recipe-store");
    }
}
