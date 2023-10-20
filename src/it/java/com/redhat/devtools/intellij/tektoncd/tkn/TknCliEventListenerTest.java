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
package com.redhat.devtools.intellij.tektoncd.tkn;

import com.redhat.devtools.intellij.tektoncd.TestUtils;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Test;


import static com.redhat.devtools.intellij.tektoncd.TestUtils.getValueFromResource;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TknCliEventListenerTest extends TknCliTest {
    private static final String EL_PLURAL = "eventlisteners";
    private static final String EL_NAME_IN_FILE = "elfoo";

    public void testVerifyCreateEventListenerAndDelete() throws IOException, InterruptedException {
        final String EL_NAME = "elfirst";
        String resourceBody = TestUtils.load("eventlistener1.yaml").replace(EL_NAME_IN_FILE, EL_NAME);
        TestUtils.saveResource(getTkn(), resourceBody, NAMESPACE, EL_PLURAL);
        // verify el has been created
        List<String> els = getTkn().getEventListeners(NAMESPACE);
        assertTrue(els.contains(EL_NAME));
        // clean up and verify cleaning succeed
        getTkn().deleteEventListeners(NAMESPACE, els.stream().filter(el -> el.equalsIgnoreCase(EL_NAME)).collect(Collectors.toList()));
        Thread.sleep(2000); // adding a bit delay to allow run to be created
        els = getTkn().getEventListeners(NAMESPACE);
        assertFalse(els.contains(EL_NAME));
    }

    public void testVerifyEventListenerYAMLIsReturnedCorrectly() throws IOException, InterruptedException {
        final String EL_NAME = "elsecond";
        String resourceBody = TestUtils.load("eventlistener1.yaml").replace(EL_NAME_IN_FILE, EL_NAME);
        TestUtils.saveResource(getTkn(), resourceBody, NAMESPACE, EL_PLURAL);
        // verify tb has been created
        List<String> els = getTkn().getEventListeners(NAMESPACE);
        assertTrue(els.contains(EL_NAME));
        // get YAML from cluster and verify is the same uploaded
        String resourceBodyFromCluster = getTkn().getEventListenerYAML(NAMESPACE, EL_NAME);
        String[] triggersPath = new String[] {"spec", "triggers", "name"};
        assertEquals(getValueFromResource(resourceBody, triggersPath), getValueFromResource(resourceBodyFromCluster, triggersPath));
        String[] saPath = new String[] {"spec", "serviceAccountName"};
        assertEquals(getValueFromResource(resourceBody, saPath), getValueFromResource(resourceBodyFromCluster, saPath));
        /// clean up and verify cleaning succeed
        getTkn().deleteEventListeners(NAMESPACE, els.stream().filter(el -> el.equalsIgnoreCase(EL_NAME)).collect(Collectors.toList()));
        Thread.sleep(2000); // adding a bit delay to allow run to be created
        els = getTkn().getEventListeners(NAMESPACE);
        assertFalse(els.contains(EL_NAME));
    }


}
