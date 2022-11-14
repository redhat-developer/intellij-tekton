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
package com.redhat.devtools.intellij.tektoncd.utils.model.actions;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Strings;
import com.redhat.devtools.intellij.common.utils.YAMLHelper;
import com.redhat.devtools.intellij.tektoncd.tkn.component.field.Input;
import com.redhat.devtools.intellij.tektoncd.tkn.component.field.Workspace;
import com.redhat.devtools.intellij.tektoncd.utils.model.ConfigurationModel;
import com.redhat.devtools.intellij.tektoncd.utils.model.ConfigurationModelFactory;
import com.redhat.devtools.intellij.tektoncd.utils.model.ResourceConfigurationModel;
import com.redhat.devtools.intellij.tektoncd.utils.model.resources.PipelineConfigurationModel;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_CLUSTERTASK;

public abstract class ActionToRunModel extends ConfigurationModel {

    protected ResourceConfigurationModel resource;
    protected boolean isValid = true;
    protected String errorMessage;
    protected Map<String, Workspace> workspaces;
    protected Map<String, String> taskServiceAccountNames;
    protected String globalServiceAccount;
    protected List<String> serviceAccounts, secrets, configMaps, persistentVolumeClaims;

    public ActionToRunModel(String configuration, List<String> serviceAccounts, List<String> secrets, List<String> configMaps, List<String> persistentVolumeClaims) {
        super(configuration);
        this.errorMessage = "Tekton configuration has an invalid format:\n";
        this.serviceAccounts = serviceAccounts;
        this.secrets = secrets;
        this.configMaps = configMaps;
        this.persistentVolumeClaims = persistentVolumeClaims;
        init(configuration);
    }

    public void init(String configuration) {
        if (Strings.isNullOrEmpty(name)) {
            errorMessage += " * Name field is missing or its value is not valid.\n";
            isValid = false;
        }
        if (Strings.isNullOrEmpty(kind)) {
            errorMessage += " * Kind field is missing or its value is not valid.\n";
            isValid = false;
        }
        if (Strings.isNullOrEmpty(namespace) && !KIND_CLUSTERTASK.equalsIgnoreCase(kind)) {
            errorMessage += " * Namespace field is missing or its value is not valid.\n";
            isValid = false;
        }

        if (isValid) {
            resource = (ResourceConfigurationModel) ConfigurationModelFactory.getModel(configuration);
            if (resource == null) {
                errorMessage += "Unable to create a model for the following resource kind - " + kind;
                isValid = false;
                return;
            }

            this.globalServiceAccount = "";
            this.workspaces = new HashMap<>();
            resource.getWorkspaces().forEach(ws -> this.workspaces.put(ws.getName(), ws));
            this.taskServiceAccountNames = Collections.emptyMap();

            if (resource instanceof PipelineConfigurationModel) {
                initTaskServiceAccounts(configuration);
            }
        }
    }

    public boolean isValid() {
        return this.isValid;
    }

    public ResourceConfigurationModel getResource() {
        return resource;
    }

    public String getErrorMessage() {
        return this.errorMessage;
    }

    public List<String> getServiceAccounts() {
        return this.serviceAccounts;
    }

    public List<String> getSecrets() {
        return this.secrets;
    }

    public List<String> getConfigMaps() {
        return this.configMaps;
    }

    public List<String> getPersistentVolumeClaims() {
        return this.persistentVolumeClaims;
    }

    public List<Input> getParams() {
        return resource.getParams();
    }

    public Map<String, Workspace> getWorkspaces() {
        return this.workspaces;
    }

    public Map<String, String> getTaskServiceAccounts() { return this.taskServiceAccountNames; }

    public void setServiceAccount(String serviceAccountName) { this.globalServiceAccount = serviceAccountName; }

    public String getServiceAccount() { return this.globalServiceAccount; }

    private void initTaskServiceAccounts(String configuration) {
        this.taskServiceAccountNames = new LinkedHashMap<>();
        JsonNode tasksNode = null;
        try {
            tasksNode = YAMLHelper.getValueFromYAML(configuration, new String[] {"spec", "tasks"});
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (tasksNode != null) {
            for(JsonNode item : tasksNode) {
                if (item.has("name")) {
                    String task = item.get("name").asText();
                    this.taskServiceAccountNames.put(task, "");
                }
            }
        }
        // get tasks from finally section
        JsonNode finallyNode = null;
        try {
            finallyNode = YAMLHelper.getValueFromYAML(configuration, new String[] {"spec", "finally"});
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (finallyNode != null) {
            for(JsonNode item : finallyNode) {
                if (item.has("name")) {
                    String finalTask = item.get("name").asText();
                    this.taskServiceAccountNames.put(finalTask, "");
                }
            }
        }
    }

}
