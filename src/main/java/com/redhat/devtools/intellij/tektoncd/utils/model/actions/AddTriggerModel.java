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

import com.redhat.devtools.intellij.tektoncd.tkn.Resource;
import com.redhat.devtools.intellij.tektoncd.utils.model.resources.TriggerBindingConfigurationModel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AddTriggerModel extends ActionToRunModel {

    private Map<String, String> bindingsSelectedByUser;
    private String newBindingAdded;
    private Map<String, String> bindingsAvailableOnCluster;

    public AddTriggerModel(String configuration, List<Resource> resources, List<String> serviceAccounts, List<String> secrets, List<String> configMaps, List<String> persistentVolumeClaims, Map<String, String> bindingsAvailableOnCluster) {
        super(configuration, resources, serviceAccounts, secrets, configMaps, persistentVolumeClaims);
        this.bindingsAvailableOnCluster = bindingsAvailableOnCluster;
        this.bindingsSelectedByUser = new HashMap<>();
        this.newBindingAdded = "";
    }

    public Map<String, String> getBindingsAvailableOnCluster() {
        return this.bindingsAvailableOnCluster;
    }

    public String getNewBindingAdded() {
        return newBindingAdded;
    }

    public void setNewBindingAdded(String newBinding) {
        this.newBindingAdded = newBinding;
    }

    public Map<String, String> getBindingsSelectedByUser() {
        return bindingsSelectedByUser;
    }

    public Set<String> extractVariablesFromSelectedBindings() {
        Set<String> variablesInBindings = new HashSet<>();
        if (!this.newBindingAdded.isEmpty()) {
            TriggerBindingConfigurationModel tbModel = new TriggerBindingConfigurationModel(this.newBindingAdded);
            if (tbModel.isValid()) {
                variablesInBindings.addAll(tbModel.getParams().keySet());
            }
        }

        if (!this.bindingsSelectedByUser.isEmpty()) {
            this.bindingsSelectedByUser.keySet().forEach(binding -> {
                TriggerBindingConfigurationModel tbModel = new TriggerBindingConfigurationModel(this.bindingsSelectedByUser.get(binding));
                if (tbModel.isValid()) {
                    variablesInBindings.addAll(tbModel.getParams().keySet());
                }
            });
        }
        return variablesInBindings;
    }
}
