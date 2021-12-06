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
package com.redhat.devtools.intellij.tektoncd.inspector;

import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.fixtures.IdeaProjectTestFixture;
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory;
import com.intellij.testFramework.fixtures.TestFixtureBuilder;
import com.redhat.devtools.intellij.common.utils.VfsRootAccessHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class VariableReferenceInspectorTest {

    private CodeInsightTestFixture myFixture;

    @Before
    public void setup() throws Exception {
        IdeaTestFixtureFactory factory = IdeaTestFixtureFactory.getFixtureFactory();
        TestFixtureBuilder<IdeaProjectTestFixture> fixtureBuilder = factory.createLightFixtureBuilder(null);
        IdeaProjectTestFixture fixture = fixtureBuilder.getFixture();

        myFixture = IdeaTestFixtureFactory.getFixtureFactory().createCodeInsightFixture(fixture, factory.createTempDirTestFixture());

        myFixture.setTestDataPath("src/test/resources/utils/variableReferenceInspector/");
        myFixture.setUp();
        myFixture.enableInspections(VariableReferencesInspector.class);
        VfsRootAccessHelper.allowRootAccess(new File("src").getAbsoluteFile().getParentFile().getAbsolutePath());
    }

    @After
    public void tearDown() throws Exception {
        myFixture.tearDown();
    }

    @Test
    public void testPipelineHighlightWithUnusedParameter() {
        myFixture.configureByFile("pipeline1.yaml");
        List<HighlightInfo> hightlightInfo =  myFixture.doHighlighting();
        assertEquals(hightlightInfo.size(), 1);
        assertTrue(hightlightInfo.get(0).getDescription().equals("Variable p1 is never used"));
    }

    @Test
    public void testPipelineHighlightWithUnusedResource() {
        myFixture.configureByFile("pipeline2.yaml");
        List<HighlightInfo> hightlightInfo =  myFixture.doHighlighting();
        assertEquals(hightlightInfo.size(), 2);
        assertTrue(hightlightInfo.get(0).getDescription().equals("Variable source-repo is never used"));
        assertTrue(hightlightInfo.get(1).getDescription().equals("Variable web-image is never used"));
    }

    @Test
    public void testPipelineHighlightWithUnusedWorkspace() {
        myFixture.configureByFile("pipeline3.yaml");
        List<HighlightInfo> hightlightInfo =  myFixture.doHighlighting();
        assertEquals(hightlightInfo.size(), 1);
        assertTrue(hightlightInfo.get(0).getDescription().equals("Variable password-vault is never used"));
    }

    @Test
    public void testPipelineHighlightWithUnusedVariables() {
        myFixture.configureByFile("pipeline4.yaml");
        List<HighlightInfo> hightlightInfo =  myFixture.doHighlighting();
        assertEquals(hightlightInfo.size(), 3);
        assertTrue(hightlightInfo.get(0).getDescription().equals("Variable p1 is never used"));
        assertTrue(hightlightInfo.get(1).getDescription().equals("Variable source-repo is never used"));
        assertTrue(hightlightInfo.get(2).getDescription().equals("Variable password-vault is never used"));
    }

    @Test
    public void testTaskHighlightWithUnusedParameter() {
        myFixture.configureByFile("task1.yaml");
        List<HighlightInfo> hightlightInfo =  myFixture.doHighlighting();
        assertEquals(hightlightInfo.size(), 1);
        assertTrue(hightlightInfo.get(0).getDescription().equals("Variable parm1 is never used"));
    }

    @Test
    public void testTaskHighlightWithUnusedInputResource() {
        myFixture.configureByFile("task2.yaml");
        List<HighlightInfo> hightlightInfo =  myFixture.doHighlighting();
        assertEquals(hightlightInfo.size(), 1);
        assertTrue(hightlightInfo.get(0).getDescription().equals("Variable resource1 is never used"));
    }

    @Test
    public void testTaskHighlightWithUnusedOutputResource() {
        myFixture.configureByFile("task3.yaml");
        List<HighlightInfo> hightlightInfo =  myFixture.doHighlighting();
        assertEquals(hightlightInfo.size(), 1);
        assertTrue(hightlightInfo.get(0).getDescription().equals("Variable resource1 is never used"));
    }

    @Test
    public void testTaskHighlightWithUnusedWorkspace() {
        myFixture.configureByFile("task4.yaml");
        List<HighlightInfo> hightlightInfo =  myFixture.doHighlighting();
        assertEquals(hightlightInfo.size(), 2);
        assertTrue(hightlightInfo.get(0).getDescription().equals("Variable write-allowed is never used"));
        assertTrue(hightlightInfo.get(1).getDescription().equals("Variable write-disallowed is never used"));
    }

    @Test
    public void testTaskHighlightWithUnusedVariables() {
        myFixture.configureByFile("task5.yaml");
        List<HighlightInfo> hightlightInfo =  myFixture.doHighlighting();
        assertEquals(hightlightInfo.size(), 4);
        assertTrue(hightlightInfo.get(0).getDescription().equals("Variable write-allowed is never used"));
        assertTrue(hightlightInfo.get(1).getDescription().equals("Variable parm1 is never used"));
        assertTrue(hightlightInfo.get(2).getDescription().equals("Variable resource2 is never used"));
        assertTrue(hightlightInfo.get(3).getDescription().equals("Variable resource1 is never used"));

    }

}
