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
import org.mockito.MockedStatic;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;

public class VirtualFileHelperTest extends BaseTest {

    public void testCreateVirtualFile_IsReadOnly_LightVirtualFile() throws IOException {
        VirtualFile vf = VirtualFileHelper.createVirtualFile("name", "content", true);
        assertTrue(vf instanceof LightVirtualFile);
        assertFalse(vf.isWritable());
    }

    public void testCreateVirtualFile_IsNotReadOnly_TempFile() throws IOException {
        try(MockedStatic<com.redhat.devtools.intellij.common.utils.VirtualFileHelper> virtualFileHelperMockedStatic = mockStatic(com.redhat.devtools.intellij.common.utils.VirtualFileHelper.class)) {
            virtualFileHelperMockedStatic
                    .when(() -> com.redhat.devtools.intellij.common.utils.VirtualFileHelper.createTempFile(anyString(), anyString()))
                    .thenReturn(new LightVirtualFile("name", "content"));
            VirtualFileHelper.createVirtualFile("name", "content", false);
            virtualFileHelperMockedStatic
                    .verify(() -> com.redhat.devtools.intellij.common.utils.VirtualFileHelper.createTempFile(anyString(), anyString()), times(1));
        }
    }

    public void testCreateVirtualFile_IsNotReadOnlyAndFails_Throws() {
        try(MockedStatic<com.redhat.devtools.intellij.common.utils.VirtualFileHelper> virtualFileHelperMockedStatic = mockStatic(com.redhat.devtools.intellij.common.utils.VirtualFileHelper.class)) {
            virtualFileHelperMockedStatic
                    .when(() -> com.redhat.devtools.intellij.common.utils.VirtualFileHelper.createTempFile(anyString(), anyString()))
                    .thenThrow(new IOException("error"));
            VirtualFileHelper.createVirtualFile("name", "content", false);
        } catch (IOException e) {
            assertEquals("error", e.getLocalizedMessage());
        }
    }
}
