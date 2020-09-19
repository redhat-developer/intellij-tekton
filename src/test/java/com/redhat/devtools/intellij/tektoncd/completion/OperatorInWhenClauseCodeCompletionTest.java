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

import org.junit.Test;


import static com.intellij.testFramework.UsefulTestCase.assertOrderedEquals;

public class OperatorInWhenClauseCodeCompletionTest extends BaseCompletionProviderTest{

    @Test
    public void testCompletionOperatorInWhenClause() {
        assertOrderedEquals(getSuggestionsForFile("pipeline1.yaml"), "in", "notin");
    }

    @Override
    public String getTestDataPath() {
        return "src/test/resources/completion/operatorInWhenClause";
    }
}
