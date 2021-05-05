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
package com.redhat.devtools.intellij.tektoncd.kubernetes;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.psi.PsiFile;
import com.redhat.devtools.intellij.tektoncd.FixtureBaseTest;
import java.io.IOException;
import org.jetbrains.yaml.YAMLFileType;
import org.junit.Before;
import org.junit.Test;


import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TektonHighlightInfoFilterTest extends FixtureBaseTest {
    private static final String RESOURCE_PATH = "kubernetes/";
    private TektonHighlightInfoFilter tektonHighlightInfoFilter;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        tektonHighlightInfoFilter = new TektonHighlightInfoFilter();
    }

    @Test
    public void IsCustomFile_FileIsNotTekton_False() throws IOException {
        String yaml = load(RESOURCE_PATH + "not_tekton.yaml");
        PsiFile psiFile = myFixture.configureByText(YAMLFileType.YML, yaml);
        ApplicationManager.getApplication().runReadAction(() -> {
            boolean result = tektonHighlightInfoFilter.isCustomFile(psiFile);
            assertFalse(result);
        });
    }

    @Test
    public void IsCustomFile_FileIsPipelineTekton_True() throws IOException {
        String yaml = load(RESOURCE_PATH + "pipeline.yaml");
        PsiFile psiFile = myFixture.configureByText(YAMLFileType.YML, yaml);
        ApplicationManager.getApplication().runReadAction(() -> {
            boolean result = tektonHighlightInfoFilter.isCustomFile(psiFile);
            assertTrue(result);
        });
    }

    @Test
    public void IsCustomFile_FileIsTriggerTekton_True() throws IOException {
        String yaml = load(RESOURCE_PATH + "eventlistener.yaml");
        PsiFile psiFile = myFixture.configureByText(YAMLFileType.YML, yaml);
        ApplicationManager.getApplication().runReadAction(() -> {
            boolean result = tektonHighlightInfoFilter.isCustomFile(psiFile);
            assertTrue(result);
        });
    }
}
