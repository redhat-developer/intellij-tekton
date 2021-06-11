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
package com.redhat.devtools.intellij.tektoncd.actions.editor;

import com.fasterxml.jackson.databind.JsonNode;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.LightVirtualFile;
import com.redhat.devtools.intellij.common.utils.YAMLHelper;
import java.io.IOException;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.invocation.InvocationOnMock;


import static com.redhat.devtools.intellij.tektoncd.Constants.CONTENT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RestoreYAMLClutterActionHandlerTest extends ActionTest {

    private static final String RESOURCE_PATH = "actions/restoreYAMLClutterActionHandler/";

    @Test
    public void ExecuteWriteAction_FileIsTekton_EditorTextIsUpdated() {
        RestoreYAMLClutterActionHandler restoreYAMLClutterActionHandler = mock(RestoreYAMLClutterActionHandler.class, InvocationOnMock::callRealMethod);
        VirtualFile vf = new LightVirtualFile("name", "content");
        vf.putUserData(CONTENT, "content");
        FileDocumentManager fileDocumentManager = mock(FileDocumentManager.class);
        when(fileDocumentManager.getFile(any())).thenReturn(vf);
        try(MockedStatic<FileDocumentManager> fileDocumentManagerMockedStatic = mockStatic(FileDocumentManager.class)) {
            when(restoreYAMLClutterActionHandler.getUpdatedContent(anyString(), anyString())).thenReturn("content");
            fileDocumentManagerMockedStatic.when(FileDocumentManager::getInstance).thenReturn(fileDocumentManager);
            restoreYAMLClutterActionHandler.executeWriteAction(editor, null, dataContext);
            verify(document, times(1)).setText(anyString());
        }
    }

    @Test
    public void ExecuteWriteAction_FileIsNotTekton_EditorTextIsNotUpdated() {
        RestoreYAMLClutterActionHandler restoreYAMLClutterActionHandler = new RestoreYAMLClutterActionHandler();
        VirtualFile vf = new LightVirtualFile("name", "content");
        FileDocumentManager fileDocumentManager = mock(FileDocumentManager.class);
        when(fileDocumentManager.getFile(any())).thenReturn(vf);
        try(MockedStatic<FileDocumentManager> fileDocumentManagerMockedStatic = mockStatic(FileDocumentManager.class)) {
            fileDocumentManagerMockedStatic.when(FileDocumentManager::getInstance).thenReturn(fileDocumentManager);
            restoreYAMLClutterActionHandler.executeWriteAction(editor, null, dataContext);
            verify(document, times(0)).setText(anyString());
        }
    }

    @Test
    public void GetUpdatedContent_OriginalContentIsEmpty_CurrentContent() {
        RestoreYAMLClutterActionHandler restoreYAMLClutterActionHandler = new RestoreYAMLClutterActionHandler();
        String result = restoreYAMLClutterActionHandler.getUpdatedContent("", "content");
        assertEquals("content", result);
    }

    @Test
    public void GetUpdatedContent_CurrentContentHasNoMetadata_CurrentContentWithOriginalMetadata() throws IOException {
        String content_with_clutters = load(RESOURCE_PATH + "pipeline_with_clutters.yaml");
        String content_without_metadata = load(RESOURCE_PATH + "pipeline_without_metadata.yaml");
        String content_without_metadata_with_merged_metadata = load(RESOURCE_PATH + "pipeline_without_metadata_with_merged_metadata.yaml");
        JsonNode content_without_metadata_with_merged_metadata_Node = YAMLHelper.YAMLToJsonNode(content_without_metadata_with_merged_metadata);
        RestoreYAMLClutterActionHandler restoreYAMLClutterActionHandler = new RestoreYAMLClutterActionHandler();
        String result = restoreYAMLClutterActionHandler.getUpdatedContent(content_with_clutters, content_without_metadata);
        JsonNode result_Node = YAMLHelper.YAMLToJsonNode(result);
        assertEquals(content_without_metadata_with_merged_metadata_Node.get("metadata"), result_Node.get("metadata"));
        assertEquals(content_without_metadata_with_merged_metadata_Node.get("spec"), result_Node.get("spec"));
    }

    @Test
    public void GetUpdatedContent_CurrentContentHasMetadata_CurrentContentWithMetadataMergedWithOriginalMetadata() throws IOException {
        String content_with_clutters = load(RESOURCE_PATH + "pipeline_with_clutters.yaml");
        String content_with_no_clutters = load(RESOURCE_PATH + "pipeline_with_no_clutters.yaml");
        JsonNode content_with_no_clutters_Node = YAMLHelper.YAMLToJsonNode(content_with_no_clutters);
        RestoreYAMLClutterActionHandler restoreYAMLClutterActionHandler = new RestoreYAMLClutterActionHandler();
        String result = restoreYAMLClutterActionHandler.getUpdatedContent(content_with_clutters, content_with_no_clutters);
        JsonNode result_Node = YAMLHelper.YAMLToJsonNode(result);
        assertEquals(content_with_no_clutters_Node.get("metadata").get("name"), result_Node.get("metadata").get("name"));
        assertFalse(content_with_no_clutters_Node.get("metadata").has("uid"));
        assertTrue(result_Node.get("metadata").has("uid"));
        assertFalse(content_with_no_clutters_Node.get("metadata").has("resourceVersion"));
        assertTrue(result_Node.get("metadata").has("resourceVersion"));
        assertEquals(content_with_no_clutters_Node.get("spec"), result_Node.get("spec"));
    }

    @Test
    public void IsCleaned_False() {
        RestoreYAMLClutterActionHandler restoreYAMLClutterActionHandler = new RestoreYAMLClutterActionHandler();
        assertFalse(restoreYAMLClutterActionHandler.isCleaned());
    }
}