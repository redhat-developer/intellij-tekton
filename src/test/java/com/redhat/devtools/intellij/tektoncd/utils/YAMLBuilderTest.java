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
import com.redhat.devtools.intellij.tektoncd.utils.model.resources.TaskConfigurationModel;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

import static com.redhat.devtools.intellij.tektoncd.Constants.TRIGGER_BETA1_API_VERSION;

public class YAMLBuilderTest extends BaseTest {

    private static final String RESOURCE_PATH = "utils/yamlBuilder/";

    /////////////////////////////////////////////////////////
    ///             CREATE PIPELINERUN
    /////////////////////////////////////////////////////////

    public void testCheckPipelineRunCreatedWithNoInputs() throws IOException {
        String content = load("pipeline1.yaml");
        AddTriggerModel model = new AddTriggerModel(content, Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyMap());

        ObjectNode pipelineRunNode = YAMLBuilder.createPipelineRun(model);

        assertEquals(pipelineRunNode.get("apiVersion").asText(), "tekton.dev/v1beta1");
        assertEquals(pipelineRunNode.get("kind").asText(), "PipelineRun");
        assertEquals(pipelineRunNode.get("metadata").get("generateName").asText(), "foo-");
        assertEquals(pipelineRunNode.get("spec").get("pipelineRef").get("name").asText(), "foo");
        assertFalse(pipelineRunNode.get("spec").has("serviceAccountName"));
        assertFalse(pipelineRunNode.get("spec").has("serviceAccountNames"));
        assertFalse(pipelineRunNode.get("spec").has("params"));
        assertFalse(pipelineRunNode.get("spec").has("resources"));
        assertFalse(pipelineRunNode.get("spec").has("workspaces"));
    }

    public void testCheckPipelineRunCreatedHasServiceAccount() throws IOException {
        String content = load("pipeline1.yaml");
        AddTriggerModel model = new AddTriggerModel(content, Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyMap());
        model.setServiceAccount("sa");

        ObjectNode pipelineRunNode = YAMLBuilder.createPipelineRun(model);

        assertEquals(pipelineRunNode.get("apiVersion").asText(), "tekton.dev/v1beta1");
        assertEquals(pipelineRunNode.get("kind").asText(), "PipelineRun");
        assertEquals(pipelineRunNode.get("metadata").get("generateName").asText(), "foo-");
        assertEquals(pipelineRunNode.get("spec").get("pipelineRef").get("name").asText(), "foo");
        assertEquals(pipelineRunNode.get("spec").get("serviceAccountName").asText(), "sa");
        assertFalse(pipelineRunNode.get("spec").has("serviceAccountNames"));
        assertFalse(pipelineRunNode.get("spec").has("params"));
        assertFalse(pipelineRunNode.get("spec").has("resources"));
        assertFalse(pipelineRunNode.get("spec").has("workspaces"));
    }

    public void testCheckPipelineRunCreatedHasParams() throws IOException {
        String content = load("pipeline3.yaml");
        AddTriggerModel model = new AddTriggerModel(content, Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyMap());

        ObjectNode pipelineRunNode = YAMLBuilder.createPipelineRun(model);

        assertEquals(pipelineRunNode.get("apiVersion").asText(), "tekton.dev/v1beta1");
        assertEquals(pipelineRunNode.get("kind").asText(), "PipelineRun");
        assertEquals(pipelineRunNode.get("metadata").get("generateName").asText(), "foo-");
        assertEquals(pipelineRunNode.get("spec").get("pipelineRef").get("name").asText(), "foo");
        assertFalse(pipelineRunNode.get("spec").has("serviceAccountName"));
        assertFalse(pipelineRunNode.get("spec").has("serviceAccountNames"));
        assertTrue(pipelineRunNode.get("spec").has("params"));
        assertEquals(pipelineRunNode.get("spec").get("params").get(0).get("name").asText(), "param1");
        assertFalse(pipelineRunNode.get("spec").has("resources"));
        assertFalse(pipelineRunNode.get("spec").has("workspaces"));
    }

    public void testCheckPipelineRunCreatedHasResource() throws IOException {
        String content = load("pipeline4.yaml");
        AddTriggerModel model = new AddTriggerModel(content, Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyMap());

        ObjectNode pipelineRunNode = YAMLBuilder.createPipelineRun(model);

        assertEquals(pipelineRunNode.get("apiVersion").asText(), "tekton.dev/v1beta1");
        assertEquals(pipelineRunNode.get("kind").asText(), "PipelineRun");
        assertEquals(pipelineRunNode.get("metadata").get("generateName").asText(), "foo-");
        assertEquals(pipelineRunNode.get("spec").get("pipelineRef").get("name").asText(), "foo");
        assertFalse(pipelineRunNode.get("spec").has("serviceAccountName"));
        assertFalse(pipelineRunNode.get("spec").has("serviceAccountNames"));
        assertFalse(pipelineRunNode.get("spec").has("params"));
        assertFalse(pipelineRunNode.get("spec").has("workspaces"));
    }

    public void testCheckPipelineRunCreatedHasWorkspace() throws IOException {
        String content = load("pipeline5.yaml");
        AddTriggerModel model = new AddTriggerModel(content, Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyMap());
        if (model.getWorkspaces().containsKey("password-vault")) {
            model.getWorkspaces().put("password-vault", new Workspace("foo1", Workspace.Kind.EMPTYDIR, "foo"));
        }
        if (model.getWorkspaces().containsKey("recipe-store")) {
            model.getWorkspaces().put("recipe-store", new Workspace("foo2", Workspace.Kind.EMPTYDIR, "foo"));
        }

        ObjectNode pipelineRunNode = YAMLBuilder.createPipelineRun(model);

        assertEquals(pipelineRunNode.get("apiVersion").asText(), "tekton.dev/v1beta1");
        assertEquals(pipelineRunNode.get("kind").asText(), "PipelineRun");
        assertEquals(pipelineRunNode.get("metadata").get("generateName").asText(), "foo-");
        assertEquals(pipelineRunNode.get("spec").get("pipelineRef").get("name").asText(), "foo");
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

    public void testCheckTaskRunCreatedWithNoInputs() throws IOException {
        String content = load("task1.yaml");
        AddTriggerModel model = new AddTriggerModel(content, Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyMap());

        ObjectNode taskRunNode = YAMLBuilder.createTaskRun(model);

        assertEquals(taskRunNode.get("apiVersion").asText(), "tekton.dev/v1beta1");
        assertEquals(taskRunNode.get("kind").asText(), "TaskRun");
        assertEquals(taskRunNode.get("metadata").get("generateName").asText(), "foo-");
        assertEquals(taskRunNode.get("spec").get("taskRef").get("name").asText(), "foo");
        assertFalse(taskRunNode.get("spec").has("serviceAccountName"));
        assertFalse(taskRunNode.get("spec").has("serviceAccountNames"));
        assertFalse(taskRunNode.get("spec").has("params"));
        assertFalse(taskRunNode.get("spec").has("resources"));
        assertFalse(taskRunNode.get("spec").has("workspaces"));
    }

    public void testCheckTaskRunCreatedHasServiceAccount() throws IOException {
        String content = load("task1.yaml");
        AddTriggerModel model = new AddTriggerModel(content, Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyMap());
        model.setServiceAccount("sa");

        ObjectNode taskRunNode = YAMLBuilder.createTaskRun(model);

        assertEquals(taskRunNode.get("apiVersion").asText(), "tekton.dev/v1beta1");
        assertEquals(taskRunNode.get("kind").asText(), "TaskRun");
        assertEquals(taskRunNode.get("metadata").get("generateName").asText(), "foo-");
        assertEquals(taskRunNode.get("spec").get("taskRef").get("name").asText(), "foo");
        assertEquals(taskRunNode.get("spec").get("serviceAccountName").asText(), "sa");
        assertFalse(taskRunNode.get("spec").has("serviceAccountNames"));
        assertFalse(taskRunNode.get("spec").has("params"));
        assertFalse(taskRunNode.get("spec").has("resources"));
        assertFalse(taskRunNode.get("spec").has("workspaces"));
    }

    public void testCheckTaskRunCreatedHasParams() throws IOException {
        String content = load("task3.yaml");
        AddTriggerModel model = new AddTriggerModel(content, Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyMap());

        ObjectNode taskRunNode = YAMLBuilder.createTaskRun(model);

        assertEquals(taskRunNode.get("apiVersion").asText(), "tekton.dev/v1beta1");
        assertEquals(taskRunNode.get("kind").asText(), "TaskRun");
        assertEquals(taskRunNode.get("metadata").get("generateName").asText(), "foo-");
        assertEquals(taskRunNode.get("spec").get("taskRef").get("name").asText(), "foo");
        assertFalse(taskRunNode.get("spec").has("serviceAccountName"));
        assertFalse(taskRunNode.get("spec").has("serviceAccountNames"));
        assertTrue(taskRunNode.get("spec").has("params"));
        assertEquals(taskRunNode.get("spec").get("params").get(0).get("name").asText(), "parm1");
        assertFalse(taskRunNode.get("spec").has("resources"));
        assertFalse(taskRunNode.get("spec").has("workspaces"));
    }

    public void testCheckTaskRunCreatedHasInputResource() throws IOException {
        String content = load("task6.yaml");
        AddTriggerModel model = new AddTriggerModel(content, Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyMap());

        ObjectNode taskRunNode = YAMLBuilder.createTaskRun(model);

        assertEquals(taskRunNode.get("apiVersion").asText(), "tekton.dev/v1beta1");
        assertEquals(taskRunNode.get("kind").asText(), "TaskRun");
        assertEquals(taskRunNode.get("metadata").get("generateName").asText(), "foo-");
        assertEquals(taskRunNode.get("spec").get("taskRef").get("name").asText(), "foo");
        assertFalse(taskRunNode.get("spec").has("serviceAccountName"));
        assertFalse(taskRunNode.get("spec").has("serviceAccountNames"));
        assertFalse(taskRunNode.get("spec").has("params"));
        assertFalse(taskRunNode.get("spec").has("workspaces"));
    }

    public void testCheckTaskRunCreatedHasOutputResource() throws IOException {
        String content = load("task8.yaml");
        AddTriggerModel model = new AddTriggerModel(content, Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyMap());

        ObjectNode taskRunNode = YAMLBuilder.createTaskRun(model);

        assertEquals(taskRunNode.get("apiVersion").asText(), "tekton.dev/v1beta1");
        assertEquals(taskRunNode.get("kind").asText(), "TaskRun");
        assertEquals(taskRunNode.get("metadata").get("generateName").asText(), "foo-");
        assertEquals(taskRunNode.get("spec").get("taskRef").get("name").asText(), "foo");
        assertFalse(taskRunNode.get("spec").has("serviceAccountName"));
        assertFalse(taskRunNode.get("spec").has("serviceAccountNames"));
        assertFalse(taskRunNode.get("spec").has("params"));
        assertFalse(taskRunNode.get("spec").has("workspaces"));
    }

    public void testCheckTaskRunCreatedHasWorkspace() throws IOException {
        String content = load("task10.yaml");
        AddTriggerModel model = new AddTriggerModel(content, Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyMap());
        if (model.getWorkspaces().containsKey("write-allowed")) {
            model.getWorkspaces().put("write-allowed", new Workspace("foo1", Workspace.Kind.EMPTYDIR, "foo"));
        }
        if (model.getWorkspaces().containsKey("write-disallowed")) {
            model.getWorkspaces().put("write-disallowed", new Workspace("foo2", Workspace.Kind.EMPTYDIR, "foo"));
        }

        ObjectNode taskRunNode = YAMLBuilder.createTaskRun(model);

        assertEquals(taskRunNode.get("apiVersion").asText(), "tekton.dev/v1beta1");
        assertEquals(taskRunNode.get("kind").asText(), "TaskRun");
        assertEquals(taskRunNode.get("metadata").get("generateName").asText(), "foo-");
        assertEquals(taskRunNode.get("spec").get("taskRef").get("name").asText(), "foo");
        assertFalse(taskRunNode.get("spec").has("serviceAccountName"));
        assertFalse(taskRunNode.get("spec").has("serviceAccountNames"));
        assertFalse(taskRunNode.get("spec").has("params"));
        assertFalse(taskRunNode.get("spec").has("resources"));
        assertTrue(taskRunNode.get("spec").has("workspaces"));
        assertEquals(taskRunNode.get("spec").get("workspaces").get(0).get("name").asText(), "foo1");
        assertEquals(taskRunNode.get("spec").get("workspaces").get(1).get("name").asText(), "foo2");
    }

    public void testCheckTaskRunCreatedWithTaskConfigurationModelWithoutWorkspace() throws IOException {
        String content = load("task5.yaml");
        TaskConfigurationModel model = new TaskConfigurationModel(content);


        ObjectNode taskRunNode = YAMLBuilder.createTaskRun(model);

        assertEquals(taskRunNode.get("apiVersion").asText(), "tekton.dev/v1beta1");
        assertEquals(taskRunNode.get("kind").asText(), "TaskRun");
        assertEquals(taskRunNode.get("metadata").get("generateName").asText(), "foo-");
        assertEquals(taskRunNode.get("spec").get("taskRef").get("name").asText(), "foo");
        assertTrue(taskRunNode.get("spec").has("serviceAccountName"));
        assertEquals(taskRunNode.get("spec").get("serviceAccountName").asText(), "");
        assertFalse(taskRunNode.get("spec").has("serviceAccountNames"));
        assertTrue(taskRunNode.get("spec").has("params"));
        assertFalse(taskRunNode.get("spec").has("resources"));
        assertFalse(taskRunNode.get("spec").has("workspaces"));
    }

    public void testCheckTaskRunCreatedWithTaskConfigurationModelWithWorkspace() throws IOException {
        String content = load("task10.yaml");
        TaskConfigurationModel model = new TaskConfigurationModel(content);


        ObjectNode taskRunNode = YAMLBuilder.createTaskRun(model);

        assertEquals(taskRunNode.get("apiVersion").asText(), "tekton.dev/v1beta1");
        assertEquals(taskRunNode.get("kind").asText(), "TaskRun");
        assertEquals(taskRunNode.get("metadata").get("generateName").asText(), "foo-");
        assertEquals(taskRunNode.get("spec").get("taskRef").get("name").asText(), "foo");
        assertTrue(taskRunNode.get("spec").has("serviceAccountName"));
        assertEquals(taskRunNode.get("spec").get("serviceAccountName").asText(), "");
        assertFalse(taskRunNode.get("spec").has("serviceAccountNames"));
        assertFalse(taskRunNode.get("spec").has("params"));
        assertFalse(taskRunNode.get("spec").has("resources"));
        assertTrue(taskRunNode.get("spec").has("workspaces"));
        assertEquals(taskRunNode.get("spec").get("workspaces").get(0).get("name").asText(), "write-allowed");
        assertEquals(taskRunNode.get("spec").get("workspaces").get(1).get("name").asText(), "write-disallowed");
        assertTrue(taskRunNode.get("spec").get("workspaces").get(0).has("emptyDir"));
        assertTrue(taskRunNode.get("spec").get("workspaces").get(1).has("emptyDir"));
    }

    /////////////////////////////////////////////////////////
    ///             CREATE TRIGGERTEMPLATE
    /////////////////////////////////////////////////////////

    public void testCheckTriggerTemplateCreatedWithNoInputs() throws IOException {
        String content = load("task1.yaml");
        AddTriggerModel model = new AddTriggerModel(content, Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyMap());

        ObjectNode taskRunNode = YAMLBuilder.createTaskRun(model);
        ObjectNode triggerTemplateNode = YAMLBuilder.createTriggerTemplate("template", TRIGGER_BETA1_API_VERSION, Collections.emptyList(), Arrays.asList(taskRunNode));

        assertEquals(triggerTemplateNode.get("apiVersion").asText(), TRIGGER_BETA1_API_VERSION);
        assertEquals(triggerTemplateNode.get("kind").asText(), "TriggerTemplate");
        assertEquals(triggerTemplateNode.get("metadata").get("name").asText(), "template");
        assertFalse(triggerTemplateNode.get("spec").has("params"));
        assertTrue(triggerTemplateNode.get("spec").has("resourcetemplates"));
    }

    public void testCheckTriggerTemplateCreatedWithInputs() throws IOException {
        String content = load("task1.yaml");
        AddTriggerModel model = new AddTriggerModel(content, Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyMap());

        ObjectNode taskRunNode = YAMLBuilder.createTaskRun(model);
        ObjectNode triggerTemplateNode = YAMLBuilder.createTriggerTemplate("template", TRIGGER_BETA1_API_VERSION, Arrays.asList("param1", "param2"), Arrays.asList(taskRunNode));

        assertEquals(triggerTemplateNode.get("apiVersion").asText(), TRIGGER_BETA1_API_VERSION);
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

    public void testCheckEventListenerCreatedWithNoBindings() {
        ObjectNode eventListenerNode = YAMLBuilder.createEventListener("el", TRIGGER_BETA1_API_VERSION, "sa", Collections.emptyList(), "triggerTemplate");

        assertEquals(eventListenerNode.get("apiVersion").asText(), TRIGGER_BETA1_API_VERSION);
        assertEquals(eventListenerNode.get("kind").asText(), "EventListener");
        assertEquals(eventListenerNode.get("metadata").get("name").asText(), "el");
        assertFalse(eventListenerNode.get("spec").get("triggers").get(0).has("bindings"));
        assertTrue(eventListenerNode.get("spec").get("triggers").get(0).has("template"));
        assertEquals(eventListenerNode.get("spec").get("triggers").get(0).get("template").get("ref").asText(), "triggerTemplate");
    }

    public void testCheckEventListenerCreatedWithOneBinding() {
        ObjectNode eventListenerNode = YAMLBuilder.createEventListener("el", TRIGGER_BETA1_API_VERSION, "sa", Arrays.asList("binding"), "triggerTemplate");

        assertEquals(eventListenerNode.get("apiVersion").asText(), TRIGGER_BETA1_API_VERSION);
        assertEquals(eventListenerNode.get("kind").asText(), "EventListener");
        assertEquals(eventListenerNode.get("metadata").get("name").asText(), "el");
        assertTrue(eventListenerNode.get("spec").get("triggers").get(0).has("bindings"));
        assertEquals(eventListenerNode.get("spec").get("triggers").get(0).get("bindings").get(0).get("ref").asText(), "binding");
        assertTrue(eventListenerNode.get("spec").get("triggers").get(0).has("template"));
        assertEquals(eventListenerNode.get("spec").get("triggers").get(0).get("template").get("ref").asText(), "triggerTemplate");
    }

    public void testCheckEventListenerCreatedWithMoreBindings() {
        ObjectNode eventListenerNode = YAMLBuilder.createEventListener("el", TRIGGER_BETA1_API_VERSION, "sa", Arrays.asList("binding1", "binding2", "binding3"), "triggerTemplate");

        assertEquals(eventListenerNode.get("apiVersion").asText(), TRIGGER_BETA1_API_VERSION);
        assertEquals(eventListenerNode.get("kind").asText(), "EventListener");
        assertEquals(eventListenerNode.get("metadata").get("name").asText(), "el");
        assertTrue(eventListenerNode.get("spec").get("triggers").get(0).has("bindings"));
        assertEquals(eventListenerNode.get("spec").get("triggers").get(0).get("bindings").get(0).get("ref").asText(), "binding1");
        assertEquals(eventListenerNode.get("spec").get("triggers").get(0).get("bindings").get(1).get("ref").asText(), "binding2");
        assertEquals(eventListenerNode.get("spec").get("triggers").get(0).get("bindings").get(2).get("ref").asText(), "binding3");
        assertTrue(eventListenerNode.get("spec").get("triggers").get(0).has("template"));
        assertEquals(eventListenerNode.get("spec").get("triggers").get(0).get("template").get("ref").asText(), "triggerTemplate");
    }

    public void testCreateTask_TaskSpecHasMetadataField_ObjectNodeRepresentingTask() throws IOException {
        String taskSpec = load(RESOURCE_PATH + "taskSpec1.yaml");
        ObjectNode taskToSave = YAMLBuilder.convertToObjectNode(taskSpec);
        ObjectNode resultingTask = YAMLBuilder.createTask("test", "Task", taskToSave);
        assertEquals("tekton.dev/v1beta1", resultingTask.get("apiVersion").asText());
        assertEquals("Task", resultingTask.get("kind").asText());
        assertEquals("test", resultingTask.get("metadata").get("name").asText());
        assertTrue(resultingTask.get("spec").get("steps").get(0).has("image"));
        assertEquals("fedora", resultingTask.get("spec").get("steps").get(0).get("image").asText());
        assertTrue(resultingTask.get("spec").get("steps").get(0).has("name"));
        assertEquals("echo", resultingTask.get("spec").get("steps").get(0).get("name").asText());
    }

    public void testCreateTask_TaskSpecHasNotMetadataField_ObjectNodeRepresentingTask() throws IOException {
        String taskSpec = load(RESOURCE_PATH + "taskSpec2.yaml");
        ObjectNode taskToSave = YAMLBuilder.convertToObjectNode(taskSpec);
        ObjectNode resultingTask = YAMLBuilder.createTask("test", "Task", taskToSave);
        assertEquals("tekton.dev/v1beta1", resultingTask.get("apiVersion").asText());
        assertEquals("Task", resultingTask.get("kind").asText());
        assertEquals("test", resultingTask.get("metadata").get("name").asText());
        assertTrue(resultingTask.get("spec").get("steps").get(0).has("image"));
        assertEquals("fedora", resultingTask.get("spec").get("steps").get(0).get("image").asText());
        assertTrue(resultingTask.get("spec").get("steps").get(0).has("name"));
        assertEquals("echo", resultingTask.get("spec").get("steps").get(0).get("name").asText());
    }

    public void testCreateTask_TaskSpecHasParamsField_ObjectNodeRepresentingTask() throws IOException {
        String taskSpec = load(RESOURCE_PATH + "taskSpec3.yaml");
        ObjectNode taskToSave = YAMLBuilder.convertToObjectNode(taskSpec);
        ObjectNode resultingTask = YAMLBuilder.createTask("test", "ClusterTask", taskToSave);
        assertEquals("tekton.dev/v1beta1", resultingTask.get("apiVersion").asText());
        assertEquals("ClusterTask", resultingTask.get("kind").asText());
        assertEquals("test", resultingTask.get("metadata").get("name").asText());
        assertTrue(resultingTask.get("spec").get("params").get(0).has("name"));
        assertEquals("x", resultingTask.get("spec").get("params").get(0).get("name").asText());
        assertTrue(resultingTask.get("spec").get("steps").get(0).has("image"));
        assertEquals("fedora", resultingTask.get("spec").get("steps").get(0).get("image").asText());
        assertTrue(resultingTask.get("spec").get("steps").get(0).has("name"));
        assertEquals("echo", resultingTask.get("spec").get("steps").get(0).get("name").asText());
    }

    public void testCreateTaskRef_ObjectNodeRepresentingTaskRef() {
        ObjectNode resultingTaskRef = YAMLBuilder.createTaskRef("test", "kind");
        assertEquals("kind", resultingTaskRef.get("taskRef").get("kind").asText());
        assertEquals("test", resultingTaskRef.get("taskRef").get("name").asText());
    }

    public void testCreateVCT_ObjectNodeRepresentingVolumeClaimTemplate() {
        ObjectNode vct = YAMLBuilder.createVCT("vct", "mode", "1", "MiB");
        assertTrue(vct.has("metadata"));
        assertTrue(vct.get("metadata").has("name"));
        assertEquals("vct", vct.get("metadata").get("name").asText());
        assertTrue(vct.has("spec"));
        assertTrue(vct.get("spec").has("resources"));
        assertTrue(vct.get("spec").get("resources").has("requests"));
        assertTrue(vct.get("spec").get("resources").get("requests").has("storage"));
        assertEquals("1MiB", vct.get("spec").get("resources").get("requests").get("storage").asText());
        assertTrue(vct.get("spec").has("volumeMode"));
        assertEquals("Filesystem", vct.get("spec").get("volumeMode").asText());
        assertTrue(vct.get("spec").has("accessModes"));
        assertEquals("mode", vct.get("spec").get("accessModes").get(0).asText());
    }
}
