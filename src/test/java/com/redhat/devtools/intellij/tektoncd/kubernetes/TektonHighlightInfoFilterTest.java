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
import com.redhat.devtools.intellij.tektoncd.BaseTest;
import org.jetbrains.yaml.YAMLFileType;

import java.io.IOException;

public class TektonHighlightInfoFilterTest extends BaseTest {
    private static final String RESOURCE_PATH = "kubernetes/";
    private TektonHighlightInfoFilter tektonHighlightInfoFilter;

    public void setUp() throws Exception {
        super.setUp();
        tektonHighlightInfoFilter = new TektonHighlightInfoFilter();
    }

    public void testIsCustomFile_FileIsNotTekton_False() throws IOException {
        String yaml = load(RESOURCE_PATH + "not_tekton.yaml");
        PsiFile psiFile = myFixture.configureByText(YAMLFileType.YML, yaml);
        ApplicationManager.getApplication().runReadAction(() -> {
            boolean result = tektonHighlightInfoFilter.isCustomFile(psiFile);
            assertFalse(result);
        });
    }

    public void testIsCustomFile_FileIsPipelineTekton_True() throws IOException {
        String yaml = load(RESOURCE_PATH + "pipeline.yaml");
        PsiFile psiFile = myFixture.configureByText(YAMLFileType.YML, yaml);
        ApplicationManager.getApplication().runReadAction(() -> {
            boolean result = tektonHighlightInfoFilter.isCustomFile(psiFile);
            assertTrue(result);
        });
    }

    public void testIsCustomFile_FileIsTriggerTekton_True() throws IOException {
        String yaml = load(RESOURCE_PATH + "eventlistener.yaml");
        PsiFile psiFile = myFixture.configureByText(YAMLFileType.YML, yaml);
        ApplicationManager.getApplication().runReadAction(() -> {
            boolean result = tektonHighlightInfoFilter.isCustomFile(psiFile);
            assertTrue(result);
        });
    }
}
