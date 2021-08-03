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
package com.redhat.devtools.intellij.tektoncd.ui.hub;

import org.junit.Test;


import static org.junit.Assert.assertEquals;

public class HubItemTest extends HubTest{

    @Test
    public void Constructor_SingleParam_HubItem() {
        HubItem hubItem = new HubItem(resourceData);
        assertEquals("v1", hubItem.getVersion());
        assertEquals("kind", hubItem.getKind());
        assertEquals("resource", hubItem.getResource().getName());
    }

    @Test
    public void Constructor_MultipleParams_HubItem() {
        HubItem hubItem = new HubItem(resourceData, "kind2", "v2");
        assertEquals("v2", hubItem.getVersion());
        assertEquals("kind2", hubItem.getKind());
        assertEquals("resource", hubItem.getResource().getName());
    }
}
