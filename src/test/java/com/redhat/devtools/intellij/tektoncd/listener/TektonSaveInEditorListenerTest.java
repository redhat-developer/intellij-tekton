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
package com.redhat.devtools.intellij.tektoncd.listener;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.RangeMarker;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.LightVirtualFile;
import com.redhat.devtools.intellij.tektoncd.FixtureBaseTest;
import com.redhat.devtools.intellij.tektoncd.utils.DeployHelper;
import com.redhat.devtools.intellij.tektoncd.utils.TreeHelper;
import java.io.IOException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;


import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_PLURAL;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class TektonSaveInEditorListenerTest extends FixtureBaseTest {

    private TektonSaveInEditorListener tektonSaveInEditorListener;
    private static final String RESOURCE_PATH = "listener/tektonSaveInEditorListener/";

    @Before
    public void setUp() throws Exception {
        super.setUp();
        tektonSaveInEditorListener = new TektonSaveInEditorListener();
    }

    @Test
    public void Refresh_NodeIsNull_Nothing() {
        executeRefresh(project, null, 0);
    }

    @Test
    public void Refresh_NodeIsUnknownType_Nothing() {
        executeRefresh(project, tektonRootNode, 0);
    }

    @Test
    public void Refresh_NodeIsParentableNode_Refresh() {
        executeRefresh(null, parentableNode, 1);
    }

    private void executeRefresh(Project project, Object node, int numberOfInvocations) {
        try (MockedStatic<TreeHelper> treeHelperMockedStatic = mockStatic(TreeHelper.class)) {
            tektonSaveInEditorListener.refresh(project, node);
            treeHelperMockedStatic.verify(times(numberOfInvocations), () -> TreeHelper.refresh(any(), any()));
        }
    }

    @Test
    public void Save_SaveOnClusterIsFalse_False() throws IOException {
        Document document = createDocument();
        try (MockedStatic<DeployHelper> deployHelperMockedStatic = mockStatic(DeployHelper.class)) {
            deployHelperMockedStatic.when(() -> DeployHelper.saveOnCluster(any(), anyString())).thenReturn(false);
            boolean result = tektonSaveInEditorListener.save(document, project);
            assertFalse(result);
        }
    }

    @Test
    public void Save_SaveOnClusterIsTrue_True() throws IOException {
        Document document = createDocument();
        try (MockedStatic<DeployHelper> deployHelperMockedStatic = mockStatic(DeployHelper.class)) {
            deployHelperMockedStatic.when(() -> DeployHelper.saveOnCluster(any(), anyString())).thenReturn(true);
            boolean result = tektonSaveInEditorListener.save(document, project);
            assertTrue(result);
        }
    }

    @Test
    public void Save_SaveOnClusterThrows_False() throws IOException {
        Document document = createDocument();
        try (MockedStatic<DeployHelper> deployHelperMockedStatic = mockStatic(DeployHelper.class)) {
            deployHelperMockedStatic.when(() -> DeployHelper.saveOnCluster(any(), anyString())).thenThrow(new IOException("text"));
            boolean result = tektonSaveInEditorListener.save(document, project);
            assertFalse(result);
        }
    }

    @Test
    public void IsFileToPush_VirtualFileIsNull_False() {
        assertFalse(tektonSaveInEditorListener.isFileToPush(project, null));
    }

    @Test
    public void IsFileToPush_VirtualFileWithMissingTektonProperty_False() throws IOException {
        VirtualFile virtualFile = createVirtualFile(null);
        assertFalse(tektonSaveInEditorListener.isFileToPush(project, virtualFile));

    }

    @Test
    public void IsFileToPush_VirtualFileWithInvalidTektonProperty_False() throws IOException {
        VirtualFile virtualFile = createVirtualFile("");
        assertFalse(tektonSaveInEditorListener.isFileToPush(project, virtualFile));
    }

    @Test
    public void IsFileToPush_VirtualFileIsValid_ParentMethodCalled() throws IOException {
        VirtualFile virtualFile = createVirtualFile("pipelines");
        FileEditorManager fileEditorManager = mock(FileEditorManager.class);
        try (MockedStatic<FileEditorManager> fileEditorManagerMockedStatic = mockStatic(FileEditorManager.class)) {
            fileEditorManagerMockedStatic.when(() -> FileEditorManager.getInstance(any())).thenReturn(fileEditorManager);
            tektonSaveInEditorListener.isFileToPush(project, virtualFile);
            verify(fileEditorManager, times(1)).getSelectedEditor();
        }
    }

    private VirtualFile createVirtualFile(String userData) throws IOException {
        String yaml = load(RESOURCE_PATH + "pipeline.yaml");
        VirtualFile virtualFile = new LightVirtualFile("test", yaml);
        if (userData != null) {
            virtualFile.putUserData(KIND_PLURAL, userData);
        }
        return virtualFile;
    }

    private Document createDocument() throws IOException {
        String text = load(RESOURCE_PATH + "pipeline.yaml");
        return new Document() {
            @Override
            public @NotNull String getText() {
                return text;
            }

            @Override
            public @NotNull CharSequence getImmutableCharSequence() {
                return null;
            }

            @Override
            public int getLineCount() {
                return 0;
            }

            @Override
            public int getLineNumber(int offset) {
                return 0;
            }

            @Override
            public int getLineStartOffset(int line) {
                return 0;
            }

            @Override
            public int getLineEndOffset(int line) {
                return 0;
            }

            @Override
            public void insertString(int offset, @NotNull CharSequence s) {

            }

            @Override
            public void deleteString(int startOffset, int endOffset) {

            }

            @Override
            public void replaceString(int startOffset, int endOffset, @NotNull CharSequence s) {

            }

            @Override
            public boolean isWritable() {
                return false;
            }

            @Override
            public long getModificationStamp() {
                return 0;
            }

            @Override
            public @NotNull RangeMarker createRangeMarker(int startOffset, int endOffset, boolean surviveOnExternalChange) {
                return null;
            }

            @Override
            public @NotNull RangeMarker createGuardedBlock(int startOffset, int endOffset) {
                return null;
            }

            @Override
            public void setText(@NotNull CharSequence text) {

            }

            @Override
            public <T> @Nullable T getUserData(@NotNull Key<T> key) {
                return null;
            }

            @Override
            public <T> void putUserData(@NotNull Key<T> key, @Nullable T value) {

            }
        };
    }
}
