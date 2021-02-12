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

import com.redhat.devtools.intellij.tektoncd.TestUtils;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Test;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TknCliTriggerBindingTest extends TknCliTest {
    private static final String TB_PLURAL = "triggerbindings";

    @Test
    public void verifyCreateTriggerBindingAndDelete() throws IOException {
        final String TB_NAME = "tbfirst";
        String resourceBody = TestUtils.load("triggerbinding1.yaml").replace("tbfoo", TB_NAME);
        TestUtils.saveResource(tkn, resourceBody, NAMESPACE, TB_PLURAL);
        // verify tb has been created
        List<String> tbs = tkn.getTriggerBindings(NAMESPACE);
        assertTrue(tbs.contains(TB_NAME));
        // clean up and verify cleaning succeed
        tkn.deleteTriggerBindings(NAMESPACE, tbs.stream().filter(tb -> tb.equalsIgnoreCase(TB_NAME)).collect(Collectors.toList()));
        tbs = tkn.getTriggerBindings(NAMESPACE);
        assertFalse(tbs.contains(TB_NAME));
    }

    @Test
    public void verifyTriggerBindingYAMLIsReturnedCorrectly() throws IOException {
        final String TB_NAME = "ctbsecond";
        String resourceBody = TestUtils.load("triggerbinding1.yaml").replace("tbfoo", TB_NAME);
        TestUtils.saveResource(tkn, resourceBody, NAMESPACE, TB_PLURAL);
        // verify tb has been created
        List<String> tbs = tkn.getTriggerBindings(NAMESPACE);
        assertTrue(tbs.contains(TB_NAME));
        // get YAML from cluster and verify is the same uploaded
        String resourceBodyFromCluster = tkn.getTriggerBindingYAML(NAMESPACE, TB_NAME);
        assertEquals(TestUtils.getSpecFromResource(resourceBody), TestUtils.getSpecFromResource(resourceBodyFromCluster));
        /// clean up and verify cleaning succeed
        tkn.deleteTriggerBindings(NAMESPACE, tbs.stream().filter(tb -> tb.equalsIgnoreCase(TB_NAME)).collect(Collectors.toList()));
        tbs = tkn.getTriggerBindings(NAMESPACE);
        assertFalse(tbs.contains(TB_NAME));
    }
}
