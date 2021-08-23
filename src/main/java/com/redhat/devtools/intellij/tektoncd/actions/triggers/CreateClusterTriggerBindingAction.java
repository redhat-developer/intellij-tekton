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

import com.redhat.devtools.intellij.tektoncd.tree.ClusterTriggerBindingsNode;


import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_CLUSTERTRIGGERBINDING;
import static com.redhat.devtools.intellij.tektoncd.telemetry.TelemetryService.NAME_PREFIX_CRUD;

public class CreateClusterTriggerBindingAction extends CreateTriggerAction {

    public CreateClusterTriggerBindingAction() { super(ClusterTriggerBindingsNode.class); }

    @Override
    public String getKind() {
        return KIND_CLUSTERTRIGGERBINDING;
    }

    @Override
    public String getActionName() {
        return NAME_PREFIX_CRUD + "create cluster trigger binding";
    }

    @Override
    public String getNewFilename() {
        return "-newclustertriggerbinding.yaml";
    }

    @Override
    public String getSnippetName() {
        return "Tekton: ClusterTriggerBinding";
    }

    @Override
    public String getErrorMessage() {
        return "Could not create cluster trigger binding: ";
    }
}
