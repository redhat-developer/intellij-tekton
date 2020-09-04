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
import java.io.IOException;
import org.junit.Test;


import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class ConfigurationModelFactoryTest extends BaseTest {

    @Test
    public void checkTaskModelIsReturned() throws IOException {
        String configuration = load("task1.yaml");
        ConfigurationModel model = ConfigurationModelFactory.getModel(configuration);
        assertTrue(model instanceof TaskConfigurationModel);
    }

    @Test
    public void checkPipelineModelIsReturned() throws IOException {
        String configuration = load("pipeline1.yaml");
        ConfigurationModel model = ConfigurationModelFactory.getModel(configuration);
        assertTrue(model instanceof PipelineConfigurationModel);
    }

    @Test
    public void checkConditionModelIsReturned() throws IOException {
        String configuration = load("condition1.yaml");
        ConfigurationModel model = ConfigurationModelFactory.getModel(configuration);
        assertTrue(model instanceof ConditionConfigurationModel);
    }

    @Test
    public void checkNoModelIsReturned() throws IOException {
        String configuration = load("invalidfile.yaml");
        ConfigurationModel model = ConfigurationModelFactory.getModel(configuration);
        assertNull(model);
    }
}
