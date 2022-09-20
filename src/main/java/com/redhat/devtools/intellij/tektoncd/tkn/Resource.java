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
package com.redhat.devtools.intellij.tektoncd.tkn;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import javax.swing.Icon;
import java.util.Objects;

@JsonDeserialize(using = ResourceDeserializer.class)
public class Resource {
    private String name;
    private String type;
    private Icon icon;

    public Resource(String name, String type) {
        this(name, type, null);
    }

    public Resource(String name, String type, Icon icon) {
        this.name = name;
        this.type = type;
        this.icon = icon;
    }

    public String name() {
        return name;
    }

    public String type() {
        return type;
    }

    public Icon getIcon() {
        return icon;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Resource resource = (Resource) o;
        return Objects.equals(name, resource.name) && Objects.equals(type, resource.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, type);
    }
}
