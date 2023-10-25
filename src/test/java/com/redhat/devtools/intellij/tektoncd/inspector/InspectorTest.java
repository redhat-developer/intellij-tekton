/*******************************************************************************
 *  Copyright (c) 2021 Red Hat, Inc.
 *  Distributed under license by Red Hat, Inc. All rights reserved.
 *  This program is made available under the terms of the
 *  Eclipse Public License v2.0 which accompanies this distribution,
 *  and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 *  Contributors:
 *  Red Hat, Inc.
 ******************************************************************************/
package com.redhat.devtools.intellij.tektoncd.inspector;

import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.newvfs.impl.VfsRootAccess;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;

import java.io.File;

public abstract class InspectorTest extends BasePlatformTestCase {

    public void setUp() throws Exception {
        super.setUp();
        enableInspections();
        VfsRootAccess.allowRootAccess(Disposer.newDisposable(), new File("src").getAbsoluteFile().getParentFile().getAbsolutePath());
    }

    @Override
    protected String getTestDataPath() {
        return "src/test/resources/inspector/" + getTestInspectorFolder() + "/";
    }

    public abstract String getTestInspectorFolder();
    public abstract void enableInspections();

}
