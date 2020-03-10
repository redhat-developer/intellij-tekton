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

import java.util.Optional;

public class Input {

    public static enum Kind { PARAMETER, RESOURCE };

    private String name;
    private String type;
    private Kind kind;
    private Optional<String> description;
    private String value;
    private Optional<String> defaultValue;

    public Input(String name, String type, Kind kind, Optional<String> description, Optional<String> defaultValue) {
        this.name = name;
        this.type = type;
        this.kind = kind;
        this.description = description;
        this.defaultValue = defaultValue;
    }

    public String name() {
        return name;
    }

    public String type() {
        return type;
    }

    public Optional<String> description() {
        return description;
    }

    public Optional<String> defaultValue() {
        return defaultValue;
    }

    public String value() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Kind kind() {
        return kind;
    }

    @Override
    public String toString() {
        return name;
    }
}
