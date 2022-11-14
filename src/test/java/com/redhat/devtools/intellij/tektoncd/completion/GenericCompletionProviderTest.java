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

public class GenericCompletionProviderTest extends BaseCompletionProviderTest{

    /////////////////////////////////////////////////////////
    ///             PIPELINE - VARIABLES
    /////////////////////////////////////////////////////////

    @Test
    public void testPipelineCompletionWithoutAnyParam() {
        assertTrue(getSuggestionsForFile("pipeline5.yaml").isEmpty());
    }

    @Test
    public void testPipelineCompletionWithParams() {
        assertOrderedEquals(getSuggestionsForFile("pipeline7.yaml"), "$(params.param1", "$(params.param2");
    }

    @Test
    public void testPipelineCompletionWithParamsAndKeywordPartiallyTyped() {
        assertOrderedEquals(getSuggestionsForFile("pipeline8.yaml"), "$(parparams.param1", "$(parparams.param2");
    }

    @Test
    public void testPipelineCompletionWithOneTask() {
        assertTrue(getSuggestionsForFile("pipeline9.yaml").isEmpty());
    }

    @Test
    public void testPipelineCompletionWithMultipleTasks() {
        assertOrderedEquals(getSuggestionsForFile("pipeline10.yaml"), "$(tasks.step2", "$(tasks.step3");
    }

    @Test
    public void testPipelineCompletionWithTasksAndParams() {
        assertOrderedEquals(getSuggestionsForFile("pipeline13.yaml"), "$(params.param1", "$(params.param2", "$(tasks.step2", "$(tasks.step3");
    }

    /////////////////////////////////////////////////////////
    ///                 TASK - VARIABLES
    /////////////////////////////////////////////////////////

    @Test
    public void testTaskCompletionWithoutAnyInput() {
        assertTrue(getSuggestionsForFile("task1.yaml").isEmpty());
    }

    @Test
    public void testTaskCompletionWithParams() {
        assertOrderedEquals(getSuggestionsForFile("task2.yaml"), "$(params.param1", "$(params.param2");
    }

    @Test
    public void testTaskCompletionWithWorkspaces() {
        assertOrderedEquals(getSuggestionsForFile("task5.yaml"), "$(workspaces.write-allowed", "$(workspaces.write-disallowed");
    }

    @Test
    public void testTaskCompletionWithMultipleInputs() {
        assertOrderedEquals(getSuggestionsForFile("task6.yaml"), "$(params.param1", "$(workspaces.write-allowed", "$(workspaces.write-disallowed");
    }

    @Test
    public void testTaskCompletionWithParamsAndKeywordPartiallyTyped() {
        assertOrderedEquals(getSuggestionsForFile("task7.yaml"), "$(parparams.param1", "$(parparams.param2");
    }

    @Test
    public void testTaskCompletionWithWorkspaceResourcesAndKeywordPartiallyTyped() {
        assertOrderedEquals(getSuggestionsForFile("task10.yaml"), "$(workworkspaces.write-allowed", "$(workworkspaces.write-disallowed");
    }

    @Test
    public void testTaskCompletionWithWorkspaceSelected() {
        String[] lookups = new String[] {
                "$(workspaces.write-allowed.claim",
                "$(workspaces.write-allowed.path",
                "$(workspaces.write-allowed.volume"
        };
        assertOrderedEquals(getSuggestionsForFile("task13.yaml"), lookups);
    }

    @Override
    public String getTestDataPath() {
        return "src/test/resources/completion/generic";
    }
}
