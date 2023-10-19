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

    public void testCheckEmptyPipelineModel() throws IOException {
        String configuration = load("pipeline1.yaml");
        PipelineConfigurationModel model = (PipelineConfigurationModel) ConfigurationModelFactory.getModel(configuration);
        assertEquals(model.getName(), "foo");
        assertEquals(model.getNamespace(), "tekton");
        assertEquals(model.getKind(), "Pipeline");
        assertTrue(model.getParams().isEmpty());
        assertTrue(model.getWorkspaces().isEmpty());
    }

    public void testCheckPipelineModelWithParams() throws IOException {
        String configuration = load("pipeline3.yaml");
        PipelineConfigurationModel model = (PipelineConfigurationModel) ConfigurationModelFactory.getModel(configuration);
        assertEquals(model.getName(), "foo");
        assertEquals(model.getNamespace(), "tekton");
        assertEquals(model.getKind(), "Pipeline");
        assertEquals(1, model.getParams().size());
        assertEquals(model.getParams().get(0).name(), "param1");
        assertTrue(model.getWorkspaces().isEmpty());
    }

    public void testCheckPipelineModelWithInputResources() throws IOException {
        String configuration = load("pipeline4.yaml");
        PipelineConfigurationModel model = (PipelineConfigurationModel) ConfigurationModelFactory.getModel(configuration);
        assertEquals(model.getName(), "foo");
        assertEquals(model.getNamespace(), "tekton");
        assertEquals(model.getKind(), "Pipeline");
        assertTrue(model.getParams().isEmpty());
        assertTrue(model.getWorkspaces().isEmpty());
    }

    public void testCheckPipelineModelWithWorkspaces() throws IOException {
        String configuration = load("pipeline5.yaml");
        PipelineConfigurationModel model = (PipelineConfigurationModel) ConfigurationModelFactory.getModel(configuration);
        assertEquals(model.getName(), "foo");
        assertEquals(model.getNamespace(), "tekton");
        assertEquals(model.getKind(), "Pipeline");
        assertTrue(model.getParams().isEmpty());
        assertEquals(2, model.getWorkspaces().size());
        assertEquals(model.getWorkspaces().get(0).getName(), "password-vault");
        assertEquals(model.getWorkspaces().get(1).getName(), "recipe-store");
    }

    public void testCheckPipelineModelWithMultipleInputs() throws IOException {
        String configuration = load("pipeline6.yaml");
        PipelineConfigurationModel model = (PipelineConfigurationModel) ConfigurationModelFactory.getModel(configuration);
        assertEquals(model.getName(), "foo");
        assertEquals(model.getNamespace(), "tekton");
        assertEquals(model.getKind(), "Pipeline");
        assertEquals(1, model.getParams().size());
        assertEquals(model.getParams().get(0).name(), "path");
        assertEquals(2, model.getWorkspaces().size());
        assertEquals(model.getWorkspaces().get(0).getName(), "password-vault");
        assertEquals(model.getWorkspaces().get(1).getName(), "recipe-store");
    }
}
