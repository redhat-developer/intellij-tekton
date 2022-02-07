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
package com.redhat.devtools.intellij.tektoncd.utils.model.resources;

import com.redhat.devtools.intellij.tektoncd.tkn.component.field.Input;
import com.redhat.devtools.intellij.tektoncd.tkn.component.field.Output;
import com.redhat.devtools.intellij.tektoncd.tkn.component.field.Workspace;
import com.redhat.devtools.intellij.tektoncd.utils.model.ResourceConfigurationModel;
import java.util.Collections;
import java.util.List;

public class PipelineConfigurationModel extends ResourceConfigurationModel {
    private List<Input> params;
    private List<Input> inputResource;
    private List<Workspace> workspaces;

    public PipelineConfigurationModel(String configuration) {
        super(configuration);
        this.params = findParams(configuration);
        this.inputResource = findInputResources(configuration, new String[] {"spec", "resources"});
        this.workspaces = findWorkspaces(configuration);
    }

    @Override
    public List<Input> getParams() {
        return this.params;
    }

    @Override
    public List<Input> getInputResources() {
        return this.inputResource;
    }

    @Override
    public List<Output> getOutputResources() {
        return Collections.emptyList();
    }

    @Override
    public List<Workspace> getWorkspaces() {
        return this.workspaces;
    }
}
