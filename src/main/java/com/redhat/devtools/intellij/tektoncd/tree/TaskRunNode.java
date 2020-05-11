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

import com.redhat.devtools.intellij.common.utils.StringHelper;
import com.redhat.devtools.intellij.tektoncd.tkn.TaskRun;

public class TaskRunNode extends RunNode {
    public TaskRunNode(TektonRootNode root, ParentableNode parent, TaskRun run) {
        super(root, parent, run);
    }

    public String getDisplayName() {
        TaskRun run = (TaskRun)getRun();
        String displayName = "";
        String triggeredBy = run.getTriggeredBy();
        String stepName = run.getStepName();
        if (!triggeredBy.isEmpty()) {
            displayName += StringHelper.beautify(triggeredBy) + "/";
        }
        displayName +=  StringHelper.beautify(stepName);
        if (displayName.isEmpty()) {
            displayName = run.getName();
        }
        return displayName;
    }
}
