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
package com.redhat.devtools.intellij.tektoncd.schemas;

import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import com.redhat.devtools.intellij.common.utils.VfsRootAccessHelper;
import org.jetbrains.yaml.schema.YamlJsonSchemaHighlightingInspection;
import org.junit.Before;

import java.io.File;

public abstract class SchemasTest extends BasePlatformTestCase {

    @Before
    public void setup() throws Exception {
        super.setUp();
        myFixture.enableInspections(YamlJsonSchemaHighlightingInspection.class);
        VfsRootAccessHelper.allowRootAccess(new File("src").getAbsoluteFile().getParentFile().getAbsolutePath());
    }

    @Override
    protected String getTestDataPath() {
        return "src/test/resources";
    }
}
