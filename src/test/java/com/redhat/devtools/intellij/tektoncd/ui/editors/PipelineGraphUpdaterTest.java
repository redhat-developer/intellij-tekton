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
package com.redhat.devtools.intellij.tektoncd.ui.editors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.fabric8.tekton.pipeline.v1beta1.Pipeline;
import org.junit.Test;

import java.io.IOException;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class PipelineGraphUpdaterTest {
    private final ObjectMapper MAPPER  = new ObjectMapper(new YAMLFactory());

    private Pipeline load(String path) throws IOException {
        return MAPPER.readValue(PipelineGraphUpdaterTest.class.getResource(path), Pipeline.class);
    }

    @Test
    public void checkSingleTaskPipeline() throws IOException {
        Pipeline pipeline = load("/schemas/pipeline1.yaml");
        Map<String, PipelineGraphUpdater.Node> tree = PipelineGraphUpdater.generateTree(pipeline.getSpec().getTasks(), "");
        assertNotNull(tree);
        assertEquals(1, tree.size());
        PipelineGraphUpdater.Node node = tree.values().iterator().next();
        assertEquals(PipelineGraphUpdater.Type.TASK, node.type);
        assertEquals("foo", node.name);
        assertNotNull(node.childs);
        assertTrue(node.childs.isEmpty());
    }

    @Test
    public void checkSingleTaskFinallyPipeline() throws IOException {
        Pipeline pipeline = load("/schemas/pipeline-finally.yaml");
        Map<String, PipelineGraphUpdater.Node> tree = PipelineGraphUpdater.generateTree(pipeline.getSpec().getFinally(), "");
        assertNotNull(tree);
        assertEquals(1, tree.size());
        PipelineGraphUpdater.Node node = tree.values().iterator().next();
        assertEquals(PipelineGraphUpdater.Type.TASK, node.type);
        assertEquals("foo", node.name);
        assertNotNull(node.childs);
        assertTrue(node.childs.isEmpty());
    }

    @Test
    public void checkTwoTasksRunAfterPipeline() throws IOException {
        Pipeline pipeline = load("/schemas/pipeline-runafter.yaml");
        Map<String, PipelineGraphUpdater.Node> tree = PipelineGraphUpdater.generateTree(pipeline.getSpec().getTasks(), "");
        assertNotNull(tree);
        assertEquals(1, tree.size());
        PipelineGraphUpdater.Node node = tree.values().iterator().next();
        assertEquals(PipelineGraphUpdater.Type.TASK, node.type);
        assertEquals("first", node.name);
        assertNotNull(node.childs);
        assertEquals(1, node.childs.size());
        node = node.childs.iterator().next();
        assertEquals(PipelineGraphUpdater.Type.TASK, node.type);
        assertEquals("second", node.name);
        assertNotNull(node.childs);
        assertTrue(node.childs.isEmpty());
    }

    @Test
    public void checkTwoTasksFromPipeline() throws IOException {
        Pipeline pipeline = load("/schemas/pipeline-from.yaml");
        Map<String, PipelineGraphUpdater.Node> tree = PipelineGraphUpdater.generateTree(pipeline.getSpec().getTasks(), "");
        assertNotNull(tree);
        assertEquals(1, tree.size());
        PipelineGraphUpdater.Node node = tree.values().iterator().next();
        assertEquals(PipelineGraphUpdater.Type.TASK, node.type);
        assertEquals("first", node.name);
        assertNotNull(node.childs);
        assertEquals(1, node.childs.size());
        node = node.childs.iterator().next();
        assertEquals(PipelineGraphUpdater.Type.TASK, node.type);
        assertEquals("second", node.name);
        assertNotNull(node.childs);
        assertTrue(node.childs.isEmpty());
    }

    @Test
    public void checkTwoTasksWhenPipeline() throws IOException {
        Pipeline pipeline = load("/schemas/pipeline-when-2tasks.yaml");
        Map<String, PipelineGraphUpdater.Node> tree = PipelineGraphUpdater.generateTree(pipeline.getSpec().getTasks(), "");
        assertNotNull(tree);
        assertEquals(1, tree.size());
        PipelineGraphUpdater.Node node = tree.values().iterator().next();
        assertEquals(PipelineGraphUpdater.Type.TASK, node.type);
        assertEquals("first", node.name);
        assertNotNull(node.childs);
        assertEquals(1, node.childs.size());
        node = node.childs.iterator().next();
        assertEquals(PipelineGraphUpdater.Type.TASK, node.type);
        assertEquals("second", node.name);
        assertNotNull(node.childs);
        assertTrue(node.childs.isEmpty());
    }

    @Test
    public void checkTwoTasksParameterStringValuePipeline() throws IOException {
        Pipeline pipeline = load("/schemas/pipeline-tasks-linked-by-parameter-string-value.yaml");
        Map<String, PipelineGraphUpdater.Node> tree = PipelineGraphUpdater.generateTree(pipeline.getSpec().getTasks(), "");
        assertNotNull(tree);
        assertEquals(1, tree.size());
        PipelineGraphUpdater.Node node = tree.values().iterator().next();
        assertEquals(PipelineGraphUpdater.Type.TASK, node.type);
        assertEquals("first", node.name);
        assertNotNull(node.childs);
        assertEquals(1, node.childs.size());
        node = node.childs.iterator().next();
        assertEquals(PipelineGraphUpdater.Type.TASK, node.type);
        assertEquals("second", node.name);
        assertNotNull(node.childs);
        assertTrue(node.childs.isEmpty());
    }

    @Test
    public void checkTwoTasksParameterArrayValuePipeline() throws IOException {
        Pipeline pipeline = load("/schemas/pipeline-tasks-linked-by-parameter-array-value.yaml");
        Map<String, PipelineGraphUpdater.Node> tree = PipelineGraphUpdater.generateTree(pipeline.getSpec().getTasks(), "");
        assertNotNull(tree);
        assertEquals(1, tree.size());
        PipelineGraphUpdater.Node node = tree.values().iterator().next();
        assertEquals(PipelineGraphUpdater.Type.TASK, node.type);
        assertEquals("first", node.name);
        assertNotNull(node.childs);
        assertEquals(1, node.childs.size());
        node = node.childs.iterator().next();
        assertEquals(PipelineGraphUpdater.Type.TASK, node.type);
        assertEquals("second", node.name);
        assertNotNull(node.childs);
        assertTrue(node.childs.isEmpty());
    }
}
