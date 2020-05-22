/*******************************************************************************
 * Copyright (c) 2020 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package com.redhat.devtools.intellij.tektoncd.utils;

import com.redhat.devtools.intellij.tektoncd.tkn.Resource;
import com.redhat.devtools.intellij.tektoncd.tkn.component.field.Input;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class StartResourceModelTest {
    private String load(String name) throws IOException {
        return IOUtils.toString(StartResourceModelTest.class.getResource("/" + name), StandardCharsets.UTF_8);
    }

    @Test
    public void checkEmptyTask() throws IOException {
        String content = load("task1.yaml");
        StartResourceModel model = new StartResourceModel(content, Collections.emptyList(), Collections.emptyList());
        assertTrue(model.getServiceAccounts().isEmpty());
        assertTrue(model.getTaskServiceAccounts().isEmpty());
        assertTrue(model.getInputs().isEmpty());
        assertTrue(model.getOutputs().isEmpty());
    }

    @Test
    public void checkSingleServiceNameTask() throws IOException {
        String content = load("task1.yaml");
        StartResourceModel model = new StartResourceModel(content, Collections.emptyList(), Arrays.asList("serviceName1"));
        assertFalse(model.getServiceAccounts().isEmpty());
        assertEquals(1, model.getServiceAccounts().size());
        assertTrue(model.getTaskServiceAccounts().isEmpty());
        assertTrue(model.getInputs().isEmpty());
        assertTrue(model.getOutputs().isEmpty());
    }

    @Test
    public void checkSingleServiceNamePipeline() throws IOException {
        String content = load("pipeline1.yaml");
        StartResourceModel model = new StartResourceModel(content, Collections.emptyList(), Arrays.asList("serviceName1"));
        assertFalse(model.getServiceAccounts().isEmpty());
        assertEquals(1, model.getServiceAccounts().size());
        assertFalse(model.getTaskServiceAccounts().isEmpty());
        assertEquals(1, model.getTaskServiceAccounts().size());
        assertTrue(model.getTaskServiceAccounts().containsKey("step1"));
        assertTrue(model.getTaskServiceAccounts().get("step1").isEmpty());
        assertTrue(model.getInputs().isEmpty());
        assertTrue(model.getOutputs().isEmpty());
    }

    @Test
    public void checkSingleInputParameter() throws IOException {
        String content = load("task2.yaml");
        StartResourceModel model = new StartResourceModel(content, Collections.emptyList(), Collections.emptyList());
        assertTrue(model.getServiceAccounts().isEmpty());
        assertTrue(model.getTaskServiceAccounts().isEmpty());
        assertFalse(model.getInputs().isEmpty());
        assertEquals(1, model.getInputs().size());
        assertEquals("parm1", model.getInputs().get(0).name());
        assertEquals("string", model.getInputs().get(0).type());
        assertEquals(Input.Kind.PARAMETER, model.getInputs().get(0).kind());
        assertFalse(model.getInputs().get(0).defaultValue().isPresent());
        assertFalse(model.getInputs().get(0).description().isPresent());
        assertTrue(model.getOutputs().isEmpty());
    }

    @Test
    public void checkServiceNameMultipleTasksPipeline() throws IOException {
        String content = load("pipeline2.yaml");
        StartResourceModel model = new StartResourceModel(content, Collections.emptyList(), Arrays.asList("serviceName1"));
        assertFalse(model.getServiceAccounts().isEmpty());
        assertEquals(1, model.getServiceAccounts().size());
        assertFalse(model.getTaskServiceAccounts().isEmpty());
        assertEquals(2, model.getTaskServiceAccounts().size());
        assertTrue(model.getTaskServiceAccounts().containsKey("step1"));
        assertTrue(model.getTaskServiceAccounts().get("step1").isEmpty());
        assertTrue(model.getTaskServiceAccounts().containsKey("step2"));
        assertTrue(model.getTaskServiceAccounts().get("step2").isEmpty());
        assertTrue(model.getInputs().isEmpty());
        assertTrue(model.getOutputs().isEmpty());
    }

    @Test
    public void checkSingleInputParameterWithDefault() throws IOException {
        String content = load("task3.yaml");
        StartResourceModel model = new StartResourceModel(content, Collections.emptyList(), Collections.emptyList());
        assertTrue(model.getServiceAccounts().isEmpty());
        assertTrue(model.getTaskServiceAccounts().isEmpty());
        assertFalse(model.getInputs().isEmpty());
        assertEquals(1, model.getInputs().size());
        assertEquals("parm1", model.getInputs().get(0).name());
        assertEquals("string", model.getInputs().get(0).type());
        assertEquals(Input.Kind.PARAMETER, model.getInputs().get(0).kind());
        assertTrue(model.getInputs().get(0).defaultValue().isPresent());
        assertEquals("default value", model.getInputs().get(0).defaultValue().get());
        assertFalse(model.getInputs().get(0).description().isPresent());
        assertTrue(model.getOutputs().isEmpty());
    }

    @Test
    public void checkSingleInputParameterWithDescription() throws IOException {
        String content = load("task4.yaml");
        StartResourceModel model = new StartResourceModel(content, Collections.emptyList(), Collections.emptyList());
        assertTrue(model.getServiceAccounts().isEmpty());
        assertTrue(model.getTaskServiceAccounts().isEmpty());
        assertFalse(model.getInputs().isEmpty());
        assertEquals(1, model.getInputs().size());
        assertEquals("parm1", model.getInputs().get(0).name());
        assertEquals("string", model.getInputs().get(0).type());
        assertEquals(Input.Kind.PARAMETER, model.getInputs().get(0).kind());
        assertFalse(model.getInputs().get(0).defaultValue().isPresent());
        assertTrue(model.getInputs().get(0).description().isPresent());
        assertEquals("description", model.getInputs().get(0).description().get());
        assertTrue(model.getOutputs().isEmpty());
    }

    @Test
    public void checkMultipleInputParameter() throws IOException {
        String content = load("task5.yaml");
        StartResourceModel model = new StartResourceModel(content, Collections.emptyList(), Collections.emptyList());
        assertTrue(model.getServiceAccounts().isEmpty());
        assertTrue(model.getTaskServiceAccounts().isEmpty());
        assertFalse(model.getInputs().isEmpty());
        assertEquals(2, model.getInputs().size());
        assertEquals("parm1", model.getInputs().get(0).name());
        assertEquals(Input.Kind.PARAMETER, model.getInputs().get(0).kind());
        assertEquals("parm2", model.getInputs().get(1).name());
        assertEquals(Input.Kind.PARAMETER, model.getInputs().get(1).kind());
        assertTrue(model.getOutputs().isEmpty());
    }

    @Test
    public void checkSingleInputResource() throws IOException {
        String content = load("task6.yaml");
        StartResourceModel model = new StartResourceModel(content, Collections.emptyList(), Collections.emptyList());
        assertFalse(model.getInputs().isEmpty());
        assertEquals(1, model.getInputs().size());
        assertEquals("resource1", model.getInputs().get(0).name());
        assertEquals("git", model.getInputs().get(0).type());
        assertEquals(Input.Kind.RESOURCE, model.getInputs().get(0).kind());
        assertFalse(model.getInputs().get(0).defaultValue().isPresent());
        assertFalse(model.getInputs().get(0).description().isPresent());
        assertTrue(model.getOutputs().isEmpty());
    }

    @Test
    public void checkMultipleInputResource() throws IOException {
        String content = load("task7.yaml");
        StartResourceModel model = new StartResourceModel(content, Collections.emptyList(), Collections.emptyList());
        assertTrue(model.getServiceAccounts().isEmpty());
        assertTrue(model.getTaskServiceAccounts().isEmpty());
        assertFalse(model.getInputs().isEmpty());
        assertEquals(2, model.getInputs().size());
        assertEquals("resource1", model.getInputs().get(0).name());
        assertEquals(Input.Kind.RESOURCE, model.getInputs().get(0).kind());
        assertEquals("resource2", model.getInputs().get(1).name());
        assertEquals(Input.Kind.RESOURCE, model.getInputs().get(1).kind());
        assertTrue(model.getOutputs().isEmpty());
    }

    @Test
    public void checkSingleOutputResource() throws IOException {
        String content = load("task8.yaml");
        StartResourceModel model = new StartResourceModel(content, Collections.emptyList(), Collections.emptyList());
        assertTrue(model.getServiceAccounts().isEmpty());
        assertTrue(model.getTaskServiceAccounts().isEmpty());
        assertTrue(model.getInputs().isEmpty());
        assertFalse(model.getOutputs().isEmpty());
        assertEquals(1, model.getOutputs().size());
        assertEquals("resource1", model.getOutputs().get(0).name());
        assertEquals("image", model.getOutputs().get(0).type());
        assertNull(model.getOutputs().get(0).value());
    }

    @Test
    public void checkMultipleOutputResource() throws IOException {
        String content = load("task9.yaml");
        StartResourceModel model = new StartResourceModel(content, Collections.emptyList(), Collections.emptyList());
        assertTrue(model.getServiceAccounts().isEmpty());
        assertTrue(model.getTaskServiceAccounts().isEmpty());
        assertTrue(model.getInputs().isEmpty());
        assertFalse(model.getOutputs().isEmpty());
        assertEquals(2, model.getOutputs().size());
        assertEquals("resource1", model.getOutputs().get(0).name());
        assertEquals("image", model.getOutputs().get(0).type());
        assertNull(model.getOutputs().get(0).value());
        assertEquals("resource2", model.getOutputs().get(1).name());
        assertEquals("image", model.getOutputs().get(1).type());
        assertNull(model.getOutputs().get(1).value());
    }

    @Test
    public void checkMultipleOutputResourceWithResources() throws IOException {
        String content = load("task9.yaml");
        StartResourceModel model = new StartResourceModel(content, Arrays.asList(new Resource("resourceDefault1", "image")), Collections.emptyList());
        assertTrue(model.getServiceAccounts().isEmpty());
        assertTrue(model.getTaskServiceAccounts().isEmpty());
        assertTrue(model.getInputs().isEmpty());
        assertFalse(model.getOutputs().isEmpty());
        assertEquals(2, model.getOutputs().size());
        assertEquals("resource1", model.getOutputs().get(0).name());
        assertEquals("image", model.getOutputs().get(0).type());
        assertEquals("resourceDefault1", model.getOutputs().get(0).value());
        assertEquals("resource2", model.getOutputs().get(1).name());
        assertEquals("image", model.getOutputs().get(1).type());
        assertEquals("resourceDefault1", model.getOutputs().get(1).value());
    }


}
