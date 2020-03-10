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

public class Output {

    private String name;
    private String type;
    private Optional<String> description;
    private Optional<Boolean> optional;
    private String value;

    public Output(String name, String type, Optional<String> description, Optional<Boolean> optional) {
        this.name = name;
        this.type = type;
        this.description = description;
        this.optional = optional;
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

    public Optional<Boolean> optional() {
        return optional;
    }

    public String value() { return value; }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return name;
    }

}
