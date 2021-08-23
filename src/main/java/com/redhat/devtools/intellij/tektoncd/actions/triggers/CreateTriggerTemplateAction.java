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

import com.redhat.devtools.intellij.tektoncd.tree.TriggerTemplatesNode;


import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_TRIGGERTEMPLATE;
import static com.redhat.devtools.intellij.tektoncd.telemetry.TelemetryService.NAME_PREFIX_CRUD;

public class CreateTriggerTemplateAction extends CreateTriggerAction {

    public CreateTriggerTemplateAction() {
        super(TriggerTemplatesNode.class);
    }

    @Override
    public String getKind() {
        return KIND_TRIGGERTEMPLATE;
    }

    @Override
    public String getActionName() {
        return NAME_PREFIX_CRUD + "create trigger template";
    }

    @Override
    public String getNewFilename() {
        return "-newtriggertemplate.yaml";
    }

    @Override
    public String getSnippetName() {
        return "Tekton: TriggerTemplate";
    }

    @Override
    public String getErrorMessage() {
        return "Could not create trigger template: ";
    }
}
