/*******************************************************************************
 * Copyright (c) 2019 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package com.redhat.devtools.intellij.tektoncd.tkn;

import java.io.IOException;
import java.util.List;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;


import static org.junit.Assert.assertEquals;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TknCliPipelineTest extends TknCliTest {
    @Test
    public void verifyNoPipelines() throws IOException {
        List<String> pipelines = tkn.getPipelines(NAMESPACE);
        assertEquals(0, pipelines.size());
    }

    @Test
    public void verifyCreatePipelineAndDelete() throws IOException {
        saveResource(load("pipeline1.yaml"), NAMESPACE, "pipelines");
        // verify pipeline has been created
        List<String> pipelines = tkn.getPipelines(NAMESPACE);
        assertEquals(1, pipelines.size());
        // clean up and verify cleaning succeed
        tkn.deletePipelines(NAMESPACE, pipelines, false);
        pipelines = tkn.getPipelines(NAMESPACE);
        assertEquals(0, pipelines.size());
    }

    @Test
    public void verifyPipelineYAMLIsReturnedCorrectly() throws IOException {
        String resourceBody = load("pipeline1.yaml");
        saveResource(resourceBody, NAMESPACE, "pipelines");
        // verify pipeline has been created
        List<String> pipelines = tkn.getPipelines(NAMESPACE);
        assertEquals(1, pipelines.size());
        // get YAML from cluster and verify is the same uploaded
        String resourceBodyFromCluster = tkn.getPipelineYAML(NAMESPACE, pipelines.get(0));
        assertEquals(getSpecFromResource(resourceBody), getSpecFromResource(resourceBodyFromCluster));
        // clean up and verify cleaning succeed
        tkn.deletePipelines(NAMESPACE, pipelines, false);
        pipelines = tkn.getPipelines(NAMESPACE);
        assertEquals(0, pipelines.size());
    }
}
