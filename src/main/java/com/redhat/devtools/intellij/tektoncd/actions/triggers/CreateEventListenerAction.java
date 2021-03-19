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
package com.redhat.devtools.intellij.tektoncd.actions.triggers;

import com.google.common.base.Strings;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.redhat.devtools.intellij.tektoncd.actions.TektonAction;
import com.redhat.devtools.intellij.tektoncd.telemetry.TelemetryService;
import com.redhat.devtools.intellij.tektoncd.tkn.Tkn;
import com.redhat.devtools.intellij.tektoncd.tree.EventListenersNode;
import com.redhat.devtools.intellij.tektoncd.utils.VirtualFileHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.tree.TreePath;

import java.io.IOException;

import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_EVENTLISTENER;
import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_EVENTLISTENERS;
import static com.redhat.devtools.intellij.tektoncd.telemetry.TelemetryService.PROP_RESOURCE_KIND;
import static com.redhat.devtools.intellij.telemetry.core.service.TelemetryMessageBuilder.ActionMessage;
import static com.redhat.devtools.intellij.telemetry.core.util.AnonymizeUtils.anonymizeResource;

public class CreateEventListenerAction extends TektonAction {

    private static final Logger logger = LoggerFactory.getLogger(CreateEventListenerAction.class);

    public CreateEventListenerAction() { super(EventListenersNode.class); }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent, TreePath path, Object selected, Tkn tkncli) {
        ActionMessage telemetry = TelemetryService.instance()
                .action("create event listener")
                .property(PROP_RESOURCE_KIND, KIND_EVENTLISTENER);
        EventListenersNode item = getElement(selected);
        String namespace = item.getParent().getName();
        String content = getSnippet("Tekton: EventListener");

        if (Strings.isNullOrEmpty(content)) {
            telemetry
                    .error("snippet content empty")
                    .send();
        } else {
            String name = namespace + "-neweventlistener.yaml";
            try {
                VirtualFileHelper.createAndOpenVirtualFile(anActionEvent.getProject(), namespace, name, content, KIND_EVENTLISTENERS, item);
                telemetry.send();
            } catch (IOException e) {
                telemetry
                        .error(anonymizeResource(name, namespace, e.getMessage()))
                        .send();
                logger.warn("Could not create event listener: " + e.getLocalizedMessage());
            }
        }
    }
}
