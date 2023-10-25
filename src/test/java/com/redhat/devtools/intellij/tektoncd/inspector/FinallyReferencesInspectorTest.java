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

import java.util.List;

public class FinallyReferencesInspectorTest extends InspectorTest {

    @Override
    public String getTestInspectorFolder() {
        return "finallyReferencesInspector";
    }

    @Override
    public void enableInspections() {
        myFixture.enableInspections(FinallyReferencesInspector.class);
    }

    public void testPipelineWithUnvalidRunAfterSection() {
        myFixture.configureByFile("pipeline1.yaml");
        List<HighlightInfo> hightlightInfo =  myFixture.doHighlighting();
        assertEquals(hightlightInfo.size(), 1);
        assertEquals("No runAfter can be specified in final tasks.", hightlightInfo.get(0).getDescription());
    }
}
