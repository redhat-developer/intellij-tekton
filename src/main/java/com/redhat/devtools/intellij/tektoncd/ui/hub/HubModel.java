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
package com.redhat.devtools.intellij.tektoncd.ui.hub;

import com.intellij.openapi.project.Project;
import com.redhat.devtools.intellij.common.utils.ExecHelper;
import com.redhat.devtools.intellij.tektoncd.Constants;
import com.redhat.devtools.intellij.tektoncd.hub.api.ResourceApi;
import com.redhat.devtools.intellij.tektoncd.hub.invoker.ApiCallback;
import com.redhat.devtools.intellij.tektoncd.hub.invoker.ApiException;
import com.redhat.devtools.intellij.tektoncd.hub.model.ResourceVersionData;
import com.redhat.devtools.intellij.tektoncd.hub.model.Resources;
import com.redhat.devtools.intellij.tektoncd.utils.DeployHelper;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HubModel {
    private Logger logger = LoggerFactory.getLogger(HubModel.class);

    private List<HubItem> allHubItems;
    private Map<String, String> resourcesYaml;
    private Project project;
    private String selected, namespace;
    private List<String> tasksInstalled;

    public HubModel(Project project, String namespace, List<String> tasks) {
        this.allHubItems = new ArrayList<>();
        this.resourcesYaml = new HashMap<>();
        this.tasksInstalled = tasks;
        this.project = project;
        this.namespace = namespace;
    }

    public Future<List<HubItem>> retrieveAllHubItems() {
        CompletableFuture<List<HubItem>> completableFuture = new CompletableFuture<>();
        ExecHelper.submit(() -> {
            ResourceApi resApi = new ResourceApi();
            try {
                Resources resources = resApi.resourceList(500);
                completableFuture.complete(resources.getData().stream().map(resource -> new HubItem(resource)).collect(Collectors.toList()));
            } catch (ApiException e) {
                logger.warn(e.getLocalizedMessage());
            }
        });
        return completableFuture;
    }

    public List<HubItem> getAllHubItems() {
        if (allHubItems.isEmpty()) {
            try {
                allHubItems = retrieveAllHubItems().get();
            } catch (InterruptedException e) {
                logger.warn(e.getLocalizedMessage());
            } catch (ExecutionException e) {
                logger.warn(e.getLocalizedMessage());
            }
        }

        return allHubItems;
    }

    public void search(String query, List<String> kinds, List<String> tags, ApiCallback<Resources> callback) {
        ResourceApi resApi = new ResourceApi();
        try {
            resApi.resourceQueryAsync(query, kinds, tags, null, null, callback);
        } catch (ApiException e) {
            logger.warn(e.getLocalizedMessage());
        }
    }

    public String getSelectedHubItem() {
        return this.selected;
    }

    public void setSelectedHubItem(String itemSelected) {
        this.selected = itemSelected;
    }

    public List<ResourceVersionData> getVersionsById(int id) {
        List<ResourceVersionData> versions = new ArrayList<>();
        Optional<HubItem> itemSelected = allHubItems.stream().filter(item -> item.getResource().getId() == id).findFirst();
        if (itemSelected.isPresent()) {
            versions = itemSelected.get().getResource().getVersions();
        }
        if (versions.isEmpty()) {
            ResourceApi res = new ResourceApi();
            try {
                versions = res.resourceVersionsByID(id).getData().getVersions();
                if (itemSelected.isPresent()) {
                    itemSelected.get().getResource().setVersions(versions);
                }
            } catch (ApiException e) {
                e.printStackTrace();
            }
        }
        return versions;
    }

    public String getContentByURI(String uri) throws IOException {
        if (resourcesYaml.containsKey(uri)) {
            return resourcesYaml.get(uri);
        }
        String content = "";
        URL rawURL = new URL(uri);
        try (InputStreamReader in = new InputStreamReader(rawURL.openStream())) {
            try (BufferedReader reader = new BufferedReader(in)) {
                String[] lines = reader.lines().toArray(String[]::new);
                content = String.join("\n", lines);
                if (!content.isEmpty()) {
                    resourcesYaml.put(uri, content);
                }
            }
        }
        return content;
    }

    /**
     * install a hub item in the cluster
     * @param name name of the hub item to save
     * @param kind kind of the hub item to save
     * @param uri uri where to download the hub item
     * @return status of install
     * @throws IOException
     */
    public Constants.InstallStatus installHubItem(String name, String kind, String uri) throws IOException {
        String yaml = getContentByURI(uri);
        if (yaml.isEmpty()) {
            return Constants.InstallStatus.ERROR;
        }
        String confirmationMessage;
        boolean alreadyOnCluster = tasksInstalled.contains(name);
        if (alreadyOnCluster) {
            confirmationMessage = "A " + kind + " with name " + name + " already exists on the cluster. By installing this " + kind + " the one on the cluster will be overwritten. Do you want to install it?";
        } else {
            confirmationMessage = "Do you want to install the " + kind + " " + name + " on the cluster?";
        }
        if (DeployHelper.saveOnCluster(project, namespace, yaml, confirmationMessage)) {
            if (!alreadyOnCluster) {
                tasksInstalled.add(name);
                return Constants.InstallStatus.INSTALLED;
            }
            return Constants.InstallStatus.OVERWRITTEN;
        }
        return Constants.InstallStatus.ERROR;
    }

    public List<String> getTasksInstalled() {
        return tasksInstalled;
    }
}
