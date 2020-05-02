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

import java.util.Objects;

public class KubernetesTypeInfo {
    private String apiGroup;
    private String kind;

    public KubernetesTypeInfo(String apiGroup, String kind) {
        this.apiGroup = apiGroup;
        this.kind = kind;
    }

    public KubernetesTypeInfo() {}

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
