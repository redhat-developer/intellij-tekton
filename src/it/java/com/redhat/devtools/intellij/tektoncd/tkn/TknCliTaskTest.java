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

import java.io.IOException;
import java.util.List;
import org.junit.Test;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TknCliTaskTest extends TknCliTest {

    @Test
    public void verifyCreateTaskAndDelete() throws IOException {
        final String TASK_NAME = "first";
        String resourceBody = load("task1.yaml").replace("taskfoo", TASK_NAME);
        saveResource(resourceBody, NAMESPACE, "tasks");
        // verify task has been created
        List<String> tasks = tkn.getTasks(NAMESPACE);
        assertTrue(tasks.contains(TASK_NAME));
        // clean up and verify cleaning succeed
        tkn.deleteTasks(NAMESPACE, tasks, false);
        tasks = tkn.getTasks(NAMESPACE);
        assertFalse(tasks.contains(TASK_NAME));
    }

    @Test
    public void verifyPipelineYAMLIsReturnedCorrectly() throws IOException {
        final String TASK_NAME = "second";
        String resourceBody = load("task1.yaml").replace("taskfoo", TASK_NAME);
        saveResource(resourceBody, NAMESPACE, "tasks");
        // verify pipeline has been created
        List<String> tasks = tkn.getTasks(NAMESPACE);
        assertTrue(tasks.contains(TASK_NAME));
        // get YAML from cluster and verify is the same uploaded
        String resourceBodyFromCluster = tkn.getTaskYAML(NAMESPACE, TASK_NAME);
        assertEquals(getSpecFromResource(resourceBody), getSpecFromResource(resourceBodyFromCluster));
        // clean up and verify cleaning succeed
        tkn.deleteTasks(NAMESPACE, tasks, false);
        tasks = tkn.getTasks(NAMESPACE);
        assertFalse(tasks.contains(TASK_NAME));
    }

    @Test
    public void verifyCreateTaskAndTaskRunAndDeleteAll() throws IOException, InterruptedException {
        final String TASK_NAME = "third-task-test";
        final String TASK_RUN_NAME = "run-third-task-test";
        String taskConfig = load("task1.yaml").replace("taskfoo", TASK_NAME);
        String taskRunConfig = load("taskrun1.yaml").replace("taskfoo", TASK_NAME).replace("taskrunfoo", TASK_RUN_NAME);
        saveResource(taskConfig, NAMESPACE, "tasks");
        saveResource(taskRunConfig, NAMESPACE, "taskruns");

        // verify task has been created
        List<String> tasks = tkn.getTasks(NAMESPACE);
        assertTrue(tasks.contains(TASK_NAME));
        // verify taskrun has been created
        Thread.sleep(500); // adding a bit delay to allow run to be created
        List<TaskRun> taskruns = tkn.getTaskRuns(NAMESPACE, TASK_NAME);
        assertTrue(taskruns.stream().anyMatch(run -> run.getName().equals(TASK_RUN_NAME)));
        tkn.cancelTaskRun(NAMESPACE, taskruns.get(0).getName());
        // clean up and verify cleaning succeed
        tkn.deleteTasks(NAMESPACE, tasks, true);
        tasks = tkn.getTasks(NAMESPACE);
        assertFalse(tasks.contains(TASK_NAME));
        taskruns = tkn.getTaskRuns(NAMESPACE, TASK_NAME);
        assertTrue(taskruns.stream().noneMatch(run -> run.getName().equals(TASK_RUN_NAME)));

    }
}
