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
package com.redhat.devtools.intellij.tektoncd.tkn;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(using = ConditionDeserializer.class)
public class Condition {
    private String name;
    private String apiVersion;

    public Condition(String apiVersion, String name) {
        this.apiVersion = apiVersion;
        this.name = name;
    }

    public String getApiVersion() { return this.apiVersion; }

    public String getName() {
        return this.name;
    }
}
