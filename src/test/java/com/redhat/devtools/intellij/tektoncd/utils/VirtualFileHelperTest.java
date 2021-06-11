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
package com.redhat.devtools.intellij.tektoncd.utils;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.LightVirtualFile;
import com.redhat.devtools.intellij.tektoncd.BaseTest;
import java.io.IOException;
import org.junit.Test;
import org.mockito.MockedStatic;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;

public class VirtualFileHelperTest extends BaseTest {

    private static final String RESOURCE_PATH = "utils/virtualFileHelper/";

    @Test
    public void CreateVirtualFile_IsReadOnly_LightVirtualFile() throws IOException {
        VirtualFile vf = VirtualFileHelper.createVirtualFile("name", "content", true);
        assertTrue(vf instanceof LightVirtualFile);
        assertFalse(vf.isWritable());
    }

    @Test
    public void CreateVirtualFile_IsNotReadOnly_TempFile() {
        try(MockedStatic<com.redhat.devtools.intellij.common.utils.VirtualFileHelper> virtualFileHelperMockedStatic = mockStatic(com.redhat.devtools.intellij.common.utils.VirtualFileHelper.class)) {
            virtualFileHelperMockedStatic
                    .when(() -> com.redhat.devtools.intellij.common.utils.VirtualFileHelper.createTempFile(anyString(), anyString()))
                    .thenReturn(new LightVirtualFile("name", "content"));
            VirtualFileHelper.createVirtualFile("name", "content", false);
            virtualFileHelperMockedStatic
                    .verify(() -> com.redhat.devtools.intellij.common.utils.VirtualFileHelper.createTempFile(anyString(), anyString()), times(1));
        } catch (IOException e) { }
    }

    @Test
    public void CreateVirtualFile_IsNotReadOnlyAndFails_Throws() {
        try(MockedStatic<com.redhat.devtools.intellij.common.utils.VirtualFileHelper> virtualFileHelperMockedStatic = mockStatic(com.redhat.devtools.intellij.common.utils.VirtualFileHelper.class)) {
            virtualFileHelperMockedStatic
                    .when(() -> com.redhat.devtools.intellij.common.utils.VirtualFileHelper.createTempFile(anyString(), anyString()))
                    .thenThrow(new IOException("error"));
            VirtualFileHelper.createVirtualFile("name", "content", false);
        } catch (IOException e) {
            assertEquals("error", e.getLocalizedMessage());
        }
    }

    @Test
    public void CleanContent_ContentIsEmpty_OriginalContent() {
        String result = VirtualFileHelper.cleanContent("");
        assertEquals("", result);
    }

    @Test
    public void CleanContent_ContentHasNoMetadata_OriginalContent() throws IOException {
        String content = load(RESOURCE_PATH + "pipeline_without_metadata.yaml");
        String result = VirtualFileHelper.cleanContent(content);
        assertEquals(content, result);
    }

    @Test
    public void CleanContent_ContentHasMetadataWithoutClutterTags_OriginalContent() throws IOException {
        String content = load(RESOURCE_PATH + "pipeline_with_no_clutters.yaml");
        String result = VirtualFileHelper.cleanContent(content);
        assertEquals(content, result);
    }

    @Test
    public void CleanContent_ContentHasMetadataWithoutClutterTags_CleanedContent() throws IOException {
        String content = load(RESOURCE_PATH + "pipeline_with_clutters.yaml");
        String content_without_clutters = load(RESOURCE_PATH + "pipeline_with_no_clutters.yaml");
        String result = VirtualFileHelper.cleanContent(content);
        assertEquals(content_without_clutters, result);
    }
}
