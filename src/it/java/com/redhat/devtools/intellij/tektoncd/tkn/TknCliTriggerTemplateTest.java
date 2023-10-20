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

public class TknCliTriggerTemplateTest extends TknCliTest {
    private static final String TT_PLURAL = "triggertemplates";

    public void testVerifyCreateTriggerTemplateAndDelete() throws IOException {
        final String TT_NAME = "ttfirst";
        String resourceBody = TestUtils.load("triggertemplate1.yaml").replace("ttfoo", TT_NAME);
        TestUtils.saveResource(getTkn(), resourceBody, NAMESPACE, TT_PLURAL);
        // verify tt has been created
        List<String> tts = getTkn().getTriggerTemplates(NAMESPACE);
        assertTrue(tts.contains(TT_NAME));
        // clean up and verify cleaning succeed
        getTkn().deleteTriggerTemplates(NAMESPACE, tts.stream().filter(tt -> tt.equalsIgnoreCase(TT_NAME)).collect(Collectors.toList()));
        tts = getTkn().getTriggerTemplates(NAMESPACE);
        assertFalse(tts.contains(TT_NAME));
    }

    public void testVerifyTriggerTemplateYAMLIsReturnedCorrectly() throws IOException {
        final String TT_NAME = "ttsecond";
        String resourceBody = TestUtils.load("triggertemplate1.yaml").replace("ttfoo", TT_NAME);
        TestUtils.saveResource(getTkn(), resourceBody, NAMESPACE, TT_PLURAL);
        // verify tt has been created
        List<String> tts = getTkn().getTriggerTemplates(NAMESPACE);
        assertTrue(tts.contains(TT_NAME));
        // get YAML from cluster and verify is the same uploaded
        String resourceBodyFromCluster = getTkn().getTriggerTemplateYAML(NAMESPACE, TT_NAME);
        assertEquals(TestUtils.getSpecFromResource(resourceBody), TestUtils.getSpecFromResource(resourceBodyFromCluster));
        /// clean up and verify cleaning succeed
        getTkn().deleteTriggerTemplates(NAMESPACE, tts.stream().filter(tt -> tt.equalsIgnoreCase(TT_NAME)).collect(Collectors.toList()));
        tts = getTkn().getTriggerTemplates(NAMESPACE);
        assertFalse(tts.contains(TT_NAME));
    }
}
