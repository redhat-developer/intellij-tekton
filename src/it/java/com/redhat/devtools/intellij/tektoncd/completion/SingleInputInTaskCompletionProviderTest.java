/*******************************************************************************
 * Copyright (c) 2021 Red Hat, Inc.
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
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.newvfs.impl.VfsRootAccess;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.fixtures.IdeaProjectTestFixture;
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory;
import com.intellij.testFramework.fixtures.TestFixtureBuilder;
import com.redhat.devtools.intellij.tektoncd.TestUtils;
import com.redhat.devtools.intellij.tektoncd.tkn.Tkn;
import com.redhat.devtools.intellij.tektoncd.tkn.TknCliFactory;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


import static com.intellij.testFramework.UsefulTestCase.assertOrderedEquals;

public class SingleInputInTaskCompletionProviderTest {

    private Tkn tkn;
    private static final String NAMESPACE = "testns";
    private CodeInsightTestFixture myFixture;

    @Before
    public void setup() throws Exception {
        IdeaTestFixtureFactory factory = IdeaTestFixtureFactory.getFixtureFactory();
        TestFixtureBuilder<IdeaProjectTestFixture> fixtureBuilder = factory.createLightFixtureBuilder(null);
        IdeaProjectTestFixture fixture = fixtureBuilder.getFixture();

        myFixture = IdeaTestFixtureFactory.getFixtureFactory().createCodeInsightFixture(fixture, factory.createTempDirTestFixture());

        myFixture.setTestDataPath(getTestDataPath());
        myFixture.setUp();
        VfsRootAccess.allowRootAccess(new File("src").getAbsoluteFile().getParentFile().getAbsolutePath());
        Project project = myFixture.getProject();
        tkn = TknCliFactory.getInstance().getTkn(project).get();
        loadResources();
    }

    @After
    public void tearDown() throws Exception {
        deleteResources();
        myFixture.tearDown();
    }

    private void loadResources() throws IOException {
        String resourceBody = TestUtils.load("completion/singleInputInTask/task1.yaml");
        TestUtils.saveResource(tkn, resourceBody, NAMESPACE, "tasks");
    }

    private void deleteResources() throws IOException {
        tkn.deleteTasks(NAMESPACE, Arrays.asList("singleInputInTask"), false);
    }

    private String getTestDataPath() {
        return "src/it/resources/completion/singleInputInTask";
    }

    private List<String> getSuggestionsForFile(String fileName) {
        myFixture.configureByFile(fileName);
        myFixture.complete(CompletionType.BASIC);
        return myFixture.getLookupElementStrings();
    }

    @Test
    public void testCompletionWithSingleParam() {
        assertOrderedEquals(getSuggestionsForFile("pipeline1.yaml"), "param1", "param2");
    }

   /* @Test
    public void testCompletionWithSingleResourceInput() {
        assertOrderedEquals(getSuggestionsForFile("pipeline2.yaml"), "source-repo");
    }

    @Test
    public void testCompletionWithSingleResourceOutput() {
        assertOrderedEquals(getSuggestionsForFile("pipeline3.yaml"), "source-repo");
    }

    @Test
    public void testCompletionWithSingleWorkspace() {
        assertOrderedEquals(getSuggestionsForFile("pipeline4.yaml"), "source-repo", "source-repo-2", "source-repo-3");
    }*/

}
