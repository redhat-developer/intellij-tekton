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

import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.vfs.VirtualFileSystem;
import com.intellij.testFramework.LightVirtualFile;
import com.intellij.util.LocalTimeCounter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.yaml.YAMLFileType;

public class TektonVirtualFile extends LightVirtualFile {

    private String path;

    public TektonVirtualFile(@NotNull String path, @NotNull CharSequence content) {
        super(path, null, content, LocalTimeCounter.currentTime());
        this.path = path;
    }

    @NotNull
    @Override
    public VirtualFileSystem getFileSystem() {
        return TektonVirtualFileManager.getInstance();
    }

    @NotNull
    @Override
    public FileType getFileType() {
        return YAMLFileType.YML;
    }

    @Override
    public String getPresentableName() {
        String resourceName = TreeHelper.getNameFromResourcePath(path);
        String namespace = TreeHelper.getNamespaceFromResourcePath(path);
        return namespace + "-" + resourceName + ".yaml";
    }

    public String getText() {
        return getContent().toString();
    }
}
