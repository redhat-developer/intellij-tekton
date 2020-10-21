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

    private String name;

    public TektonVirtualFile(@NotNull String name, @NotNull CharSequence content) {
        super(name, null, content, LocalTimeCounter.currentTime());
        this.name = name;
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
        String resourceName = TreeHelper.getNameFromResourcePath(name);
        return "tekton-" + resourceName + ".yaml";
    }
}
