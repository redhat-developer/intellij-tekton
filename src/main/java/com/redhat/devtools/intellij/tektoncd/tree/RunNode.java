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
import io.fabric8.kubernetes.api.model.HasMetadata;
import java.time.Instant;
import java.util.Optional;

public abstract class RunNode<T, R extends HasMetadata> extends ParentableNode<T> {
    private final R run;
    public RunNode(TektonRootNode root, T parent, R run) {
        super(root, parent, run.getMetadata().getName());
        this.run = run;
    }

    public R getRun() {
        return run;
    }

    public String getInfoText() {
        String text = getTimeInfoText();
        if (text.isEmpty()) {
            text = getFailedReason();
        }
        return text;
    }

    private String getTimeInfoText() {
        String text = "";
        Instant startTime = getStartTime();
        if (startTime == null) {
            return text;
        }
        Optional<Boolean> isCompleted = isCompleted();
        if (!isCompleted.isPresent()) {
            text = "running " + DateHelper.humanizeDate(startTime);
            return text;
        }

        Instant completionTime = getCompletionTime();
        if (isCompleted.get()) {
            text = "started " + DateHelper.humanizeDate(startTime) + " ago, finished in " + DateHelper.humanizeDate(startTime, completionTime);
        } else {
            text = "started " + DateHelper.humanizeDate(startTime) + " ago";
            if (completionTime != null) {
                text += ", finished in " + DateHelper.humanizeDate(startTime, completionTime);
            }
        }
        return text;
    }

    public abstract String getFailedReason();

    public abstract Instant getStartTime();

    public abstract Optional<Boolean> isCompleted();

    public abstract Instant getCompletionTime();

}
