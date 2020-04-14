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
package com.redhat.devtools.intellij.tektoncd.tree;

import com.redhat.devtools.intellij.common.tree.IconTreeNode;
import com.redhat.devtools.intellij.common.tree.LazyMutableTreeNode;
import com.redhat.devtools.intellij.common.utils.DateHelper;
import com.redhat.devtools.intellij.tektoncd.tkn.Run;

public class RunNode extends LazyMutableTreeNode implements IconTreeNode {
    private int level;
    public RunNode(Run run, int level) {
        super(run);
        this.level = level;
    }

    public String getName() {
        return ((Run)getUserObject()).getName();
    }

    public int getLevel() { return this.level; }

    public String getTimeInfoText() {
        String text = "";
        Run run = (Run) getUserObject();
        if (!run.isCompleted().isPresent()) {
            text = "running " + DateHelper.humanizeDate(run.getStartTime());
            return text;
        }

        if (run.isCompleted().get()) {
            text = "started " + DateHelper.humanizeDate(run.getStartTime()) + " ago, finished in " + DateHelper.humanizeDate(run.getStartTime(), run.getCompletionTime());
        } else {
            text = "started " + DateHelper.humanizeDate(run.getStartTime()) + " ago";
            if (run.getCompletionTime() != null) {
                text += ", finished in " + DateHelper.humanizeDate(run.getStartTime(), run.getCompletionTime());
            }
        }
        return text;
    }

    @Override
    public String getIconName() {
        Run run = (Run) getUserObject();
        return run.isCompleted().isPresent()?run.isCompleted().get()?"/images/success.png":"/images/failed.png":"/images/running.png";
    }
}
