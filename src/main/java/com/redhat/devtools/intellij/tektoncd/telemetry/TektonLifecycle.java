/*******************************************************************************
 * Copyright (c) 2021 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package com.redhat.devtools.intellij.tektoncd.telemetry;

import com.intellij.ide.AppLifecycleListener;
import com.intellij.ide.ApplicationInitializedListener;

public class TektonLifecycle implements AppLifecycleListener, ApplicationInitializedListener {

    @Override
    public void componentsInitialized() {
        TelemetryService.instance().startupPerformed().send();
    }


    @Override
    public void appWillBeClosed(boolean isRestart) {
        TelemetryService.instance().shutdownPerformed().send();
    }

}
