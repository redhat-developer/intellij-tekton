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
package com.redhat.devtools.intellij.tektoncd.completion;

import org.junit.Test;


import static com.intellij.testFramework.UsefulTestCase.assertOrderedEquals;
import static org.junit.Assert.assertTrue;

public class ResourceInPipelineCompletionProviderTest extends BaseCompletionProviderTest{

    @Test
    public void testCompletionUnderResourcesParent() {
        assertOrderedEquals(getSuggestionsForFile("pipeline1.yaml"), "source-repo");
    }

    @Test
    public void testCompletionUnderInputResourcesParent() {
        assertOrderedEquals(getSuggestionsForFile("pipeline2.yaml"), "source-repo");
    }

    @Test
    public void testCompletionUnderOutputResourcesParent() {
        assertOrderedEquals(getSuggestionsForFile("pipeline3.yaml"), "source-repo");
    }

    @Test
    public void testCompletionWithMultipleLookups() {
        assertOrderedEquals(getSuggestionsForFile("pipeline4.yaml"), "source-repo", "source-repo-2", "source-repo-3");
    }

    @Test
    public void testCompletionWithNoLookups() {
        assertTrue(getSuggestionsForFile("pipeline5.yaml").isEmpty());
    }

    @Test
    public void testCompletionIfWrongConfiguration() {
        assertTrue(getSuggestionsForFile("task1.yaml").isEmpty());
    }

    @Override
    public String getTestDataPath() {
        return "src/test/resources/completion/resourceInPipeline";
    }
}
