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
package com.redhat.devtools.intellij.tektoncd.actions.task;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.redhat.devtools.intellij.common.utils.ExecHelper;
import com.redhat.devtools.intellij.common.utils.UIHelper;
import com.redhat.devtools.intellij.tektoncd.actions.TektonAction;
import com.redhat.devtools.intellij.tektoncd.telemetry.TelemetryService;
import com.redhat.devtools.intellij.tektoncd.tkn.Tkn;
import com.redhat.devtools.intellij.tektoncd.tree.ParentableNode;
import com.redhat.devtools.intellij.tektoncd.tree.TaskNode;
import com.redhat.devtools.intellij.tektoncd.ui.toolwindow.findusage.FindTaskRefPanelBuilder;
import com.redhat.devtools.intellij.tektoncd.ui.toolwindow.findusage.RefUsage;
import com.redhat.devtools.intellij.telemetry.core.service.TelemetryMessageBuilder;
import java.io.IOException;
import java.util.List;
import javax.swing.tree.TreePath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_TASK;
import static com.redhat.devtools.intellij.tektoncd.telemetry.TelemetryService.NAME_PREFIX_MISC;
import static com.redhat.devtools.intellij.tektoncd.telemetry.TelemetryService.PROP_RESOURCE_KIND;
import static com.redhat.devtools.intellij.telemetry.core.util.AnonymizeUtils.anonymizeResource;

public class FindTaskRefAction extends TektonAction {
    private static final Logger logger = LoggerFactory.getLogger(FindTaskRefAction.class);
    public FindTaskRefAction() { super(TaskNode.class); }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent, TreePath path, Object selected, Tkn tkncli) {
        TelemetryMessageBuilder.ActionMessage telemetry = TelemetryService.instance()
                .action(NAME_PREFIX_MISC + "find task references")
                .property(PROP_RESOURCE_KIND, KIND_TASK);
        ParentableNode element = getElement(selected);
        ExecHelper.submit(() -> {
            try {
                List<RefUsage> usages = tkncli.findTaskUsages(KIND_TASK, element.getName());
                UIHelper.executeInUI(() -> FindTaskRefPanelBuilder.instance().build(anActionEvent.getProject(), KIND_TASK, element.getName(), usages));
                telemetry.send();
            } catch (IOException e) {
                telemetry
                        .error(anonymizeResource(null, element.getNamespace(), e.getMessage()))
                        .send();
                logger.warn(e.getLocalizedMessage());
            }
        });
    }
}
