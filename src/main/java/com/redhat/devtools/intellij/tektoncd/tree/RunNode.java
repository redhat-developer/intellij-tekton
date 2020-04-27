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

import com.redhat.devtools.intellij.common.utils.DateHelper;
import com.redhat.devtools.intellij.tektoncd.tkn.Run;

public class RunNode<T, R extends Run> extends ParentableNode<T> {
    private final R run;
    private final int level;
    public RunNode(TektonRootNode root, T parent, R run, int level) {
        super(root, parent, run.getName());
        this.run = run;
        this.level = level;
    }

    public R getRun() {
        return run;
    }

    public int getLevel() { return this.level; }

    public String getTimeInfoText() {
        String text = "";
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
}
