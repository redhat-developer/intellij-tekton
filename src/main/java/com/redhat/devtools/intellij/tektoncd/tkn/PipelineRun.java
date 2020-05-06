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

public class PipelineRun extends Run {

    private List<TaskRun> tasksRun;

    public PipelineRun(String name, Optional<Boolean> completed, Instant startTime, Instant completionTime, List<TaskRun> tasksRun) {
        super(name, completed, startTime, completionTime);
        this.tasksRun = tasksRun;
    }

    public List<TaskRun> getTaskRuns() { return tasksRun; }

    @Override
    public String getFailedReason() { return ""; }
}
