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

import com.redhat.devtools.intellij.tektoncd.tkn.TaskRun;

public class TaskRunNode extends RunNode {
    public TaskRunNode(TaskRun run, int level) {
        super(run, level);
    }

    @Override
    public String toString() {
        return "<html>" +
                getDisplayName() +
                " <span style=\"font-size:90%;color:gray;\">" +
                getTimeInfoText() +
                "</span></html>";
    }

    private String getDisplayName() {
        TaskRun run = (TaskRun)getUserObject();
        String displayName = "";
        String triggeredBy = run.getTriggeredBy();
        String stepName = run.getStepName();
        if (!triggeredBy.isEmpty()) {
            displayName += triggeredBy.length() > 16 ? triggeredBy.substring(0, 16) : triggeredBy;
            displayName += "/";
        }
        displayName += stepName.length() > 16 ? stepName.substring(0, 16) : stepName;
        return displayName;
    }
}
