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

import com.redhat.devtools.intellij.tektoncd.tkn.ConditionCheckRun;

public class ConditionCheckRunNode extends RunNode {
    public ConditionCheckRunNode(TektonRootNode root, ParentableNode<Object> parent, ConditionCheckRun run) {
        super(root, parent, run);
    }

    public String getDisplayName() {
        ConditionCheckRun run = (ConditionCheckRun)getRun();
        return "condition: " + run.getName();
    }
}
