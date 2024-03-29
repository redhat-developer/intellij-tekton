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
import com.redhat.devtools.intellij.tektoncd.tkn.component.field.Input;
import com.redhat.devtools.intellij.tektoncd.tkn.component.field.Workspace;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaim;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaimList;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaimSpec;
import io.fabric8.kubernetes.api.model.Quantity;
import io.fabric8.kubernetes.api.model.ResourceRequirements;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.redhat.devtools.intellij.tektoncd.Constants.FLAG_SKIP_OPTIONAL_WORKSPACES;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TknCliTest {

    private Tkn tkn;
    private KubernetesClient kubernetesClient;

    @Before
    public void before() throws Exception {
        this.tkn = mock(TknCli.class, org.mockito.Mockito.CALLS_REAL_METHODS);
        this.kubernetesClient = mock(KubernetesClient.class);

        Field clientField = TknCli.class.getDeclaredField("client");
        clientField.setAccessible(true);
        clientField.set(tkn, kubernetesClient);
        Field commandField = TknCli.class.getDeclaredField("command");
        commandField.setAccessible(true);
        commandField.set(tkn, "command");
        Field envField = TknCli.class.getDeclaredField("envVars");
        envField.setAccessible(true);
        envField.set(tkn, Collections.emptyMap());

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
        Map<String, Input> params = new HashMap<>();
        params.put("param1", new Input("param1", "string", Input.Kind.PARAMETER, "value1", Optional.empty(), Optional.empty()));
        params.put("param2", new Input("param2", "string", Input.Kind.PARAMETER, "value2", Optional.empty(), Optional.empty()));
        MockedStatic<ExecHelper> exec  = mockStatic(ExecHelper.class);
        exec.when(() -> ExecHelper.execute(anyString(), anyMap(), any())).thenReturn("");

        tkn.startPipeline("ns", "name", params, "", Collections.emptyMap(), Collections.emptyMap(), "");
        exec.verify(() ->
                ExecHelper.execute(anyString(), anyMap(), eq("pipeline"), eq("start"), eq("name"), eq("-n"), eq("ns"),
                        eq("-p"), eq("param1=value1"), eq("-p"), eq("param2=value2"), eq(FLAG_SKIP_OPTIONAL_WORKSPACES)));
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

        tkn.startPipeline("ns", "name", Collections.emptyMap(), "", Collections.emptyMap(), workspaces, "");
        exec.verify(() ->
                ExecHelper.execute(anyString(), anyMap(), eq("pipeline"), eq("start"), eq("name"), eq("-n"), eq("ns"),
                        eq("-w"), eq("name=work2,config=value2"), eq("-w"), eq("name=work1,claimName=value1"),
                        eq("-w"), eq("name=work4,emptyDir="), eq("-w"), eq("name=work3,secret=value3"), eq(FLAG_SKIP_OPTIONAL_WORKSPACES)));
        exec.close();
    }

    @Test
    public void checkRightArgsWhenStartingPipelineWithServiceAccounts() throws IOException {
        Map<String, String> taskServiceAccount = new HashMap<>();
        taskServiceAccount.put("task1", "value1");
        taskServiceAccount.put("task2", "value2");
        MockedStatic<ExecHelper> exec  = mockStatic(ExecHelper.class);
        exec.when(() -> ExecHelper.execute(anyString(), anyMap(), any())).thenReturn("");

        tkn.startPipeline("ns", "name", Collections.emptyMap(), "sa", taskServiceAccount, Collections.emptyMap(), "");
        exec.verify(() ->
                ExecHelper.execute(anyString(), anyMap(), eq("pipeline"), eq("start"), eq("name"), eq("-n"), eq("ns"),
                        eq("-s=sa"), eq("--task-serviceaccount"), eq("task1=value1"), eq("--task-serviceaccount"), eq("task2=value2"), eq(FLAG_SKIP_OPTIONAL_WORKSPACES)));
        exec.close();
    }

    @Test
    public void checkRightArgsWhenStartingPipelineWithPrefixName() throws IOException {
        MockedStatic<ExecHelper> exec  = mockStatic(ExecHelper.class);
        exec.when(() -> ExecHelper.execute(anyString(), anyMap(), any())).thenReturn("");

        tkn.startPipeline("ns", "name", Collections.emptyMap(), "", Collections.emptyMap(), Collections.emptyMap(), "prefix");
        exec.verify(() ->
                ExecHelper.execute(anyString(), anyMap(), eq("pipeline"), eq("start"), eq("name"), eq("-n"), eq("ns"),
                        eq("--prefix-name=prefix"), eq(FLAG_SKIP_OPTIONAL_WORKSPACES)));
        exec.close();
    }

    @Test
    public void checkRightArgsWhenStartingPipelineWithMultipleInputs() throws IOException {
        Map<String, Input> params = new HashMap<>();
        params.put("param1", new Input("param1", "string", Input.Kind.PARAMETER, "value1", Optional.empty(), Optional.empty()));
        Map<String, String> taskServiceAccount = new HashMap<>();
        taskServiceAccount.put("task1", "value1");
        Map<String, Workspace> workspaces = new HashMap<>();
        workspaces.put("work1", new Workspace("work1", Workspace.Kind.PVC, "value1"));
        workspaces.put("work2", new Workspace("work2", Workspace.Kind.SECRET, "value2"));
        MockedStatic<ExecHelper> exec  = mockStatic(ExecHelper.class);
        exec.when(() -> ExecHelper.execute(anyString(), anyMap(), any())).thenReturn("");

        tkn.startPipeline("ns", "name", params, "sa", taskServiceAccount, workspaces, "prefix");
        exec.verify(() ->
                ExecHelper.execute(anyString(), anyMap(), eq("pipeline"), eq("start"), eq("name"), eq("-n"), eq("ns"),
                        eq("-s=sa"),
                        eq("--task-serviceaccount"), eq("task1=value1"),
                        eq("-w"), eq("name=work2,secret=value2"), eq("-w"), eq("name=work1,claimName=value1"),
                        eq("-p"), eq("param1=value1"),
                        eq("--prefix-name=prefix"), eq(FLAG_SKIP_OPTIONAL_WORKSPACES)));
        exec.close();
    }

    @Test
    public void checkRightRunNameIsReturnedWhenStartingPipeline() throws IOException {
        MockedStatic<ExecHelper> exec  = mockStatic(ExecHelper.class);
        exec.when(() -> ExecHelper.execute(anyString(), anyMap(), any())).thenReturn("run:foo");

        String output = tkn.startPipeline("ns", "name", Collections.emptyMap(), "", Collections.emptyMap(), Collections.emptyMap(), "");
        exec.verify(() ->
                ExecHelper.execute(anyString(), anyMap(), eq("pipeline"), eq("start"), eq("name"), eq("-n"), eq("ns"), eq(FLAG_SKIP_OPTIONAL_WORKSPACES)));
        exec.close();
        assertEquals(output, "foo");
    }

    @Test
    public void checkNullIsReturnedWhenStartingPipelineReturnInvalidResult() throws IOException {
        MockedStatic<ExecHelper> exec  = mockStatic(ExecHelper.class);
        exec.when(() -> ExecHelper.execute(anyString(), anyMap(), any())).thenReturn("");

        String output = tkn.startPipeline("ns", "name", Collections.emptyMap(), "", Collections.emptyMap(), Collections.emptyMap(), "");
        exec.verify(() ->
                ExecHelper.execute(anyString(), anyMap(), eq("pipeline"), eq("start"), eq("name"), eq("-n"), eq("ns"), eq(FLAG_SKIP_OPTIONAL_WORKSPACES)));
        exec.close();
        assertNull(output);
    }

    @Test
    public void checkRightArgsWhenStartingTaskWithParameters() throws IOException {
        Map<String, Input> params = new HashMap<>();
        params.put("param1", new Input("param1", "string", Input.Kind.PARAMETER, "value1", Optional.empty(), Optional.empty()));
        params.put("param2", new Input("param2", "string", Input.Kind.PARAMETER, "value2", Optional.empty(), Optional.empty()));
        MockedStatic<ExecHelper> exec  = mockStatic(ExecHelper.class);
        exec.when(() -> ExecHelper.execute(anyString(), anyMap(), any())).thenReturn("");

        tkn.startTask("ns", "name", params, "", Collections.emptyMap(), "");
        exec.verify(() ->
                ExecHelper.execute(anyString(), anyMap(), eq("task"), eq("start"), eq("name"), eq("-n"), eq("ns"),
                        eq("-p"), eq("param1=value1"), eq("-p"), eq("param2=value2"), eq(FLAG_SKIP_OPTIONAL_WORKSPACES)));
        exec.close();
    }

    @Test
    public void checkRightArgsWhenStartingTaskWithInputResources() throws IOException {
        MockedStatic<ExecHelper> exec  = mockStatic(ExecHelper.class);
        exec.when(() -> ExecHelper.execute(anyString(), anyMap(), any())).thenReturn("");

        tkn.startTask("ns", "name", Collections.emptyMap(), "", Collections.emptyMap(), "");
        exec.verify(() ->
                ExecHelper.execute(anyString(), anyMap(), eq("task"), eq("start"), eq("name"), eq("-n"), eq("ns"),
                        eq(FLAG_SKIP_OPTIONAL_WORKSPACES)));
        exec.close();
    }

    @Test
    public void checkRightArgsWhenStartingTaskWithOutputResources() throws IOException {
        MockedStatic<ExecHelper> exec  = mockStatic(ExecHelper.class);
        exec.when(() -> ExecHelper.execute(anyString(), anyMap(), any())).thenReturn("");

        tkn.startTask("ns", "name", Collections.emptyMap(), "", Collections.emptyMap(), "");
        exec.verify(() ->
                ExecHelper.execute(anyString(), anyMap(), eq("task"), eq("start"), eq("name"), eq("-n"), eq("ns"),
                        eq(FLAG_SKIP_OPTIONAL_WORKSPACES)));
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

        tkn.startTask("ns", "name", Collections.emptyMap(), "", workspaces, "");
        exec.verify(() ->
                ExecHelper.execute(anyString(), anyMap(), eq("task"), eq("start"), eq("name"), eq("-n"), eq("ns"),
                        eq("-w"), eq("name=work2,config=value2"), eq("-w"), eq("name=work1,claimName=value1"),
                        eq("-w"), eq("name=work4,emptyDir="), eq("-w"), eq("name=work3,secret=value3"), eq(FLAG_SKIP_OPTIONAL_WORKSPACES)));
        exec.close();
    }

    @Test
    public void checkRightArgsWhenStartingTaskWithServiceAccounts() throws IOException {
        MockedStatic<ExecHelper> exec  = mockStatic(ExecHelper.class);
        exec.when(() -> ExecHelper.execute(anyString(), anyMap(), any())).thenReturn("");

        tkn.startTask("ns", "name", Collections.emptyMap(), "sa", Collections.emptyMap(), "");
        exec.verify(() ->
                ExecHelper.execute(anyString(), anyMap(), eq("task"), eq("start"), eq("name"), eq("-n"), eq("ns"), eq("-s=sa"), eq(FLAG_SKIP_OPTIONAL_WORKSPACES)));
        exec.close();
    }

    @Test
    public void checkRightArgsWhenStartingTaskWithPrefixName() throws IOException {
        MockedStatic<ExecHelper> exec  = mockStatic(ExecHelper.class);
        exec.when(() -> ExecHelper.execute(anyString(), anyMap(), any())).thenReturn("");

        tkn.startTask("ns", "name", Collections.emptyMap(), "", Collections.emptyMap(), "prefix");
        exec.verify(() ->
                ExecHelper.execute(anyString(), anyMap(), eq("task"), eq("start"), eq("name"), eq("-n"), eq("ns"),
                        eq("--prefix-name=prefix"), eq(FLAG_SKIP_OPTIONAL_WORKSPACES)));
        exec.close();
    }

    @Test
    public void checkRightArgsWhenStartingTaskWithMultipleInputs() throws IOException {
        Map<String, Input> params = new HashMap<>();
        params.put("param1", new Input("param1", "string", Input.Kind.PARAMETER, "value1", Optional.empty(), Optional.empty()));
        Map<String, Workspace> workspaces = new HashMap<>();
        workspaces.put("work1", new Workspace("work1", Workspace.Kind.PVC, "value1"));
        workspaces.put("work2", new Workspace("work2", Workspace.Kind.SECRET, "value2"));
        MockedStatic<ExecHelper> exec  = mockStatic(ExecHelper.class);
        exec.when(() -> ExecHelper.execute(anyString(), anyMap(), any())).thenReturn("");

        tkn.startTask("ns", "name", params, "sa", workspaces, "prefix");
        exec.verify(() ->
                ExecHelper.execute(anyString(), anyMap(), eq("task"), eq("start"), eq("name"), eq("-n"), eq("ns"),
                        eq("-s=sa"),
                        eq("-w"), eq("name=work2,secret=value2"), eq("-w"), eq("name=work1,claimName=value1"),
                        eq("-p"), eq("param1=value1"),
                        eq("--prefix-name=prefix"), eq(FLAG_SKIP_OPTIONAL_WORKSPACES)));
        exec.close();
    }

    @Test
    public void checkRightRunNameIsReturnedWhenStartingTask() throws IOException {
        MockedStatic<ExecHelper> exec  = mockStatic(ExecHelper.class);
        exec.when(() -> ExecHelper.execute(anyString(), anyMap(), any())).thenReturn("run:foo");

        String output = tkn.startTask("ns", "name", Collections.emptyMap(), "", Collections.emptyMap(), "");
        exec.verify(() ->
                ExecHelper.execute(anyString(), anyMap(), eq("task"), eq("start"), eq("name"), eq("-n"), eq("ns"), eq(FLAG_SKIP_OPTIONAL_WORKSPACES)));
        exec.close();
        assertEquals(output, "foo");
    }

    @Test
    public void checkNullIsReturnedWhenStartingTaskReturnInvalidResult() throws IOException {
        MockedStatic<ExecHelper> exec  = mockStatic(ExecHelper.class);
        exec.when(() -> ExecHelper.execute(anyString(), anyMap(), any())).thenReturn("");

        String output = tkn.startTask("ns", "name", Collections.emptyMap(), "", Collections.emptyMap(), "");
        exec.verify(() ->
                ExecHelper.execute(anyString(), anyMap(), eq("task"), eq("start"), eq("name"), eq("-n"), eq("ns"), eq(FLAG_SKIP_OPTIONAL_WORKSPACES)));
        exec.close();
        assertNull(output);
    }

    @Test
    public void checkRightArgsWhenStartingClusterTaskWithParameters() throws IOException {
        Map<String, Input> params = new HashMap<>();
        params.put("param1", new Input("param1", "string", Input.Kind.PARAMETER, "value1", Optional.empty(), Optional.empty()));
        params.put("param2", new Input("param2", "string", Input.Kind.PARAMETER, "value2", Optional.empty(), Optional.empty()));
        MockedStatic<ExecHelper> exec  = mockStatic(ExecHelper.class);
        exec.when(() -> ExecHelper.execute(anyString(), anyMap(), any())).thenReturn("");

        tkn.startClusterTask("ns", "name", params, "", Collections.emptyMap(), "");
        exec.verify(() ->
                ExecHelper.execute(anyString(), anyMap(), eq("clustertask"), eq("start"), eq("name"), eq("-n"), eq("ns"),
                        eq("-p"), eq("param1=value1"), eq("-p"), eq("param2=value2"), eq(FLAG_SKIP_OPTIONAL_WORKSPACES)));
        exec.close();
    }

    @Test
    public void checkRightArgsWhenStartingClusterTaskWithInputResources() throws IOException {
        MockedStatic<ExecHelper> exec  = mockStatic(ExecHelper.class);
        exec.when(() -> ExecHelper.execute(anyString(), anyMap(), any())).thenReturn("");

        tkn.startClusterTask("ns", "name", Collections.emptyMap(), "", Collections.emptyMap(), "");
        exec.verify(() ->
                ExecHelper.execute(anyString(), anyMap(), eq("clustertask"), eq("start"), eq("name"), eq("-n"), eq("ns"),
                        eq(FLAG_SKIP_OPTIONAL_WORKSPACES)));
        exec.close();
    }

    @Test
    public void checkRightArgsWhenStartingClusterTaskWithOutputResources() throws IOException {
        MockedStatic<ExecHelper> exec  = mockStatic(ExecHelper.class);
        exec.when(() -> ExecHelper.execute(anyString(), anyMap(), any())).thenReturn("");

        tkn.startClusterTask("ns", "name", Collections.emptyMap(), "", Collections.emptyMap(), "");
        exec.verify(() ->
                ExecHelper.execute(anyString(), anyMap(), eq("clustertask"), eq("start"), eq("name"), eq("-n"), eq("ns"),
                        eq(FLAG_SKIP_OPTIONAL_WORKSPACES)));
        exec.close();
    }

    @Test
    public void checkRightArgsWhenStartingClusterTaskWithWorkspaces() throws IOException {
        Map<String, Workspace> workspaces = new HashMap<>();
        workspaces.put("work1", new Workspace("work1", Workspace.Kind.PVC, "value1"));
        workspaces.put("work2", new Workspace("work2", Workspace.Kind.CONFIGMAP, "value2"));
        workspaces.put("work3", new Workspace("work3", Workspace.Kind.SECRET, "value3"));
        workspaces.put("work4", new Workspace("work4", Workspace.Kind.EMPTYDIR, null));
        MockedStatic<ExecHelper> exec  = mockStatic(ExecHelper.class);
        exec.when(() -> ExecHelper.execute(anyString(), anyMap(), any())).thenReturn("");

        tkn.startClusterTask("ns", "name", Collections.emptyMap(), "", workspaces, "");
        exec.verify(() ->
                ExecHelper.execute(anyString(), anyMap(), eq("clustertask"), eq("start"), eq("name"), eq("-n"), eq("ns"),
                        eq("-w"), eq("name=work2,config=value2"), eq("-w"), eq("name=work1,claimName=value1"),
                        eq("-w"), eq("name=work4,emptyDir="), eq("-w"), eq("name=work3,secret=value3"), eq(FLAG_SKIP_OPTIONAL_WORKSPACES)));
        exec.close();
    }

    @Test
    public void checkRightArgsWhenStartingClusterTaskWithServiceAccounts() throws IOException {
        MockedStatic<ExecHelper> exec  = mockStatic(ExecHelper.class);
        exec.when(() -> ExecHelper.execute(anyString(), anyMap(), any())).thenReturn("");

        tkn.startClusterTask("ns", "name", Collections.emptyMap(), "sa", Collections.emptyMap(), "");
        exec.verify(() ->
                ExecHelper.execute(anyString(), anyMap(), eq("clustertask"), eq("start"), eq("name"), eq("-n"), eq("ns"), eq("-s=sa"), eq(FLAG_SKIP_OPTIONAL_WORKSPACES)));
        exec.close();
    }

    @Test
    public void checkRightArgsWhenStartingClusterTaskWithPrefixName() throws IOException {
        MockedStatic<ExecHelper> exec  = mockStatic(ExecHelper.class);
        exec.when(() -> ExecHelper.execute(anyString(), anyMap(), any())).thenReturn("");

        tkn.startClusterTask("ns", "name", Collections.emptyMap(), "", Collections.emptyMap(), "prefix");
        exec.verify(() ->
                ExecHelper.execute(anyString(), anyMap(), eq("clustertask"), eq("start"), eq("name"), eq("-n"), eq("ns"),
                        eq("--prefix-name=prefix"), eq(FLAG_SKIP_OPTIONAL_WORKSPACES)));
        exec.close();
    }

    @Test
    public void checkRightArgsWhenStartingClusterTaskWithMultipleInputs() throws IOException {
        Map<String, Input> params = new HashMap<>();
        params.put("param1", new Input("param1", "string", Input.Kind.PARAMETER, "value1", Optional.empty(), Optional.empty()));
        Map<String, Workspace> workspaces = new HashMap<>();
        workspaces.put("work1", new Workspace("work1", Workspace.Kind.PVC, "value1"));
        workspaces.put("work2", new Workspace("work2", Workspace.Kind.SECRET, "value2"));
        MockedStatic<ExecHelper> exec  = mockStatic(ExecHelper.class);
        exec.when(() -> ExecHelper.execute(anyString(), anyMap(), any())).thenReturn("");

        tkn.startClusterTask("ns", "name", params, "sa", workspaces, "prefix");
        exec.verify(() ->
                ExecHelper.execute(anyString(), anyMap(), eq("clustertask"), eq("start"), eq("name"), eq("-n"), eq("ns"),
                        eq("-s=sa"),
                        eq("-w"), eq("name=work2,secret=value2"), eq("-w"), eq("name=work1,claimName=value1"),
                        eq("-p"), eq("param1=value1"),
                        eq("--prefix-name=prefix"), eq(FLAG_SKIP_OPTIONAL_WORKSPACES)));
        exec.close();
    }

    @Test
    public void checkRightRunNameIsReturnedWhenStartingClusterTask() throws IOException {
        MockedStatic<ExecHelper> exec  = mockStatic(ExecHelper.class);
        exec.when(() -> ExecHelper.execute(anyString(), anyMap(), any())).thenReturn("run:foo");

        String output = tkn.startClusterTask("ns", "name", Collections.emptyMap(), "", Collections.emptyMap(), "");
        exec.verify(() ->
                ExecHelper.execute(anyString(), anyMap(), eq("clustertask"), eq("start"), eq("name"), eq("-n"), eq("ns"), eq(FLAG_SKIP_OPTIONAL_WORKSPACES)));
        exec.close();
        assertEquals(output, "foo");
    }

    @Test
    public void checkNullIsReturnedWhenStartingClusterTaskReturnInvalidResult() throws IOException {
        MockedStatic<ExecHelper> exec  = mockStatic(ExecHelper.class);
        exec.when(() -> ExecHelper.execute(anyString(), anyMap(), any())).thenReturn("");

        String output = tkn.startClusterTask("ns", "name", Collections.emptyMap(), "", Collections.emptyMap(), "");
        exec.verify(() ->
                ExecHelper.execute(anyString(), anyMap(), eq("clustertask"), eq("start"), eq("name"), eq("-n"), eq("ns"), eq(FLAG_SKIP_OPTIONAL_WORKSPACES)));
        exec.close();
        assertNull(output);
    }

    @Test
    public void CreatePVC_CreateSucceed_Nothing() throws IOException {
        PersistentVolumeClaim claim = createPVC("name", "mode", "1", "Mi");
        MixedOperation mixedOperation = mock(MixedOperation.class);
        when(kubernetesClient.persistentVolumeClaims()).thenReturn(mixedOperation);
        when(mixedOperation.create(any(PersistentVolumeClaim.class))).thenReturn(claim);
        tkn.createPVC("name", "mode", "1", "Mi");
        verify(mixedOperation, atLeastOnce()).create(claim);
    }

    private PersistentVolumeClaim createPVC(String name, String accessMode, String size, String unit) {
        PersistentVolumeClaim claim = new PersistentVolumeClaim();
        ObjectMeta metadata = new ObjectMeta();
        metadata.setName(name);
        claim.setMetadata(metadata);
        PersistentVolumeClaimSpec spec = new PersistentVolumeClaimSpec();
        spec.setAccessModes(Arrays.asList(accessMode));
        spec.setVolumeMode("Filesystem");
        ResourceRequirements resourceRequirements = new ResourceRequirements();
        Map<String, Quantity> requests = new HashMap<>();
        requests.put("storage", new Quantity(size, unit));
        resourceRequirements.setRequests(requests);
        spec.setResources(resourceRequirements);
        claim.setSpec(spec);
        return claim;
    }

    @Test
    public void CreatePVC_CreateFails_Throws() {
        MixedOperation<PersistentVolumeClaim, PersistentVolumeClaimList, Resource<PersistentVolumeClaim>> mixedOperation = mock(MixedOperation.class);
        when(kubernetesClient.persistentVolumeClaims()).thenReturn(mixedOperation);
        when(mixedOperation.create(any(PersistentVolumeClaim.class))).thenThrow(new KubernetesClientException("error"));
        try {
            tkn.createPVC("name", "mode", "size", "unit");
        } catch (IOException e) {
            assertEquals("error", e.getLocalizedMessage());
        }
    }

    @Test
    public void GetPipelineYAMLFromHub_PipelineAndVersion_Call() throws IOException {
        MockedStatic<ExecHelper> exec  = mockStatic(ExecHelper.class);
        exec.when(() -> ExecHelper.execute(anyString(), anyMap(), any())).thenReturn(null);
        String pipeline = "p1";
        String version = "v1";
        tkn.getPipelineYAMLFromHub(pipeline, version);
        exec.verify(() -> ExecHelper.execute(anyString(), anyMap(), eq("hub"), eq("get"), eq("pipeline"), eq(pipeline), eq("--version"), eq(version)));
        exec.close();
    }

}
