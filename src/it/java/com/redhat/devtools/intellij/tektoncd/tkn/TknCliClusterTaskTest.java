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
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Test;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TknCliClusterTaskTest extends TknCliTest {

    @Test
    public void verifyCreateClusterTaskAndDelete() throws IOException {
        final String TASK_NAME = "ctfirst";
        String resourceBody = TestUtils.load("clustertask1.yaml").replace("ctfoo", TASK_NAME);
        TestUtils.saveResource(tkn, resourceBody, "", "clustertasks");
        // verify task has been created
        List<String> tasks = tkn.getClusterTasks().stream().map(task -> task.getMetadata().getName()).collect(Collectors.toList());
        assertTrue(tasks.contains(TASK_NAME));
        // clean up and verify cleaning succeed
        tkn.deleteClusterTasks(tasks, false);
        tasks = tkn.getClusterTasks().stream().map(task -> task.getMetadata().getName()).collect(Collectors.toList());
        assertFalse(tasks.contains(TASK_NAME));
    }

    @Test
    public void verifyClusterTaskYAMLIsReturnedCorrectly() throws IOException {
        final String TASK_NAME = "ctsecond";
        String resourceBody = TestUtils.load("clustertask1.yaml").replace("ctfoo", TASK_NAME);
        TestUtils.saveResource(tkn, resourceBody, "", "clustertasks");
        // verify pipeline has been created
        List<String> tasks = tkn.getClusterTasks().stream().map(task -> task.getMetadata().getName()).collect(Collectors.toList());;
        assertTrue(tasks.contains(TASK_NAME));
        // get YAML from cluster and verify is the same uploaded
        String resourceBodyFromCluster = tkn.getClusterTaskYAML(TASK_NAME);
        assertEquals(TestUtils.getSpecFromResource(resourceBody), TestUtils.getSpecFromResource(resourceBodyFromCluster));
        // clean up and verify cleaning succeed
        tkn.deleteClusterTasks(tasks, false);
        tasks = tkn.getClusterTasks().stream().map(task -> task.getMetadata().getName()).collect(Collectors.toList());;
        assertFalse(tasks.contains(TASK_NAME));
    }
}
