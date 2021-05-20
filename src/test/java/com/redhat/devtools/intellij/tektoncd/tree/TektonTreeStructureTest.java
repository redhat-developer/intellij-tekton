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
package com.redhat.devtools.intellij.tektoncd.tree;

import com.intellij.openapi.project.Project;
import com.redhat.devtools.intellij.common.utils.ConfigWatcher;
import com.redhat.devtools.intellij.tektoncd.tkn.PipelineRun;
import com.redhat.devtools.intellij.tektoncd.tkn.TaskRun;
import com.redhat.devtools.intellij.tektoncd.tkn.Tkn;
import com.redhat.devtools.intellij.tektoncd.tkn.TknCliFactory;
import io.fabric8.kubernetes.api.model.AuthInfo;
import io.fabric8.kubernetes.api.model.Cluster;
import io.fabric8.kubernetes.api.model.Config;
import io.fabric8.kubernetes.api.model.Context;
import io.fabric8.kubernetes.api.model.NamedAuthInfo;
import io.fabric8.kubernetes.api.model.NamedCluster;
import io.fabric8.kubernetes.api.model.NamedContext;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.tekton.pipeline.v1alpha1.Condition;
import java.io.IOException;
import java.lang.reflect.Field;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TektonTreeStructureTest {

    private TektonTreeStructure structure;
    private TektonRootNode root;
    private Project project;
    private Tkn tkn;
    private NamespaceNode parent;

    private ObjectMeta metadata1;
    private ObjectMeta metadata2;
    private ObjectMeta metadata3;

    private TknCliFactory tknCliFactory;
    private ConfigWatcher configWatcher;
    private Config config;

    @Before
    public void before() throws Exception {
        this.project = mock(Project.class);
        this.structure = mock(TektonTreeStructure.class, org.mockito.Mockito.CALLS_REAL_METHODS);
        this.root = spy(new TektonRootNode(project));
        this.tkn = mock(Tkn.class);
        when(root.getTkn()).thenReturn(tkn);
        this.parent = new NamespaceNode(root, "parent");
        Field rootField = TektonTreeStructure.class.getDeclaredField("root");
        rootField.setAccessible(true);
        rootField.set(structure, root);

        config = createConfig("cluster", "namespace", "token", "user");
        Field configField = TektonTreeStructure.class.getDeclaredField("config");
        configField.setAccessible(true);
        configField.set(structure, config);

        this.configWatcher = mock(ConfigWatcher.class);
        this.tknCliFactory = mock(TknCliFactory.class);

        this.metadata1 = new ObjectMeta();
        this.metadata1.setName("test1");

        this.metadata2 = new ObjectMeta();
        this.metadata2.setName("test2");

        this.metadata3 = new ObjectMeta();
        this.metadata3.setName("test3");

    }

    /*  ////////////////////////////////////////////////////////////
     *                          CONDITIONS
     *  ////////////////////////////////////////////////////////////
     */

    @Test
    public void checkNoConditionReturnEmptyArray() throws IOException {
        List<Condition> resultConditions = new ArrayList<>();
        when(tkn.getConditions(anyString())).thenReturn(resultConditions);

        ConditionsNode node = new ConditionsNode(root, parent);
        Object[] conditions = structure.getChildElements(node);

        assertTrue(conditions.length == 0);
    }

    @Test
    public void checkSingleConditionReturnOneElementArray() throws IOException {
        Condition condition = new Condition();
        condition.setMetadata(metadata1);

        List<Condition> resultConditions = new ArrayList<>();
        resultConditions.add(condition);

        when(tkn.getConditions(anyString())).thenReturn(resultConditions);

        ConditionsNode node = new ConditionsNode(root, parent);
        Object[] conditions = structure.getChildElements(node);

        assertTrue(conditions.length == 1);
        assertEquals(((ParentableNode) conditions[0]).getName(), "test1");
    }

    @Test
    public void checkMultipleConditionReturnMultipleItemsArray() throws IOException {
        Condition condition1 = new Condition();
        condition1.setMetadata(metadata1);

        Condition condition2 = new Condition();
        condition2.setMetadata(metadata2);

        Condition condition3 = new Condition();
        condition3.setMetadata(metadata3);

        List<Condition> resultConditions = new ArrayList<>();
        resultConditions.add(condition1);
        resultConditions.add(condition2);
        resultConditions.add(condition3);

        when(tkn.getConditions(anyString())).thenReturn(resultConditions);

        ConditionsNode node = new ConditionsNode(root, parent);
        Object[] conditions = structure.getChildElements(node);

        assertTrue(conditions.length == 3);
        assertEquals(((ParentableNode) conditions[0]).getName(), "test1");
        assertEquals(((ParentableNode) conditions[1]).getName(), "test2");
        assertEquals(((ParentableNode) conditions[2]).getName(), "test3");
    }

    @Test
    public void checkFailTknConditionsCallReturnMessageNode() throws IOException {
        List<Condition> resultConditions = new ArrayList<>();
        when(tkn.getConditions(anyString())).thenReturn(resultConditions);
        when(tkn.getConditions(anyString())).thenThrow(new IOException());

        ConditionsNode node = new ConditionsNode(root, parent);
        Object[] conditions = structure.getChildElements(node);

        assertTrue(conditions.length == 1);
        assertTrue(conditions[0] instanceof MessageNode);
        assertEquals(((MessageNode)conditions[0]).getName(), "Failed to load conditions");
    }

    /*  ////////////////////////////////////////////////////////////
     *                          TASKRUNS
     *                       Tests with TASK
     *  ////////////////////////////////////////////////////////////
     */

    @Test
    public void checkIfTaskHasNoRunsReturnsEmptyArray() throws IOException {
        List<TaskRun> resultTaskRuns = new ArrayList<>();
        when(tkn.getTaskRuns(anyString(), anyString())).thenReturn(resultTaskRuns);

        TaskNode node = new TaskNode(root, (ParentableNode) parent, "task", false);
        Object[] taskruns = structure.getChildElements(node);

        assertTrue(taskruns.length == 0);
    }

    @Test
    public void checkifTaskHasOneRunReturnsOneElementArray() throws IOException {
        TaskRun taskRun = new TaskRun("test", "", "", Optional.of(true), Instant.now(), Instant.now(), null, "");

        List<TaskRun> resultTaskRuns = new ArrayList<>();
        resultTaskRuns.add(taskRun);

        when(tkn.getTaskRuns(anyString(), anyString())).thenReturn(resultTaskRuns);

        TaskNode node = new TaskNode(root, (ParentableNode) parent, "task", false);
        Object[] taskruns = structure.getChildElements(node);

        assertTrue(taskruns.length == 1);
        assertEquals(((ParentableNode) taskruns[0]).getName(), "test");
    }

    @Test
    public void checkIfTaskWithMultipleRunsReturnOrderedMultipleItemsArray() throws IOException {
        TaskRun taskRun1 = new TaskRun("test1", "", "", Optional.of(true), Instant.now().minusMillis(1000), Instant.now().minusMillis(999), null, "");
        TaskRun taskRun2 = new TaskRun("test2", "", "", Optional.of(true), Instant.now().minusMillis(900), Instant.now().minusMillis(899), null, "");
        TaskRun taskRun3 = new TaskRun("test3", "", "", Optional.of(true), Instant.now().minusMillis(800), Instant.now().minusMillis(799), null, "");

        List<TaskRun> resultTaskRuns = new ArrayList<>();
        resultTaskRuns.add(taskRun3);
        resultTaskRuns.add(taskRun2);
        resultTaskRuns.add(taskRun1);

        when(tkn.getTaskRuns(anyString(), anyString())).thenReturn(resultTaskRuns);

        TaskNode node = new TaskNode(root, (ParentableNode) parent, "task", false);
        Object[] taskruns = structure.getChildElements(node);

        assertTrue(taskruns.length == 3);
        assertEquals(((ParentableNode) taskruns[0]).getName(), "test3");
        assertEquals(((ParentableNode) taskruns[1]).getName(), "test2");
        assertEquals(((ParentableNode) taskruns[2]).getName(), "test1");
    }

    @Test
    public void checkIfFailTknTaskNodeCallReturnCorrectMessageNode() throws IOException {
        List<TaskRun> resultTaskRuns = new ArrayList<>();
        when(tkn.getTaskRuns(anyString(), anyString())).thenReturn(resultTaskRuns);
        when(tkn.getTaskRuns(anyString(), anyString())).thenThrow(new IOException());

        TaskNode node = new TaskNode(root, (ParentableNode) parent, "task", false);
        Object[] taskruns = structure.getChildElements(node);

        assertTrue(taskruns.length == 1);
        assertTrue(taskruns[0] instanceof MessageNode);
        assertEquals(((MessageNode)taskruns[0]).getName(), "Failed to load task runs");
    }

    /*  ////////////////////////////////////////////////////////////
     *                          TASKRUNS
     *                  Tests with TASKRUNSNODE
     *  ////////////////////////////////////////////////////////////
     */
    @Test
    public void checkIfClusterHasNoTaskRunsReturnsEmptyArray() throws IOException {
        List<TaskRun> resultTaskRuns = new ArrayList<>();
        when(tkn.getTaskRuns(anyString(), anyString())).thenReturn(resultTaskRuns);

        TaskRunsNode node = new TaskRunsNode(root, parent);
        Object[] taskruns = structure.getChildElements(node);

        assertTrue(taskruns.length == 0);
    }

    @Test
    public void checkifClusterHasOneRunReturnsOneElementArray() throws IOException {
        TaskRun taskRun = new TaskRun("test", "", "", Optional.of(true), Instant.now(), Instant.now(), null, "");

        List<TaskRun> resultTaskRuns = new ArrayList<>();
        resultTaskRuns.add(taskRun);

        when(tkn.getTaskRuns(anyString(), anyString())).thenReturn(resultTaskRuns);

        TaskRunsNode node = new TaskRunsNode(root, parent);
        Object[] taskruns = structure.getChildElements(node);

        assertTrue(taskruns.length == 1);
        assertEquals(((ParentableNode) taskruns[0]).getName(), "test");
    }

    @Test
    public void checkIfClusterWithMultipleRunsReturnOrderedMultipleItemsArray() throws IOException {
        TaskRun taskRun1 = new TaskRun("test1", "", "", Optional.of(true), Instant.now().minusMillis(1000), Instant.now().minusMillis(999), null, "");
        TaskRun taskRun2 = new TaskRun("test2", "", "", Optional.of(true), Instant.now().minusMillis(900), Instant.now().minusMillis(899), null, "");
        TaskRun taskRun3 = new TaskRun("test3", "", "", Optional.of(true), Instant.now().minusMillis(800), Instant.now().minusMillis(799), null, "");

        List<TaskRun> resultTaskRuns = new ArrayList<>();
        resultTaskRuns.add(taskRun3);
        resultTaskRuns.add(taskRun2);
        resultTaskRuns.add(taskRun1);

        when(tkn.getTaskRuns(anyString(), anyString())).thenReturn(resultTaskRuns);

        TaskRunsNode node = new TaskRunsNode(root, parent);
        Object[] taskruns = structure.getChildElements(node);

        assertTrue(taskruns.length == 3);
        assertEquals(((ParentableNode) taskruns[0]).getName(), "test3");
        assertEquals(((ParentableNode) taskruns[1]).getName(), "test2");
        assertEquals(((ParentableNode) taskruns[2]).getName(), "test1");
    }

    @Test
    public void checkIfFailTknTaskRunsNodeCallReturnCorrectMessageNode() throws IOException {
        List<TaskRun> resultTaskRuns = new ArrayList<>();
        when(tkn.getTaskRuns(anyString(), anyString())).thenReturn(resultTaskRuns);
        when(tkn.getTaskRuns(anyString(), anyString())).thenThrow(new IOException());

        TaskRunsNode node = new TaskRunsNode(root, parent);
        Object[] taskruns = structure.getChildElements(node);

        assertTrue(taskruns.length == 1);
        assertTrue(taskruns[0] instanceof MessageNode);
        assertEquals(((MessageNode)taskruns[0]).getName(), "Failed to load task runs");
    }

    /*  ////////////////////////////////////////////////////////////
     *                          TASKRUNS
     *                   Tests with TASKRUNNODE
     *  ////////////////////////////////////////////////////////////
     */
    @Test
    public void checkIfTaskRunHasNoTaskRunsReturnsEmptyArray() {
        TaskRun run = new TaskRun("run", "", "", Optional.of(true), Instant.now(), Instant.now(), null, "");
        TaskRunNode node = new TaskRunNode(root, parent, run);
        Object[] taskruns = structure.getChildElements(node);

        assertTrue(taskruns.length == 0);
    }

    @Test
    public void checkifTaskRunHasOneRunReturnsOneElementArray() {
        TaskRun taskRun = new TaskRun("test", "", "", Optional.of(true), Instant.now(), Instant.now(), null, "");

        List<TaskRun> resultTaskRuns = new ArrayList<>();
        resultTaskRuns.add(taskRun);

        TaskRun run = new TaskRun("run", "", "", Optional.of(true), Instant.now(), Instant.now(), resultTaskRuns, "");
        TaskRunNode node = new TaskRunNode(root, parent, run);
        Object[] taskruns = structure.getChildElements(node);

        assertTrue(taskruns.length == 1);
        assertEquals(((ParentableNode) taskruns[0]).getName(), "test");
    }

    @Test
    public void checkIfTaskRunWithMultipleRunsReturnOrderedMultipleItemsArray() {
        TaskRun taskRun1 = new TaskRun("test1", "", "", Optional.of(true), Instant.now().minusMillis(1000), Instant.now().minusMillis(999), null, "");
        TaskRun taskRun2 = new TaskRun("test2", "", "", Optional.of(true), Instant.now().minusMillis(900), Instant.now().minusMillis(899), null, "");
        TaskRun taskRun3 = new TaskRun("test3", "", "", Optional.of(true), Instant.now().minusMillis(800), Instant.now().minusMillis(799), null, "");

        List<TaskRun> resultTaskRuns = new ArrayList<>();
        resultTaskRuns.add(taskRun3);
        resultTaskRuns.add(taskRun2);
        resultTaskRuns.add(taskRun1);

        TaskRun run = new TaskRun("run", "", "", Optional.of(true), Instant.now(), Instant.now(), resultTaskRuns, "");
        TaskRunNode node = new TaskRunNode(root, parent, run);
        Object[] taskruns = structure.getChildElements(node);

        assertTrue(taskruns.length == 3);
        assertEquals(((ParentableNode) taskruns[0]).getName(), "test1");
        assertEquals(((ParentableNode) taskruns[1]).getName(), "test2");
        assertEquals(((ParentableNode) taskruns[2]).getName(), "test3");
    }

    /*  ////////////////////////////////////////////////////////////
     *                          TASKRUNS
     *                   Tests with PIPELINERUNNODE
     *  ////////////////////////////////////////////////////////////
     */
    @Test
    public void checkIfPipelineRunHasNoTaskRunsReturnsEmptyArray() {
        PipelineRun run = new PipelineRun("run", Optional.of(true), Instant.now(), Instant.now(), null);
        PipelineRunNode node = new PipelineRunNode(root, parent, run);
        Object[] taskruns = structure.getChildElements(node);

        assertTrue(taskruns.length == 0);
    }

    @Test
    public void checkifPipelineRunHasOneRunReturnsOneElementArray() {
        TaskRun taskRun = new TaskRun("test", "", "", Optional.of(true), Instant.now(), Instant.now(), null, "");

        List<TaskRun> resultTaskRuns = new ArrayList<>();
        resultTaskRuns.add(taskRun);

        PipelineRun run = new PipelineRun("run", Optional.of(true), Instant.now(), Instant.now(), resultTaskRuns);
        PipelineRunNode node = new PipelineRunNode(root, parent, run);
        Object[] taskruns = structure.getChildElements(node);

        assertTrue(taskruns.length == 1);
        assertEquals(((ParentableNode) taskruns[0]).getName(), "test");
    }

    @Test
    public void checkIfPipelineRunWithMultipleRunsReturnOrderedMultipleItemsArray() {
        TaskRun taskRun1 = new TaskRun("test1", "", "", Optional.of(true), Instant.now().minusMillis(1000), Instant.now().minusMillis(999), null, "");
        TaskRun taskRun2 = new TaskRun("test2", "", "", Optional.of(true), Instant.now().minusMillis(900), Instant.now().minusMillis(899), null, "");
        TaskRun taskRun3 = new TaskRun("test3", "", "", Optional.of(true), Instant.now().minusMillis(800), Instant.now().minusMillis(799), null, "");

        List<TaskRun> resultTaskRuns = new ArrayList<>();
        resultTaskRuns.add(taskRun3);
        resultTaskRuns.add(taskRun2);
        resultTaskRuns.add(taskRun1);

        PipelineRun run = new PipelineRun("run", Optional.of(true), Instant.now(), Instant.now(), resultTaskRuns);
        PipelineRunNode node = new PipelineRunNode(root, parent, run);
        Object[] taskruns = structure.getChildElements(node);

        assertTrue(taskruns.length == 3);
        assertEquals(((ParentableNode) taskruns[0]).getName(), "test1");
        assertEquals(((ParentableNode) taskruns[1]).getName(), "test2");
        assertEquals(((ParentableNode) taskruns[2]).getName(), "test3");
    }

    @Test
    public void OnUpdate_ConfigIsNotChanged_NoRefresh() {
        try(MockedStatic<TknCliFactory> tknCliFactoryMockedStatic = mockStatic(TknCliFactory.class)) {
            tknCliFactoryMockedStatic.when(() -> TknCliFactory.getInstance()).thenReturn(tknCliFactory);
            structure.onUpdate(configWatcher, config);
            verify(structure, times(0)).refresh();
        }
    }

    @Test
    public void OnUpdate_ClusterHasBeenChanged_Refresh() {
        Config newConfig = createConfig("changed", "namespace", "token", "user");

        try(MockedStatic<TknCliFactory> tknCliFactoryMockedStatic = mockStatic(TknCliFactory.class)) {
            tknCliFactoryMockedStatic.when(() -> TknCliFactory.getInstance()).thenReturn(tknCliFactory);
            structure.onUpdate(configWatcher, newConfig);
            verify(structure, times(1)).refresh();
        }
    }

    @Test
    public void OnUpdate_UserHasBeenChanged_Refresh() {
        Config newConfig = createConfig("cluster", "namespace", "token", "changed");

        try(MockedStatic<TknCliFactory> tknCliFactoryMockedStatic = mockStatic(TknCliFactory.class)) {
            tknCliFactoryMockedStatic.when(() -> TknCliFactory.getInstance()).thenReturn(tknCliFactory);
            structure.onUpdate(configWatcher, newConfig);
            verify(structure, times(1)).refresh();
        }
    }

    @Test
    public void OnUpdate_NamespaceHasBeenChanged_Refresh() {
        Config newConfig = createConfig("cluster", "changed", "token", "user");

        try(MockedStatic<TknCliFactory> tknCliFactoryMockedStatic = mockStatic(TknCliFactory.class)) {
            tknCliFactoryMockedStatic.when(() -> TknCliFactory.getInstance()).thenReturn(tknCliFactory);
            structure.onUpdate(configWatcher, newConfig);
            verify(structure, times(1)).refresh();
        }
    }

    @Test
    public void OnUpdate_TokenHasBeenChanged_Refresh() {
        Config newConfig = createConfig("cluster", "namespace", "changed", "user");

        try(MockedStatic<TknCliFactory> tknCliFactoryMockedStatic = mockStatic(TknCliFactory.class)) {
            tknCliFactoryMockedStatic.when(() -> TknCliFactory.getInstance()).thenReturn(tknCliFactory);
            structure.onUpdate(configWatcher, newConfig);
            verify(structure, times(1)).refresh();
        }
    }

    private Config createConfig(String nameCluster, String namespace, String token, String user) {
        Config config = new Config();
        config.setApiVersion("v1");
        config.setKind("Config");

        Cluster cluster = new Cluster();
        cluster.setServer("server");

        NamedCluster namedCluster = new NamedCluster();
        namedCluster.setCluster(cluster);
        namedCluster.setName(nameCluster);

        config.setClusters(Arrays.asList(namedCluster));

        Context context = new Context();
        context.setNamespace(namespace);
        context.setCluster(nameCluster);
        context.setUser(user);

        NamedContext namedContext = new NamedContext();
        namedContext.setName("test");
        namedContext.setContext(context);

        config.setContexts(Arrays.asList(namedContext));

        AuthInfo authInfo = new AuthInfo();
        authInfo.setToken(token);

        NamedAuthInfo namedAuthInfo = new NamedAuthInfo();
        namedAuthInfo.setName(user);
        namedAuthInfo.setUser(authInfo);

        config.setUsers(Arrays.asList(namedAuthInfo));
        config.setCurrentContext("test");

        return config;
    }
}
