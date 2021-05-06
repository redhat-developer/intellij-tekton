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
package com.redhat.devtools.intellij.tektoncd.tkn.component.field;

import java.util.HashMap;
import java.util.Map;
import org.junit.Test;


import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_CONFIGMAP;
import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_EMPTYDIR;
import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_PVC;
import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_SECRET;
import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_VCT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class WorkspaceTest {

    @Test
    public void Constructor_NoItems_Workspace() {
        Workspace workspace = new Workspace("name", Workspace.Kind.PVC, "resource");
        assertEquals("name", workspace.getName());
        assertEquals(Workspace.Kind.PVC, workspace.getKind());
        assertEquals("resource", workspace.getResource());
        assertTrue(workspace.getItems().isEmpty());
    }

    @Test
    public void Constructor_WithItems_Workspace() {
        Map<String, String> items = new HashMap<>();
        items.put("test", "value");
        Workspace workspace = new Workspace("name", Workspace.Kind.VCT, "resource", items);
        assertEquals("name", workspace.getName());
        assertEquals(Workspace.Kind.VCT, workspace.getKind());
        assertEquals("resource", workspace.getResource());
        assertFalse(workspace.getItems().isEmpty());
        assertTrue(workspace.getItems().containsKey("test"));
        assertEquals("value", workspace.getItems().get("test"));
    }

    @Test
    public void Constructor_WorkspaceWithPVC_PVCKind() {
        Workspace workspace = new Workspace("name", Workspace.Kind.PVC, "resource");
        assertEquals(Workspace.Kind.PVC, workspace.getKind());
        assertEquals(KIND_PVC, workspace.getKind().toString());
    }

    @Test
    public void Constructor_WorkspaceWithSECRET_SECRETKind() {
        Workspace workspace = new Workspace("name", Workspace.Kind.SECRET, "resource");
        assertEquals(Workspace.Kind.SECRET, workspace.getKind());
        assertEquals(KIND_SECRET, workspace.getKind().toString());
    }

    @Test
    public void Constructor_WorkspaceWithCONFIGMAP_CONFIGMAPKind() {
        Workspace workspace = new Workspace("name", Workspace.Kind.CONFIGMAP, "resource");
        assertEquals(Workspace.Kind.CONFIGMAP, workspace.getKind());
        assertEquals(KIND_CONFIGMAP, workspace.getKind().toString());
    }

    @Test
    public void Constructor_WorkspaceWithEMPTYDIR_EMPTYDIRKind() {
        Workspace workspace = new Workspace("name", Workspace.Kind.EMPTYDIR, "resource");
        assertEquals(Workspace.Kind.EMPTYDIR, workspace.getKind());
        assertEquals(KIND_EMPTYDIR, workspace.getKind().toString());
    }

    @Test
    public void Constructor_WorkspaceWithVCT_VCTKind() {
        Workspace workspace = new Workspace("name", Workspace.Kind.VCT, "resource");
        assertEquals(Workspace.Kind.VCT, workspace.getKind());
        assertEquals(KIND_VCT, workspace.getKind().toString());
    }
}
