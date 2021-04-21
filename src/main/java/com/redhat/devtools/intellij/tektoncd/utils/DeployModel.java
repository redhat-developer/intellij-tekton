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
package com.redhat.devtools.intellij.tektoncd.utils;

import com.fasterxml.jackson.databind.JsonNode;
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext;

public class DeployModel extends com.redhat.devtools.intellij.common.utils.DeployModel {
    private String namespace;
    private JsonNode labels;
    public DeployModel(String namespace, String name, String kind, String apiVersion, JsonNode spec, JsonNode labels, CustomResourceDefinitionContext crdContext) {
        super(name, kind, apiVersion, spec, crdContext);
        this.namespace = namespace;
        this.labels = labels;
    }

    public String getNamespace() {
        return namespace;
    }

    public JsonNode getLabels() {
        return labels;
    }
}
