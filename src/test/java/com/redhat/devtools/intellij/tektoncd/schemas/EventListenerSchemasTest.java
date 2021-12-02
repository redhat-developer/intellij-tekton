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

import org.junit.Test;

public class EventListenerSchemasTest extends SchemasTest {
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
