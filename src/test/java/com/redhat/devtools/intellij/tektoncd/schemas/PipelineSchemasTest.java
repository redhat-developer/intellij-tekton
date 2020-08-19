/*******************************************************************************
 * Copyright (c) 2020 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package com.redhat.devtools.intellij.tektoncd.schemas;

import com.intellij.openapi.vfs.newvfs.impl.VfsRootAccess;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.fixtures.IdeaProjectTestFixture;
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory;
import com.intellij.testFramework.fixtures.TestFixtureBuilder;
import org.jetbrains.yaml.schema.YamlJsonSchemaHighlightingInspection;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

public class PipelineSchemasTest {
    private CodeInsightTestFixture myFixture;

    @Before
    public void setup() throws Exception {
        IdeaTestFixtureFactory factory = IdeaTestFixtureFactory.getFixtureFactory();
        TestFixtureBuilder<IdeaProjectTestFixture> fixtureBuilder = factory.createLightFixtureBuilder(null);
        IdeaProjectTestFixture fixture = fixtureBuilder.getFixture();

        myFixture = IdeaTestFixtureFactory.getFixtureFactory().createCodeInsightFixture(fixture, factory.createTempDirTestFixture());

        myFixture.setTestDataPath("src/test/resources");
        myFixture.setUp();
        myFixture.enableInspections(YamlJsonSchemaHighlightingInspection.class);
        VfsRootAccess.allowRootAccess(new File("src").getAbsoluteFile().getParentFile().getAbsolutePath());
    }

    @After
    public void tearDown() throws Exception {
        myFixture.tearDown();
    }

    @Test
    public void testPipelineWithStringParameter() {
        myFixture.configureByFile("schemas/pipeline1.yaml");
        myFixture.checkHighlighting();
    }

    @Test
    public void testPipelineWithArrayStringParameter() {
        myFixture.configureByFile("schemas/pipeline2.yaml");
        myFixture.checkHighlighting();
    }

    @Test
    public void testPipelineWithTimeoutInHours() {
        myFixture.configureByFile("schemas/pipeline-timeout-hours.yaml");
        myFixture.checkHighlighting();
    }

    @Test
    public void testPipelineWithTimeoutInMinutes() {
        myFixture.configureByFile("schemas/pipeline-timeout-minutes.yaml");
        myFixture.checkHighlighting();
    }

    @Test
    public void testPipelineWithTimeoutInSeconds() {
        myFixture.configureByFile("schemas/pipeline-timeout-seconds.yaml");
        myFixture.checkHighlighting();
    }

    @Test
    public void testPipelineWithTimeoutInMillisSeconds() {
        myFixture.configureByFile("schemas/pipeline-timeout-milliseconds.yaml");
        myFixture.checkHighlighting();
    }

    @Test
    public void testPipelineWithTimeoutInMicroSeconds() {
        myFixture.configureByFile("schemas/pipeline-timeout-microseconds.yaml");
        myFixture.checkHighlighting();
    }

    @Test
    public void testPipelineWithTimeoutInMicroSeconds1() {
        myFixture.configureByFile("schemas/pipeline-timeout-microseconds1.yaml");
        myFixture.checkHighlighting();
    }

    @Test
    public void testPipelineWithTimeoutInMicroSeconds2() {
        myFixture.configureByFile("schemas/pipeline-timeout-microseconds2.yaml");
        myFixture.checkHighlighting();
    }

    @Test
    public void testPipelineWithTimeoutInNanoSeconds() {
        myFixture.configureByFile("schemas/pipeline-timeout-nanoseconds.yaml");
        myFixture.checkHighlighting();
    }

    @Test
    public void testPipelineWithTimeoutWithSeveralUnits() {
        myFixture.configureByFile("schemas/pipeline-timeout-several-units.yaml");
        myFixture.checkHighlighting();
    }

    @Test
    public void testPipelineWithFinally() {
        myFixture.configureByFile("schemas/pipeline-finally.yaml");
        myFixture.checkHighlighting();
    }
}
