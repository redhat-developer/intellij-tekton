/*******************************************************************************
 * Copyright (c) 2020 Red Hat, Inc.
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
import com.redhat.devtools.intellij.tektoncd.tkn.PipelineRun;

public class PipelineRunNode extends LazyMutableTreeNode implements IconTreeNode {
    public PipelineRunNode(PipelineRun pipelineRun) {
        super(pipelineRun);
    }

    @Override
    public String toString() {
        PipelineRun pRun = (PipelineRun) getUserObject();
        return "<html>" +
                pRun.getName() +
                " <span style=\"font-size:90%;color:gray;\">" +
                pRun.getStartTimeText() + pRun.getCompletionTimeText() +
                "</span></html>";
    }

    @Override
    public String getIconName() {
        PipelineRun run = (PipelineRun) getUserObject();
        return run.isCompleted().isPresent()?run.isCompleted().get()?"/images/success.png":"/images/failed.png":"/images/running.png";
    }
}
