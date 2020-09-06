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
package com.redhat.devtools.intellij.tektoncd.utils.model.runs;

import com.redhat.devtools.intellij.tektoncd.utils.model.RunConfigurationModel;
import java.util.Map;

public class PipelineRunConfigurationModel extends RunConfigurationModel {
    private Map<String, String> resources;

    public PipelineRunConfigurationModel(String configuration) {
        super(configuration);
        this.resources = findResources(configuration, new String[]{"spec", "resources"});
    }

    public Map<String, String> getResources() {
        return this.resources;
    }
}
