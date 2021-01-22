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

public class TknCliClusterTriggerBindingTest extends TknCliTest {
    private static final String CTB_PLURAL = "clustertriggerbindings";

    @Test
    public void verifyCreateClusterTriggerBindingAndDelete() throws IOException {
        final String CTB_NAME = "ctbfirst";
        String resourceBody = TestUtils.load("clustertriggerbinding1.yaml").replace("ctbfoo", CTB_NAME);
        TestUtils.saveResource(tkn, resourceBody, "", CTB_PLURAL);
        // verify ctb has been created
        List<String> ctbs = tkn.getClusterTriggerBindings();
        assertTrue(ctbs.contains(CTB_NAME));
        // clean up and verify cleaning succeed
        tkn.deleteClusterTriggerBindings(ctbs.stream().filter(ctb -> ctb.equalsIgnoreCase(CTB_NAME)).collect(Collectors.toList()));
        ctbs = tkn.getClusterTriggerBindings();
        assertFalse(ctbs.contains(CTB_NAME));
    }

    @Test
    public void verifyClusterTriggerBindingYAMLIsReturnedCorrectly() throws IOException {
        final String CTB_NAME = "ctbsecond";
        String resourceBody = TestUtils.load("clustertriggerbinding1.yaml").replace("ctbfoo", CTB_NAME);
        TestUtils.saveResource(tkn, resourceBody, "", CTB_PLURAL);
        // verify ctb has been created
        List<String> ctbs = tkn.getClusterTriggerBindings();
        assertTrue(ctbs.contains(CTB_NAME));
        // get YAML from cluster and verify is the same uploaded
        String resourceBodyFromCluster = tkn.getClusterTriggerBindingYAML(CTB_NAME);
        assertEquals(TestUtils.getSpecFromResource(resourceBody), TestUtils.getSpecFromResource(resourceBodyFromCluster));
        /// clean up and verify cleaning succeed
        tkn.deleteClusterTriggerBindings(ctbs.stream().filter(ctb -> ctb.equalsIgnoreCase(CTB_NAME)).collect(Collectors.toList()));
        ctbs = tkn.getClusterTriggerBindings();
        assertFalse(ctbs.contains(CTB_NAME));
    }
}
