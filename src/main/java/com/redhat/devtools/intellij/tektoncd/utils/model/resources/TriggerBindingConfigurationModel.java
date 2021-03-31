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

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Strings;
import com.redhat.devtools.intellij.common.utils.YAMLHelper;
import com.redhat.devtools.intellij.tektoncd.utils.model.ConfigurationModel;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TriggerBindingConfigurationModel extends ConfigurationModel {
    private Logger logger = LoggerFactory.getLogger(TriggerBindingConfigurationModel.class);
    private Map<String, String> params;

    public TriggerBindingConfigurationModel(String configuration) {
        super(configuration);
        findParams(configuration);
    }

    public Map<String, String> getParams() {
        return this.params;
    }

    private void findParams(String configuration) {
        this.params = new HashMap<>();
        try {
            JsonNode paramsNode = YAMLHelper.getValueFromYAML(configuration, new String[]{"spec", "params"});
            if (paramsNode != null) {
                for (JsonNode item: paramsNode) {
                    this.params.put(item.get("name").asText(), item.get("value").asText());
                }
            }
        } catch (IOException e) {
            logger.warn(e.getLocalizedMessage(), e);
        }
    }

    public String getErrorMessage() {
        String errorMessage = "<html>";
        if (Strings.isNullOrEmpty(kind)) {
            errorMessage += " * Kind field is missing or its value is not valid.<br>";
        }
        if (Strings.isNullOrEmpty(name)) {
            errorMessage += " * Name field is missing or its value is not valid.<br>";
        }
        if (this.params.isEmpty()) {
            errorMessage += " * Params field is missing or its value is not valid.<br>";
        }

        errorMessage = errorMessage.equals("<html>") ? "" : errorMessage + "</html>";

        return errorMessage;
    }
}
