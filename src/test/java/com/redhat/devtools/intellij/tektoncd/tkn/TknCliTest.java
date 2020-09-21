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

import com.redhat.devtools.intellij.common.utils.ExecHelper;
import com.redhat.devtools.intellij.tektoncd.tkn.component.field.Workspace;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.internal.util.reflection.FieldSetter;


import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

import static org.junit.Assert.assertEquals;

public class TknCliTest {

    private Tkn tkn;

    @Before
    public void before() throws Exception {
        this.tkn = mock(TknCli.class, org.mockito.Mockito.CALLS_REAL_METHODS);

        FieldSetter.setField(tkn, TknCli.class.getDeclaredField("command"), "command");
        FieldSetter.setField(tkn, TknCli.class.getDeclaredField("envVars"), Collections.emptyMap());

    }

    /*  ////////////////////////////////////////////////////////////
     *                          DELETE
     *  ////////////////////////////////////////////////////////////
     */

    @Test
    public void checkRightArgsWhenDeletingSinglePipeline() throws IOException {
        MockedStatic<ExecHelper> exec  = mockStatic(ExecHelper.class);
        exec.when(() -> ExecHelper.execute(anyString(), anyMap(), any())).thenReturn(null);
        tkn.deletePipelines("test", Arrays.asList("pp1"), false);
        exec.verify(() -> ExecHelper.execute(anyString(), anyMap(), eq("pipeline"), eq("delete"), eq("-f"), eq("pp1"), eq("-n"), eq("test")));
        exec.close();
    }

    @Test
    public void checkRightArgsWhenDeletingMultiplePipelines() throws IOException {
        MockedStatic<ExecHelper> exec  = mockStatic(ExecHelper.class);
        exec.when(() -> ExecHelper.execute(anyString(), anyMap(), any())).thenReturn(null);
        tkn.deletePipelines("test", Arrays.asList("pp1", "pp2", "pp3"), false);
        exec.verify(() -> ExecHelper.execute(anyString(), anyMap(), eq("pipeline"), eq("delete"), eq("-f"), eq("pp1"), eq("pp2"), eq("pp3"), eq("-n"), eq("test")));
        exec.close();
    }

    @Test
    public void checkRightArgsWhenDeletingPipelineAndItsRelatedResources() throws IOException {
        MockedStatic<ExecHelper> exec  = mockStatic(ExecHelper.class);
        exec.when(() -> ExecHelper.execute(anyString(), anyMap(), any())).thenReturn(null);
        tkn.deletePipelines("test", Arrays.asList("pp1"), true);
        exec.verify(() -> ExecHelper.execute(anyString(), anyMap(), eq("pipeline"), eq("delete"), eq("-f"), eq("pp1"), eq("--prs=true"), eq("-n"), eq("test")));
        exec.close();
    }

    @Test
    public void checkRightArgsWhenDeletingSingleTask() throws IOException {
        MockedStatic<ExecHelper> exec  = mockStatic(ExecHelper.class);
        exec.when(() -> ExecHelper.execute(anyString(), anyMap(), any())).thenReturn(null);
        tkn.deleteTasks("test", Arrays.asList("t1"), false);
        exec.verify(() -> ExecHelper.execute(anyString(), anyMap(), eq("task"), eq("delete"), eq("-f"), eq("t1"), eq("-n"), eq("test")));
        exec.close();
    }

    @Test
    public void checkRightArgsWhenDeletingMultipleTasks() throws IOException {
        MockedStatic<ExecHelper> exec  = mockStatic(ExecHelper.class);
        exec.when(() -> ExecHelper.execute(anyString(), anyMap(), any())).thenReturn(null);
        tkn.deleteTasks("test", Arrays.asList("t1", "t2", "t3"), false);
        exec.verify(() -> ExecHelper.execute(anyString(), anyMap(), eq("task"), eq("delete"), eq("-f"), eq("t1"), eq("t2"), eq("t3"), eq("-n"), eq("test")));
        exec.close();
    }

    @Test
    public void checkRightArgsWhenDeletingTaskAndItsRelatedResources() throws IOException {
        MockedStatic<ExecHelper> exec  = mockStatic(ExecHelper.class);
        exec.when(() -> ExecHelper.execute(anyString(), anyMap(), any())).thenReturn(null);
        tkn.deleteTasks("test", Arrays.asList("t1"), true);
        exec.verify(() -> ExecHelper.execute(anyString(), anyMap(), eq("task"), eq("delete"), eq("-f"), eq("t1"), eq("--trs=true"), eq("-n"), eq("test")));
        exec.close();
    }

    @Test
    public void checkRightArgsWhenDeletingSingleClusterTask() throws IOException {
        MockedStatic<ExecHelper> exec  = mockStatic(ExecHelper.class);
        exec.when(() -> ExecHelper.execute(anyString(), anyMap(), any())).thenReturn(null);
        tkn.deleteClusterTasks(Arrays.asList("t1"), false);
        exec.verify(() -> ExecHelper.execute(anyString(), anyMap(), eq("clustertask"), eq("delete"), eq("-f"), eq("t1")));
        exec.close();
    }

    @Test
    public void checkRightArgsWhenDeletingMultipleClusterTasks() throws IOException {
        MockedStatic<ExecHelper> exec  = mockStatic(ExecHelper.class);
        exec.when(() -> ExecHelper.execute(anyString(), anyMap(), any())).thenReturn(null);
        tkn.deleteClusterTasks(Arrays.asList("t1", "t2", "t3"), false);
        exec.verify(() -> ExecHelper.execute(anyString(), anyMap(), eq("clustertask"), eq("delete"), eq("-f"), eq("t1"), eq("t2"), eq("t3")));
        exec.close();
    }

    @Test
    public void checkRightArgsWhenDeletingClusterTaskAndItsRelatedResources() throws IOException {
        MockedStatic<ExecHelper> exec  = mockStatic(ExecHelper.class);
        exec.when(() -> ExecHelper.execute(anyString(), anyMap(), any())).thenReturn(null);
        tkn.deleteClusterTasks(Arrays.asList("t1"), true);
        exec.verify(() -> ExecHelper.execute(anyString(), anyMap(), eq("clustertask"), eq("delete"), eq("-f"), eq("t1"), eq("--trs=true")));
        exec.close();
    }

    /*  ////////////////////////////////////////////////////////////
     *                          START
     *  ////////////////////////////////////////////////////////////
     */

    @Test
    public void checkRightArgsWhenStartingPipelineWithParameters() throws IOException {
        Map<String, String> params = new HashMap<>();
        params.put("param1", "value1");
        params.put("param2", "value2");
        MockedStatic<ExecHelper> exec  = mockStatic(ExecHelper.class);
        exec.when(() -> ExecHelper.execute(anyString(), anyMap(), any())).thenReturn("");

        tkn.startPipeline("ns", "name", params, Collections.emptyMap(), "", Collections.emptyMap(), Collections.emptyMap(), "");
        exec.verify(() ->
                ExecHelper.execute(anyString(), anyMap(), eq("pipeline"), eq("start"), eq("name"), eq("-n"), eq("ns"),
                        eq("-p"), eq("param1=value1"), eq("-p"), eq("param2=value2")));
        exec.close();
    }

    @Test
    public void checkRightArgsWhenStartingPipelineWithResources() throws IOException {
        Map<String, String> resources = new HashMap<>();
        resources.put("res1", "value1");
        resources.put("res2", "value2");
        MockedStatic<ExecHelper> exec  = mockStatic(ExecHelper.class);
        exec.when(() -> ExecHelper.execute(anyString(), anyMap(), any())).thenReturn("");

        tkn.startPipeline("ns", "name", Collections.emptyMap(), resources, "", Collections.emptyMap(), Collections.emptyMap(), "");
        exec.verify(() ->
                ExecHelper.execute(anyString(), anyMap(), eq("pipeline"), eq("start"), eq("name"), eq("-n"), eq("ns"),
                        eq("-r"), eq("res1=value1"), eq("-r"), eq("res2=value2")));
        exec.close();
    }

    @Test
    public void checkRightArgsWhenStartingPipelineWithWorkspaces() throws IOException {
        Map<String, Workspace> workspaces = new HashMap<>();
        workspaces.put("work1", new Workspace("work1", Workspace.Kind.PVC, "value1"));
        workspaces.put("work2", new Workspace("work2", Workspace.Kind.CONFIGMAP, "value2"));
        workspaces.put("work3", new Workspace("work3", Workspace.Kind.SECRET, "value3"));
        workspaces.put("work4", new Workspace("work4", Workspace.Kind.EMPTYDIR, null));
        MockedStatic<ExecHelper> exec  = mockStatic(ExecHelper.class);
        exec.when(() -> ExecHelper.execute(anyString(), anyMap(), any())).thenReturn("");

        tkn.startPipeline("ns", "name", Collections.emptyMap(), Collections.emptyMap(), "", Collections.emptyMap(), workspaces, "");
        exec.verify(() ->
                ExecHelper.execute(anyString(), anyMap(), eq("pipeline"), eq("start"), eq("name"), eq("-n"), eq("ns"),
                        eq("-w"), eq("name=work2,config=value2"), eq("-w"), eq("name=work1,claimName=value1"),
                        eq("-w"), eq("name=work4,emptyDir="), eq("-w"), eq("name=work3,secret=value3")));
        exec.close();
    }

    @Test
    public void checkRightArgsWhenStartingPipelineWithServiceAccounts() throws IOException {
        Map<String, String> taskServiceAccount = new HashMap<>();
        taskServiceAccount.put("task1", "value1");
        taskServiceAccount.put("task2", "value2");
        MockedStatic<ExecHelper> exec  = mockStatic(ExecHelper.class);
        exec.when(() -> ExecHelper.execute(anyString(), anyMap(), any())).thenReturn("");

        tkn.startPipeline("ns", "name", Collections.emptyMap(), Collections.emptyMap(), "sa", taskServiceAccount, Collections.emptyMap(), "");
        exec.verify(() ->
                ExecHelper.execute(anyString(), anyMap(), eq("pipeline"), eq("start"), eq("name"), eq("-n"), eq("ns"),
                        eq("-s=sa"), eq("--task-serviceaccount"), eq("task1=value1"), eq("--task-serviceaccount"), eq("task2=value2")));
        exec.close();
    }

    @Test
    public void checkRightArgsWhenStartingPipelineWithPrefixName() throws IOException {
        MockedStatic<ExecHelper> exec  = mockStatic(ExecHelper.class);
        exec.when(() -> ExecHelper.execute(anyString(), anyMap(), any())).thenReturn("");

        tkn.startPipeline("ns", "name", Collections.emptyMap(), Collections.emptyMap(), "", Collections.emptyMap(), Collections.emptyMap(), "prefix");
        exec.verify(() ->
                ExecHelper.execute(anyString(), anyMap(), eq("pipeline"), eq("start"), eq("name"), eq("-n"), eq("ns"),
                        eq("--prefix-name=prefix")));
        exec.close();
    }

    @Test
    public void checkRightArgsWhenStartingPipelineWithMultipleInputs() throws IOException {
        Map<String, String> params = new HashMap<>();
        params.put("param1", "value1");
        Map<String, String> resources = new HashMap<>();
        resources.put("res1", "value1");
        resources.put("res2", "value2");
        Map<String, String> taskServiceAccount = new HashMap<>();
        taskServiceAccount.put("task1", "value1");
        Map<String, Workspace> workspaces = new HashMap<>();
        workspaces.put("work1", new Workspace("work1", Workspace.Kind.PVC, "value1"));
        workspaces.put("work2", new Workspace("work2", Workspace.Kind.SECRET, "value2"));
        MockedStatic<ExecHelper> exec  = mockStatic(ExecHelper.class);
        exec.when(() -> ExecHelper.execute(anyString(), anyMap(), any())).thenReturn("");

        tkn.startPipeline("ns", "name", params, resources, "sa", taskServiceAccount, workspaces, "prefix");
        exec.verify(() ->
                ExecHelper.execute(anyString(), anyMap(), eq("pipeline"), eq("start"), eq("name"), eq("-n"), eq("ns"),
                        eq("-s=sa"),
                        eq("--task-serviceaccount"), eq("task1=value1"),
                        eq("-w"), eq("name=work2,secret=value2"), eq("-w"), eq("name=work1,claimName=value1"),
                        eq("-p"), eq("param1=value1"),
                        eq("-r"), eq("res1=value1"), eq("-r"), eq("res2=value2"),
                        eq("--prefix-name=prefix")));
        exec.close();
    }

    @Test
    public void checkRightRunNameIsReturnedWhenStartingPipeline() throws IOException {
        MockedStatic<ExecHelper> exec  = mockStatic(ExecHelper.class);
        exec.when(() -> ExecHelper.execute(anyString(), anyMap(), any())).thenReturn("run:foo");

        String output = tkn.startPipeline("ns", "name", Collections.emptyMap(), Collections.emptyMap(), "", Collections.emptyMap(), Collections.emptyMap(), "");
        exec.verify(() ->
                ExecHelper.execute(anyString(), anyMap(), eq("pipeline"), eq("start"), eq("name"), eq("-n"), eq("ns")));
        exec.close();
        assertEquals(output, "foo");
    }

    @Test
    public void checkNullIsReturnedWhenStartingPipelineReturnInvalidResult() throws IOException {
        MockedStatic<ExecHelper> exec  = mockStatic(ExecHelper.class);
        exec.when(() -> ExecHelper.execute(anyString(), anyMap(), any())).thenReturn("");

        String output = tkn.startPipeline("ns", "name", Collections.emptyMap(), Collections.emptyMap(), "", Collections.emptyMap(), Collections.emptyMap(), "");
        exec.verify(() ->
                ExecHelper.execute(anyString(), anyMap(), eq("pipeline"), eq("start"), eq("name"), eq("-n"), eq("ns")));
        exec.close();
        assertNull(output);
    }

    @Test
    public void checkRightArgsWhenStartingTaskWithParameters() throws IOException {
        Map<String, String> params = new HashMap<>();
        params.put("param1", "value1");
        params.put("param2", "value2");
        MockedStatic<ExecHelper> exec  = mockStatic(ExecHelper.class);
        exec.when(() -> ExecHelper.execute(anyString(), anyMap(), any())).thenReturn("");

        tkn.startTask("ns", "name", params, Collections.emptyMap(), Collections.emptyMap(), "", Collections.emptyMap(), "");
        exec.verify(() ->
                ExecHelper.execute(anyString(), anyMap(), eq("task"), eq("start"), eq("name"), eq("-n"), eq("ns"),
                        eq("-p"), eq("param1=value1"), eq("-p"), eq("param2=value2")));
        exec.close();
    }

    @Test
    public void checkRightArgsWhenStartingTaskWithInputResources() throws IOException {
        Map<String, String> resources = new HashMap<>();
        resources.put("res1", "value1");
        resources.put("res2", "value2");
        MockedStatic<ExecHelper> exec  = mockStatic(ExecHelper.class);
        exec.when(() -> ExecHelper.execute(anyString(), anyMap(), any())).thenReturn("");

        tkn.startTask("ns", "name", Collections.emptyMap(), resources, Collections.emptyMap(), "", Collections.emptyMap(), "");
        exec.verify(() ->
                ExecHelper.execute(anyString(), anyMap(), eq("task"), eq("start"), eq("name"), eq("-n"), eq("ns"),
                        eq("-i"), eq("res1=value1"), eq("-i"), eq("res2=value2")));
        exec.close();
    }

    @Test
    public void checkRightArgsWhenStartingTaskWithOutputResources() throws IOException {
        Map<String, String> resources = new HashMap<>();
        resources.put("res1", "value1");
        resources.put("res2", "value2");
        MockedStatic<ExecHelper> exec  = mockStatic(ExecHelper.class);
        exec.when(() -> ExecHelper.execute(anyString(), anyMap(), any())).thenReturn("");

        tkn.startTask("ns", "name", Collections.emptyMap(), Collections.emptyMap(), resources, "", Collections.emptyMap(), "");
        exec.verify(() ->
                ExecHelper.execute(anyString(), anyMap(), eq("task"), eq("start"), eq("name"), eq("-n"), eq("ns"),
                        eq("-o"), eq("res1=value1"), eq("-o"), eq("res2=value2")));
        exec.close();
    }

    @Test
    public void checkRightArgsWhenStartingTaskWithWorkspaces() throws IOException {
        Map<String, Workspace> workspaces = new HashMap<>();
        workspaces.put("work1", new Workspace("work1", Workspace.Kind.PVC, "value1"));
        workspaces.put("work2", new Workspace("work2", Workspace.Kind.CONFIGMAP, "value2"));
        workspaces.put("work3", new Workspace("work3", Workspace.Kind.SECRET, "value3"));
        workspaces.put("work4", new Workspace("work4", Workspace.Kind.EMPTYDIR, null));
        MockedStatic<ExecHelper> exec  = mockStatic(ExecHelper.class);
        exec.when(() -> ExecHelper.execute(anyString(), anyMap(), any())).thenReturn("");

        tkn.startTask("ns", "name", Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap(), "", workspaces, "");
        exec.verify(() ->
                ExecHelper.execute(anyString(), anyMap(), eq("task"), eq("start"), eq("name"), eq("-n"), eq("ns"),
                        eq("-w"), eq("name=work2,config=value2"), eq("-w"), eq("name=work1,claimName=value1"),
                        eq("-w"), eq("name=work4,emptyDir="), eq("-w"), eq("name=work3,secret=value3")));
        exec.close();
    }

    @Test
    public void checkRightArgsWhenStartingTaskWithServiceAccounts() throws IOException {
        MockedStatic<ExecHelper> exec  = mockStatic(ExecHelper.class);
        exec.when(() -> ExecHelper.execute(anyString(), anyMap(), any())).thenReturn("");

        tkn.startTask("ns", "name", Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap(), "sa", Collections.emptyMap(), "");
        exec.verify(() ->
                ExecHelper.execute(anyString(), anyMap(), eq("task"), eq("start"), eq("name"), eq("-n"), eq("ns"), eq("-s=sa")));
        exec.close();
    }

    @Test
    public void checkRightArgsWhenStartingTaskWithPrefixName() throws IOException {
        MockedStatic<ExecHelper> exec  = mockStatic(ExecHelper.class);
        exec.when(() -> ExecHelper.execute(anyString(), anyMap(), any())).thenReturn("");

        tkn.startTask("ns", "name", Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap(), "", Collections.emptyMap(), "prefix");
        exec.verify(() ->
                ExecHelper.execute(anyString(), anyMap(), eq("task"), eq("start"), eq("name"), eq("-n"), eq("ns"),
                        eq("--prefix-name=prefix")));
        exec.close();
    }

    @Test
    public void checkRightArgsWhenStartingTaskWithMultipleInputs() throws IOException {
        Map<String, String> params = new HashMap<>();
        params.put("param1", "value1");
        Map<String, String> inputResources = new HashMap<>();
        inputResources.put("res1", "value1");
        inputResources.put("res2", "value2");
        Map<String, String> outputResources = new HashMap<>();
        outputResources.put("out1", "value1");
        Map<String, Workspace> workspaces = new HashMap<>();
        workspaces.put("work1", new Workspace("work1", Workspace.Kind.PVC, "value1"));
        workspaces.put("work2", new Workspace("work2", Workspace.Kind.SECRET, "value2"));
        MockedStatic<ExecHelper> exec  = mockStatic(ExecHelper.class);
        exec.when(() -> ExecHelper.execute(anyString(), anyMap(), any())).thenReturn("");

        tkn.startTask("ns", "name", params, inputResources, outputResources, "sa", workspaces, "prefix");
        exec.verify(() ->
                ExecHelper.execute(anyString(), anyMap(), eq("task"), eq("start"), eq("name"), eq("-n"), eq("ns"),
                        eq("-s=sa"),
                        eq("-w"), eq("name=work2,secret=value2"), eq("-w"), eq("name=work1,claimName=value1"),
                        eq("-p"), eq("param1=value1"),
                        eq("-i"), eq("res1=value1"), eq("-i"), eq("res2=value2"),
                        eq("-o"), eq("out1=value1"),
                        eq("--prefix-name=prefix")));
        exec.close();
    }

    @Test
    public void checkRightRunNameIsReturnedWhenStartingTask() throws IOException {
        MockedStatic<ExecHelper> exec  = mockStatic(ExecHelper.class);
        exec.when(() -> ExecHelper.execute(anyString(), anyMap(), any())).thenReturn("run:foo");

        String output = tkn.startTask("ns", "name", Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap(), "", Collections.emptyMap(), "");
        exec.verify(() ->
                ExecHelper.execute(anyString(), anyMap(), eq("task"), eq("start"), eq("name"), eq("-n"), eq("ns")));
        exec.close();
        assertEquals(output, "foo");
    }

    @Test
    public void checkNullIsReturnedWhenStartingTaskReturnInvalidResult() throws IOException {
        MockedStatic<ExecHelper> exec  = mockStatic(ExecHelper.class);
        exec.when(() -> ExecHelper.execute(anyString(), anyMap(), any())).thenReturn("");

        String output = tkn.startTask("ns", "name", Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap(), "", Collections.emptyMap(), "");
        exec.verify(() ->
                ExecHelper.execute(anyString(), anyMap(), eq("task"), eq("start"), eq("name"), eq("-n"), eq("ns")));
        exec.close();
        assertNull(output);
    }
}
