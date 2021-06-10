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

import com.google.common.base.Strings;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.actionSystem.EditorWriteActionHandler;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.vfs.VirtualFile;
import java.io.IOException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


import static com.redhat.devtools.intellij.tektoncd.Constants.CLEANED;
import static com.redhat.devtools.intellij.tektoncd.Constants.CONTENT;

public abstract class YAMLClutterActionHandler extends EditorWriteActionHandler {

    @Override
    public void executeWriteAction(Editor editor, @Nullable Caret caret, DataContext dataContext) {
        VirtualFile vf = FileDocumentManager.getInstance().getFile(editor.getDocument());
        if (!Strings.isNullOrEmpty(vf.getUserData(CONTENT))) {
            editor.getDocument().setText(getUpdatedContent(vf.getUserData(CONTENT), editor.getDocument().getText()));
            vf.putUserData(CLEANED, isCleaned());
        }
    }

    @Override
    public void doExecute(@NotNull Editor editor, @Nullable Caret caret, DataContext dataContext) {
        VirtualFile vf = FileDocumentManager.getInstance().getFile(editor.getDocument());
        boolean isWritable = vf.isWritable();
        try {
            editor.getDocument().setReadOnly(false);
            vf.setWritable(true);
            super.doExecute(editor, caret, dataContext);
            vf.setWritable(isWritable);
            editor.getDocument().setReadOnly(!isWritable);
        } catch (IOException e) {
        }
    }

    public abstract String getUpdatedContent(String originalContent, String currentContent);
    public abstract boolean isCleaned();
}
