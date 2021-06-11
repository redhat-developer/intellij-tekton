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

import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.LightVirtualFile;
import com.redhat.devtools.intellij.tektoncd.utils.VirtualFileHelper;
import java.io.IOException;
import org.junit.Test;
import org.mockito.MockedStatic;


import static com.redhat.devtools.intellij.tektoncd.Constants.CONTENT;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RemoveYAMLClutterActionHandlerTest extends ActionTest {

    private static final String RESOURCE_PATH = "actions/removeYAMLClutterActionHandler/";

    @Test
    public void ExecuteWriteAction_FileIsTekton_EditorTextIsUpdated() {
        ExecuteWriteAction("content", "content", "content");
    }

    @Test
    public void ExecuteWriteAction_FileIsTektonAndHasNoClutters_EditorTextIsUpdatedWithSameContent() throws IOException {
        String content_with_clutters = load(RESOURCE_PATH + "pipeline_with_clutters.yaml");
        String content = load(RESOURCE_PATH + "pipeline_with_no_clutters.yaml");
        ExecuteWriteAction(content_with_clutters, content, content);
    }

    @Test
    public void ExecuteWriteAction_FileIsTektonAndHasClutter_EditorTextIsUpdatedWithCleanedContent() throws IOException {
        String content_with_clutters = load(RESOURCE_PATH + "pipeline_with_clutters.yaml");
        String content = load(RESOURCE_PATH + "pipeline_with_no_clutters.yaml");
        ExecuteWriteAction(content_with_clutters, content_with_clutters, content);

    }

    private void ExecuteWriteAction(String originalContent, String contentDisplayedInEditor, String cleanedContent) {
        RemoveYAMLClutterActionHandler removeYAMLClutterActionHandler = new RemoveYAMLClutterActionHandler();
        VirtualFile vf = new LightVirtualFile("name", contentDisplayedInEditor);
        vf.putUserData(CONTENT, originalContent);
        FileDocumentManager fileDocumentManager = mock(FileDocumentManager.class);
        when(fileDocumentManager.getFile(any())).thenReturn(vf);
        try(MockedStatic<FileDocumentManager> fileDocumentManagerMockedStatic = mockStatic(FileDocumentManager.class)) {
            try(MockedStatic<VirtualFileHelper> virtualFileHelperMockedStatic = mockStatic(VirtualFileHelper.class)) {
                virtualFileHelperMockedStatic.when(() -> VirtualFileHelper.cleanContent(anyString())).thenReturn(cleanedContent);
                fileDocumentManagerMockedStatic.when(FileDocumentManager::getInstance).thenReturn(fileDocumentManager);
                removeYAMLClutterActionHandler.executeWriteAction(editor, null, dataContext);
                verify(document, times(1)).setText(cleanedContent);
            }
        }
    }

    @Test
    public void ExecuteWriteAction_FileIsNotTekton_EditorTextIsNotUpdated() {
        RemoveYAMLClutterActionHandler removeYAMLClutterActionHandler = new RemoveYAMLClutterActionHandler();
        VirtualFile vf = new LightVirtualFile("name", "content");
        FileDocumentManager fileDocumentManager = mock(FileDocumentManager.class);
        when(fileDocumentManager.getFile(any())).thenReturn(vf);
        try(MockedStatic<FileDocumentManager> fileDocumentManagerMockedStatic = mockStatic(FileDocumentManager.class)) {
                fileDocumentManagerMockedStatic.when(FileDocumentManager::getInstance).thenReturn(fileDocumentManager);
                removeYAMLClutterActionHandler.executeWriteAction(editor, null, dataContext);
                verify(document, times(0)).setText(anyString());
        }
    }

    @Test
    public void GetUpdatedContent_CallCleanContentMethod() {
        RemoveYAMLClutterActionHandler removeYAMLClutterActionHandler = new RemoveYAMLClutterActionHandler();
        try(MockedStatic<VirtualFileHelper> virtualFileHelperMockedStatic = mockStatic(VirtualFileHelper.class)) {
            virtualFileHelperMockedStatic.when(() -> VirtualFileHelper.cleanContent(anyString())).thenReturn("");
            removeYAMLClutterActionHandler.getUpdatedContent("", "");
            virtualFileHelperMockedStatic.verify(() -> VirtualFileHelper.cleanContent(anyString()), times(1));
        }
    }

    @Test
    public void IsCleaned_True() {
        RemoveYAMLClutterActionHandler removeYAMLClutterActionHandler = new RemoveYAMLClutterActionHandler();
        assertTrue(removeYAMLClutterActionHandler.isCleaned());
    }
}
