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
package com.redhat.devtools.intellij.tektoncd.completion;

import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import com.redhat.devtools.intellij.common.utils.VfsRootAccessHelper;
import org.junit.Before;

import java.io.File;
import java.util.List;

public abstract class BaseCompletionProviderTest extends BasePlatformTestCase {

    public void setup() throws Exception {
        VfsRootAccessHelper.allowRootAccess(new File("src").getAbsoluteFile().getParentFile().getAbsolutePath());
    }

    protected List<String> getSuggestionsForFile(String fileName) {
        myFixture.configureByFile(fileName);
        myFixture.complete(CompletionType.BASIC);
        return myFixture.getLookupElementStrings();
    }

}
