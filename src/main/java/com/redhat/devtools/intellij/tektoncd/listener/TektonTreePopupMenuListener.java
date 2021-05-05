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
package com.redhat.devtools.intellij.tektoncd.listener;

import com.redhat.devtools.intellij.common.listener.TreePopupMenuListener;
import com.redhat.devtools.intellij.tektoncd.utils.RefreshQueue;
import javax.swing.event.PopupMenuEvent;

public class TektonTreePopupMenuListener extends TreePopupMenuListener {
    private static final RefreshQueue queue = RefreshQueue.get();

    public TektonTreePopupMenuListener() {
        super();
    }

    @Override
    public void popupMenuWillBecomeInvisible(PopupMenuEvent popupMenuEvent) {
        super.popupMenuWillBecomeInvisible(popupMenuEvent);
        queue.update();
    }

    @Override
    public void popupMenuCanceled(PopupMenuEvent popupMenuEvent) {
        super.popupMenuCanceled(popupMenuEvent);
        queue.update();
    }
}
