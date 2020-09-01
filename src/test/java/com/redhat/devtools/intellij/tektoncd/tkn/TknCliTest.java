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
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.internal.util.reflection.FieldSetter;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

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
}
