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
package com.redhat.devtools.intellij.tektoncd.tkn;

import com.redhat.devtools.intellij.tektoncd.TestUtils;
import io.fabric8.tekton.client.TektonClient;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.junit.Test;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TknCliTaskTest extends TknCliTest {

    @Test
    public void verifyCreateTaskAndDelete() throws IOException {
        final String TASK_NAME = "first";
        String resourceBody = TestUtils.load("task1.yaml").replace("taskfoo", TASK_NAME);
        TestUtils.saveResource(tkn, resourceBody, NAMESPACE, "tasks");
        // verify task has been created
        List<String> tasks = tkn.getTasks(NAMESPACE).stream().map(task -> task.getMetadata().getName()).collect(Collectors.toList());
        assertTrue(tasks.contains(TASK_NAME));
        // clean up and verify cleaning succeed
        tkn.deleteTasks(NAMESPACE, tasks, false);
        tasks = tkn.getTasks(NAMESPACE).stream().map(task -> task.getMetadata().getName()).collect(Collectors.toList());
        assertFalse(tasks.contains(TASK_NAME));
    }

    @Test
    public void verifyTaskYAMLIsReturnedCorrectly() throws IOException {
        final String TASK_NAME = "second";
        String resourceBody = TestUtils.load("task1.yaml").replace("taskfoo", TASK_NAME);
        TestUtils.saveResource(tkn, resourceBody, NAMESPACE, "tasks");
        // verify pipeline has been created
        List<String> tasks = tkn.getTasks(NAMESPACE).stream().map(task -> task.getMetadata().getName()).collect(Collectors.toList());
        assertTrue(tasks.contains(TASK_NAME));
        // get YAML from cluster and verify is the same uploaded
        String resourceBodyFromCluster = tkn.getTaskYAML(NAMESPACE, TASK_NAME);
        assertEquals(TestUtils.getSpecFromResource(resourceBody), TestUtils.getSpecFromResource(resourceBodyFromCluster));
        // clean up and verify cleaning succeed
        tkn.deleteTasks(NAMESPACE, tasks, false);
        tasks = tkn.getTasks(NAMESPACE).stream().map(task -> task.getMetadata().getName()).collect(Collectors.toList());
        assertFalse(tasks.contains(TASK_NAME));
    }

    @Test
    public void verifyCreateTaskAndTaskRunAndDeleteAll() throws IOException, InterruptedException {
        final String TASK_NAME = "third-task-test";
        final String TASK_RUN_NAME = "run-third-task-test";
        String taskConfig = TestUtils.load("task1.yaml").replace("taskfoo", TASK_NAME);
        String taskRunConfig = TestUtils.load("taskrun1.yaml").replace("taskfoo", TASK_NAME).replace("taskrunfoo", TASK_RUN_NAME);
        TestUtils.saveResource(tkn, taskConfig, NAMESPACE, "tasks");
        TestUtils.saveResource(tkn, taskRunConfig, NAMESPACE, "taskruns");

        // verify task has been created
        List<String> tasks = tkn.getTasks(NAMESPACE).stream().map(task -> task.getMetadata().getName()).collect(Collectors.toList());
        assertTrue(tasks.contains(TASK_NAME));
        // verify taskrun has been created
        tkn.getClient(TektonClient.class).v1beta1().taskRuns().inNamespace(NAMESPACE)
                .waitUntilCondition(taskRun -> taskRun.getMetadata().getName() != null && taskRun.getMetadata().getName().equals(TASK_RUN_NAME), 1, TimeUnit.HOURS);
        tkn.cancelTaskRun(NAMESPACE, TASK_RUN_NAME);
        // clean up and verify cleaning succeed
        tkn.deleteTasks(NAMESPACE, Arrays.asList(TASK_NAME), true);
        tasks = tkn.getTasks(NAMESPACE).stream().map(task -> task.getMetadata().getName()).collect(Collectors.toList());
        assertFalse(tasks.contains(TASK_NAME));
        List<TaskRun> taskRuns = tkn.getTaskRuns(NAMESPACE, TASK_NAME);
        assertTrue(taskRuns.stream().noneMatch(run -> run.getName().equals(TASK_RUN_NAME)));

    }

    @Test
    public void verifyStartTaskCreateRuns() throws IOException, InterruptedException {
        String TASK_NAME = "add-task-start-test";
        String taskConfig = TestUtils.load("start/add-task.yaml").replace("add-task", TASK_NAME);
        TestUtils.saveResource(tkn, taskConfig, NAMESPACE, "tasks");

        // verify task has been created
        List<String> tasks = tkn.getTasks(NAMESPACE).stream().map(task -> task.getMetadata().getName()).collect(Collectors.toList());
        assertTrue(tasks.contains(TASK_NAME));
        // start task and verify taskrun has been created
        Map<String, String> params = new HashMap<>();
        params.put("first", "1");
        params.put("second", "2");
        tkn.startTask(NAMESPACE, TASK_NAME, params, Collections.emptyMap(), Collections.emptyMap(), "", Collections.emptyMap(), "");
        io.fabric8.tekton.pipeline.v1beta1.TaskRun tRun = tkn.getClient(TektonClient.class).v1beta1().taskRuns().inNamespace(NAMESPACE)
                .waitUntilCondition(taskRun -> taskRun.getMetadata().getName() != null && taskRun.getMetadata().getName().startsWith(TASK_NAME), 1, TimeUnit.HOURS);
        tkn.cancelTaskRun(NAMESPACE, tRun.getMetadata().getName());
        // clean up
        tkn.deleteTasks(NAMESPACE, Arrays.asList(TASK_NAME), true);
    }
}
