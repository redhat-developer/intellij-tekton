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
package com.redhat.devtools.intellij.tektoncd.validation;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.jetbrains.jsonSchema.extension.JsonSchemaFileProvider;
import com.jetbrains.jsonSchema.extension.SchemaType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TektonSchemaProvider implements JsonSchemaFileProvider {
    private Project project;
    private final KubernetesTypeInfo info;
    private final VirtualFile schemaFile;

    public TektonSchemaProvider(KubernetesTypeInfo info, VirtualFile file) {
        this.info = info;
        this.schemaFile = file;
    }

    private TektonSchemaProvider(Project project, KubernetesTypeInfo info, VirtualFile file) {
        this(info, file);
        this.project = project;
    }

    @Override
    public boolean isAvailable(@NotNull VirtualFile file) {
        return ApplicationManager.getApplication().runReadAction((Computable<Boolean>) () -> {
            PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
            if (psiFile != null) {
                KubernetesTypeInfo fileInfo = KubernetesTypeInfo.extractMeta(psiFile);
                return info.equals(fileInfo);
            }
            return false;
        });
    }

    @NotNull
    @Override
    public String getName() {
        return info.toString();
    }

    @Nullable
    @Override
    public VirtualFile getSchemaFile() {
        return schemaFile;
    }

    @NotNull
    @Override
    public SchemaType getSchemaType() {
        return SchemaType.schema;
    }

    public TektonSchemaProvider withProject(Project project) {
        if (this.project == project) {
            return this;
        }
        return new TektonSchemaProvider(project, info, schemaFile);
    }
}
