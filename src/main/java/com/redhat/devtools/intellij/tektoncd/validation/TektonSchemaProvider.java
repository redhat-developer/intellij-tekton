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

import com.intellij.json.psi.JsonFile;
import com.intellij.json.psi.JsonProperty;
import com.intellij.json.psi.JsonValue;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.jetbrains.jsonSchema.extension.JsonSchemaFileProvider;
import com.jetbrains.jsonSchema.extension.SchemaType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.yaml.psi.YAMLFile;
import org.jetbrains.yaml.psi.YAMLKeyValue;
import org.jetbrains.yaml.psi.YAMLValue;

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
                KubernetesTypeInfo fileInfo = extractMeta(psiFile);
                return info.equals(fileInfo);
            }
            return false;
        });
    }

    private KubernetesTypeInfo extractMeta(PsiFile file) {
        KubernetesTypeInfo info = new KubernetesTypeInfo();
        if (file instanceof JsonFile) {
            extractJsonMeta((JsonFile) file, info);
        } else if (file instanceof YAMLFile) {
            extractYAMLMeta((YAMLFile) file, info);
        }
        return info;
    }

    private void extractJsonMeta(JsonFile file, KubernetesTypeInfo info) {
        JsonValue content = file.getTopLevelValue();
        if (content != null) {
            content.acceptChildren(new PsiElementVisitor() {
                @Override
                public void visitElement(@NotNull PsiElement element) {
                    if (element instanceof JsonProperty) {
                        JsonProperty property = (JsonProperty) element;
                        if (property.getName().equals("apiVersion")) {
                            info.setApiGroup(property.getValue().getText());
                        } else if (property.getName().equals("kind")) {
                            info.setKind(property.getValue().getText());
                        }
                    }
                }
            });
        }
    }

    private void extractYAMLMeta(YAMLFile file, KubernetesTypeInfo info) {
        if (!file.getDocuments().isEmpty()) {
            YAMLValue content = file.getDocuments().get(0).getTopLevelValue();
            if (content != null) {
            content.acceptChildren(new PsiElementVisitor() {
                    @Override
                    public void visitElement(@NotNull PsiElement element) {
                        if (element instanceof YAMLKeyValue) {
                            YAMLKeyValue property = (YAMLKeyValue) element;
                            if (property.getKeyText().equals("apiVersion")) {
                                info.setApiGroup(property.getValueText());
                            } else if (property.getKeyText().equals("kind")) {
                                info.setKind(property.getValueText());
                            }
                        }
                    }
                });

            }
        }
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
