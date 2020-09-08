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
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.yaml.psi.YAMLFile;
import org.jetbrains.yaml.psi.YAMLKeyValue;
import org.jetbrains.yaml.psi.YAMLValue;

import java.util.Objects;

public class KubernetesTypeInfo {
    private String apiGroup = "";
    private String kind= "";

    public KubernetesTypeInfo(String apiGroup, String kind) {
        this.apiGroup = apiGroup;
        this.kind = kind;
    }

    public KubernetesTypeInfo() {}

    public static KubernetesTypeInfo extractMeta(PsiFile file) {
        KubernetesTypeInfo info = new KubernetesTypeInfo();
        if (file instanceof JsonFile) {
            extractJsonMeta((JsonFile) file, info);
        } else if (file instanceof YAMLFile) {
            extractYAMLMeta((YAMLFile) file, info);
        }
        return info;
    }

    private static void extractJsonMeta(JsonFile file, KubernetesTypeInfo info) {
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

    private static void extractYAMLMeta(YAMLFile file, KubernetesTypeInfo info) {
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

    public String getApiGroup() {
        return apiGroup;
    }

    public void setApiGroup(String apiGroup) {
        this.apiGroup = apiGroup;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        KubernetesTypeInfo that = (KubernetesTypeInfo) o;
        return Objects.equals(apiGroup, that.apiGroup) &&
                Objects.equals(kind, that.kind);
    }

    @Override
    public int hashCode() {
        return Objects.hash(apiGroup, kind);
    }

    @Override
    public String toString() {
        return apiGroup + '#' + kind;
    }

    public static KubernetesTypeInfo fromFileName(String filename) {
        int index = filename.indexOf('_');
            String apiGroup = (index != (-1))?filename.substring(0, index):"";
            String kind = (index != (-1))?filename.substring(index + 1):filename;
            index = kind.lastIndexOf('.');
            kind = (index != (-1))?kind.substring(0, index):kind;
            return new KubernetesTypeInfo(apiGroup, kind);

        }
}
