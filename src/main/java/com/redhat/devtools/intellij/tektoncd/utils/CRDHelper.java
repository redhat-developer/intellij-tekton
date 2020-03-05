package com.redhat.devtools.intellij.tektoncd.utils;

import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext;

public class CRDHelper {
    public static CustomResourceDefinitionContext getCRDContext(String plural) {
        return new CustomResourceDefinitionContext.Builder()
                .withName(plural + ".tekton.dev")
                .withGroup("tekton.dev")
                .withScope("Namespaced")
                .withVersion("v1alpha1")
                .withPlural(plural)
                .build();
    }
}
