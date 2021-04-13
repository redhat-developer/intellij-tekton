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
package com.redhat.devtools.intellij.tektoncd.ui.toolwindow.findusage;

public class RefUsage {
    private String namespace, name, kind;
    private int occurrence;

    public RefUsage(String namespace, String name, String kind) {
        this.namespace = namespace;
        this.name = name;
        this.kind = kind;
        this.occurrence = 1;
    }

    public String getNamespace() { return namespace; }

    public String getName() {
        return name;
    }

    public String getKind() {
        return kind;
    }

    public int getOccurrence() {
        return occurrence;
    }

    public void incremetOccurrence() {
        occurrence += 1;
    }
}
