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
import java.util.stream.Collectors;

public class HubModel {

    private List<HubItem> allHubItems;
    private Map<String, String> resourcesYaml;
    private String selected;

    private static HubModel instance;

    public static HubModel getInstance() {
        if (instance == null) {
            instance = new HubModel();
        }

        return instance;
    }
    private HubModel() {
        allHubItems = new ArrayList<>();
        resourcesYaml = new HashMap<>();
        init();
    }

    public void init() {
        ExecHelper.submit(() -> {
            ResourceApi resApi = new ResourceApi();
            try {
                Resources resources = resApi.resourceList(500);
                allHubItems.addAll(resources.getData().stream().map(resource -> new HubItem(resource)).collect(Collectors.toList()));
            } catch (ApiException e) { }
        });
    }

    public List<HubItem> getAllHubItems() {
        return allHubItems;
    }

    public void search(String query, List<String> kinds, List<String> tags, ApiCallback<Resources> callback) {
        String querySanitized = getActualQuery(query);
        ResourceApi resApi = new ResourceApi();
        try {
            resApi.resourceQueryAsync(querySanitized, kinds, tags, null, null, callback);
            //allHubItems.addAll(resources.getData().stream().map(resource -> new HubItem(resource)).collect(Collectors.toList()));
        } catch (ApiException e) { }
       // return allHubItems.stream().filter(item -> item.getResource().getName().contains(querySanitized) || item.getResource().getTags().contains(querySanitized)).collect(Collectors.toList());
    }

    public String getSelectedHubItem() {
        return this.selected;
    }

    public void setSelectedHubItem(String itemSelected) {
        this.selected = itemSelected;
    }

    private String getActualQuery(String query) {
        if (query.contains("/sortBy:downloads")) {
            return query.replace("/sortBy:downloads", "");
        } else if (query.contains("/sortBy:name")) {
            return query.replace("/sortBy:name", "");
        } else if (query.contains("/sortBy:rating")) {
            return query.replace("/sortBy:rating", "");
        } else if (query.contains("/sortBy:updated")) {
            return query.replace("/sortBy:updated", "");
        }
        return query;
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

    public boolean installHubItem(Project project, String namespace, String uri, String confirmationMessage) throws IOException {
        String yaml = getContentByURI(uri);
        if (yaml.isEmpty()) {
            return false;
        }
        return DeployHelper.saveOnCluster(project, namespace, yaml, confirmationMessage);
    }
}
