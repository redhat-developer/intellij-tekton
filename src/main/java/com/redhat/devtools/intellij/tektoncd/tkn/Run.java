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
import java.util.Optional;

public class Run {

    private String name;
    private Optional<Boolean> completed;
    private Instant startTime, completionTime;

    public Run(String name, Optional<Boolean> completed, Instant startTime, Instant completionTime) {
        this.name = name;
        this.completed = completed;
        this.startTime = startTime;
        this.completionTime = completionTime;
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

}
