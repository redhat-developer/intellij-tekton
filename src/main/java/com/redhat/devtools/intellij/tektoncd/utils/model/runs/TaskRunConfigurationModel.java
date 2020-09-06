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

public class TaskRunConfigurationModel extends RunConfigurationModel {
    private Map<String, String> inputResources, outputResources;

    public TaskRunConfigurationModel(String configuration) {
        super(configuration);
        this.inputResources = findResources(configuration, new String[]{"spec", "resources", "inputs"});
        this.outputResources = findResources(configuration, new String[]{"spec", "resources", "outputs"});
    }

    public Map<String, String> getInputResources() {
        return this.inputResources;
    }

    public Map<String, String> getOutputResources() {
        return this.outputResources;
    }
}
