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

public class CRDHelper {
    public static CustomResourceDefinitionContext getCRDContext(String version, String plural) {
        if (Strings.isNullOrEmpty(version) || Strings.isNullOrEmpty(plural)) return null;
        String[] apiVersion = version.split("/");
        if (apiVersion.length != 2) return null;
        return new CustomResourceDefinitionContext.Builder()
                .withName(plural + ".tekton.dev")
                .withGroup(apiVersion[0])
                .withScope("Namespaced")
                .withVersion(apiVersion[1])
                .withPlural(plural)
                .build();
    }
}
