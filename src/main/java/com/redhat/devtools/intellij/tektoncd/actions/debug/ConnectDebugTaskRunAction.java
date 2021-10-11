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

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.redhat.devtools.intellij.tektoncd.actions.TektonAction;
import com.redhat.devtools.intellij.tektoncd.tkn.Tkn;
import com.redhat.devtools.intellij.tektoncd.tree.ParentableNode;
import com.redhat.devtools.intellij.tektoncd.tree.TaskRunNode;
import com.redhat.devtools.intellij.tektoncd.ui.toolwindow.debug.DebugTabPanelFactory;
import com.redhat.devtools.intellij.tektoncd.utils.DebugHelper;
import javax.swing.tree.TreePath;

public class ConnectDebugTaskRunAction extends TektonAction {

    public ConnectDebugTaskRunAction() { super(TaskRunNode.class); }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent, TreePath path, Object selected, Tkn tkncli) {
        ParentableNode<?> element = getElement(selected);
        DebugHelper.doDebugTaskRun(getEventProject(anActionEvent), tkncli, element.getNamespace(), element.getName());
    }

    @Override
    public boolean isVisible(Object selected) {
        // action should be visibile if taskrun is running, it is started in debug mode
        // and debug panel for it is not opened yet
        Object element = getElement(selected);
        if (element instanceof TaskRunNode) {
            return (!((TaskRunNode) element).isCompleted().isPresent()
                && DebugTabPanelFactory.instance().getResourceDebugPanel(((TaskRunNode) element).getName()) == null
                && ((TaskRunNode) element).isStartedOnDebug());
        }
        return false;
    }
}
