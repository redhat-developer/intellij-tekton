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
package com.redhat.devtools.intellij.tektoncd.utils;

import com.intellij.mock.MockDocument;
import com.intellij.mock.MockFileDocumentManagerImpl;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.impl.DocumentImpl;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.redhat.devtools.intellij.tektoncd.tkn.Tkn;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;


import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_CLUSTERTASK;
import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_CLUSTERTRIGGERBINDING;
import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_CONDITION;
import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_EVENTLISTENER;
import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_PIPELINE;
import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_PIPELINERESOURCE;
import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_PIPELINERUN;
import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_PLURAL;
import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_TASK;
import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_TASKRUN;
import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_TRIGGERBINDING;
import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_TRIGGERTEMPLATE;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TektonVirtualFileManagerTest {
    private Tkn tkn;


    @Before
    public void setup() throws IOException {
        this.tkn = mock(Tkn.class);
        when(tkn.getPipelineYAML(anyString(), eq("resource"))).thenReturn("file");
        doNothing().when(tkn).deletePipelines(anyString(), eq(Arrays.asList("resource")), anyBoolean());

        when(tkn.getTaskYAML(anyString(), eq("resource"))).thenReturn("file");
        doNothing().when(tkn).deleteTasks(anyString(), eq(Arrays.asList("resource")), anyBoolean());

        when(tkn.getTaskRunYAML(anyString(), eq("resource"))).thenReturn("file");
        doNothing().when(tkn).deleteTaskRuns(anyString(), eq(Arrays.asList("resource")));

        when(tkn.getPipelineRunYAML(anyString(), eq("resource"))).thenReturn("file");
        doNothing().when(tkn).deletePipelineRuns(anyString(), eq(Arrays.asList("resource")));

        when(tkn.getResourceYAML(anyString(), eq("resource"))).thenReturn("file");
        doNothing().when(tkn).deleteResources(anyString(), eq(Arrays.asList("resource")));

        when(tkn.getClusterTaskYAML(eq("resource"))).thenReturn("file");
        doNothing().when(tkn).deleteClusterTasks(eq(Arrays.asList("resource")), anyBoolean());

        when(tkn.getConditionYAML(anyString(), eq("resource"))).thenReturn("file");
        doNothing().when(tkn).deleteConditions(anyString(), eq(Arrays.asList("resource")));

        when(tkn.getTriggerTemplateYAML(anyString(), eq("resource"))).thenReturn("file");
        doNothing().when(tkn).deleteTriggerTemplates(anyString(), eq(Arrays.asList("resource")));

        when(tkn.getTriggerBindingYAML(anyString(), eq("resource"))).thenReturn("file");
        doNothing().when(tkn).deleteTriggerBindings(anyString(), eq(Arrays.asList("resource")));

        when(tkn.getClusterTriggerBindingYAML(eq("resource"))).thenReturn("file");
        doNothing().when(tkn).deleteClusterTriggerBindings(eq(Arrays.asList("resource")));

        when(tkn.getEventListenerYAML(anyString(), eq("resource"))).thenReturn("file");
        doNothing().when(tkn).deleteEventListeners(anyString(), eq(Arrays.asList("resource")));

        WatchHandler watchHandler = mock(WatchHandler.class);
        doNothing().when(watchHandler).setWatchByResourceName(any(), anyString(), any());

        when(tkn.getCustomResource(anyString(), eq("resource"), any())).thenReturn(null);

        Map<String, Object> map = new HashMap<>();
        map.put("key", "value");
        map.put("spec", "value");
        when(tkn.getCustomResource(anyString(), eq("resource1"), any())).thenReturn(map);

        doNothing().when(tkn).createCustomResource(anyString(), any(), anyString());
        doNothing().when(tkn).editCustomResource(anyString(), anyString(), any(), anyString());

        
    }

    @Test
    public void checkReturnsNullIfTknCliNotInitialized() {
        MockedStatic<TreeHelper> treeHelper = mockStatic(TreeHelper.class);
        treeHelper.when(() -> TreeHelper.getTkn(any())).thenReturn(null);

        TektonVirtualFileManager tvf = TektonVirtualFileManager.getInstance();
        tvf.setTkn(null);
        assertEquals(tvf.findFileByPath("namespace/kind/resource"), null);
        treeHelper.close();
    }

    @Test
    public void checkFindPipelineResourceInVFS() throws IOException {
        MockedStatic<TreeHelper> treeHelper = getTreeHelperStaticMock();
        TektonVirtualFileManager.getInstance().setTkn(tkn);

        TektonVirtualFileManager.getInstance().findFileByPath("namespace/pipeline/resource");
        // if resource doesn't exist in the vfs, it is retrieved remotely
        verify(tkn, times(1)).getPipelineYAML("namespace", "resource");

        TektonVirtualFileManager.getInstance().findFileByPath("namespace/pipeline/resource");
        // if resource does exist in the vfs, no call is made to the cluster
        verify(tkn, times(1)).getPipelineYAML("namespace", "resource");

        TektonVirtualFileManager.getInstance().deleteResources(Arrays.asList("namespace/pipeline/resource"), false);
        treeHelper.close();
    }

    @Test
    public void checkFindPipelineRunResourceInVFS() throws IOException {
        
        MockedStatic<TreeHelper> treeHelper = getTreeHelperStaticMock();
        TektonVirtualFileManager.getInstance().setTkn(tkn);

        String path = "namespace/" + KIND_PIPELINERUN + "/resource";
        TektonVirtualFileManager.getInstance().findFileByPath(path);
        // if resource doesn't exist in the vfs, it is retrieved remotely
        verify(tkn, times(1)).getPipelineRunYAML("namespace", "resource");

        TektonVirtualFileManager.getInstance().findFileByPath(path);
        // if resource does exist in the vfs, no call is made to the cluster
        verify(tkn, times(1)).getPipelineRunYAML("namespace", "resource");

        TektonVirtualFileManager.getInstance().deleteResources(Arrays.asList(path), false);
        
        treeHelper.close();
    }

    @Test
    public void checkFindTaskResourceInVFS() throws IOException {
       
        MockedStatic<TreeHelper> treeHelper = getTreeHelperStaticMock();
        TektonVirtualFileManager.getInstance().setTkn(tkn);

        String path = "namespace/" + KIND_TASK + "/resource";
        TektonVirtualFileManager.getInstance().findFileByPath(path);
        // if resource doesn't exist in the vfs, it is retrieved remotely
        verify(tkn, times(1)).getTaskYAML("namespace", "resource");

        TektonVirtualFileManager.getInstance().findFileByPath(path);
        // if resource does exist in the vfs, no call is made to the cluster
        verify(tkn, times(1)).getTaskYAML("namespace", "resource");

        TektonVirtualFileManager.getInstance().deleteResources(Arrays.asList(path), false);
        
        treeHelper.close();
    }

    @Test
    public void checkFindPipelineResourceResourceInVFS() throws IOException {
        
        MockedStatic<TreeHelper> treeHelper = getTreeHelperStaticMock();
        TektonVirtualFileManager.getInstance().setTkn(tkn);

        String path = "namespace/" + KIND_PIPELINERESOURCE + "/resource";
        TektonVirtualFileManager.getInstance().findFileByPath(path);
        // if resource doesn't exist in the vfs, it is retrieved remotely
        verify(tkn, times(1)).getResourceYAML("namespace", "resource");

        TektonVirtualFileManager.getInstance().findFileByPath(path);
        // if resource does exist in the vfs, no call is made to the cluster
        verify(tkn, times(1)).getResourceYAML("namespace", "resource");

        TektonVirtualFileManager.getInstance().deleteResources(Arrays.asList(path), false);
       // 
        treeHelper.close();
    }

    @Test
    public void checkFindClusterTaskResourceInVFS() throws IOException {
        
        MockedStatic<TreeHelper> treeHelper = getTreeHelperStaticMock();
        TektonVirtualFileManager.getInstance().setTkn(tkn);

        String path = "namespace/" + KIND_CLUSTERTASK + "/resource";
        TektonVirtualFileManager.getInstance().findFileByPath(path);
        // if resource doesn't exist in the vfs, it is retrieved remotely
        verify(tkn, times(1)).getClusterTaskYAML("resource");

        TektonVirtualFileManager.getInstance().findFileByPath(path);
        // if resource does exist in the vfs, no call is made to the cluster
        verify(tkn, times(1)).getClusterTaskYAML("resource");

        TektonVirtualFileManager.getInstance().deleteResources(Arrays.asList(path), false);
        
        treeHelper.close();
    }

    @Test
    public void checkFindConditionResourceInVFS() throws IOException {
        
        MockedStatic<TreeHelper> treeHelper = getTreeHelperStaticMock();
        TektonVirtualFileManager.getInstance().setTkn(tkn);

        String path = "namespace/" + KIND_CONDITION + "/resource";
        TektonVirtualFileManager.getInstance().findFileByPath(path);
        // if resource doesn't exist in the vfs, it is retrieved remotely
        verify(tkn, times(1)).getConditionYAML("namespace", "resource");

        TektonVirtualFileManager.getInstance().findFileByPath(path);
        // if resource does exist in the vfs, no call is made to the cluster
        verify(tkn, times(1)).getConditionYAML("namespace", "resource");

        TektonVirtualFileManager.getInstance().deleteResources(Arrays.asList(path), false);
        
        treeHelper.close();
    }

    @Test
    public void checkFindTriggerTemplateResourceInVFS() throws IOException {
        
        MockedStatic<TreeHelper> treeHelper = getTreeHelperStaticMock();
        TektonVirtualFileManager.getInstance().setTkn(tkn);

        String path = "namespace/" + KIND_TRIGGERTEMPLATE + "/resource";
        TektonVirtualFileManager.getInstance().findFileByPath(path);
        // if resource doesn't exist in the vfs, it is retrieved remotely
        verify(tkn, times(1)).getTriggerTemplateYAML("namespace", "resource");

        TektonVirtualFileManager.getInstance().findFileByPath(path);
        // if resource does exist in the vfs, no call is made to the cluster
        verify(tkn, times(1)).getTriggerTemplateYAML("namespace", "resource");

        TektonVirtualFileManager.getInstance().deleteResources(Arrays.asList(path), false);
        
        treeHelper.close();
    }

    @Test
    public void checkFindTriggerBindingResourceInVFS() throws IOException {
        
        MockedStatic<TreeHelper> treeHelper = getTreeHelperStaticMock();
        TektonVirtualFileManager.getInstance().setTkn(tkn);

        String path = "namespace/" + KIND_TRIGGERBINDING + "/resource";
        TektonVirtualFileManager.getInstance().findFileByPath(path);
        // if resource doesn't exist in the vfs, it is retrieved remotely
        verify(tkn, times(1)).getTriggerBindingYAML("namespace", "resource");

        TektonVirtualFileManager.getInstance().findFileByPath(path);
        // if resource does exist in the vfs, no call is made to the cluster
        verify(tkn, times(1)).getTriggerBindingYAML("namespace", "resource");

        TektonVirtualFileManager.getInstance().deleteResources(Arrays.asList(path), false);
        
        treeHelper.close();
    }

    @Test
    public void checkFindClusterTriggerBindingResourceInVFS() throws IOException {
        
        MockedStatic<TreeHelper> treeHelper = getTreeHelperStaticMock();
        TektonVirtualFileManager.getInstance().setTkn(tkn);

        String path = "namespace/" + KIND_CLUSTERTRIGGERBINDING + "/resource";
        TektonVirtualFileManager.getInstance().findFileByPath(path);
        // if resource doesn't exist in the vfs, it is retrieved remotely
        verify(tkn, times(1)).getClusterTriggerBindingYAML("resource");

        TektonVirtualFileManager.getInstance().findFileByPath(path);
        // if resource does exist in the vfs, no call is made to the cluster
        verify(tkn, times(1)).getClusterTriggerBindingYAML("resource");

        TektonVirtualFileManager.getInstance().deleteResources(Arrays.asList(path), false);
        
        treeHelper.close();
    }

    @Test
    public void checkFindEventListenerResourceInVFS() throws IOException {
        
        MockedStatic<TreeHelper> treeHelper = getTreeHelperStaticMock();
        TektonVirtualFileManager.getInstance().setTkn(tkn);

        String path = "namespace/" + KIND_EVENTLISTENER + "/resource";
        TektonVirtualFileManager.getInstance().findFileByPath(path);
        // if resource doesn't exist in the vfs, it is retrieved remotely
        verify(tkn, times(1)).getEventListenerYAML("namespace", "resource");

        TektonVirtualFileManager.getInstance().findFileByPath(path);
        // if resource does exist in the vfs, no call is made to the cluster
        verify(tkn, times(1)).getEventListenerYAML("namespace", "resource");

        TektonVirtualFileManager.getInstance().deleteResources(Arrays.asList(path), false);
        
        treeHelper.close();
    }

    @Test
    public void checkFindTaskRunResourceInVFS() throws IOException {
        
        MockedStatic<TreeHelper> treeHelper = getTreeHelperStaticMock();
        TektonVirtualFileManager.getInstance().setTkn(tkn);

        String path = "namespace/" + KIND_TASKRUN + "/resource";
        TektonVirtualFileManager.getInstance().findFileByPath(path);
        // if resource doesn't exist in the vfs, it is retrieved remotely
        verify(tkn, times(1)).getTaskRunYAML("namespace", "resource");

        TektonVirtualFileManager.getInstance().findFileByPath(path);
        // if resource does exist in the vfs, no call is made to the cluster
        verify(tkn, times(1)).getTaskRunYAML("namespace", "resource");

        TektonVirtualFileManager.getInstance().deleteResources(Arrays.asList(path), false);
        
        treeHelper.close();
    }

    @Test
    public void checkErrorIfTknIsNullWhenDeletingInVFS() {
        
        MockedStatic<TreeHelper> treeHelper = mockStatic(TreeHelper.class);
        treeHelper.when(() -> TreeHelper.getTkn(any())).thenReturn(null);

        TektonVirtualFileManager tvf = TektonVirtualFileManager.getInstance();
        tvf.setTkn(null);
        try {
            tvf.deleteResources(Arrays.asList("path"), false);
        } catch(IOException ex) {
            assertEquals(ex.getMessage(), "Unable to contact the cluster");
        }

        
        treeHelper.close();
    }

    @Test
    public void checkErrorIfTwoResourcesHaveDifferentNamespacesWhenDeletingInVFS() {
        
        MockedStatic<TreeHelper> treeHelper = getTreeHelperStaticMock();
        TektonVirtualFileManager.getInstance().setTkn(tkn);

        try {
            TektonVirtualFileManager.getInstance().deleteResources(Arrays.asList("namespace/pipeline/resource", "clustertask/resource", "namespace2/pipeline/resource2"), false);
        } catch(IOException ex) {
            assertEquals(ex.getMessage(), "Delete action is only enable on resources of the same namespace");
        }

        
        treeHelper.close();
    }

    @Test
    public void checkOnePipelineDeleteInVFS() throws IOException {
        
        MockedStatic<TreeHelper> treeHelper = getTreeHelperStaticMock();
        TektonVirtualFileManager.getInstance().setTkn(tkn);

        String path = "namespace/" + KIND_PIPELINE + "/resource";

        TektonVirtualFileManager.getInstance().deleteResources(Arrays.asList(path), false);

        verify(tkn, times(1)).deletePipelines("namespace", Arrays.asList("resource"), false);

        
        treeHelper.close();
    }

    @Test
    public void checkMultiplePipelinesDeleteInVFS() throws IOException {
        
        MockedStatic<TreeHelper> treeHelper = getTreeHelperStaticMock();
        TektonVirtualFileManager.getInstance().setTkn(tkn);

        String path = "namespace/" + KIND_PIPELINE + "/resource";
        String path1 = "namespace/" + KIND_PIPELINE + "/resource1";
        String path2 = "namespace/" + KIND_PIPELINE + "/resource2";

        TektonVirtualFileManager.getInstance().deleteResources(Arrays.asList(path, path1, path2), false);

        verify(tkn, times(1)).deletePipelines("namespace", Arrays.asList("resource", "resource1", "resource2"), false);

        
        treeHelper.close();
    }

    @Test
    public void checkOneTaskDeleteInVFS() throws IOException {
        
        MockedStatic<TreeHelper> treeHelper = getTreeHelperStaticMock();
        TektonVirtualFileManager.getInstance().setTkn(tkn);

        String path = "namespace/" + KIND_TASK + "/resource";

        TektonVirtualFileManager.getInstance().deleteResources(Arrays.asList(path), false);

        verify(tkn, times(1)).deleteTasks("namespace", Arrays.asList("resource"), false);

        
        treeHelper.close();
    }

    @Test
    public void checkMultipleTasksDeleteInVFS() throws IOException {
        
        MockedStatic<TreeHelper> treeHelper = getTreeHelperStaticMock();
        TektonVirtualFileManager.getInstance().setTkn(tkn);

        String path = "namespace/" + KIND_TASK + "/resource";
        String path1 = "namespace/" + KIND_TASK + "/resource1";
        String path2 = "namespace/" + KIND_TASK + "/resource2";

        TektonVirtualFileManager.getInstance().deleteResources(Arrays.asList(path, path1, path2), false);

        verify(tkn, times(1)).deleteTasks("namespace", Arrays.asList("resource", "resource1", "resource2"), false);

        
        treeHelper.close();
    }

    @Test
    public void checkOneClusterTaskDeleteInVFS() throws IOException {
        
        MockedStatic<TreeHelper> treeHelper = getTreeHelperStaticMock();
        TektonVirtualFileManager.getInstance().setTkn(tkn);

        String path = "namespace/" + KIND_CLUSTERTASK + "/resource";

        TektonVirtualFileManager.getInstance().deleteResources(Arrays.asList(path), false);

        verify(tkn, times(1)).deleteClusterTasks(Arrays.asList("resource"), false);

        
        treeHelper.close();
    }

    @Test
    public void checkMultipleClusterTasksDeleteInVFS() throws IOException {
        
        MockedStatic<TreeHelper> treeHelper = getTreeHelperStaticMock();
        TektonVirtualFileManager.getInstance().setTkn(tkn);

        String path = "namespace/" + KIND_CLUSTERTASK + "/resource";
        String path1 = "namespace/" + KIND_CLUSTERTASK + "/resource1";
        String path2 = "namespace/" + KIND_CLUSTERTASK + "/resource2";

        TektonVirtualFileManager.getInstance().deleteResources(Arrays.asList(path, path1, path2), false);

        verify(tkn, times(1)).deleteClusterTasks(Arrays.asList("resource", "resource1", "resource2"), false);

        
        treeHelper.close();
    }

    @Test
    public void checkOnePipelineResourceDeleteInVFS() throws IOException {
        
        MockedStatic<TreeHelper> treeHelper = getTreeHelperStaticMock();
        TektonVirtualFileManager.getInstance().setTkn(tkn);

        String path = "namespace/" + KIND_PIPELINERESOURCE + "/resource";

        TektonVirtualFileManager.getInstance().deleteResources(Arrays.asList(path), false);

        verify(tkn, times(1)).deleteResources("namespace", Arrays.asList("resource"));

        
        treeHelper.close();
    }

    @Test
    public void checkMultiplePipelineResourcesDeleteInVFS() throws IOException {
        
        MockedStatic<TreeHelper> treeHelper = getTreeHelperStaticMock();
        TektonVirtualFileManager.getInstance().setTkn(tkn);

        String path = "namespace/" + KIND_PIPELINERESOURCE + "/resource";
        String path1 = "namespace/" + KIND_PIPELINERESOURCE + "/resource1";
        String path2 = "namespace/" + KIND_PIPELINERESOURCE + "/resource2";

        TektonVirtualFileManager.getInstance().deleteResources(Arrays.asList(path, path1, path2), false);

        verify(tkn, times(1)).deleteResources("namespace", Arrays.asList("resource", "resource1", "resource2"));

        
        treeHelper.close();
    }

    @Test
    public void checkOnePipelineRunDeleteInVFS() throws IOException {
        
        MockedStatic<TreeHelper> treeHelper = getTreeHelperStaticMock();
        TektonVirtualFileManager.getInstance().setTkn(tkn);

        String path = "namespace/" + KIND_PIPELINERUN + "/resource";

        TektonVirtualFileManager.getInstance().deleteResources(Arrays.asList(path), false);

        verify(tkn, times(1)).deletePipelineRuns("namespace", Arrays.asList("resource"));

        
        treeHelper.close();
    }

    @Test
    public void checkMultiplePipelineRunDeleteInVFS() throws IOException {
        
        MockedStatic<TreeHelper> treeHelper = getTreeHelperStaticMock();
        TektonVirtualFileManager.getInstance().setTkn(tkn);

        String path = "namespace/" + KIND_PIPELINERUN + "/resource";
        String path1 = "namespace/" + KIND_PIPELINERUN + "/resource1";
        String path2 = "namespace/" + KIND_PIPELINERUN + "/resource2";

        TektonVirtualFileManager.getInstance().deleteResources(Arrays.asList(path, path1, path2), false);

        verify(tkn, times(1)).deletePipelineRuns("namespace", Arrays.asList("resource", "resource1", "resource2"));

        
        treeHelper.close();
    }

    @Test
    public void checkOneTaskRunDeleteInVFS() throws IOException {
        
        MockedStatic<TreeHelper> treeHelper = getTreeHelperStaticMock();
        TektonVirtualFileManager.getInstance().setTkn(tkn);

        String path = "namespace/" + KIND_TASKRUN + "/resource";

        TektonVirtualFileManager.getInstance().deleteResources(Arrays.asList(path), false);

        verify(tkn, times(1)).deleteTaskRuns("namespace", Arrays.asList("resource"));

        
        treeHelper.close();
    }

    @Test
    public void checkMultipleTaskRunsDeleteInVFS() throws IOException {
        
        MockedStatic<TreeHelper> treeHelper = getTreeHelperStaticMock();
        TektonVirtualFileManager.getInstance().setTkn(tkn);

        String path = "namespace/" + KIND_TASKRUN + "/resource";
        String path1 = "namespace/" + KIND_TASKRUN + "/resource1";
        String path2 = "namespace/" + KIND_TASKRUN + "/resource2";

        TektonVirtualFileManager.getInstance().deleteResources(Arrays.asList(path, path1, path2), false);

        verify(tkn, times(1)).deleteTaskRuns("namespace", Arrays.asList("resource", "resource1", "resource2"));

        
        treeHelper.close();
    }

    @Test
    public void checkOneConditionDeleteInVFS() throws IOException {
        
        MockedStatic<TreeHelper> treeHelper = getTreeHelperStaticMock();
        TektonVirtualFileManager.getInstance().setTkn(tkn);

        String path = "namespace/" + KIND_CONDITION + "/resource";

        TektonVirtualFileManager.getInstance().deleteResources(Arrays.asList(path), false);

        verify(tkn, times(1)).deleteConditions("namespace", Arrays.asList("resource"));

        
        treeHelper.close();
    }

    @Test
    public void checkMultipleConditionsDeleteInVFS() throws IOException {
        
        MockedStatic<TreeHelper> treeHelper = getTreeHelperStaticMock();
        TektonVirtualFileManager.getInstance().setTkn(tkn);

        String path = "namespace/" + KIND_CONDITION + "/resource";
        String path1 = "namespace/" + KIND_CONDITION + "/resource1";
        String path2 = "namespace/" + KIND_CONDITION + "/resource2";

        TektonVirtualFileManager.getInstance().deleteResources(Arrays.asList(path, path1, path2), false);

        verify(tkn, times(1)).deleteConditions("namespace", Arrays.asList("resource", "resource1", "resource2"));

        
        treeHelper.close();
    }

    @Test
    public void checkOneTriggerTemplateDeleteInVFS() throws IOException {
        
        MockedStatic<TreeHelper> treeHelper = getTreeHelperStaticMock();
        TektonVirtualFileManager.getInstance().setTkn(tkn);

        String path = "namespace/" + KIND_TRIGGERTEMPLATE + "/resource";

        TektonVirtualFileManager.getInstance().deleteResources(Arrays.asList(path), false);

        verify(tkn, times(1)).deleteTriggerTemplates("namespace", Arrays.asList("resource"));

        
        treeHelper.close();
    }

    @Test
    public void checkMultipleTriggerTemplatesDeleteInVFS() throws IOException {
        
        MockedStatic<TreeHelper> treeHelper = getTreeHelperStaticMock();
        TektonVirtualFileManager.getInstance().setTkn(tkn);

        String path = "namespace/" + KIND_TRIGGERTEMPLATE + "/resource";
        String path1 = "namespace/" + KIND_TRIGGERTEMPLATE + "/resource1";
        String path2 = "namespace/" + KIND_TRIGGERTEMPLATE + "/resource2";

        TektonVirtualFileManager.getInstance().deleteResources(Arrays.asList(path, path1, path2), false);

        verify(tkn, times(1)).deleteTriggerTemplates("namespace", Arrays.asList("resource", "resource1", "resource2"));

        
        treeHelper.close();
    }

    @Test
    public void checkOneTriggerBindingDeleteInVFS() throws IOException {
        
        MockedStatic<TreeHelper> treeHelper = getTreeHelperStaticMock();
        TektonVirtualFileManager.getInstance().setTkn(tkn);

        String path = "namespace/" + KIND_TRIGGERBINDING + "/resource";

        TektonVirtualFileManager.getInstance().deleteResources(Arrays.asList(path), false);

        verify(tkn, times(1)).deleteTriggerBindings("namespace", Arrays.asList("resource"));

        
        treeHelper.close();
    }

    @Test
    public void checkMultipleTriggerBindingsDeleteInVFS() throws IOException {
        
        MockedStatic<TreeHelper> treeHelper = getTreeHelperStaticMock();
        TektonVirtualFileManager.getInstance().setTkn(tkn);

        String path = "namespace/" + KIND_TRIGGERBINDING + "/resource";
        String path1 = "namespace/" + KIND_TRIGGERBINDING + "/resource1";
        String path2 = "namespace/" + KIND_TRIGGERBINDING + "/resource2";

        TektonVirtualFileManager.getInstance().deleteResources(Arrays.asList(path, path1, path2), false);

        verify(tkn, times(1)).deleteTriggerBindings("namespace", Arrays.asList("resource", "resource1", "resource2"));

        
        treeHelper.close();
    }

    @Test
    public void checkOneClusterTriggerBindingsDeleteInVFS() throws IOException {
        
        MockedStatic<TreeHelper> treeHelper = getTreeHelperStaticMock();
        TektonVirtualFileManager.getInstance().setTkn(tkn);

        String path = "namespace/" + KIND_CLUSTERTRIGGERBINDING + "/resource";

        TektonVirtualFileManager.getInstance().deleteResources(Arrays.asList(path), false);

        verify(tkn, times(1)).deleteClusterTriggerBindings(Arrays.asList("resource"));

        
        treeHelper.close();
    }

    @Test
    public void checkMultipleClusterTriggerBindingsDeleteInVFS() throws IOException {
        
        MockedStatic<TreeHelper> treeHelper = getTreeHelperStaticMock();
        TektonVirtualFileManager.getInstance().setTkn(tkn);

        String path = "namespace/" + KIND_CLUSTERTRIGGERBINDING + "/resource";
        String path1 = "namespace/" + KIND_CLUSTERTRIGGERBINDING + "/resource1";
        String path2 = "namespace/" + KIND_CLUSTERTRIGGERBINDING + "/resource2";

        TektonVirtualFileManager.getInstance().deleteResources(Arrays.asList(path, path1, path2), false);

        verify(tkn, times(1)).deleteClusterTriggerBindings(Arrays.asList("resource", "resource1", "resource2"));

        
        treeHelper.close();
    }

    @Test
    public void checkOneEventListenerDeleteInVFS() throws IOException {
        
        MockedStatic<TreeHelper> treeHelper = getTreeHelperStaticMock();
        TektonVirtualFileManager.getInstance().setTkn(tkn);

        String path = "namespace/" + KIND_EVENTLISTENER + "/resource";

        TektonVirtualFileManager.getInstance().deleteResources(Arrays.asList(path), false);

        verify(tkn, times(1)).deleteEventListeners("namespace", Arrays.asList("resource"));

        
        treeHelper.close();
    }

    @Test
    public void checkMultipleEventListenersDeleteInVFS() throws IOException {
        
        MockedStatic<TreeHelper> treeHelper = getTreeHelperStaticMock();
        TektonVirtualFileManager.getInstance().setTkn(tkn);

        String path = "namespace/" + KIND_EVENTLISTENER + "/resource";
        String path1 = "namespace/" + KIND_EVENTLISTENER + "/resource1";
        String path2 = "namespace/" + KIND_EVENTLISTENER + "/resource2";

        TektonVirtualFileManager.getInstance().deleteResources(Arrays.asList(path, path1, path2), false);

        verify(tkn, times(1)).deleteEventListeners("namespace", Arrays.asList("resource", "resource1", "resource2"));

        
        treeHelper.close();
    }

    @Test
    public void checkErrorIfTknIsNullWhenSavingFileInVFS() {
        
        MockedStatic<TreeHelper> treeHelper = mockStatic(TreeHelper.class);
        treeHelper.when(() -> TreeHelper.getTkn(any())).thenReturn(null);

        TektonVirtualFileManager tvf = TektonVirtualFileManager.getInstance();
        tvf.setTkn(null);
        try {
            tvf.saveResource("namespace", new MockDocument());
        } catch(IOException ex) {
            assertEquals(ex.getMessage(), "Unable to contact the cluster");
        }

        
        treeHelper.close();
    }

    @Test
    public void checkCorrectErrorIfDocumentHasNoName() {
        
        MockedStatic<TreeHelper> treeHelper = getTreeHelperStaticMock();
        TektonVirtualFileManager.getInstance().setTkn(tkn);

        Document d = new MockDocument();
        d.replaceString(0,100, "invalidcontent");

        try {
            TektonVirtualFileManager.getInstance().saveResource("namespace", d);
        } catch (IOException e) {
            assertEquals(e.getMessage(),"Tekton file has not a valid format. Name field is not valid or found.");
        }

        
        treeHelper.close();
    }

    @Test
    public void checkCorrectErrorIfDocumentHasNoKind() {
        
        MockedStatic<TreeHelper> treeHelper = getTreeHelperStaticMock();
        TektonVirtualFileManager.getInstance().setTkn(tkn);

        Document d = new MockDocument();
        d.replaceString(0,100, "apiVersion: tekton.dev/v1beta1\n" +
                "metadata:\n" +
                "  name: app-deploy-ct");

        try {
            TektonVirtualFileManager.getInstance().saveResource("namespace", d);
        } catch (IOException e) {
            assertEquals(e.getMessage(),"Tekton file has not a valid format. Kind field is not found.");
        }

        
        treeHelper.close();
    }

    @Test
    public void checkCorrectErrorIfDocumentHasNoApiVersion() {
        
        MockedStatic<TreeHelper> treeHelper = getTreeHelperStaticMock();
        TektonVirtualFileManager.getInstance().setTkn(tkn);

        Document d = new MockDocument();
        d.replaceString(0,100, "kind: pipeline\n" +
                "metadata:\n" +
                "  name: app-deploy-ct");

        try {
            TektonVirtualFileManager.getInstance().saveResource("namespace", d);
        } catch (IOException e) {
            assertEquals(e.getMessage(),"Tekton file has not a valid format. ApiVersion field is not found.");
        }

        
        treeHelper.close();
    }

    @Test
    public void checkCorrectErrorIfDocumentHasNoSpec() {
        
        MockedStatic<TreeHelper> treeHelper = getTreeHelperStaticMock();
        TektonVirtualFileManager.getInstance().setTkn(tkn);

        TektonVirtualFile vf = new TektonVirtualFile("", "apiVersion: tekton.dev/v1beta1\n" +
                "kind: Pipeline\n" +
                "metadata:\n" +
                "  name: app-deploy-ct");
        vf.putUserData(KIND_PLURAL, "invalid");

        MockFileDocumentManagerImpl mockFdm = new MockFileDocumentManagerImpl(charSequence -> new DocumentImpl(charSequence), null);
        MockedStatic<FileDocumentManager> fdm  = mockStatic(FileDocumentManager.class);
        fdm.when(() -> FileDocumentManager.getInstance()).thenReturn(mockFdm);

        Document d = mockFdm.getDocument(vf);

        try {
            TektonVirtualFileManager.getInstance().saveResource("namespace", d);
        } catch (IOException e) {
            assertEquals(e.getMessage(),"Tekton file has not a valid format. Spec field is not found.");
        }

        fdm.close();
        
        treeHelper.close();
    }

    @Test
    public void checkIfNewResourceIsCreatedIfNotFoundRemotely() throws IOException {
        
        MockedStatic<TreeHelper> treeHelper = getTreeHelperStaticMock();

        TektonVirtualFileManager.getInstance().setTkn(tkn);

        TektonVirtualFile vf = new TektonVirtualFile("", "apiVersion: tekton.dev/v1beta1\n" +
                "kind: Pipeline\n" +
                "metadata:\n" +
                "  name: resource\n" +
                "  namespace: pipelines-tutorial\n" +
                "spec:\n" +
                "  tasks:");
        vf.putUserData(KIND_PLURAL, "invalid");

        MockFileDocumentManagerImpl mockFdm = new MockFileDocumentManagerImpl(charSequence -> new DocumentImpl(charSequence), null);
        MockedStatic<FileDocumentManager> fdm  = mockStatic(FileDocumentManager.class);
        fdm.when(() -> FileDocumentManager.getInstance()).thenReturn(mockFdm);

        Document d = mockFdm.getDocument(vf);

        TektonVirtualFileManager.getInstance().saveResource("namespace", d);

        verify(tkn).createCustomResource(anyString(), any(), anyString());
        verify(tkn, times(0)).editCustomResource(anyString(), anyString(), any(), anyString());

        fdm.close();
        
        treeHelper.close();
    }

    @Test
    public void checkResourceIsUpdatedIfFoundRemotely() throws IOException {
        
        MockedStatic<TreeHelper> treeHelper = getTreeHelperStaticMock();

        TektonVirtualFileManager.getInstance().setTkn(tkn);

        TektonVirtualFile vf = new TektonVirtualFile("", "apiVersion: tekton.dev/v1beta1\n" +
                "kind: Pipeline\n" +
                "metadata:\n" +
                "  name: resource1\n" +
                "  namespace: pipelines-tutorial\n" +
                "spec:\n" +
                "  tasks:");
        vf.putUserData(KIND_PLURAL, "invalid");

        MockFileDocumentManagerImpl mockFdm = new MockFileDocumentManagerImpl(charSequence -> new DocumentImpl(charSequence), null);
        MockedStatic<FileDocumentManager> fdm  = mockStatic(FileDocumentManager.class);
        fdm.when(() -> FileDocumentManager.getInstance()).thenReturn(mockFdm);

        Document d = mockFdm.getDocument(vf);

        TektonVirtualFileManager.getInstance().saveResource("namespace", d);

        verify(tkn).editCustomResource(anyString(), anyString(), any(), anyString());

        fdm.close();
        
        treeHelper.close();
    }

    private MockedStatic<TektonVirtualFileManager> getTektonFileManagerStaticMock() {
        MockedStatic<TektonVirtualFileManager> tvfm  = mockStatic(TektonVirtualFileManager.class);
        tvfm.when(() -> TektonVirtualFileManager.getInstance()).thenReturn(new TektonVirtualFileManager());
        return tvfm;
    }

    private MockedStatic<TreeHelper> getTreeHelperStaticMock() {
        MockedStatic<TreeHelper> treeHelper = mockStatic(TreeHelper.class);
        treeHelper.when(() -> TreeHelper.getTkn(any())).thenReturn(tkn);
        treeHelper.when(() -> TreeHelper.getKindFromResourcePath(anyString())).thenCallRealMethod();
        treeHelper.when(() -> TreeHelper.getNamespaceFromResourcePath(anyString())).thenCallRealMethod();
        treeHelper.when(() -> TreeHelper.getNameFromResourcePath(anyString())).thenCallRealMethod();
        treeHelper.when(() -> TreeHelper.getTektonResourceUrl(anyString(), anyString(), anyString(), anyBoolean())).thenCallRealMethod();
        return treeHelper;
    }
}
