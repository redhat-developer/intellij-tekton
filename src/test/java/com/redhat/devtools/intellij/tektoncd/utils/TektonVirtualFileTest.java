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
package com.redhat.devtools.intellij.tektoncd.utils;

import org.jetbrains.yaml.YAMLFileType;
import org.junit.Test;
import org.mockito.MockedStatic;


import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mockStatic;

public class TektonVirtualFileTest {

    @Test
    public void checkTektonVirtualFileCorrectlyCreated() {
        MockedStatic<TektonVirtualFileManager> exec  = mockStatic(TektonVirtualFileManager.class);
        exec.when(() -> TektonVirtualFileManager.getInstance()).thenReturn(new TektonVirtualFileManager());

        TektonVirtualFile file = new TektonVirtualFile("namespace/kind/resource", "");
        assertTrue(file.getName().equals("namespace/kind/resource"));
        assertTrue(file.getPresentableName().equals("namespace-resource.yaml"));
        assertTrue(file.getFileType().equals(YAMLFileType.YML));
        assertTrue(file.getFileSystem() instanceof TektonVirtualFileManager);
    }
}
