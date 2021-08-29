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

import com.redhat.devtools.intellij.tektoncd.tree.EventListenersNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_EVENTLISTENER;
import static com.redhat.devtools.intellij.tektoncd.telemetry.TelemetryService.NAME_PREFIX_CRUD;

public class CreateEventListenerAction extends CreateTriggerAction {

    private static final Logger logger = LoggerFactory.getLogger(CreateEventListenerAction.class);

    public CreateEventListenerAction() { super(EventListenersNode.class); }

    @Override
    public String getKind() {
        return KIND_EVENTLISTENER;
    }

    @Override
    public String getActionName() {
        return NAME_PREFIX_CRUD + "create event listener";
    }

    @Override
    public String getNewFilename() {
        return "-neweventlistener.yaml";
    }

    @Override
    public String getSnippetName() {
        return "Tekton: EventListener";
    }

    @Override
    public String getErrorMessage() {
        return "Could not create event listener: ";
    }
}
