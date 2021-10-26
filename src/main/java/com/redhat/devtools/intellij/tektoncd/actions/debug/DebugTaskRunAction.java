/*******************************************************************************
 * Copyright (c) 2021 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc.
 ******************************************************************************/
package com.redhat.devtools.intellij.tektoncd.actions.debug;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.intellij.openapi.project.Project;
import com.redhat.devtools.intellij.common.utils.YAMLHelper;
import com.redhat.devtools.intellij.tektoncd.actions.MirrorStartAction;
import com.redhat.devtools.intellij.tektoncd.tkn.Tkn;
import com.redhat.devtools.intellij.tektoncd.tree.ParentableNode;
import com.redhat.devtools.intellij.tektoncd.tree.TaskRunNode;
import com.redhat.devtools.intellij.tektoncd.utils.DebugHelper;
import com.redhat.devtools.intellij.tektoncd.utils.DeployHelper;
import com.redhat.devtools.intellij.tektoncd.utils.YAMLBuilder;
import com.redhat.devtools.intellij.tektoncd.utils.model.actions.StartResourceModel;
import java.io.IOException;


import static com.redhat.devtools.intellij.tektoncd.utils.Utils.isActiveTektonVersionOlder;

public class DebugTaskRunAction extends MirrorStartAction {

    public DebugTaskRunAction() {
        super(TaskRunNode.class);
    }

    @Override
    protected boolean canBeStarted(Project project, ParentableNode element, StartResourceModel model) {
        return true;
    }

    @Override
    protected String doStart(Tkn tkncli, String namespace, StartResourceModel model) throws IOException {
        ObjectNode run = YAMLBuilder.createRun(model, true);
        if (run == null) {
            throw new IOException("Unable to debug task" + model.getName());
        }
        String runAsYAML = YAMLHelper.JSONToYAML(run, false);
        String runName = DeployHelper.saveResource(runAsYAML, namespace, tkncli);
        DebugHelper.doDebugTaskRun(tkncli, namespace, runName);
        return null;
    }

    @Override
    public boolean isVisible(Object selected) {
        Object element = getElement(selected);
        if (element instanceof TaskRunNode) {
            Tkn tkn = ((ParentableNode)element).getRoot().getTkn();
            return isActiveTektonVersionOlder(tkn.getTektonVersion(), "0.26.0") &&
                    tkn.isTektonAlphaFeatureEnabled();
        }
        return false;
    }
}
