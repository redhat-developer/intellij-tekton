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
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.fixtures.IdeaProjectTestFixture;
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory;
import com.intellij.testFramework.fixtures.TestFixtureBuilder;
import com.redhat.devtools.intellij.common.utils.VfsRootAccessHelper;
import org.junit.After;
import org.junit.Before;

import java.io.File;
import java.util.List;

public abstract class BaseCompletionProviderTest {

    private CodeInsightTestFixture myFixture;

    @Before
    public void setup() throws Exception {
        IdeaTestFixtureFactory factory = IdeaTestFixtureFactory.getFixtureFactory();
        TestFixtureBuilder<IdeaProjectTestFixture> fixtureBuilder = factory.createLightFixtureBuilder((LightProjectDescriptor) null);
        IdeaProjectTestFixture fixture = fixtureBuilder.getFixture();

        myFixture = IdeaTestFixtureFactory.getFixtureFactory().createCodeInsightFixture(fixture, factory.createTempDirTestFixture());

        myFixture.setTestDataPath(getTestDataPath());
        myFixture.setUp();
        VfsRootAccessHelper.allowRootAccess(new File("src").getAbsoluteFile().getParentFile().getAbsolutePath());
    }

    @After
    public void tearDown() throws Exception {
        myFixture.tearDown();
    }

    protected List<String> getSuggestionsForFile(String fileName) {
        myFixture.configureByFile(fileName);
        myFixture.complete(CompletionType.BASIC);
        return myFixture.getLookupElementStrings();
    }

    public abstract String getTestDataPath();
}
