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

public class TknCliPipelineResourceTest extends TknCliTest {

    @Test
    public void verifyCreatePipelineResourceAndDelete() throws IOException {
        final String RESOURCE_NAME = "first";
        String resourceBody = TestUtils.load("pipelineresource1.yaml").replace("resourcefoo", RESOURCE_NAME);
        TestUtils.saveResource(tkn, resourceBody, NAMESPACE, "pipelineresources");
        // verify pipeline resource has been created
        List<Resource> resources = tkn.getResources(NAMESPACE);
        assertTrue(resources.stream().anyMatch(resource -> resource.name().equals(RESOURCE_NAME)));
        // clean up and verify cleaning succeed
        tkn.deleteResources(NAMESPACE, resources.stream().map(condition -> condition.name()).collect(Collectors.toList()));
        resources = tkn.getResources(NAMESPACE);
        assertFalse(resources.stream().anyMatch(resource -> resource.name().equals(RESOURCE_NAME)));
    }

    @Test
    public void verifyPipelineResourceYAMLIsReturnedCorrectly() throws IOException {
        final String RESOURCE_NAME = "second";
        String resourceBody = TestUtils.load("pipelineresource1.yaml").replace("resourcefoo", RESOURCE_NAME);
        TestUtils.saveResource(tkn, resourceBody, NAMESPACE, "pipelineresources");
        // verify pipeline resource has been created
        List<Resource> resources = tkn.getResources(NAMESPACE);
        assertTrue(resources.stream().anyMatch(resource -> resource.name().equals(RESOURCE_NAME)));
        // get YAML from cluster and verify is the same uploaded
        String resourceBodyFromCluster = tkn.getResourceYAML(NAMESPACE, RESOURCE_NAME);
        assertEquals(TestUtils.getSpecFromResource(resourceBody), TestUtils.getSpecFromResource(resourceBodyFromCluster));
        // clean up and verify cleaning succeed
        tkn.deleteResources(NAMESPACE, resources.stream().map(condition -> condition.name()).collect(Collectors.toList()));
        resources = tkn.getResources(NAMESPACE);
        assertFalse(resources.stream().anyMatch(resource -> resource.name().equals(RESOURCE_NAME)));
    }

}