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

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.redhat.devtools.intellij.tektoncd.BaseTest;
import com.redhat.devtools.intellij.tektoncd.tkn.component.field.Workspace;
import com.redhat.devtools.intellij.tektoncd.utils.model.actions.AddTriggerModel;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import org.junit.Test;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class YAMLBuilderTest extends BaseTest {

    /////////////////////////////////////////////////////////
    ///             CREATE PIPELINERUN
    /////////////////////////////////////////////////////////

    @Test
    public void checkPipelineRunCreatedWithNoInputs() throws IOException {
        String content = load("pipeline1.yaml");
        AddTriggerModel model = new AddTriggerModel(content, Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyMap());

        ObjectNode pipelineRunNode = YAMLBuilder.createPipelineRun("pipeline", model);

        assertEquals(pipelineRunNode.get("apiVersion").asText(), "tekton.dev/v1beta1");
        assertEquals(pipelineRunNode.get("kind").asText(), "PipelineRun");
        assertEquals(pipelineRunNode.get("metadata").get("generateName").asText(), "pipeline-");
        assertEquals(pipelineRunNode.get("spec").get("pipelineRef").get("name").asText(), "pipeline");
        assertFalse(pipelineRunNode.get("spec").has("serviceAccountName"));
        assertFalse(pipelineRunNode.get("spec").has("serviceAccountNames"));
        assertFalse(pipelineRunNode.get("spec").has("params"));
        assertFalse(pipelineRunNode.get("spec").has("resources"));
        assertFalse(pipelineRunNode.get("spec").has("workspaces"));
    }

    @Test
    public void checkPipelineRunCreatedHasServiceAccount() throws IOException {
        String content = load("pipeline1.yaml");
        AddTriggerModel model = new AddTriggerModel(content, Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyMap());
        model.setServiceAccount("sa");

        ObjectNode pipelineRunNode = YAMLBuilder.createPipelineRun("pipeline", model);

        assertEquals(pipelineRunNode.get("apiVersion").asText(), "tekton.dev/v1beta1");
        assertEquals(pipelineRunNode.get("kind").asText(), "PipelineRun");
        assertEquals(pipelineRunNode.get("metadata").get("generateName").asText(), "pipeline-");
        assertEquals(pipelineRunNode.get("spec").get("pipelineRef").get("name").asText(), "pipeline");
        assertEquals(pipelineRunNode.get("spec").get("serviceAccountName").asText(), "sa");
        assertFalse(pipelineRunNode.get("spec").has("serviceAccountNames"));
        assertFalse(pipelineRunNode.get("spec").has("params"));
        assertFalse(pipelineRunNode.get("spec").has("resources"));
        assertFalse(pipelineRunNode.get("spec").has("workspaces"));
    }

    @Test
    public void checkPipelineRunCreatedHasParams() throws IOException {
        String content = load("pipeline3.yaml");
        AddTriggerModel model = new AddTriggerModel(content, Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyMap());

        ObjectNode pipelineRunNode = YAMLBuilder.createPipelineRun("pipeline", model);

        assertEquals(pipelineRunNode.get("apiVersion").asText(), "tekton.dev/v1beta1");
        assertEquals(pipelineRunNode.get("kind").asText(), "PipelineRun");
        assertEquals(pipelineRunNode.get("metadata").get("generateName").asText(), "pipeline-");
        assertEquals(pipelineRunNode.get("spec").get("pipelineRef").get("name").asText(), "pipeline");
        assertFalse(pipelineRunNode.get("spec").has("serviceAccountName"));
        assertFalse(pipelineRunNode.get("spec").has("serviceAccountNames"));
        assertTrue(pipelineRunNode.get("spec").has("params"));
        assertEquals(pipelineRunNode.get("spec").get("params").get(0).get("name").asText(), "param1");
        assertFalse(pipelineRunNode.get("spec").has("resources"));
        assertFalse(pipelineRunNode.get("spec").has("workspaces"));
    }

    @Test
    public void checkPipelineRunCreatedHasResource() throws IOException {
        String content = load("pipeline4.yaml");
        AddTriggerModel model = new AddTriggerModel(content, Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyMap());

        ObjectNode pipelineRunNode = YAMLBuilder.createPipelineRun("pipeline", model);

        assertEquals(pipelineRunNode.get("apiVersion").asText(), "tekton.dev/v1beta1");
        assertEquals(pipelineRunNode.get("kind").asText(), "PipelineRun");
        assertEquals(pipelineRunNode.get("metadata").get("generateName").asText(), "pipeline-");
        assertEquals(pipelineRunNode.get("spec").get("pipelineRef").get("name").asText(), "pipeline");
        assertFalse(pipelineRunNode.get("spec").has("serviceAccountName"));
        assertFalse(pipelineRunNode.get("spec").has("serviceAccountNames"));
        assertFalse(pipelineRunNode.get("spec").has("params"));
        assertTrue(pipelineRunNode.get("spec").has("resources"));
        assertEquals(pipelineRunNode.get("spec").get("resources").get(0).get("name").asText(), "resource1");
        assertFalse(pipelineRunNode.get("spec").has("workspaces"));
    }

    @Test
    public void checkPipelineRunCreatedHasWorkspace() throws IOException {
        String content = load("pipeline5.yaml");
        AddTriggerModel model = new AddTriggerModel(content, Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyMap());
        if (model.getWorkspaces().containsKey("password-vault")) {
            model.getWorkspaces().put("password-vault", new Workspace("foo1", Workspace.Kind.EMPTYDIR, "foo"));
        }
        if (model.getWorkspaces().containsKey("recipe-store")) {
            model.getWorkspaces().put("recipe-store", new Workspace("foo2", Workspace.Kind.EMPTYDIR, "foo"));
        }

        ObjectNode pipelineRunNode = YAMLBuilder.createPipelineRun("pipeline", model);

        assertEquals(pipelineRunNode.get("apiVersion").asText(), "tekton.dev/v1beta1");
        assertEquals(pipelineRunNode.get("kind").asText(), "PipelineRun");
        assertEquals(pipelineRunNode.get("metadata").get("generateName").asText(), "pipeline-");
        assertEquals(pipelineRunNode.get("spec").get("pipelineRef").get("name").asText(), "pipeline");
        assertFalse(pipelineRunNode.get("spec").has("serviceAccountName"));
        assertFalse(pipelineRunNode.get("spec").has("serviceAccountNames"));
        assertFalse(pipelineRunNode.get("spec").has("params"));
        assertFalse(pipelineRunNode.get("spec").has("resources"));
        assertTrue(pipelineRunNode.get("spec").has("workspaces"));
        assertEquals(pipelineRunNode.get("spec").get("workspaces").get(0).get("name").asText(), "foo1");
        assertEquals(pipelineRunNode.get("spec").get("workspaces").get(1).get("name").asText(), "foo2");
    }

    /////////////////////////////////////////////////////////
    ///             CREATE TASKRUN
    /////////////////////////////////////////////////////////

    @Test
    public void checkTaskRunCreatedWithNoInputs() throws IOException {
        String content = load("task1.yaml");
        AddTriggerModel model = new AddTriggerModel(content, Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyMap());

        ObjectNode taskRunNode = YAMLBuilder.createTaskRun("task", model);

        assertEquals(taskRunNode.get("apiVersion").asText(), "tekton.dev/v1beta1");
        assertEquals(taskRunNode.get("kind").asText(), "TaskRun");
        assertEquals(taskRunNode.get("metadata").get("generateName").asText(), "task-");
        assertEquals(taskRunNode.get("spec").get("taskRef").get("name").asText(), "task");
        assertFalse(taskRunNode.get("spec").has("serviceAccountName"));
        assertFalse(taskRunNode.get("spec").has("serviceAccountNames"));
        assertFalse(taskRunNode.get("spec").has("params"));
        assertFalse(taskRunNode.get("spec").has("resources"));
        assertFalse(taskRunNode.get("spec").has("workspaces"));
    }

    @Test
    public void checkTaskRunCreatedHasServiceAccount() throws IOException {
        String content = load("task1.yaml");
        AddTriggerModel model = new AddTriggerModel(content, Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyMap());
        model.setServiceAccount("sa");

        ObjectNode taskRunNode = YAMLBuilder.createTaskRun("task", model);

        assertEquals(taskRunNode.get("apiVersion").asText(), "tekton.dev/v1beta1");
        assertEquals(taskRunNode.get("kind").asText(), "TaskRun");
        assertEquals(taskRunNode.get("metadata").get("generateName").asText(), "task-");
        assertEquals(taskRunNode.get("spec").get("taskRef").get("name").asText(), "task");
        assertEquals(taskRunNode.get("spec").get("serviceAccountName").asText(), "sa");
        assertFalse(taskRunNode.get("spec").has("serviceAccountNames"));
        assertFalse(taskRunNode.get("spec").has("params"));
        assertFalse(taskRunNode.get("spec").has("resources"));
        assertFalse(taskRunNode.get("spec").has("workspaces"));
    }

    @Test
    public void checkTaskRunCreatedHasParams() throws IOException {
        String content = load("task3.yaml");
        AddTriggerModel model = new AddTriggerModel(content, Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyMap());

        ObjectNode taskRunNode = YAMLBuilder.createTaskRun("task", model);

        assertEquals(taskRunNode.get("apiVersion").asText(), "tekton.dev/v1beta1");
        assertEquals(taskRunNode.get("kind").asText(), "TaskRun");
        assertEquals(taskRunNode.get("metadata").get("generateName").asText(), "task-");
        assertEquals(taskRunNode.get("spec").get("taskRef").get("name").asText(), "task");
        assertFalse(taskRunNode.get("spec").has("serviceAccountName"));
        assertFalse(taskRunNode.get("spec").has("serviceAccountNames"));
        assertTrue(taskRunNode.get("spec").has("params"));
        assertEquals(taskRunNode.get("spec").get("params").get(0).get("name").asText(), "parm1");
        assertFalse(taskRunNode.get("spec").has("resources"));
        assertFalse(taskRunNode.get("spec").has("workspaces"));
    }

    @Test
    public void checkTaskRunCreatedHasInputResource() throws IOException {
        String content = load("task6.yaml");
        AddTriggerModel model = new AddTriggerModel(content, Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyMap());

        ObjectNode taskRunNode = YAMLBuilder.createTaskRun("task", model);

        assertEquals(taskRunNode.get("apiVersion").asText(), "tekton.dev/v1beta1");
        assertEquals(taskRunNode.get("kind").asText(), "TaskRun");
        assertEquals(taskRunNode.get("metadata").get("generateName").asText(), "task-");
        assertEquals(taskRunNode.get("spec").get("taskRef").get("name").asText(), "task");
        assertFalse(taskRunNode.get("spec").has("serviceAccountName"));
        assertFalse(taskRunNode.get("spec").has("serviceAccountNames"));
        assertFalse(taskRunNode.get("spec").has("params"));
        assertTrue(taskRunNode.get("spec").has("resources"));
        assertEquals(taskRunNode.get("spec").get("resources").get("inputs").get(0).get("name").asText(), "resource1");
        assertFalse(taskRunNode.get("spec").get("resources").has("outputs"));
        assertFalse(taskRunNode.get("spec").has("workspaces"));
    }

    @Test
    public void checkTaskRunCreatedHasOutputResource() throws IOException {
        String content = load("task8.yaml");
        AddTriggerModel model = new AddTriggerModel(content, Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyMap());

        ObjectNode taskRunNode = YAMLBuilder.createTaskRun("task", model);

        assertEquals(taskRunNode.get("apiVersion").asText(), "tekton.dev/v1beta1");
        assertEquals(taskRunNode.get("kind").asText(), "TaskRun");
        assertEquals(taskRunNode.get("metadata").get("generateName").asText(), "task-");
        assertEquals(taskRunNode.get("spec").get("taskRef").get("name").asText(), "task");
        assertFalse(taskRunNode.get("spec").has("serviceAccountName"));
        assertFalse(taskRunNode.get("spec").has("serviceAccountNames"));
        assertFalse(taskRunNode.get("spec").has("params"));
        assertTrue(taskRunNode.get("spec").has("resources"));
        assertFalse(taskRunNode.get("spec").get("resources").has("inputs"));
        assertEquals(taskRunNode.get("spec").get("resources").get("outputs").get(0).get("name").asText(), "resource1");
        assertFalse(taskRunNode.get("spec").has("workspaces"));
    }

    @Test
    public void checkTaskRunCreatedHasWorkspace() throws IOException {
        String content = load("task10.yaml");
        AddTriggerModel model = new AddTriggerModel(content, Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyMap());
        if (model.getWorkspaces().containsKey("write-allowed")) {
            model.getWorkspaces().put("write-allowed", new Workspace("foo1", Workspace.Kind.EMPTYDIR, "foo"));
        }
        if (model.getWorkspaces().containsKey("write-disallowed")) {
            model.getWorkspaces().put("write-disallowed", new Workspace("foo2", Workspace.Kind.EMPTYDIR, "foo"));
        }

        ObjectNode taskRunNode = YAMLBuilder.createTaskRun("task", model);

        assertEquals(taskRunNode.get("apiVersion").asText(), "tekton.dev/v1beta1");
        assertEquals(taskRunNode.get("kind").asText(), "TaskRun");
        assertEquals(taskRunNode.get("metadata").get("generateName").asText(), "task-");
        assertEquals(taskRunNode.get("spec").get("taskRef").get("name").asText(), "task");
        assertFalse(taskRunNode.get("spec").has("serviceAccountName"));
        assertFalse(taskRunNode.get("spec").has("serviceAccountNames"));
        assertFalse(taskRunNode.get("spec").has("params"));
        assertFalse(taskRunNode.get("spec").has("resources"));
        assertTrue(taskRunNode.get("spec").has("workspaces"));
        assertEquals(taskRunNode.get("spec").get("workspaces").get(0).get("name").asText(), "foo1");
        assertEquals(taskRunNode.get("spec").get("workspaces").get(1).get("name").asText(), "foo2");
    }

    /////////////////////////////////////////////////////////
    ///             CREATE TRIGGERTEMPLATE
    /////////////////////////////////////////////////////////

    @Test
    public void checkTriggerTemplateCreatedWithNoInputs() throws IOException {
        String content = load("task1.yaml");
        AddTriggerModel model = new AddTriggerModel(content, Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyMap());

        ObjectNode taskRunNode = YAMLBuilder.createTaskRun("task", model);
        ObjectNode triggerTemplateNode = YAMLBuilder.createTriggerTemplate("template", Collections.emptyList(), Arrays.asList(taskRunNode));

        assertEquals(triggerTemplateNode.get("apiVersion").asText(), "triggers.tekton.dev/v1alpha1");
        assertEquals(triggerTemplateNode.get("kind").asText(), "TriggerTemplate");
        assertEquals(triggerTemplateNode.get("metadata").get("name").asText(), "template");
        assertFalse(triggerTemplateNode.get("spec").has("params"));
        assertTrue(triggerTemplateNode.get("spec").has("resourcetemplates"));
    }

    @Test
    public void checkTriggerTemplateCreatedWithInputs() throws IOException {
        String content = load("task1.yaml");
        AddTriggerModel model = new AddTriggerModel(content, Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyMap());

        ObjectNode taskRunNode = YAMLBuilder.createTaskRun("task", model);
        ObjectNode triggerTemplateNode = YAMLBuilder.createTriggerTemplate("template", Arrays.asList("param1", "param2"), Arrays.asList(taskRunNode));

        assertEquals(triggerTemplateNode.get("apiVersion").asText(), "triggers.tekton.dev/v1alpha1");
        assertEquals(triggerTemplateNode.get("kind").asText(), "TriggerTemplate");
        assertEquals(triggerTemplateNode.get("metadata").get("name").asText(), "template");
        assertTrue(triggerTemplateNode.get("spec").has("params"));
        assertEquals(triggerTemplateNode.get("spec").get("params").get(0).get("name").asText(), "param1");
        assertEquals(triggerTemplateNode.get("spec").get("params").get(1).get("name").asText(), "param2");
        assertTrue(triggerTemplateNode.get("spec").has("resourcetemplates"));
    }

    /////////////////////////////////////////////////////////
    ///             CREATE EVENTLISTENER
    /////////////////////////////////////////////////////////

    @Test
    public void checkEventListenerCreatedWithNoBindings() {
        ObjectNode eventListenerNode = YAMLBuilder.createEventListener("el", "sa", Collections.emptyList(), "triggerTemplate");

        assertEquals(eventListenerNode.get("apiVersion").asText(), "triggers.tekton.dev/v1alpha1");
        assertEquals(eventListenerNode.get("kind").asText(), "EventListener");
        assertEquals(eventListenerNode.get("metadata").get("name").asText(), "el");
        assertFalse(eventListenerNode.get("spec").get("triggers").get(0).has("bindings"));
        assertTrue(eventListenerNode.get("spec").get("triggers").get(0).has("template"));
        assertEquals(eventListenerNode.get("spec").get("triggers").get(0).get("template").get("name").asText(), "triggerTemplate");
    }

    @Test
    public void checkEventListenerCreatedWithOneBinding() {
        ObjectNode eventListenerNode = YAMLBuilder.createEventListener("el", "sa", Arrays.asList("binding"), "triggerTemplate");

        assertEquals(eventListenerNode.get("apiVersion").asText(), "triggers.tekton.dev/v1alpha1");
        assertEquals(eventListenerNode.get("kind").asText(), "EventListener");
        assertEquals(eventListenerNode.get("metadata").get("name").asText(), "el");
        assertTrue(eventListenerNode.get("spec").get("triggers").get(0).has("bindings"));
        assertEquals(eventListenerNode.get("spec").get("triggers").get(0).get("bindings").get(0).get("ref").asText(), "binding");
        assertTrue(eventListenerNode.get("spec").get("triggers").get(0).has("template"));
        assertEquals(eventListenerNode.get("spec").get("triggers").get(0).get("template").get("name").asText(), "triggerTemplate");
    }

    @Test
    public void checkEventListenerCreatedWithMoreBindings() {
        ObjectNode eventListenerNode = YAMLBuilder.createEventListener("el", "sa", Arrays.asList("binding1", "binding2", "binding3"), "triggerTemplate");

        assertEquals(eventListenerNode.get("apiVersion").asText(), "triggers.tekton.dev/v1alpha1");
        assertEquals(eventListenerNode.get("kind").asText(), "EventListener");
        assertEquals(eventListenerNode.get("metadata").get("name").asText(), "el");
        assertTrue(eventListenerNode.get("spec").get("triggers").get(0).has("bindings"));
        assertEquals(eventListenerNode.get("spec").get("triggers").get(0).get("bindings").get(0).get("ref").asText(), "binding1");
        assertEquals(eventListenerNode.get("spec").get("triggers").get(0).get("bindings").get(1).get("ref").asText(), "binding2");
        assertEquals(eventListenerNode.get("spec").get("triggers").get(0).get("bindings").get(2).get("ref").asText(), "binding3");
        assertTrue(eventListenerNode.get("spec").get("triggers").get(0).has("template"));
        assertEquals(eventListenerNode.get("spec").get("triggers").get(0).get("template").get("name").asText(), "triggerTemplate");
    }
}
