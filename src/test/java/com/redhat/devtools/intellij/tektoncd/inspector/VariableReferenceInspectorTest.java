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
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class VariableReferenceInspectorTest extends InspectorTest{

    @Override
    public String getTestInspectorFolder() {
        return "variableReferenceInspector";
    }

    @Override
    public void enableInspections() {
        myFixture.enableInspections(VariableReferencesInspector.class);
    }

    @Test
    public void testPipelineHighlightWithUnusedParameter() {
        myFixture.configureByFile("pipeline1.yaml");
        List<HighlightInfo> hightlightInfo =  myFixture.doHighlighting();
        assertEquals(hightlightInfo.size(), 1);
        assertTrue(hightlightInfo.get(0).getDescription().equals("Variable p1 is never used"));
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
        assertEquals(hightlightInfo.size(), 2);
        assertTrue(hightlightInfo.get(0).getDescription().equals("Variable p1 is never used"));
        assertTrue(hightlightInfo.get(1).getDescription().equals("Variable password-vault is never used"));
    }

    @Test
    public void testPipelineHighlightWithNotExactMatchingWords() {
        myFixture.configureByFile("pipeline6.yaml");
        List<HighlightInfo> hightlightInfo =  myFixture.doHighlighting();
        assertEquals(hightlightInfo.size(), 2);
        assertTrue(hightlightInfo.get(0).getDescription().equals("Variable p1 is never used"));
        assertTrue(hightlightInfo.get(1).getDescription().equals("Variable password-vault is never used"));
    }

    @Test
    public void testTaskHighlightWithUnusedParameter() {
        myFixture.configureByFile("task1.yaml");
        List<HighlightInfo> hightlightInfo =  myFixture.doHighlighting();
        assertEquals(hightlightInfo.size(), 1);
        assertTrue(hightlightInfo.get(0).getDescription().equals("Variable parm1 is never used"));
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
        assertEquals(hightlightInfo.size(), 2);
        assertTrue(hightlightInfo.get(0).getDescription().equals("Variable write-allowed is never used"));
        assertTrue(hightlightInfo.get(1).getDescription().equals("Variable parm1 is never used"));

    }

}
