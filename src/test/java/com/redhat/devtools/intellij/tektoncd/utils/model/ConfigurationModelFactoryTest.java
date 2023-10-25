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

import com.redhat.devtools.intellij.tektoncd.BaseTest;
import com.redhat.devtools.intellij.tektoncd.utils.model.resources.PipelineConfigurationModel;
import com.redhat.devtools.intellij.tektoncd.utils.model.resources.TaskConfigurationModel;

import java.io.IOException;

public class ConfigurationModelFactoryTest extends BaseTest {

    public void testCheckTaskModelIsReturned() throws IOException {
        String configuration = load("task1.yaml");
        ConfigurationModel model = ConfigurationModelFactory.getModel(configuration);
        assertTrue(model instanceof TaskConfigurationModel);
    }

    public void testCheckPipelineModelIsReturned() throws IOException {
        String configuration = load("pipeline1.yaml");
        ConfigurationModel model = ConfigurationModelFactory.getModel(configuration);
        assertTrue(model instanceof PipelineConfigurationModel);
    }

    public void testCheckNoModelIsReturned() throws IOException {
        String configuration = load("invalidfile.yaml");
        ConfigurationModel model = ConfigurationModelFactory.getModel(configuration);
        assertNull(model);
    }
}
