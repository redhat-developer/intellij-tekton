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
package com.redhat.devtools.intellij.tektoncd.listener;

import com.redhat.devtools.intellij.tektoncd.utils.RefreshQueue;

import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

public class TreePopupMenuListener implements PopupMenuListener {
    private static RefreshQueue queue = RefreshQueue.get();
    private static boolean isMenuVisible = false;

    public static boolean isTreeMenuVisible() {
        return isMenuVisible;
    }

    @Override
    public void popupMenuWillBecomeVisible(PopupMenuEvent popupMenuEvent) {
        isMenuVisible = true;
    }

    @Override
    public void popupMenuWillBecomeInvisible(PopupMenuEvent popupMenuEvent) {
        isMenuVisible = false;
        queue.refresh();
    }

    @Override
    public void popupMenuCanceled(PopupMenuEvent popupMenuEvent) {
        isMenuVisible = false;
        queue.refresh();
    }
}
