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

public class RunAfterCompletionProviderTest extends BaseCompletionProviderTest {

    @Test
    public void testCompletionWithASingleLookup() {
        assertOrderedEquals(getSuggestionsForFile("pipeline1.yaml"), "step1");
    }

    @Test
    public void testCompletionWithMultipleLookups() {
        assertOrderedEquals(getSuggestionsForFile("pipeline2.yaml"), "step2", "step3");
    }

    @Test
    public void testCompletionWithMultipleLookupsAndTasksAlreadyUsed() {
        assertOrderedEquals(getSuggestionsForFile("pipeline3.yaml"), "step4", "step5");
    }

    @Test
    public void testCompletionWithNoLookups() {
        assertTrue(getSuggestionsForFile("pipeline4.yaml").isEmpty());
    }

    @Override
    public String getTestDataPath() {
        return "src/test/resources/completion/runAfter";
    }
}
