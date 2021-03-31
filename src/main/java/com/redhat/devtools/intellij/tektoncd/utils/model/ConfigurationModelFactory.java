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
package com.redhat.devtools.intellij.tektoncd.utils.model;

import com.google.common.base.Strings;
import com.redhat.devtools.intellij.common.utils.YAMLHelper;
import com.redhat.devtools.intellij.tektoncd.utils.model.runs.PipelineRunConfigurationModel;
import com.redhat.devtools.intellij.tektoncd.utils.model.runs.TaskRunConfigurationModel;
import com.redhat.devtools.intellij.tektoncd.utils.model.resources.ConditionConfigurationModel;
import com.redhat.devtools.intellij.tektoncd.utils.model.resources.PipelineConfigurationModel;
import com.redhat.devtools.intellij.tektoncd.utils.model.resources.TaskConfigurationModel;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigurationModelFactory {
    private static Logger logger = LoggerFactory.getLogger(ConfigurationModelFactory.class);

    public static ConfigurationModel getModel(String configuration) {
        try {
            String kind = YAMLHelper.getStringValueFromYAML(configuration, new String[] {"kind"});
            if (!Strings.isNullOrEmpty(kind)) {
                switch (kind.toLowerCase()) {
                    case "pipeline": return new PipelineConfigurationModel(configuration);
                    case "task":
                    case "clustertask": return new TaskConfigurationModel(configuration);
                    case "condition": return new ConditionConfigurationModel(configuration);
                    case "pipelinerun": return new PipelineRunConfigurationModel(configuration);
                    case "taskrun": return new TaskRunConfigurationModel(configuration);
                    default: break;
                }
            }
        } catch (IOException e) {
            logger.warn(e.getLocalizedMessage(), e);
        }

        return null;
    }
}
