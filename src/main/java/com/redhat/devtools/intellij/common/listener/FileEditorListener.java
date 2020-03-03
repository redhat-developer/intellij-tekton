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
package com.redhat.devtools.intellij.common.listener;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static com.redhat.devtools.intellij.common.CommonConstants.TEKTON;

public class FileEditorListener implements FileEditorManagerListener {
    Logger logger = LoggerFactory.getLogger(FileEditorListener.class);
    @Override
    public void fileClosed(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
        String tknProperty = file.getUserData(TEKTON);
        if (tknProperty != null && tknProperty.equals("file")) {
            ApplicationManager.getApplication().runWriteAction(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (Files.exists(Paths.get(file.getPath()))) {
                            file.delete(this);
                        }
                    } catch (IOException e) {
                        logger.error("Error: " + e.getLocalizedMessage());
                    }
                }
            });
        }
    }
}
