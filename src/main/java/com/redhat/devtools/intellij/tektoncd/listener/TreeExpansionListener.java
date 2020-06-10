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

import com.redhat.devtools.intellij.common.actions.StructureTreeAction;
import com.redhat.devtools.intellij.tektoncd.tree.ParentableNode;
import com.redhat.devtools.intellij.tektoncd.utils.WatchHandler;

import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;

public class TreeExpansionListener implements TreeWillExpandListener {
    @Override
    public void treeWillExpand(TreeExpansionEvent treeExpansionEvent) {
        ParentableNode<? extends ParentableNode<?>> expandingElement = StructureTreeAction.getElement(treeExpansionEvent.getPath().getLastPathComponent());
        if (WatchHandler.get().canBeWatched(expandingElement)) {
            WatchHandler.get().setWatch(expandingElement);
        }
    }

    @Override
    public void treeWillCollapse(TreeExpansionEvent treeExpansionEvent) {
        ParentableNode<? extends ParentableNode<?>> collapsingElement = StructureTreeAction.getElement(treeExpansionEvent.getPath().getLastPathComponent());
        if (WatchHandler.get().canBeWatched(collapsingElement)) {
            WatchHandler.get().removeWatch(collapsingElement);
        }
    }
}
