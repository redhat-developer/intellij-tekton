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
package com.redhat.devtools.intellij.tektoncd.actions.setting;

public class ShowConfigDefaultsAction extends ShowConfigurationAction {

    public ShowConfigDefaultsAction() { super(); }

    @Override
    public String getNamespace() {
        return "tekton-pipelines";
    }

    @Override
    public String getName() {
        return "config-defaults";
    }
}
