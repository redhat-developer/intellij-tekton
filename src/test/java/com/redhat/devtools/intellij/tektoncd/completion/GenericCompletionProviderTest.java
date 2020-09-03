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

import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.openapi.vfs.newvfs.impl.VfsRootAccess;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.fixtures.IdeaProjectTestFixture;
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory;
import com.intellij.testFramework.fixtures.TestFixtureBuilder;
import java.io.File;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


import static com.intellij.testFramework.UsefulTestCase.assertOrderedEquals;
import static org.junit.Assert.assertTrue;

public class GenericCompletionProviderTest {

    private CodeInsightTestFixture myFixture;

    @Before
    public void setup() throws Exception {
        IdeaTestFixtureFactory factory = IdeaTestFixtureFactory.getFixtureFactory();
        TestFixtureBuilder<IdeaProjectTestFixture> fixtureBuilder = factory.createLightFixtureBuilder(null);
        IdeaProjectTestFixture fixture = fixtureBuilder.getFixture();

        myFixture = IdeaTestFixtureFactory.getFixtureFactory().createCodeInsightFixture(fixture, factory.createTempDirTestFixture());

        myFixture.setTestDataPath("src/test/resources/completion");
        myFixture.setUp();
        VfsRootAccess.allowRootAccess(new File("src").getAbsoluteFile().getParentFile().getAbsolutePath());
    }

    @After
    public void tearDown() throws Exception {
        myFixture.tearDown();
    }

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
    public void testTaskCompletionWithInputResources() {
        assertOrderedEquals(getSuggestionsForFile("task3.yaml"), "$(resources.inputs.resource1", "$(resources.inputs.resource2");
    }

    @Test
    public void testTaskCompletionWithOutputResources() {
        assertOrderedEquals(getSuggestionsForFile("task4.yaml"), "$(resources.outputs.resource1", "$(resources.outputs.resource2");
    }

    @Test
    public void testTaskCompletionWithWorkspaces() {
        assertOrderedEquals(getSuggestionsForFile("task5.yaml"), "$(workspaces.write-allowed", "$(workspaces.write-disallowed");
    }

    @Test
    public void testTaskCompletionWithMultipleInputs() {
        assertOrderedEquals(getSuggestionsForFile("task6.yaml"), "$(params.param1", "$(resources.inputs.resource1", "$(resources.outputs.resource1", "$(resources.outputs.resource2", "$(workspaces.write-allowed", "$(workspaces.write-disallowed");
    }

    @Test
    public void testTaskCompletionWithParamsAndKeywordPartiallyTyped() {
        assertOrderedEquals(getSuggestionsForFile("task7.yaml"), "$(parparams.param1", "$(parparams.param2");
    }

    @Test
    public void testTaskCompletionWithInputResourcesAndKeywordPartiallyTyped() {
        assertOrderedEquals(getSuggestionsForFile("task8.yaml"), "$(resresources.inputs.resource1", "$(resresources.inputs.resource2");
    }

    @Test
    public void testTaskCompletionWithOutputResourcesAndKeywordPartiallyTyped() {
        assertOrderedEquals(getSuggestionsForFile("task9.yaml"), "$(resresources.outputs.resource1", "$(resresources.outputs.resource2");
    }

    @Test
    public void testTaskCompletionWithWorkspaceResourcesAndKeywordPartiallyTyped() {
        assertOrderedEquals(getSuggestionsForFile("task10.yaml"), "$(workworkspaces.write-allowed", "$(workworkspaces.write-disallowed");
    }

    @Test
    public void testTaskCompletionWithInputResourceSelected() {
        String[] lookups = new String[] {
                "$(resources.inputs.resource1.depth",
                "$(resources.inputs.resource1.httpProxy",
                "$(resources.inputs.resource1.httpsProxy",
                "$(resources.inputs.resource1.name",
                "$(resources.inputs.resource1.noProxy",
                "$(resources.inputs.resource1.path",
                "$(resources.inputs.resource1.refspec",
                "$(resources.inputs.resource1.revision",
                "$(resources.inputs.resource1.sslVerify",
                "$(resources.inputs.resource1.type",
                "$(resources.inputs.resource1.url"
        };
        assertOrderedEquals(getSuggestionsForFile("task11.yaml"), lookups);
    }

    @Test
    public void testTaskCompletionWithOutputResourceSelected() {
        String[] lookups = new String[] {
                "$(resources.outputs.resource1.digest",
                "$(resources.outputs.resource1.name",
                "$(resources.outputs.resource1.path",
                "$(resources.outputs.resource1.type",
                "$(resources.outputs.resource1.url"
        };
        assertOrderedEquals(getSuggestionsForFile("task12.yaml"), lookups);
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

    /////////////////////////////////////////////////////////
    ///             CONDITIONS - VARIABLES
    /////////////////////////////////////////////////////////

    @Test
    public void testConditionCompletionWithoutAnyInput() {
        assertTrue(getSuggestionsForFile("condition1.yaml").isEmpty());
    }

    @Test
    public void testConditionCompletionWithParams() {
        assertOrderedEquals(getSuggestionsForFile("condition2.yaml"), "$(params.param1", "$(params.param2");
    }

    @Test
    public void testConditionCompletionWithParamsAndKeywordPartiallyTyped() {
        assertOrderedEquals(getSuggestionsForFile("condition3.yaml"), "$(paraparams.param1", "$(paraparams.param2");
    }

    @Test
    public void testConditionCompletionWithInputResources() {
        assertOrderedEquals(getSuggestionsForFile("condition4.yaml"), "$(resources.resource1", "$(resources.resource2");
    }

    @Test
    public void testConditionCompletionWithMultipleInputs() {
        assertOrderedEquals(getSuggestionsForFile("condition5.yaml"), "$(params.param1", "$(params.param2", "$(resources.resource1", "$(resources.resource2");
    }

    @Test
    public void testConditionCompletionWithInputResourcesAndKeywordPartiallyTyped() {
        assertOrderedEquals(getSuggestionsForFile("condition6.yaml"), "$(resourcresources.resource1", "$(resourcresources.resource2");
    }

    @Test
    public void testConditionCompletionWithInputResourceSelected() {
        String[] lookups = new String[] {
                "$(resources.resource2.location",
                "$(resources.resource2.name",
                "$(resources.resource2.path",
                "$(resources.resource2.type",
        };
        assertOrderedEquals(getSuggestionsForFile("condition7.yaml"), lookups);
    }

    private List<String> getSuggestionsForFile(String fileName) {
        myFixture.configureByFile(fileName);
        myFixture.complete(CompletionType.BASIC);
        return myFixture.getLookupElementStrings();
    }

}
