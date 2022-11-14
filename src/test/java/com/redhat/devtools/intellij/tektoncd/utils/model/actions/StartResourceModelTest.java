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
package com.redhat.devtools.intellij.tektoncd.utils.model.actions;

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
        StartResourceModel model = new StartResourceModel(content, Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        assertTrue(model.getServiceAccounts().isEmpty());
        assertTrue(model.getTaskServiceAccounts().isEmpty());
        assertTrue(model.getRunPrefixName().isEmpty());
    }

    @Test
    public void checkSingleServiceNameTask() throws IOException {
        String content = load("task1.yaml");
        StartResourceModel model = new StartResourceModel(content, Arrays.asList("serviceName1"), Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        assertFalse(model.getServiceAccounts().isEmpty());
        assertEquals(1, model.getServiceAccounts().size());
        assertTrue(model.getTaskServiceAccounts().isEmpty());
        assertTrue(model.getRunPrefixName().isEmpty());
    }

    @Test
    public void checkSingleServiceNamePipeline() throws IOException {
        String content = load("pipeline1.yaml");
        StartResourceModel model = new StartResourceModel(content, Arrays.asList("serviceName1"), Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        assertFalse(model.getServiceAccounts().isEmpty());
        assertEquals(1, model.getServiceAccounts().size());
        assertFalse(model.getTaskServiceAccounts().isEmpty());
        assertEquals(1, model.getTaskServiceAccounts().size());
        assertTrue(model.getTaskServiceAccounts().containsKey("step1"));
        assertTrue(model.getTaskServiceAccounts().get("step1").isEmpty());
        assertTrue(model.getRunPrefixName().isEmpty());
    }

    @Test
    public void checkSingleInputParameter() throws IOException {
        String content = load("task2.yaml");
        StartResourceModel model = new StartResourceModel(content, Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        assertTrue(model.getServiceAccounts().isEmpty());
        assertTrue(model.getTaskServiceAccounts().isEmpty());
        assertFalse(model.getParams().isEmpty());
        assertEquals(1, model.getParams().size());
        assertEquals("parm1", model.getParams().get(0).name());
        assertEquals("string", model.getParams().get(0).type());
        assertEquals(Input.Kind.PARAMETER, model.getParams().get(0).kind());
        assertFalse(model.getParams().get(0).defaultValue().isPresent());
        assertFalse(model.getParams().get(0).description().isPresent());
        assertTrue(model.getRunPrefixName().isEmpty());
    }

    @Test
    public void checkServiceNameMultipleTasksPipeline() throws IOException {
        String content = load("pipeline2.yaml");
        StartResourceModel model = new StartResourceModel(content, Arrays.asList("serviceName1"), Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        assertFalse(model.getServiceAccounts().isEmpty());
        assertEquals(1, model.getServiceAccounts().size());
        assertFalse(model.getTaskServiceAccounts().isEmpty());
        assertEquals(2, model.getTaskServiceAccounts().size());
        assertTrue(model.getTaskServiceAccounts().containsKey("step1"));
        assertTrue(model.getTaskServiceAccounts().get("step1").isEmpty());
        assertTrue(model.getTaskServiceAccounts().containsKey("step2"));
        assertTrue(model.getTaskServiceAccounts().get("step2").isEmpty());
        assertTrue(model.getRunPrefixName().isEmpty());
    }

    @Test
    public void checkServiceNameMultipleTasksAndFinallyPipeline() throws IOException {
        String content = load("pipeline7.yaml");
        StartResourceModel model = new StartResourceModel(content, Arrays.asList("serviceName1"), Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        assertFalse(model.getServiceAccounts().isEmpty());
        assertEquals(1, model.getServiceAccounts().size());
        assertFalse(model.getTaskServiceAccounts().isEmpty());
        assertEquals(3, model.getTaskServiceAccounts().size());
        assertTrue(model.getTaskServiceAccounts().containsKey("step1"));
        assertTrue(model.getTaskServiceAccounts().get("step1").isEmpty());
        assertTrue(model.getTaskServiceAccounts().containsKey("step2"));
        assertTrue(model.getTaskServiceAccounts().get("step2").isEmpty());
        assertTrue(model.getTaskServiceAccounts().containsKey("step3"));
        assertTrue(model.getTaskServiceAccounts().get("step3").isEmpty());
    }

    @Test
    public void checkSingleInputParameterWithDefault() throws IOException {
        String content = load("task3.yaml");
        StartResourceModel model = new StartResourceModel(content, Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        assertTrue(model.getServiceAccounts().isEmpty());
        assertTrue(model.getTaskServiceAccounts().isEmpty());
        assertFalse(model.getParams().isEmpty());
        assertEquals(1, model.getParams().size());
        assertEquals("parm1", model.getParams().get(0).name());
        assertEquals("string", model.getParams().get(0).type());
        assertEquals(Input.Kind.PARAMETER, model.getParams().get(0).kind());
        assertTrue(model.getParams().get(0).defaultValue().isPresent());
        assertEquals("default value", model.getParams().get(0).defaultValue().get());
        assertFalse(model.getParams().get(0).description().isPresent());
        assertTrue(model.getRunPrefixName().isEmpty());
    }

    @Test
    public void checkSingleInputParameterWithDescription() throws IOException {
        String content = load("task4.yaml");
        StartResourceModel model = new StartResourceModel(content, Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        assertTrue(model.getServiceAccounts().isEmpty());
        assertTrue(model.getTaskServiceAccounts().isEmpty());
        assertFalse(model.getParams().isEmpty());
        assertEquals(1, model.getParams().size());
        assertEquals("parm1", model.getParams().get(0).name());
        assertEquals("string", model.getParams().get(0).type());
        assertEquals(Input.Kind.PARAMETER, model.getParams().get(0).kind());
        assertFalse(model.getParams().get(0).defaultValue().isPresent());
        assertTrue(model.getParams().get(0).description().isPresent());
        assertEquals("description", model.getParams().get(0).description().get());
        assertTrue(model.getRunPrefixName().isEmpty());
    }

    @Test
    public void checkMultipleInputParameter() throws IOException {
        String content = load("task5.yaml");
        StartResourceModel model = new StartResourceModel(content, Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        assertTrue(model.getServiceAccounts().isEmpty());
        assertTrue(model.getTaskServiceAccounts().isEmpty());
        assertFalse(model.getParams().isEmpty());
        assertEquals(2, model.getParams().size());
        assertEquals("parm1", model.getParams().get(0).name());
        assertEquals(Input.Kind.PARAMETER, model.getParams().get(0).kind());
        assertEquals("parm2", model.getParams().get(1).name());
        assertEquals(Input.Kind.PARAMETER, model.getParams().get(1).kind());
        assertTrue(model.getRunPrefixName().isEmpty());
    }

    @Test
    public void checkTaskWithWorkspaces() throws IOException {
        String content = load("task10.yaml");
        StartResourceModel model = new StartResourceModel(content, Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        assertEquals(2, model.getWorkspaces().size());
        assertTrue(model.getServiceAccounts().isEmpty());
        assertTrue(model.getTaskServiceAccounts().isEmpty());
        assertTrue(model.getRunPrefixName().isEmpty());
    }

    @Test
    public void checkTaskWithWorkspaceAndInputParameter() throws IOException {
        String content = load("task11.yaml");
        StartResourceModel model = new StartResourceModel(content, Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        assertTrue(model.getServiceAccounts().isEmpty());
        assertTrue(model.getTaskServiceAccounts().isEmpty());
        assertFalse(model.getParams().isEmpty());
        assertEquals(1, model.getParams().size());
        assertEquals("parm1", model.getParams().get(0).name());
        assertEquals("string", model.getParams().get(0).type());
        assertEquals(Input.Kind.PARAMETER, model.getParams().get(0).kind());
        assertFalse(model.getParams().get(0).defaultValue().isPresent());
        assertFalse(model.getParams().get(0).description().isPresent());
        assertEquals(2, model.getWorkspaces().size());
        assertTrue(model.getWorkspaces().containsKey("write-allowed"));
        assertTrue(model.getWorkspaces().containsKey("write-disallowed"));
        assertNull(model.getWorkspaces().get(0));
        assertNull(model.getWorkspaces().get(1));
        assertTrue(model.getRunPrefixName().isEmpty());
    }

    @Test
    public void checkTaskWithWorkspaceAndInputResourceParameter() throws IOException {
        String content = load("task12.yaml");
        StartResourceModel model = new StartResourceModel(content, Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        assertEquals(2, model.getWorkspaces().size());
        assertTrue(model.getWorkspaces().containsKey("write-allowed"));
        assertTrue(model.getWorkspaces().containsKey("write-disallowed"));
        assertNull(model.getWorkspaces().get(0));
        assertNull(model.getWorkspaces().get(1));
        assertTrue(model.getRunPrefixName().isEmpty());
    }

    @Test
    public void checkTaskWithWorkspaceAndOutputResourceParameter() throws IOException {
        String content = load("task13.yaml");
        StartResourceModel model = new StartResourceModel(content, Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        assertTrue(model.getServiceAccounts().isEmpty());
        assertTrue(model.getTaskServiceAccounts().isEmpty());
        assertEquals(2, model.getWorkspaces().size());
        assertTrue(model.getWorkspaces().containsKey("write-allowed"));
        assertTrue(model.getWorkspaces().containsKey("write-disallowed"));
        assertNull(model.getWorkspaces().get(0));
        assertNull(model.getWorkspaces().get(1));
        assertTrue(model.getRunPrefixName().isEmpty());
    }

    @Test
    public void checkTaskWithMultipleInputs() throws IOException {
        String content = load("task14.yaml");
        StartResourceModel model = new StartResourceModel(content, Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        assertTrue(model.getServiceAccounts().isEmpty());
        assertTrue(model.getTaskServiceAccounts().isEmpty());
        assertFalse(model.getParams().isEmpty());
        assertEquals(1, model.getParams().size());
        assertEquals("parm1", model.getParams().get(0).name());
        assertEquals("string", model.getParams().get(0).type());
        assertEquals(Input.Kind.PARAMETER, model.getParams().get(0).kind());
        assertFalse(model.getParams().get(0).defaultValue().isPresent());
        assertFalse(model.getParams().get(0).description().isPresent());
        assertFalse(model.getWorkspaces().isEmpty());
        assertEquals(2, model.getWorkspaces().size());
        assertTrue(model.getWorkspaces().containsKey("write-allowed"));
        assertTrue(model.getWorkspaces().containsKey("write-disallowed"));
        assertNull(model.getWorkspaces().get(0));
        assertNull(model.getWorkspaces().get(1));
        assertTrue(model.getRunPrefixName().isEmpty());
    }

    @Test
    public void checkPrefixNameIsCorrectlySet() throws IOException {
        String content = load("task14.yaml");
        StartResourceModel model = new StartResourceModel(content, Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        assertTrue(model.getRunPrefixName().isEmpty());
        model.setRunPrefixName("prefix");
        assertEquals(model.getRunPrefixName(), "prefix");
    }
}
