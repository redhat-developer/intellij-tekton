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
package com.redhat.devtools.intellij.tektoncd.tkn;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public abstract class Run {

    private String name;
    private Optional<Boolean> completed;
    private Instant startTime, completionTime;
    private List<TaskRun> children;

    public Run(String name, Optional<Boolean> completed, Instant startTime, Instant completionTime, List<TaskRun> children) {
        this.name = name;
        this.completed = completed;
        this.startTime = startTime;
        this.completionTime = completionTime;
        this.children = children;
    }

    public String getName() {
        return name;
    }

    public Optional<Boolean> isCompleted() {
        return completed;
    }

    public Instant getStartTime() {
        return startTime;
    }

    public Instant getCompletionTime() {
        return completionTime;
    }

    public List<TaskRun> getChildren() { return children; }

    public abstract String getFailedReason();

}
