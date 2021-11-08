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
import io.fabric8.knative.internal.pkg.apis.Condition;
import io.fabric8.kubernetes.api.model.HasMetadata;
import java.time.Instant;
import java.util.List;
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

    protected String getFailedReason(List<Condition> conditionsList) {
        if (conditionsList.size() > 0) {
            return conditionsList.get(0).getReason();
        }
        return "";
    }

    protected static Instant getStartTime(String startTimeText) {
        Instant startTime = null;
        try {
            if (startTimeText != null && !startTimeText.isEmpty()) {
                startTime = Instant.parse(startTimeText);
            }
        } catch (NullPointerException ignored) { }
        return startTime;
    }

    protected Optional<Boolean> isCompleted(List<Condition> conditionsList) {
        Optional<Boolean> completed = Optional.empty();
        try {
            if (conditionsList.size() > 0) {
                if (conditionsList.get(0).getStatus().equalsIgnoreCase("True")) {
                    completed = Optional.of(true);
                } else if (conditionsList.get(0).getStatus().equalsIgnoreCase("False")) {
                    completed = Optional.of(false);
                }
            }
        } catch (Exception e) {}
        return completed;
    }

    protected Instant getCompletionTime(String completionTimeText) {
        Instant completionTime = null;
        try {
            if (completionTimeText != null && !completionTimeText.isEmpty()) completionTime = Instant.parse(completionTimeText);
        } catch (NullPointerException ne) { }
        return completionTime;
    }

    public abstract String getFailedReason();

    public abstract Instant getStartTime();

    public abstract Optional<Boolean> isCompleted();

    public abstract Instant getCompletionTime();

}
