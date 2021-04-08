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
package com.redhat.devtools.intellij.tektoncd.actions.clustertask;

import com.google.common.base.Strings;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.redhat.devtools.intellij.tektoncd.actions.TektonAction;
import com.redhat.devtools.intellij.tektoncd.telemetry.TelemetryService;
import com.redhat.devtools.intellij.tektoncd.tkn.Tkn;
import com.redhat.devtools.intellij.tektoncd.tree.ClusterTasksNode;
import com.redhat.devtools.intellij.tektoncd.utils.VirtualFileHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.tree.TreePath;

import java.io.IOException;

import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_CLUSTERTASK;
import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_CLUSTERTASKS;
import static com.redhat.devtools.intellij.tektoncd.telemetry.TelemetryService.NAME_PREFIX_CRUD;
import static com.redhat.devtools.intellij.tektoncd.telemetry.TelemetryService.PROP_RESOURCE_KIND;
import static com.redhat.devtools.intellij.telemetry.core.service.TelemetryMessageBuilder.ActionMessage;
import static com.redhat.devtools.intellij.telemetry.core.util.AnonymizeUtils.anonymizeResource;

public class CreateClusterTaskAction extends TektonAction {

    private static final Logger logger = LoggerFactory.getLogger(CreateClusterTaskAction.class);

    public CreateClusterTaskAction() { super(ClusterTasksNode.class); }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent, TreePath path, Object selected, Tkn tkncli) {
        ActionMessage telemetry = TelemetryService.instance().action(NAME_PREFIX_CRUD + "create cluster task")
                .property(PROP_RESOURCE_KIND, KIND_CLUSTERTASK);
        ClusterTasksNode item = getElement(selected);
        String namespace = item.getParent().getName();
        String content = getSnippet("Tekton: ClusterTask");

        if (Strings.isNullOrEmpty(content)) {
            telemetry
                    .error("snippet content empty")
                    .send();
        } else {
            String name = "newclustertask.yaml";
            try {
                VirtualFileHelper.createAndOpenVirtualFile(anActionEvent.getProject(), namespace, name, content, KIND_CLUSTERTASKS, item);
                telemetry.success().send();
            } catch (IOException e) {
                String errorMessage = "Could not create cluster cluster task: " + e.getLocalizedMessage();
                telemetry
                        .error(anonymizeResource(name, namespace, errorMessage))
                        .send();
                logger.warn(errorMessage, e);
            }
        }
    }
}