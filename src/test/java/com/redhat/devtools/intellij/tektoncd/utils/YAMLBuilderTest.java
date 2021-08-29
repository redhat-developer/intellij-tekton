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
import org.junit.Test;


import static com.redhat.devtools.intellij.tektoncd.Constants.TRIGGER_BETA1_API_VERSION;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class YAMLBuilderTest extends BaseTest {

    private static final String RESOURCE_PATH = "utils/yamlBuilder/";

    /////////////////////////////////////////////////////////
    ///             CREATE PIPELINERUN
    /////////////////////////////////////////////////////////

    @Test
    public void checkPipelineRunCreatedWithNoInputs() throws IOException {
        String content = load("pipeline1.yaml");
        AddTriggerModel model = new AddTriggerModel(content, Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyMap());

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

    @Test
    public void checkPipelineRunCreatedHasServiceAccount() throws IOException {
        String content = load("pipeline1.yaml");
        AddTriggerModel model = new AddTriggerModel(content, Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyMap());
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

    @Test
    public void checkPipelineRunCreatedHasParams() throws IOException {
        String content = load("pipeline3.yaml");
        AddTriggerModel model = new AddTriggerModel(content, Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyMap());

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

    @Test
    public void checkPipelineRunCreatedHasResource() throws IOException {
        String content = load("pipeline4.yaml");
        AddTriggerModel model = new AddTriggerModel(content, Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyMap());

        ObjectNode pipelineRunNode = YAMLBuilder.createPipelineRun(model);

        assertEquals(pipelineRunNode.get("apiVersion").asText(), "tekton.dev/v1beta1");
        assertEquals(pipelineRunNode.get("kind").asText(), "PipelineRun");
        assertEquals(pipelineRunNode.get("metadata").get("generateName").asText(), "foo-");
        assertEquals(pipelineRunNode.get("spec").get("pipelineRef").get("name").asText(), "foo");
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

    @Test
    public void checkTaskRunCreatedWithNoInputs() throws IOException {
        String content = load("task1.yaml");
        AddTriggerModel model = new AddTriggerModel(content, Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyMap());

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

    @Test
    public void checkTaskRunCreatedHasServiceAccount() throws IOException {
        String content = load("task1.yaml");
        AddTriggerModel model = new AddTriggerModel(content, Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyMap());
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

    @Test
    public void checkTaskRunCreatedHasParams() throws IOException {
        String content = load("task3.yaml");
        AddTriggerModel model = new AddTriggerModel(content, Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyMap());

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

    @Test
    public void checkTaskRunCreatedHasInputResource() throws IOException {
        String content = load("task6.yaml");
        AddTriggerModel model = new AddTriggerModel(content, Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyMap());

        ObjectNode taskRunNode = YAMLBuilder.createTaskRun(model);

        assertEquals(taskRunNode.get("apiVersion").asText(), "tekton.dev/v1beta1");
        assertEquals(taskRunNode.get("kind").asText(), "TaskRun");
        assertEquals(taskRunNode.get("metadata").get("generateName").asText(), "foo-");
        assertEquals(taskRunNode.get("spec").get("taskRef").get("name").asText(), "foo");
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

        ObjectNode taskRunNode = YAMLBuilder.createTaskRun(model);

        assertEquals(taskRunNode.get("apiVersion").asText(), "tekton.dev/v1beta1");
        assertEquals(taskRunNode.get("kind").asText(), "TaskRun");
        assertEquals(taskRunNode.get("metadata").get("generateName").asText(), "foo-");
        assertEquals(taskRunNode.get("spec").get("taskRef").get("name").asText(), "foo");
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

    @Test
    public void checkTaskRunCreatedWithTaskConfigurationModelWithoutWorkspace() throws IOException {
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

    @Test
    public void checkTaskRunCreatedWithTaskConfigurationModelWithWorkspace() throws IOException {
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

    @Test
    public void checkTriggerTemplateCreatedWithNoInputs() throws IOException {
        String content = load("task1.yaml");
        AddTriggerModel model = new AddTriggerModel(content, Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyMap());

        ObjectNode taskRunNode = YAMLBuilder.createTaskRun(model);
        ObjectNode triggerTemplateNode = YAMLBuilder.createTriggerTemplate("template", TRIGGER_BETA1_API_VERSION, Collections.emptyList(), Arrays.asList(taskRunNode));

        assertEquals(triggerTemplateNode.get("apiVersion").asText(), TRIGGER_BETA1_API_VERSION);
        assertEquals(triggerTemplateNode.get("kind").asText(), "TriggerTemplate");
        assertEquals(triggerTemplateNode.get("metadata").get("name").asText(), "template");
        assertFalse(triggerTemplateNode.get("spec").has("params"));
        assertTrue(triggerTemplateNode.get("spec").has("resourcetemplates"));
    }

    @Test
    public void checkTriggerTemplateCreatedWithInputs() throws IOException {
        String content = load("task1.yaml");
        AddTriggerModel model = new AddTriggerModel(content, Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyMap());

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

    @Test
    public void checkEventListenerCreatedWithNoBindings() {
        ObjectNode eventListenerNode = YAMLBuilder.createEventListener("el", TRIGGER_BETA1_API_VERSION, "sa", Collections.emptyList(), "triggerTemplate");

        assertEquals(eventListenerNode.get("apiVersion").asText(), TRIGGER_BETA1_API_VERSION);
        assertEquals(eventListenerNode.get("kind").asText(), "EventListener");
        assertEquals(eventListenerNode.get("metadata").get("name").asText(), "el");
        assertFalse(eventListenerNode.get("spec").get("triggers").get(0).has("bindings"));
        assertTrue(eventListenerNode.get("spec").get("triggers").get(0).has("template"));
        assertEquals(eventListenerNode.get("spec").get("triggers").get(0).get("template").get("ref").asText(), "triggerTemplate");
    }

    @Test
    public void checkEventListenerCreatedWithOneBinding() {
        ObjectNode eventListenerNode = YAMLBuilder.createEventListener("el", TRIGGER_BETA1_API_VERSION, "sa", Arrays.asList("binding"), "triggerTemplate");

        assertEquals(eventListenerNode.get("apiVersion").asText(), TRIGGER_BETA1_API_VERSION);
        assertEquals(eventListenerNode.get("kind").asText(), "EventListener");
        assertEquals(eventListenerNode.get("metadata").get("name").asText(), "el");
        assertTrue(eventListenerNode.get("spec").get("triggers").get(0).has("bindings"));
        assertEquals(eventListenerNode.get("spec").get("triggers").get(0).get("bindings").get(0).get("ref").asText(), "binding");
        assertTrue(eventListenerNode.get("spec").get("triggers").get(0).has("template"));
        assertEquals(eventListenerNode.get("spec").get("triggers").get(0).get("template").get("ref").asText(), "triggerTemplate");
    }

    @Test
    public void checkEventListenerCreatedWithMoreBindings() {
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

    @Test
    public void CreateTask_TaskSpecHasMetadataField_ObjectNodeRepresentingTask() throws IOException {
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

    @Test
    public void CreateTask_TaskSpecHasNotMetadataField_ObjectNodeRepresentingTask() throws IOException {
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

    @Test
    public void CreateTask_TaskSpecHasParamsField_ObjectNodeRepresentingTask() throws IOException {
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

    @Test
    public void CreateTaskRef_ObjectNodeRepresentingTaskRef() {
        ObjectNode resultingTaskRef = YAMLBuilder.createTaskRef("test", "kind");
        assertEquals("kind", resultingTaskRef.get("taskRef").get("kind").asText());
        assertEquals("test", resultingTaskRef.get("taskRef").get("name").asText());
    }

    @Test
    public void CreateVCT_ObjectNodeRepresentingVolumeClaimTemplate() {
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
