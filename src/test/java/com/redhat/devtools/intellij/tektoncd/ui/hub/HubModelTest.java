/*******************************************************************************
 * Copyright (c) 2021 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc.
 ******************************************************************************/
package com.redhat.devtools.intellij.tektoncd.ui.hub;

import com.redhat.devtools.intellij.tektoncd.BaseTest;
import com.redhat.devtools.intellij.tektoncd.tree.ClusterTasksNode;
import com.redhat.devtools.intellij.tektoncd.tree.PipelinesNode;
import io.fabric8.kubernetes.client.Watch;
import org.junit.Before;
import org.junit.Test;


import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class HubModelTest extends BaseTest {

    protected PipelinesNode pipelinesNode;
    protected ClusterTasksNode clusterTasksNode;
    private Watch watch;
    @Before
    public void setUp() throws Exception {
        super.setUp();
        watch = mock(Watch.class);
        when(tkn.getNamespace()).thenReturn("namespace");
        when(tkn.watchPipelines(anyString(), any())).thenReturn(watch);
        when(tkn.watchTasks(anyString(), any())).thenReturn(watch);
        when(tkn.watchClusterTasks(any())).thenReturn(watch);
        pipelinesNode = mock(PipelinesNode.class);
        clusterTasksNode = mock(ClusterTasksNode.class);
    }

    @Test
    public void GetIsClusterTaskView_CallerIsNull_False() {
        HubModel model = new HubModel(project, tkn, null);
        assertFalse(model.getIsClusterTaskView());
    }

    @Test
    public void GetIsClusterTaskView_CallerIsNotNullAndNotAClusterTasksNode_False() {
        HubModel model = new HubModel(project, tkn, pipelinesNode);
        assertFalse(model.getIsClusterTaskView());
    }

    @Test
    public void GetIsClusterTaskView_CallerIsAClusterTasksNode_True() {
        HubModel model = new HubModel(project, tkn, clusterTasksNode);
        assertTrue(model.getIsClusterTaskView());
    }

    @Test
    public void GetIsPipelineView_CallerIsNull_False() {
        HubModel model = new HubModel(project, tkn, null);
        assertFalse(model.getIsPipelineView());
    }

    @Test
    public void GetIsPipelineView_CallerIsNotNullAndNotAClusterTasksNode_False() {
        HubModel model = new HubModel(project, tkn, clusterTasksNode);
        assertFalse(model.getIsPipelineView());
    }

    @Test
    public void GetIsPipelineView_CallerIsAClusterTasksNode_True() {
        HubModel model = new HubModel(project, tkn, pipelinesNode);
        assertTrue(model.getIsPipelineView());
    }

}
