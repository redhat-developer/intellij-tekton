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
package com.redhat.devtools.intellij.tektoncd.actions.logs;

import com.intellij.openapi.ui.Messages;
import com.redhat.devtools.intellij.common.utils.UIHelper;
import com.redhat.devtools.intellij.tektoncd.tkn.Tkn;
import com.redhat.devtools.intellij.tektoncd.tree.PipelineNode;
import com.redhat.devtools.intellij.tektoncd.tree.RunNode;
import com.redhat.devtools.intellij.tektoncd.tree.TaskNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_PIPELINERUN;
import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_TASKRUN;

public class FollowLogsAction extends LogsBaseAction {
    Logger logger = LoggerFactory.getLogger(FollowLogsAction.class);

    public void actionPerformed(String namespace, String resourceName, String kind, Class nodeClass,  Tkn tkncli) {
        try {
            if (PipelineNode.class.equals(nodeClass) || (RunNode.class.equals(nodeClass) && kind.equalsIgnoreCase(KIND_PIPELINERUN))) {
                tkncli.followLogsPipelineRun(namespace, resourceName);
            } else if (TaskNode.class.equals(nodeClass) || (RunNode.class.equals(nodeClass) && kind.equalsIgnoreCase(KIND_TASKRUN))) {
                tkncli.followLogsTaskRun(namespace, resourceName);
            }
        } catch (IOException e) {
            UIHelper.executeInUI(() ->
                    Messages.showErrorDialog(
                            "An error occurred while requesting logs for " + resourceName + "\n" + e.getLocalizedMessage(),
                            "Error"));
            logger.warn("Error: " + e.getLocalizedMessage(), e);
        }
    }
}
