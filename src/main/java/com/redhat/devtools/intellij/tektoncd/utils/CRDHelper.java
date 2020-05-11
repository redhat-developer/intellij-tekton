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

import com.google.common.base.Strings;
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext;

import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_CLUSTERTASKS;
import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_CLUSTERTRIGGERBINDINGS;

public class CRDHelper {
    public static CustomResourceDefinitionContext getCRDContext(String apiVersion, String plural) {
        if (Strings.isNullOrEmpty(apiVersion) || Strings.isNullOrEmpty(plural)) return null;
        String[] groupVersion = apiVersion.split("/");
        if (groupVersion.length != 2) return null;
        String group = groupVersion[0];
        String version = groupVersion[1];
        return new CustomResourceDefinitionContext.Builder()
                .withName(plural + "." + group)
                .withGroup(group)
                .withScope("Namespaced")
                .withVersion(version)
                .withPlural(plural)
                .build();
    }

    public static boolean isClusterScopedResource(String kind_plural) {
        switch (kind_plural) {
            case KIND_CLUSTERTASKS:
            case KIND_CLUSTERTRIGGERBINDINGS:
                return true;
            default:
                return false;
        }
    }
}
