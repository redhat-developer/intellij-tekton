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

import io.fabric8.tekton.pipeline.v1alpha1.Condition;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Test;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TknCliConditionTest extends TknCliTest {

    @Test
    public void verifyCreateConditionAndDelete() throws IOException {
        final String CONDITION_NAME = "first";
        String resourceBody = load("condition1.yaml").replace("conditionfoo", CONDITION_NAME);
        saveResource(resourceBody, NAMESPACE, "conditions");
        // verify condition has been created
        List<Condition> conditions = tkn.getConditions(NAMESPACE);
        assertTrue(conditions.stream().anyMatch(condition -> condition.getMetadata().getName().equals(CONDITION_NAME)));
        // clean up and verify cleaning succeed
        tkn.deleteConditions(NAMESPACE, conditions.stream().map(condition -> condition.getMetadata().getName()).collect(Collectors.toList()));
        conditions = tkn.getConditions(NAMESPACE);
        assertFalse(conditions.stream().anyMatch(condition -> condition.getMetadata().getName().equals(CONDITION_NAME)));
    }

    @Test
    public void verifyConditionYAMLIsReturnedCorrectly() throws IOException {
        final String CONDITION_NAME = "second";
        String resourceBody = load("condition1.yaml").replace("conditionfoo", CONDITION_NAME);
        saveResource(resourceBody, NAMESPACE, "conditions");
        // verify condition has been created
        List<Condition> conditions = tkn.getConditions(NAMESPACE);
        assertTrue(conditions.stream().anyMatch(condition -> condition.getMetadata().getName().equals(CONDITION_NAME)));
        // get YAML from cluster and verify is the same uploaded
        String resourceBodyFromCluster = tkn.getConditionYAML(NAMESPACE, CONDITION_NAME);
        assertEquals(getSpecFromResource(resourceBody), getSpecFromResource(resourceBodyFromCluster));
        // clean up and verify cleaning succeed
        tkn.deleteConditions(NAMESPACE, conditions.stream().map(condition -> condition.getMetadata().getName()).collect(Collectors.toList()));
        conditions = tkn.getConditions(NAMESPACE);
        assertFalse(conditions.stream().anyMatch(condition -> condition.getMetadata().getName().equals(CONDITION_NAME)));
    }

}