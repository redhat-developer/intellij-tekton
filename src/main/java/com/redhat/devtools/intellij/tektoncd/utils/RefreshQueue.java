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
package com.redhat.devtools.intellij.tektoncd.utils;

import com.intellij.ui.treeStructure.Tree;
import com.redhat.devtools.intellij.tektoncd.Constants;
import com.redhat.devtools.intellij.tektoncd.listener.TreePopupMenuListener;
import com.redhat.devtools.intellij.tektoncd.tree.ParentableNode;
import com.redhat.devtools.intellij.tektoncd.tree.TektonTreeStructure;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;

public class RefreshQueue {
    private static RefreshQueue instance;
    private Queue<ParentableNode> queue;
    private Timer timer;

    private RefreshQueue() {
        queue = new LinkedList<>();
    }

    public static RefreshQueue get() {
        if (instance == null) {
            instance = new RefreshQueue();
        }
        return instance;
    }

    public void addAll(List<ParentableNode> nodes) {
        if (timer != null) {
            timer.cancel();
        }

        for (ParentableNode node: nodes) {
            if (!queue.stream().anyMatch(element -> element.getName().equals(node.getName()) && element.getParent().equals(node.getParent()))) {
                queue.add(node);
            }
        }

        timer = new Timer();
        timer.schedule(
                new TimerTask() {
                    @Override
                    public void run() {
                        if (!TreePopupMenuListener.isTreeMenuVisible()) {
                            refresh();
                        }
                    }
                },
                500
        );
    }

    public void refresh() {
        while (!queue.isEmpty()) {
            ParentableNode element = queue.poll();
            if (element == null) return;
            Tree tree = TreeHelper.getTree(element.getRoot().getProject());
            TektonTreeStructure treeStructure = (TektonTreeStructure)tree.getClientProperty(Constants.STRUCTURE_PROPERTY);
            treeStructure.fireModified(element);
        }
    }
}
