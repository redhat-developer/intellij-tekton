/*******************************************************************************
 * Copyright (c) 2020 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package com.redhat.devtools.intellij.tektoncd.ui.editors;

import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorPolicy;
import com.intellij.openapi.fileEditor.FileEditorProvider;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.fileEditor.TextEditorWithPreview;
import com.intellij.openapi.fileEditor.impl.text.TextEditorProvider;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.redhat.devtools.intellij.common.validation.KubernetesTypeInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TektonFileEditorProvider implements FileEditorProvider, DumbAware {
    private static final String EDITOR_TYPE_ID = "tekton";
    private static final Key<GraphUpdater> GRAPH_UPDATER_KEY = Key.create(TektonFileEditorProvider.class.getName() + ".graphUpdater");

    @Override
    public boolean accept(@NotNull Project project, @NotNull VirtualFile file) {
        boolean valid = false;
        PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
        if (psiFile != null) {
            KubernetesTypeInfo typeInfo = KubernetesTypeInfo.extractMeta(psiFile);
            if (typeInfo != null) {
                GraphUpdater updater = getGraphUpdater(typeInfo);
                if (updater != null) {
                    file.putUserDataIfAbsent(GRAPH_UPDATER_KEY, updater);
                }
                valid = updater != null;
            }
        }
        return valid;
    }

    private GraphUpdater getGraphUpdater(KubernetesTypeInfo typeInfo) {
        GraphUpdater updater = null;
        if (typeInfo.getApiGroup().startsWith("tekton.dev") && "Pipeline".equals(typeInfo.getKind())) {
            updater =  new PipelineGraphUpdater();
        } else if (typeInfo.getApiGroup().startsWith("tekton.dev") && "PipelineRun".equals(typeInfo.getKind())) {
            updater = new PipelineRunGraphUpdater();
        }
        return updater;
    }

    @NotNull
    @Override
    public FileEditor createEditor(@NotNull Project project, @NotNull VirtualFile file) {
        TextEditor editor = (TextEditor) TextEditorProvider.getInstance().createEditor(project, file);
        return new TextEditorWithPreview(editor, new TektonPreviewEditor(project, file, file.getUserData(GRAPH_UPDATER_KEY)), "TektonEditor") {
            @Nullable
            @Override
            public VirtualFile getFile() {
                return file;
            }
        };
    }

    @NotNull
    @Override
    public String getEditorTypeId() {
        return EDITOR_TYPE_ID;
    }

    @NotNull
    @Override
    public FileEditorPolicy getPolicy() {
        return FileEditorPolicy.HIDE_DEFAULT_EDITOR;
    }
}
