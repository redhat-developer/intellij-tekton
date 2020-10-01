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

import com.google.common.base.Strings;
import com.redhat.devtools.intellij.tektoncd.utils.model.ConfigurationModel;

public class TriggerBindingConfigurationModel extends ConfigurationModel {
    public TriggerBindingConfigurationModel(String configuration) {
        super(configuration);
    }

    public String getErrorMessage() {
        String errorMessage = "<html>";
        if (Strings.isNullOrEmpty(kind)) {
            errorMessage += " * Kind field is missing or its value is not valid.<br>";
        }
        if (Strings.isNullOrEmpty(name)) {
            errorMessage += " * Name field is missing or its value is not valid.";
        }

        errorMessage = errorMessage.equals("<html>") ? "" : errorMessage + "</html>";

        return errorMessage;
    }
}
