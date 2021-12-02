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

import com.intellij.openapi.ui.TestDialog;
import com.intellij.openapi.ui.TestDialogManager;
import com.redhat.devtools.intellij.tektoncd.BaseTest;
import io.fabric8.kubernetes.client.Watch;
import org.apache.commons.lang.time.StopWatch;
import org.junit.After;
import org.junit.Before;

import java.util.function.Supplier;

public class TknCliTest extends BaseTest {

    private TestDialog previousTestDialog;
    protected Tkn tkn;
    protected static final String NAMESPACE = "testns";

    @Before
    public void init() throws Exception {
        previousTestDialog = TestDialogManager.setTestDialog(new TestDialog() {
            @Override
            public int show(String message) {
                return 0;
            }
        });
        tkn = TknCliFactory.getInstance().getTkn(project).get();
    }

    @After
    public void shutdown() {
        TestDialogManager.setTestDialog(previousTestDialog);
    }

    protected static void stopAndWaitOnConditionOrTimeout(Watch watch, Supplier<Boolean> condition) throws InterruptedException {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        while (condition.get()) {
            Thread.sleep(200);
            if ( stopWatch.getTime() > 10000 ) {
                break;
            }
        }
        stopWatch.stop();
        watch.close();
    }
}
