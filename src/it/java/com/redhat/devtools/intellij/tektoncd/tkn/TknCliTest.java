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
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import com.redhat.devtools.intellij.common.utils.MessagesHelper;
import io.fabric8.kubernetes.client.Watch;
import org.apache.commons.lang.time.StopWatch;

import java.util.function.Supplier;

public class TknCliTest extends BasePlatformTestCase {

    private Tkn tkn;
    public static final String NAMESPACE = "testns";
    private TestDialog previousTestDialog;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        previousTestDialog = MessagesHelper.setTestDialog(message -> 0);
        tkn = TknCliFactory.getInstance().getTkn(getProject()).get();
    }

    @Override
    public void tearDown() throws Exception {
        MessagesHelper.setTestDialog(previousTestDialog);
        super.tearDown();
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

    protected Tkn getTkn(){
        return tkn;
    }
}
