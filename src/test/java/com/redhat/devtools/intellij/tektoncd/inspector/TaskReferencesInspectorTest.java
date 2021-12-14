/*******************************************************************************
 *  Copyright (c) 2021 Red Hat, Inc.
 *  Distributed under license by Red Hat, Inc. All rights reserved.
 *  This program is made available under the terms of the
 *  Eclipse Public License v2.0 which accompanies this distribution,
 *  and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 *  Contributors:
 *  Red Hat, Inc.
 ******************************************************************************/
package com.redhat.devtools.intellij.tektoncd.inspector;

import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TaskReferencesInspectorTest extends InspectorTest {

    @Override
    public String getTestInspectorFolder() {
        return "taskReferencesInspector";
    }

    @Override
    public void enableInspections() {
        myFixture.enableInspections(TaskReferencesInspector.class);
    }

    @Test
    public void testPipelineWithTaskNotFoundOnCluster() {
        myFixture.configureByFile("pipeline1.yaml");
        List<HighlightInfo> hightlightInfo =  myFixture.doHighlighting();
        assertEquals(hightlightInfo.size(), 1);
        assertTrue(hightlightInfo.get(0).getDescription().equals("No task named foo found on cluster."));
    }

    @Test
    public void testPipelineWithTaskNotFoundOnClusterWithInvertedKindName() {
        myFixture.configureByFile("pipeline2.yaml");
        List<HighlightInfo> hightlightInfo =  myFixture.doHighlighting();
        assertEquals(hightlightInfo.size(), 1);
        assertTrue(hightlightInfo.get(0).getDescription().equals("No task named foo found on cluster."));
    }

    @Test
    public void testPipelineWithTaskNotFoundOnClusterWithoutKind() {
        myFixture.configureByFile("pipeline3.yaml");
        List<HighlightInfo> hightlightInfo =  myFixture.doHighlighting();
        assertEquals(hightlightInfo.size(), 1);
        assertTrue(hightlightInfo.get(0).getDescription().equals("No task named foo found on cluster."));
    }

    @Test
    public void testPipelineWithMultipleTasksNotFoundOnCluster() {
        myFixture.configureByFile("pipeline4.yaml");
        List<HighlightInfo> hightlightInfo =  myFixture.doHighlighting();
        assertEquals(hightlightInfo.size(), 2);
        assertTrue(hightlightInfo.get(0).getDescription().equals("No task named foo found on cluster."));
        assertTrue(hightlightInfo.get(1).getDescription().equals("No task named foo1 found on cluster."));
    }

    @Test
    public void testPipelineWithClusterTaskNotFoundOnCluster() {
        myFixture.configureByFile("pipeline5.yaml");
        List<HighlightInfo> hightlightInfo =  myFixture.doHighlighting();
        assertEquals(hightlightInfo.size(), 1);
        assertTrue(hightlightInfo.get(0).getDescription().equals("No task named foo found on cluster."));
    }

    @Test
    public void testPipelineWithMultipleTasksAndClusterTasksNotFoundOnCluster() {
        myFixture.configureByFile("pipeline6.yaml");
        List<HighlightInfo> hightlightInfo =  myFixture.doHighlighting();
        assertEquals(hightlightInfo.size(), 2);
        assertTrue(hightlightInfo.get(0).getDescription().equals("No task named foo found on cluster."));
        assertTrue(hightlightInfo.get(1).getDescription().equals("No task named foo1 found on cluster."));
    }

    @Test
    public void testPipelineWithQuotedTaskNotFoundOnCluster() {
        myFixture.configureByFile("pipeline7.yaml");
        List<HighlightInfo> hightlightInfo =  myFixture.doHighlighting();
        assertEquals(hightlightInfo.size(), 1);
        assertTrue(hightlightInfo.get(0).getDescription().equals("No task named foo found on cluster."));
    }

    @Test
    public void testPipelineWithSingleQuotedTaskNotFoundOnCluster() {
        myFixture.configureByFile("pipeline8.yaml");
        List<HighlightInfo> hightlightInfo =  myFixture.doHighlighting();
        assertEquals(hightlightInfo.size(), 1);
        assertTrue(hightlightInfo.get(0).getDescription().equals("No task named foo found on cluster."));
    }
}
