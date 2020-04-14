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

import com.redhat.devtools.intellij.tektoncd.tkn.PipelineRun;

public class PipelineRunNode extends RunNode {
    public PipelineRunNode(PipelineRun run, int level) {
        super(run, level);
    }

    @Override
    public String toString() {
        return "<html>" +
                getName() +
                " <span style=\"font-size:90%;color:gray;\">" +
                getTimeInfoText() +
                "</span></html>";
    }

    @Override
    public void load() {
        super.load();
        PipelineRun run = (PipelineRun)getUserObject();
        run.getTaskRuns().forEach(taskRun -> add(new TaskRunNode(taskRun, 5)));

    }
}
