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
import com.redhat.devtools.alizer.api.RecognizerFactory;
import com.redhat.devtools.intellij.common.utils.ExecHelper;
import com.redhat.devtools.intellij.common.utils.YAMLHelper;
import com.redhat.devtools.intellij.tektoncd.Constants;
import com.redhat.devtools.intellij.tektoncd.hub.api.ResourceApi;
import com.redhat.devtools.intellij.tektoncd.hub.invoker.ApiCallback;
import com.redhat.devtools.intellij.tektoncd.hub.invoker.ApiException;
import com.redhat.devtools.intellij.tektoncd.hub.model.ResourceData;
import com.redhat.devtools.intellij.tektoncd.hub.model.ResourceVersionData;
import com.redhat.devtools.intellij.tektoncd.hub.model.Resources;
import com.redhat.devtools.intellij.tektoncd.tkn.Tkn;
import com.redhat.devtools.intellij.tektoncd.tree.ClusterTasksNode;
import com.redhat.devtools.intellij.tektoncd.tree.ParentableNode;
import com.redhat.devtools.intellij.tektoncd.tree.PipelinesNode;
import com.redhat.devtools.intellij.tektoncd.utils.DeployHelper;
import com.redhat.devtools.intellij.tektoncd.utils.YAMLBuilder;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.WatcherException;
import io.fabric8.tekton.pipeline.v1beta1.ClusterTask;
import io.fabric8.tekton.pipeline.v1beta1.Pipeline;
import io.fabric8.tekton.pipeline.v1beta1.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;


import static com.redhat.devtools.intellij.tektoncd.Constants.APP_K8S_IO_VERSION;
import static com.redhat.devtools.intellij.tektoncd.Constants.HUB_CATALOG_TAG;
import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_CLUSTERTASK;
import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_PIPELINE;
import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_TASK;

public class HubModel {
    private Logger logger = LoggerFactory.getLogger(HubModel.class);

    private Tkn tkn;
    private List<HubItem> allHubItems;
    private Map<String, String> resourcesYaml;
    private Project project;
    private List<HasMetadata> tasksInstalled, clusterTasksInstalled, pipelinesInstalled;
    private HubPanelCallback hubPanelCallback;
    private ParentableNode caller;

    public HubModel(Project project, Tkn tkn, ParentableNode caller) {
        this.allHubItems = new ArrayList<>();
        this.resourcesYaml = new HashMap<>();
        this.tkn = tkn;
        this.tasksInstalled = Collections.synchronizedList(new ArrayList<>());
        this.clusterTasksInstalled = Collections.synchronizedList(new ArrayList<>());
        this.pipelinesInstalled = Collections.synchronizedList(new ArrayList<>());
        this.project = project;
        this.caller = caller;
        init();
    }

    private void init() {
        ExecHelper.submit(() -> {
            try {
                initWatch();
            } catch (IOException e) {
                logger.warn(e.getLocalizedMessage());
            }
        });
    }

    public void registerHubPanelCallback(HubPanelCallback hubPanelCallback) {
        this.hubPanelCallback = hubPanelCallback;
    }

    private List<ResourceData> retrieveAllResources() {
        try {
            ResourceApi resApi = new ResourceApi();
            Resources resources = resApi.resourceList(500);
            return resources.getData();
        } catch (ApiException e) {
            logger.warn(e.getLocalizedMessage(), e);
        }
        return Collections.emptyList();
    }

    public List<HubItem> getAllHubItems() {
        if (allHubItems.isEmpty()) {
            allHubItems = getHubItems(resource -> true);
        }

        return allHubItems;
    }

    private List<HubItem> getHubItems(Predicate<ResourceData> filter) {
        try {
            List<Language> languages = new RecognizerFactory().createLanguageRecognizer().analyze(project.getBasePath());
            return retrieveAllResources().stream()
                    .filter(filter)
                    .map(HubItem::new)
                    .sorted(new HubItemScore(languages).reversed())
                    .collect(Collectors.toList());
        } catch (IOException e) {
            logger.warn(e.getLocalizedMessage(), e);
        }
        return Collections.emptyList();
    }

    private List<HubItem> getAllHubItemsPerKind(String kind) {
        return getHubItems(resourceData -> resourceData.getKind().equalsIgnoreCase(kind));
    }

    public List<HubItem> getAllTaskHubItems() {
        return getAllHubItemsPerKind(KIND_TASK);
    }

    public List<HubItem> getAllPipelineHubItems() {
        return getAllHubItemsPerKind(KIND_PIPELINE);
    }

    public List<HubItem> getRecommendedHubItems() {
        try {
            List<Language> languages = new RecognizerFactory().createLanguageRecognizer().analyze(project.getBasePath());
            List<String> hubItemsAlreadyInstalled = getAllInstalledHubItems().stream()
                    .map(task -> task.getMetadata().getName())
                    .collect(Collectors.toList());
            return retrieveAllResources().stream()
                    .map(HubItem::new)
                    .filter(item -> new HubItemScore(languages).compare(item, 0) > 0 && !hubItemsAlreadyInstalled.contains(item.getResource().getName()))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            logger.warn(e.getLocalizedMessage(), e);
        }

        return Collections.emptyList();
    }

    public List<HubItem> getInstalledHubItems() {
        List<HubItem> items = new ArrayList<>();
        List<HasMetadata> hubItemsAlreadyInstalled = getAllInstalledHubItems();
        List<ResourceData> resourceDataList = retrieveAllResources();

        for (HasMetadata task: hubItemsAlreadyInstalled) {
            Optional<HubItem> hubItem = resourceDataList.stream()
                    .filter(resourceData -> resourceData.getName().equalsIgnoreCase(task.getMetadata().getName()))
                    .map(resourceData -> {
                        String version = task.getMetadata().getLabels() != null ? task.getMetadata().getLabels().get(APP_K8S_IO_VERSION) : "";
                        if (!version.isEmpty()) {
                            return new HubItem(resourceData, task.getKind(), version);
                        } else {
                            return new HubItem(resourceData);
                        }
                    }).findFirst();
            hubItem.ifPresent(items::add);
        }
        return items;
    }

    private List<HasMetadata> getAllInstalledHubItems() {
        List<HasMetadata> items = new ArrayList<>();
        items.addAll(tasksInstalled);
        items.addAll(clusterTasksInstalled);
        items.addAll(pipelinesInstalled);
        return items;
    }

    public void search(String query, List<String> kinds, List<String> tags, ApiCallback<Resources> callback) {
        ResourceApi resApi = new ResourceApi();
        try {
            resApi.resourceQueryAsync(query, null, kinds, tags, null, null, callback);
        } catch (ApiException e) {
            logger.warn(e.getLocalizedMessage(), e);
        }
    }

    public List<ResourceVersionData> getVersionsById(int id) {
        List<ResourceVersionData> versions = new ArrayList<>();
        Optional<HubItem> itemSelected = getAllHubItems().stream().filter(item -> item.getResource().getId() == id).findFirst();
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
                logger.warn(e.getLocalizedMessage());
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
        if (kind.equalsIgnoreCase(KIND_TASK)) {
            return installTaskFromHub(name, kind, version);
        } else if (kind.equalsIgnoreCase(KIND_CLUSTERTASK)) {
            return installClusterTaskFromHub(name, kind, version, catalog);
        } else if (kind.equalsIgnoreCase(KIND_PIPELINE)) {
            return installPipelineFromHub(name, kind, version, catalog);
        }

        return Constants.InstallStatus.ERROR;
    }

    private Constants.InstallStatus installTaskFromHub(String name, String kind, String version) throws IOException {
        boolean taskAlreadyOnCluster = tasksInstalled.stream().anyMatch(task -> task.getMetadata().getName().equalsIgnoreCase(name));
        String confirmationMessage = getConfirmationMessage(kind, name, taskAlreadyOnCluster);
        if (DeployHelper.saveTaskOnClusterFromHub(project, name, version, taskAlreadyOnCluster, confirmationMessage)) {
            if (!taskAlreadyOnCluster) {
                return Constants.InstallStatus.INSTALLED;
            }
            return Constants.InstallStatus.OVERWRITTEN;
        }
        return Constants.InstallStatus.ERROR;
    }

    private Constants.InstallStatus installClusterTaskFromHub(String name, String kind, String version, String catalog) throws IOException {
        boolean clusterTaskAlreadyOnCluster = clusterTasksInstalled.stream().anyMatch(task -> task.getMetadata().getName().equalsIgnoreCase(name));;
        String confirmationMessage = getConfirmationMessage(kind, name, clusterTaskAlreadyOnCluster);
        String yamlFromHub = tkn.getTaskYAMLFromHub(name, version);
        ObjectNode yamlObject = YAMLBuilder.convertToObjectNode(yamlFromHub);
        if (yamlObject.has("metadata") &&
                yamlObject.get("metadata").has("labels") &&
                !yamlObject.get("metadata").get("labels").has(HUB_CATALOG_TAG)) {
            ((ObjectNode)yamlObject.get("metadata").get("labels")).put(HUB_CATALOG_TAG, catalog);
        }
        yamlObject.put("kind", kind);
        String yamlUpdated = YAMLHelper.JSONToYAML(yamlObject, false);
        if (DeployHelper.saveOnCluster(project, "", yamlUpdated, confirmationMessage, true, false)) {
            if (!clusterTaskAlreadyOnCluster) {
                return Constants.InstallStatus.INSTALLED;
            }
            return Constants.InstallStatus.OVERWRITTEN;
        }
        return Constants.InstallStatus.ERROR;
    }

    private Constants.InstallStatus installPipelineFromHub(String name, String kind, String version, String catalog) throws IOException {
        boolean pipelineAlreadyOnCluster = pipelinesInstalled.stream().anyMatch(pp -> pp.getMetadata().getName().equalsIgnoreCase(name));
        String confirmationMessage = getConfirmationMessage(kind, name, pipelineAlreadyOnCluster);
        String yamlFromHub = tkn.getPipelineYAMLFromHub(name, version);
        ObjectNode yamlObject = YAMLBuilder.convertToObjectNode(yamlFromHub);
        if (yamlObject.has("metadata") &&
                yamlObject.get("metadata").has("labels") &&
                !yamlObject.get("metadata").get("labels").has(HUB_CATALOG_TAG)) {
            ((ObjectNode)yamlObject.get("metadata").get("labels")).put(HUB_CATALOG_TAG, catalog);
        }
        String yamlUpdated = YAMLHelper.JSONToYAML(yamlObject, false);
        if (DeployHelper.saveOnCluster(project, "", yamlUpdated, confirmationMessage, false, false)) {
            if (!pipelineAlreadyOnCluster) {
                return Constants.InstallStatus.INSTALLED;
            }
            return Constants.InstallStatus.OVERWRITTEN;
        }
        return Constants.InstallStatus.ERROR;
    }

    private String getConfirmationMessage(String kind, String name, boolean alreadyExists) {
        if (alreadyExists) {
            return "A " + kind + " with name " + name + " already exists on the cluster. By installing this " + kind + " the one on the cluster will be overwritten. Do you want to install it?";
        }
        return "Do you want to install the " + kind + " " + name + " on the cluster?";
    }

    private void initWatch() throws IOException {
        String namespace = tkn.getNamespace();
        tkn.watchPipelines(namespace, getWatcher());
        tkn.watchTasks(namespace, getWatcher());
        tkn.watchClusterTasks(getWatcher());
    }

    private <T extends HasMetadata> Watcher<T> getWatcher() {
        return new Watcher<T>() {
            @Override
            public void eventReceived(Action action, T resource) {
                switch (action) {
                    case ADDED: {
                        if (resource instanceof Task) {
                            tasksInstalled.add(resource);
                        } else if (resource instanceof ClusterTask) {
                            clusterTasksInstalled.add(resource);
                        } else if (resource instanceof Pipeline) {
                            pipelinesInstalled.add(resource);
                        }
                        refreshHubPanel();
                        break;
                    }
                    case DELETED: {
                        if (resource instanceof Task) {
                            tasksInstalled.removeIf(task -> task.getMetadata().getName().equalsIgnoreCase(resource.getMetadata().getName()));
                        } else if (resource instanceof ClusterTask) {
                            clusterTasksInstalled.removeIf(task -> task.getMetadata().getName().equalsIgnoreCase(resource.getMetadata().getName()));
                        } else if (resource instanceof Pipeline) {
                            pipelinesInstalled.removeIf(pp -> pp.getMetadata().getName().equalsIgnoreCase(resource.getMetadata().getName()));
                        }
                        refreshHubPanel();
                        break;
                    }
                    default: {
                        break;
                    }
                }
            }

            @Override
            public void onClose(WatcherException cause) {  }
        };
    }

    private void refreshHubPanel() {
        if (hubPanelCallback != null) {
            hubPanelCallback.refresh();
        }
    }

    public List<HasMetadata> getTasksInstalled() {
        return tasksInstalled;
    }

    public List<HasMetadata> getClusterTasksInstalled() {
        return clusterTasksInstalled;
    }

    public List<HasMetadata> getPipelinesInstalled() {
        return pipelinesInstalled;
    }

    public Project getProject() {
        return project;
    }

    public boolean getIsClusterTaskView() { return caller != null && caller instanceof ClusterTasksNode; }

    public boolean getIsPipelineView() {
        return caller != null && caller instanceof PipelinesNode;
    }

}
