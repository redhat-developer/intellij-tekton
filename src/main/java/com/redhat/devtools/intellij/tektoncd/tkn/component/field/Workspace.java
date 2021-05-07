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
package com.redhat.devtools.intellij.tektoncd.tkn.component.field;

import java.util.Collections;
import java.util.Map;


import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_CONFIGMAP;
import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_EMPTYDIR;
import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_PVC;
import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_SECRET;

public class Workspace {

    public enum Kind { PVC(KIND_PVC), SECRET(KIND_SECRET), CONFIGMAP(KIND_CONFIGMAP), EMPTYDIR(KIND_EMPTYDIR);

        private String kind;

        Kind(String kind) {
            this.kind = kind;
        }

        @Override
        public String toString() {
            return this.kind;
        }
    };

    private String name;
    private Kind kind;
    private String resource;
    private String subPath;
    private Map<String, String> items;

    public Workspace(String name, Kind kind, String resource) {
        this(name, kind, resource, Collections.emptyMap());
    }

    public Workspace(String name, Kind kind, String resource, Map<String, String> items) {
        this.name = name;
        this.kind = kind;
        this.resource = resource;
        this.items = items;
    }

    public String getName() {
        return name;
    }

    public Kind getKind() {
        return kind;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public String getSubPath() {
        return subPath;
    }

    public Map<String, String> getItems() {
        return items;
    }

}
