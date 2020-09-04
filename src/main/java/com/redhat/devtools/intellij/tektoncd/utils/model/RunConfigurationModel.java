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

import com.fasterxml.jackson.databind.JsonNode;
import com.redhat.devtools.intellij.common.utils.YAMLHelper;
import com.redhat.devtools.intellij.tektoncd.tkn.component.field.Input;
import com.redhat.devtools.intellij.tektoncd.tkn.component.field.Output;
import com.redhat.devtools.intellij.tektoncd.tkn.component.field.Workspace;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RunConfigurationModel extends ConfigurationModel {
    private String serviceAccountName;
    private Map<String, String> parameters, inputResources, outputResources, taskServiceAccountNames;
    private Map<String, Workspace> workspaces;

    public RunConfigurationModel(String configuration) {
        super(configuration);
        this.parameters = findParamsValues(configuration);
        this.taskServiceAccountNames = findServiceAccountNames(configuration);

        this.inputResources = findResourcesValues(configuration);
        this.workspaces = getWorkspaces(configuration);
        this.serviceAccountName = findServiceAccountName(configuration);
    }

    private Map<String, String> findParamsValues(String configuration) {
        Map<String, String> parameters = new HashMap<>();

        try {
            JsonNode paramsNode = YAMLHelper.getValueFromYAML(configuration, new String[]{"spec", "params"});
            if (paramsNode != null) {
                for (JsonNode item : paramsNode) {
                    if (!item.has("name") || !item.has("value")) continue;
                    parameters.put(item.get("name").asText(), item.get("value").asText());
                }
            }
        } catch (IOException e) {
            logger.warn(e.getLocalizedMessage());
        }

        return parameters;

    }

    protected Map<String, String> findServiceAccountNames(String configuration) {
        Map<String, String> taskServiceAccounts = new HashMap<>();

        try {
            JsonNode serviceAccountsNode = YAMLHelper.getValueFromYAML(configuration, new String[] {"spec", "serviceAccountNames"});
            if (serviceAccountsNode != null) {
                for(JsonNode item : serviceAccountsNode) {
                    if (!item.has("serviceAccountName") || !item.has("taskName"))
                        continue;
                    taskServiceAccounts.put(item.get("taskName").asText(), item.get("serviceAccountName").asText());
                }
            }
        } catch (IOException e) {
            logger.warn(e.getLocalizedMessage());
        }

        return taskServiceAccounts;
    }

    protected String findServiceAccountName(String configuration) {
        try {
            return YAMLHelper.getStringValueFromYAML(configuration, new String[] {"spec", "serviceAccountName"});
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    protected Map<String, String> findResourcesValues(String configuration) {
        Map<String, String> resources = new HashMap<>();

        try {
            JsonNode paramsNode = YAMLHelper.getValueFromYAML(configuration, new String[]{"spec", "resources"});
            if (paramsNode != null) {
                for (JsonNode item : paramsNode) {
                    if (!item.has("name") || !item.has("resourceRef") ) continue;
                    resources.put(item.get("name").asText(), item.get("value").asText());
                }
            }
        } catch (IOException e) {
            logger.warn(e.getLocalizedMessage());
        }

        return resources;
    }

    private Map<String, Workspace> getWorkspaces(String configuration) {
        Map<String, Workspace> workspaces = new HashMap<>();
        try {
            JsonNode workspacesNode = YAMLHelper.getValueFromYAML(configuration, new String[] {"spec", "workspaces"});
            if (workspacesNode != null) {
                for(JsonNode item : workspacesNode) {
                    if (!item.has("name")) continue;
                    String name = item.get("name").asText();
                    Workspace.Kind kind = item.has("persistentVolumeClaim") ? Workspace.Kind.PVC :
                                          item.has("configMap") ? Workspace.Kind.CONFIGMAP :
                                          item.has("secret") ? Workspace.Kind.SECRET :
                                          Workspace.Kind.EMPTYDIR;
                    String resource = kind.equals(Workspace.Kind.PVC) ? item.get("persistentVolumeClaim").get("claimName").asText() :
                                      kind.equals(Workspace.Kind.CONFIGMAP) ? item.get("configMap").get("name").asText() :
                                      kind.equals(Workspace.Kind.SECRET) ? item.get("secret").get("secretName").asText() :
                                        null;
                    workspaces.put(name, new Workspace(name, kind, resource));
                }
            }
        } catch (IOException e) {
            logger.warn(e.getLocalizedMessage());
        }
        return workspaces;
    }

    public Map<String, String> getParameters() {
        return this.parameters;
    }

    public Map<String, String> getResources() {
        return this.inputResources;
    }

    public Map<String, Workspace> getWorkspacesValues() {
        return this.workspaces;
    }

    @Override
    public List<Input> getParams() {
        return Collections.emptyList();
    }

    @Override
    public List<Input> getInputResources() {
        return Collections.emptyList();
    }

    @Override
    public List<Output> getOutputResources() {
        return Collections.emptyList();
    }

    @Override
    public List<String> getWorkspaces() {
        return Collections.emptyList();
    }
}
