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
package com.redhat.devtools.intellij.tektoncd.tkn;

import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.TestDialog;
import com.redhat.devtools.intellij.tektoncd.BaseTest;
import org.junit.After;
import org.junit.Before;

public class TknCliTest extends BaseTest {

    private TestDialog previousTestDialog;
    protected Tkn tkn;
    protected static final String NAMESPACE = "testns";

    @Before
    public void init() throws Exception {
        previousTestDialog = Messages.setTestDialog(new TestDialog() {
            @Override
            public int show(String message) {
                return 0;
            }
        });
        tkn = TknCliFactory.getInstance().getTkn(project).get();
    }

    @After
    public void shutdown() {
        Messages.setTestDialog(previousTestDialog);
    }


}
