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

public class EventListenerSchemasTest {
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
    public void testEventListener() {
        myFixture.configureByFile("schemas/eventlistener1.yaml");
        myFixture.checkHighlighting();
    }

    @Test
    public void testEventListenerWithGitHubInterceptor() {
        myFixture.configureByFile("schemas/eventlistener-github-interceptor.yaml");
        myFixture.checkHighlighting();
    }

    @Test
    public void testEventListenerWithGitLabInterceptor() {
        myFixture.configureByFile("schemas/eventlistener-gitlab-interceptor.yaml");
        myFixture.checkHighlighting();
    }

    @Test
    public void testEventListenerWithBitBucketInterceptor() {
        myFixture.configureByFile("schemas/eventlistener-bitbucket-interceptor.yaml");
        myFixture.checkHighlighting();
    }

    @Test
    public void testEventListenerWithCelInterceptor() {
        myFixture.configureByFile("schemas/eventlistener-cel-interceptor.yaml");
        myFixture.checkHighlighting();
    }
}
