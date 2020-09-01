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
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;


import static org.junit.Assert.assertEquals;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TknCliTaskTest extends TknCliTest {
    @Test
    public void verifyNoTasks() throws IOException {
        List<String> tasks = tkn.getTasks(NAMESPACE);
        assertEquals(0, tasks.size());
    }

    @Test
    public void verifyCreateTaskAndDelete() throws IOException {
        saveResource(load("task1.yaml"), NAMESPACE, "tasks");
        // verify task has been created
        List<String> tasks = tkn.getTasks(NAMESPACE);
        assertEquals(1, tasks.size());
        // clean up and verify cleaning succeed
        tkn.deleteTasks(NAMESPACE, tasks, false);
        tasks = tkn.getTasks(NAMESPACE);
        assertEquals(0, tasks.size());
    }

    @Test
    public void verifyPipelineYAMLIsReturnedCorrectly() throws IOException {
        String resourceBody = load("task1.yaml");
        saveResource(resourceBody, NAMESPACE, "tasks");
        // verify pipeline has been created
        List<String> tasks = tkn.getTasks(NAMESPACE);
        assertEquals(1, tasks.size());
        // get YAML from cluster and verify is the same uploaded
        String resourceBodyFromCluster = tkn.getTaskYAML(NAMESPACE, tasks.get(0));
        assertEquals(getSpecFromResource(resourceBody), getSpecFromResource(resourceBodyFromCluster));
        // clean up and verify cleaning succeed
        tkn.deleteTasks(NAMESPACE, tasks, false);
        tasks = tkn.getTasks(NAMESPACE);
        assertEquals(0, tasks.size());
    }
}
