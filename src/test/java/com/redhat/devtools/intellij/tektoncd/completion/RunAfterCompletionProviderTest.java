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
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.fixtures.IdeaProjectTestFixture;
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory;
import com.intellij.testFramework.fixtures.TestFixtureBuilder;
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
    }

    @After
    public void tearDown() throws Exception {
        myFixture.tearDown();
    }

    @Test
    public void testCompletionWithASingleLookup() {
        testCompletion(new String[] { "pipeline1.yaml"}, "step1");
    }

    @Test
    public void testCompletionWithMultipleLookups() {
        testCompletion(new String[] { "pipeline2.yaml"}, "step2", "step3");
    }

    @Test
    public void testCompletionWithMultipleLookupsAndTasksAlreadyUsed() {
        testCompletion(new String[] { "pipeline3.yaml"}, "step4", "step5");
    }

    @Test
    public void testCompletionWithNoLookups() {
        myFixture.configureByFiles(new String[] { "pipeline4.yaml"});
        myFixture.complete(CompletionType.BASIC, 1);
        final List<String> strings = myFixture.getLookupElementStrings();
        assertTrue(strings.isEmpty());
    }

    private void testCompletion(String[] fileNames, String... expectedSuggestions) {
        myFixture.configureByFiles(fileNames);
        myFixture.complete(CompletionType.BASIC, 1);
        final List<String> strings = myFixture.getLookupElementStrings();
        assertOrderedEquals(strings, expectedSuggestions);
    }
}
