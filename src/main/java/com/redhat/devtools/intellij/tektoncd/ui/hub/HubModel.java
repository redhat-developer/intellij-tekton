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

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.intellij.openapi.project.Project;
import com.redhat.devtools.alizer.api.Language;
import com.redhat.devtools.alizer.api.LanguageRecognizerBuilder;
import com.redhat.devtools.intellij.common.utils.ExecHelper;
import com.redhat.devtools.intellij.common.utils.YAMLHelper;
import com.redhat.devtools.intellij.tektoncd.Constants;
import com.redhat.devtools.intellij.tektoncd.hub.api.ResourceApi;
import com.redhat.devtools.intellij.tektoncd.hub.invoker.ApiCallback;
import com.redhat.devtools.intellij.tektoncd.hub.invoker.ApiException;
import com.redhat.devtools.intellij.tektoncd.hub.model.ResourceVersionData;
import com.redhat.devtools.intellij.tektoncd.hub.model.Resources;
import com.redhat.devtools.intellij.tektoncd.tkn.Tkn;
import com.redhat.devtools.intellij.tektoncd.utils.DeployHelper;
import com.redhat.devtools.intellij.tektoncd.utils.YAMLBuilder;
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


import static com.redhat.devtools.intellij.tektoncd.Constants.HUB_CATALOG_TAG;
import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_CLUSTERTASK;
import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_TASK;

public class HubModel {
    private Logger logger = LoggerFactory.getLogger(HubModel.class);

    private Tkn tkn;
    private List<HubItem> allHubItems;
    private Map<String, String> resourcesYaml;
    private Project project;
    private String selected;
    private List<String> tasksInstalled, clusterTasksInstalled;
    private boolean isTaskView;

    public HubModel(Project project, Tkn tkn, List<String> tasks, List<String> clusterTasks, boolean isTaskView) {
        this.allHubItems = new ArrayList<>();
        this.resourcesYaml = new HashMap<>();
        this.tkn = tkn;
        this.tasksInstalled = tasks;
        this.clusterTasksInstalled = clusterTasks;
        this.project = project;
        this.isTaskView = isTaskView;
    }

    public Future<List<HubItem>> retrieveAllHubItems() {
        CompletableFuture<List<HubItem>> completableFuture = new CompletableFuture<>();
        ExecHelper.submit(() -> {
            ResourceApi resApi = new ResourceApi();
            try {
                Resources resources = resApi.resourceList(500);
                completableFuture.complete(resources.getData().stream().map(resource -> new HubItem(resource)).collect(Collectors.toList()));
            } catch (ApiException e) {
                logger.warn(e.getLocalizedMessage(), e);
            }
        });
        return completableFuture;
    }

    public List<HubItem> getAllHubItems() {
        if (allHubItems.isEmpty()) {
            try {
                List<Language> languages = new LanguageRecognizerBuilder().build().analyze(project.getBasePath());
                allHubItems = retrieveAllHubItems().get().stream().sorted(new HubItemScore(languages).reversed())
                        .collect(Collectors.toList());;
            } catch (InterruptedException | ExecutionException | IOException e) {
                logger.warn(e.getLocalizedMessage(), e);
            }
        }

        return allHubItems;
    }

    public List<HubItem> getRecommendedHubItems() {
        if (allHubItems.isEmpty()) {
            try {
                List<Language> languages = new LanguageRecognizerBuilder().build().analyze(project.getBasePath());
                allHubItems = retrieveAllHubItems().get().stream().filter(item -> new HubItemScore(languages).compare(item, 0) > 0)
                        .collect(Collectors.toList());;
            } catch (InterruptedException | ExecutionException | IOException e) {
                logger.warn(e.getLocalizedMessage(), e);
            }
        }

        return allHubItems;
    }

    public List<HubItem> getInstalledHubItems() {
        if (allHubItems.isEmpty()) {
            try {
                List<String> tasks = tkn.getTasks("").stream()
                        .map(task -> task.getMetadata().getName()).collect(Collectors.toList());
                allHubItems = retrieveAllHubItems().get().stream().filter(item -> tasks.contains(item.getResource().getName()))
                        .collect(Collectors.toList());
            } catch (InterruptedException | ExecutionException | IOException e) {
                logger.warn(e.getLocalizedMessage(), e);
            }
        }

        return allHubItems;
    }

    public void search(String query, List<String> kinds, List<String> tags, ApiCallback<Resources> callback) {
        ResourceApi resApi = new ResourceApi();
        try {
            resApi.resourceQueryAsync(query, null, kinds, tags, null, null, callback);
        } catch (ApiException e) {
            logger.warn(e.getLocalizedMessage(), e);
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
     * @param kind kind to be used to save the hub item
     * @param version version of hub item to download
     * @return status of install
     * @throws IOException
     */
    public Constants.InstallStatus installHubItem(String name, String kind, String version, String catalog) throws IOException {
        String confirmationMessage;
        boolean isTask = kind.equalsIgnoreCase(KIND_TASK);
        boolean isClusterTask = kind.equalsIgnoreCase(KIND_CLUSTERTASK);
        boolean clusterTaskAlreadyOnCluster = isClusterTask && clusterTasksInstalled.contains(name);
        boolean taskAlreadyOnCluster = isTask && tasksInstalled.contains(name);
        if (taskAlreadyOnCluster || clusterTaskAlreadyOnCluster) {
            confirmationMessage = "A " + kind + " with name " + name + " already exists on the cluster. By installing this " + kind + " the one on the cluster will be overwritten. Do you want to install it?";
        } else {
            confirmationMessage = "Do you want to install the " + kind + " " + name + " on the cluster?";
        }

        if (isTask && DeployHelper.saveTaskOnClusterFromHub(project, name, version, taskAlreadyOnCluster, confirmationMessage)) {
            if (!taskAlreadyOnCluster) {
                tasksInstalled.add(name);
                return Constants.InstallStatus.INSTALLED;
            }
            return Constants.InstallStatus.OVERWRITTEN;
        } else if (isClusterTask) {
            String yamlFromHub = tkn.getTaskYAMLFromHub(name, version);
            ObjectNode yamlObject = YAMLBuilder.convertToObjectNode(yamlFromHub);
            if (yamlObject.has("metadata") &&
                yamlObject.get("metadata").has("labels") &&
                !yamlObject.get("metadata").get("labels").has(HUB_CATALOG_TAG)) {
                ((ObjectNode)yamlObject.get("metadata").get("labels")).put(HUB_CATALOG_TAG, catalog);
            }
            yamlObject.put("kind", kind);
            String yamlUpdated = YAMLHelper.JSONToYAML(yamlObject);
            if (DeployHelper.saveOnCluster(project, "", yamlUpdated, confirmationMessage, true, false)) {
                if (!clusterTaskAlreadyOnCluster) {
                    clusterTasksInstalled.add(name);
                    return Constants.InstallStatus.INSTALLED;
                }
                return Constants.InstallStatus.OVERWRITTEN;
            }
        }

        return Constants.InstallStatus.ERROR;
    }

    public List<String> getTasksInstalled() {
        return tasksInstalled;
    }

    public List<String> getClusterTasksInstalled() {
        return clusterTasksInstalled;
    }

    public Project getProject() {
        return project;
    }

    public boolean getIsTaskView() { return isTaskView; }

}
