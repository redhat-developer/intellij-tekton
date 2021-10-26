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
package com.redhat.devtools.intellij.tektoncd.utils;

import com.intellij.ui.treeStructure.Tree;
import com.redhat.devtools.intellij.tektoncd.Constants;
import com.redhat.devtools.intellij.tektoncd.tree.ParentableNode;
import com.redhat.devtools.intellij.tektoncd.tree.TektonTreeStructure;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class RefreshQueue {
    private static RefreshQueue instance;
    private Queue<ParentableNode> queue;
    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
    private ScheduledFuture scheduler;

    public RefreshQueue() {
        queue = new ConcurrentLinkedQueue<>();
    }

    public void addAll(List<ParentableNode> nodes) {
        if (scheduler != null && !scheduler.isCancelled() && !scheduler.isDone()) {
            scheduler.cancel(true);
        }

        for (ParentableNode node: nodes) {
            if (!queue.stream().anyMatch(element -> element.getName().equalsIgnoreCase(node.getName()) &&
                    ((ParentableNode)element.getParent()).getName().equalsIgnoreCase(((ParentableNode)node.getParent()).getName()))){
                queue.add(node);
            }
        }

        scheduler = executor.schedule(() -> {
            update();
        }, 500, TimeUnit.MILLISECONDS);
    }

    public void update() {
        while (!queue.isEmpty()) {
            ParentableNode element = queue.poll();
            Tree tree = TreeHelper.getTree(element.getRoot().getProject());
            TektonTreeStructure treeStructure = (TektonTreeStructure) tree.getClientProperty(Constants.STRUCTURE_PROPERTY);
            treeStructure.fireModified(element);
        }
    }
}
