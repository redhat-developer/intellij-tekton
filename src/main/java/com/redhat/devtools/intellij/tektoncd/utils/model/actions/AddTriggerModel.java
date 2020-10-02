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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddTriggerModel extends ActionToRunModel {

    private Map<String, String> bindingsSelectedByUser;
    private String newBindingAdded;
    private List<String> bindingsAvailableOnCluster;

    public AddTriggerModel(String configuration, List<Resource> resources, List<String> serviceAccounts, List<String> secrets, List<String> configMaps, List<String> persistentVolumeClaims, List<String> bindingsAvailableOnCluster) {
        super(configuration, resources, serviceAccounts, secrets, configMaps, persistentVolumeClaims);
        this.bindingsAvailableOnCluster = bindingsAvailableOnCluster;
        this.bindingsSelectedByUser = new HashMap<>();
        this.newBindingAdded = "";
    }

    public List<String> getBindingsAvailableOnCluster() {
        return this.bindingsAvailableOnCluster;
    }

    public String getNewBindingAdded() {
        return newBindingAdded;
    }

    public void setNewBindingAdded(String newBinding) {
        this.newBindingAdded = newBinding;
    }

    public void loadBindingsSelectedByUser() {
        if (bindingsSelectedByUser.isEmpty()) {
            return;
        }

        new Thread(() -> {
            bindingsSelectedByUser.keySet().forEach(binding -> {
                if (bindingsSelectedByUser.get(binding).isEmpty()) {
                    // TODO call virtualfilesystem to request binding without body

                }
            });
        }).start();
    }

    public Map<String, String> getBindingsSelectedByUser() {
        return bindingsSelectedByUser;
    }
}
