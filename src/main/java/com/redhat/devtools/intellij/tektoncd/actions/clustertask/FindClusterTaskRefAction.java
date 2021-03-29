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
package com.redhat.devtools.intellij.tektoncd.actions.clustertask;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.redhat.devtools.intellij.common.utils.ExecHelper;
import com.redhat.devtools.intellij.common.utils.UIHelper;
import com.redhat.devtools.intellij.tektoncd.actions.TektonAction;
import com.redhat.devtools.intellij.tektoncd.tkn.Tkn;
import com.redhat.devtools.intellij.tektoncd.tree.ClusterTaskNode;
import com.redhat.devtools.intellij.tektoncd.tree.ParentableNode;
import com.redhat.devtools.intellij.tektoncd.ui.toolwindow.findusage.FindTaskRefPanelBuilder;
import com.redhat.devtools.intellij.tektoncd.ui.toolwindow.findusage.RefUsage;
import java.io.IOException;
import java.util.List;
import javax.swing.tree.TreePath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_CLUSTERTASK;

public class FindClusterTaskRefAction extends TektonAction {
    private static final Logger logger = LoggerFactory.getLogger(FindClusterTaskRefAction.class);

    public FindClusterTaskRefAction() {
        super(ClusterTaskNode.class);
    }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent, TreePath path, Object selected, Tkn tkncli) {
        ParentableNode element = getElement(selected);
        ExecHelper.submit(() -> {
            try {
                List<RefUsage> usages = tkncli.findTaskUsages(KIND_CLUSTERTASK, element.getName());
                UIHelper.executeInUI(() -> FindTaskRefPanelBuilder.instance().build(anActionEvent.getProject(), KIND_CLUSTERTASK, element.getName(), usages));
            } catch (IOException e) {
                logger.warn(e.getLocalizedMessage());
            }
        });
    }
}
