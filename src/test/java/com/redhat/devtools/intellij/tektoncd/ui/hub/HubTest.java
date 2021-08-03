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

import com.redhat.devtools.intellij.tektoncd.hub.model.ResourceData;
import com.redhat.devtools.intellij.tektoncd.hub.model.ResourceVersionData;
import org.junit.Before;


import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public abstract class HubTest {
    protected ResourceData resourceData;
    protected ResourceVersionData resourceVersionData;
    @Before
    public void setUp() {
        resourceData = mock(ResourceData.class);
        resourceVersionData = mock(ResourceVersionData.class);
        when(resourceData.getName()).thenReturn("resource");
        when(resourceData.getKind()).thenReturn("kind");
        when(resourceData.getLatestVersion()).thenReturn(resourceVersionData);
        when(resourceVersionData.getVersion()).thenReturn("v1");
    }
}
