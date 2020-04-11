/*******************************************************************************
 * Copyright (c) 2019 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package com.redhat.devtools.intellij.tektoncd.tree;

import com.redhat.devtools.intellij.common.tree.IconTreeNode;
import com.redhat.devtools.intellij.common.tree.LazyMutableTreeNode;
import com.redhat.devtools.intellij.tektoncd.tkn.TaskRun;

public class TaskRunNode extends LazyMutableTreeNode implements IconTreeNode {
    public TaskRunNode(TaskRun taskRun) {
        super(taskRun);
    }

    @Override
    public String toString() {
        TaskRun tRun = (TaskRun)getUserObject();
        return "<html>" +
                tRun.getName() +
                " <span style=\"font-size:90%;color:gray;\">" +
                tRun.getStartTimeText() + tRun.getCompletionTimeText() +
                "</span></html>";
    }

    @Override
    public String getIconName() {
        TaskRun run = (TaskRun) getUserObject();
        return run.isCompleted().isPresent()?run.isCompleted().get()?"/images/success.png":"/images/failed.png":"/images/running.png";
    }
}
