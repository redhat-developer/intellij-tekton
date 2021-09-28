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

import com.fasterxml.jackson.databind.JsonNode;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.redhat.devtools.intellij.common.utils.ExecHelper;
import com.redhat.devtools.intellij.common.utils.YAMLHelper;
import com.redhat.devtools.intellij.tektoncd.actions.TektonAction;
import com.redhat.devtools.intellij.tektoncd.tkn.Tkn;
import com.redhat.devtools.intellij.tektoncd.tree.ParentableNode;
import com.redhat.devtools.intellij.tektoncd.tree.TaskRunNode;
import com.redhat.devtools.intellij.tektoncd.ui.toolwindow.debug.DebugPanelBuilder;
import com.redhat.devtools.intellij.tektoncd.utils.DebugHelper;
import java.io.IOException;
import javax.swing.tree.TreePath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConnectDebugTaskRunAction extends TektonAction {
    private static final Logger logger = LoggerFactory.getLogger(ConnectDebugTaskRunAction.class);

    public ConnectDebugTaskRunAction() { super(TaskRunNode.class); }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent, TreePath path, Object selected, Tkn tkncli) {
        ExecHelper.submit(() -> {
            ParentableNode<?> element = getElement(selected);
            try {
                String yaml = tkncli.getTaskRunYAML(element.getNamespace(), element.getName());
                JsonNode debugNode = YAMLHelper.getValueFromYAML(yaml, new String[] { "spec", "debug" });
                if (debugNode != null) {
                    DebugHelper.doDebugTaskRun(tkncli, element.getNamespace(), element.getName());
                }
            } catch (IOException e) {
                logger.warn(e.getLocalizedMessage(), e);
            }
        });

    }

    @Override
    public boolean isVisible(Object selected) {
        // action should be visibile if taskrun is not completed and debug panel for it is not opened yet
        // and the taskrun has been started in debug mode
        Object element = getElement(selected);
        if (element instanceof TaskRunNode) {
            Tkn tkn = ((ParentableNode)element).getRoot().getTkn();
            return (((TaskRunNode) element).isCompleted().isPresent()
                && !((TaskRunNode) element).isCompleted().get()
                && DebugPanelBuilder.instance(tkn).getResourceDebugPanel(((TaskRunNode) element).getName()) == null
                && ((TaskRunNode) element).isStartedOnDebug());
        }
        return false;
    }
}
