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

public class RunAfterCompletionProviderTest {

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

    private List<String> getSuggestionsForFile(String fileName) {
        myFixture.configureByFile(fileName);
        myFixture.complete(CompletionType.BASIC);
        return myFixture.getLookupElementStrings();
    }
}
