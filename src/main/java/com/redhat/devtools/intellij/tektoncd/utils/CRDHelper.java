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
package com.redhat.devtools.intellij.tektoncd.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Strings;
import io.fabric8.kubernetes.api.model.GenericKubernetesResource;
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext;
import java.io.IOException;


import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_CLUSTERTASK;
import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_CLUSTERTASKS;
import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_CLUSTERTRIGGERBINDING;
import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_CLUSTERTRIGGERBINDINGS;
import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_PIPELINERUN;
import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_TASKRUN;

public class CRDHelper {
    public static CustomResourceDefinitionContext getCRDContext(String apiVersion, String plural) {
        if (Strings.isNullOrEmpty(apiVersion) || Strings.isNullOrEmpty(plural)) return null;
        String[] groupVersion = apiVersion.split("/");
        if (groupVersion.length != 2 && !apiVersion.equalsIgnoreCase("triggers.tekton.dev")) {
            return null;
        }
        String group = groupVersion[0];
        String version = groupVersion.length > 1 ? groupVersion[1]: "v1alpha1";
        if (isClusterScopedResource(plural)) {
            return new CustomResourceDefinitionContext.Builder()
                    .withName(plural + "." + group)
                    .withGroup(group)
                    .withVersion(version)
                    .withPlural(plural)
                    .build();
        } else {
            return new CustomResourceDefinitionContext.Builder()
                    .withName(plural + "." + group)
                    .withGroup(group)
                    .withScope("Namespaced")
                    .withVersion(version)
                    .withPlural(plural)
                    .build();
        }
    }

    public static boolean isClusterScopedResource(String kind) {
        return kind.equalsIgnoreCase(KIND_CLUSTERTASKS) || kind.equalsIgnoreCase(KIND_CLUSTERTASK) ||
                kind.equalsIgnoreCase(KIND_CLUSTERTRIGGERBINDING) || kind.equalsIgnoreCase(KIND_CLUSTERTRIGGERBINDINGS);
    }

    public static boolean isRunResource(String kind) {
        return kind.equalsIgnoreCase(KIND_PIPELINERUN) || kind.equalsIgnoreCase(KIND_TASKRUN);
    }

    public static JsonNode convertToJsonNode(GenericKubernetesResource resource) throws IOException {
        String resourceAsYAML = YAMLBuilder.writeValueAsString(resource);
        return YAMLBuilder.convertToObjectNode(resourceAsYAML);
    }
}
