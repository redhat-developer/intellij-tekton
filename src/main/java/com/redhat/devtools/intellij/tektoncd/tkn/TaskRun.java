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

public class TaskRun extends Run {

    private String triggeredBy;
    private String stepName;
    private List<TaskRun> conditionChecks;
    private String failedReason;

    public TaskRun(String name, String triggeredBy, String stepName, Optional<Boolean> completed, Instant startTime, Instant completionTime, List<TaskRun> conditionChecks, String failedReason) {
        super(name, completed, startTime, completionTime);
        this.triggeredBy = triggeredBy;
        this.stepName = stepName;
        this.conditionChecks = conditionChecks;
        this.failedReason = failedReason;
    }

    public String getTriggeredBy() { return triggeredBy; }

    public String getStepName() { return stepName; }

    public List<TaskRun> getConditionChecks() { return conditionChecks; }

    public String getFailedReason() { return failedReason; }
}
