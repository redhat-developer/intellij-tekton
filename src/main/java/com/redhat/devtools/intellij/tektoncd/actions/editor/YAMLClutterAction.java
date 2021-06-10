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

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.actionSystem.EditorAction;
import com.intellij.openapi.editor.actionSystem.EditorActionHandler;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.vfs.VirtualFile;
import java.awt.event.KeyEvent;
import org.jetbrains.annotations.NotNull;


import static com.redhat.devtools.intellij.tektoncd.Constants.CLEANED;

public abstract class YAMLClutterAction extends EditorAction {
    protected YAMLClutterAction(EditorActionHandler defaultHandler) {
        super(defaultHandler);
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        Presentation presentation = e.getPresentation();
        DataContext dataContext = e.getDataContext();
        Editor editor = getEditor(dataContext);
        if (editor == null) {
            presentation.setEnabled(false);
            presentation.setVisible(false);
        }
        Document document = editor.getDocument();
        VirtualFile vf = FileDocumentManager.getInstance().getFile(document);
        boolean isDisabled = true;
        if (vf != null && vf.getUserData(CLEANED) != null) {
            isDisabled = isDisabled(vf.getUserData(CLEANED));
        }
        if (isDisabled) {
            presentation.setEnabled(false);
            presentation.setVisible(false);
        }
        else {
            if (editor.isDisposed()) {
                presentation.setEnabled(false);
            }
            else {
                if (e.getInputEvent() instanceof KeyEvent) {
                    updateForKeyboardAccess(editor, presentation, dataContext);
                }
                else {
                    update(editor, presentation, dataContext);
                }
            }
        }
    }

    public abstract boolean isDisabled(boolean isAlreadyCleaned);
}
