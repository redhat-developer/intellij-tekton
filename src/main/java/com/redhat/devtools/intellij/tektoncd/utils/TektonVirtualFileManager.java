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
package com.redhat.devtools.intellij.tektoncd.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Strings;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileListener;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.VirtualFileSystem;
import com.redhat.devtools.intellij.common.utils.JSONHelper;
import com.redhat.devtools.intellij.common.utils.YAMLHelper;
import com.redhat.devtools.intellij.tektoncd.tkn.Tkn;
import gnu.trove.THashMap;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_CLUSTERTASK;
import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_CLUSTERTRIGGERBINDING;
import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_CONDITION;
import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_EVENTLISTENER;
import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_PIPELINE;
import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_PIPELINERESOURCE;
import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_PIPELINERUN;
import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_PLURAL;
import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_TASK;
import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_TASKRUN;
import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_TRIGGERBINDING;
import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_TRIGGERTEMPLATE;

public class TektonVirtualFileManager extends VirtualFileSystem {
    private static final Logger logger = LoggerFactory.getLogger(TektonVirtualFileManager.class);

    private static final Map<String, TektonVirtualFile> tektonFiles = new THashMap<>();
    private static final String myProtocol = "tekton";
    private static Project project;
    private static Tkn tkncli;

    public TektonVirtualFileManager() {}

    public static TektonVirtualFileManager getInstance() {
        return (TektonVirtualFileManager) VirtualFileManager.getInstance().getFileSystem(myProtocol);
    }

    public void init(Project pproject) {
        project = pproject;
    }

    @NotNull
    @Override
    public String getProtocol() {
        return myProtocol;
    }

    @Nullable
    @Override
    public VirtualFile findFileByPath(@NotNull String path) {
        path = getSanitizedPath(path);
        TektonVirtualFile file = tektonFiles.get(path);
        if (file == null) {
            try {
                file = getResourceRemotely(getTkn(), path);
                if (file != null) {
                    addFile(path, file);
                    WatchHandler.get().setWatchByResourceName(getTkn(), path, getWatcher(path));
                }
            } catch (IOException e) {
                logger.warn(e.getLocalizedMessage());
            }
        }
        return file;
    }

    private TektonVirtualFile getResourceRemotely(Tkn tkncli, String path) throws IOException {
        if (tkncli == null) {
            return null;
        }
        String namespace = TreeHelper.getNamespaceFromResourcePath(path);
        String kind = TreeHelper.getKindFromResourcePath(path);
        String resourceName = TreeHelper.getNameFromResourcePath(path);
        String content = "";
        if (kind.equalsIgnoreCase(KIND_PIPELINE)) {
            content = tkncli.getPipelineYAML(namespace, resourceName);
        } else if (kind.equalsIgnoreCase(KIND_PIPELINERESOURCE)) {
            content = tkncli.getResourceYAML(namespace, resourceName);
        } else if (kind.equalsIgnoreCase(KIND_TASK)) {
            content = tkncli.getTaskYAML(namespace, resourceName);
        } else if (kind.equalsIgnoreCase(KIND_CLUSTERTASK)) {
            content = tkncli.getClusterTaskYAML(resourceName);
        } else if (kind.equalsIgnoreCase(KIND_CONDITION)) {
            content = tkncli.getConditionYAML(namespace, resourceName);
        } else if (kind.equalsIgnoreCase(KIND_TRIGGERTEMPLATE)) {
            content = tkncli.getTriggerTemplateYAML(namespace, resourceName);
        } else if (kind.equalsIgnoreCase(KIND_TRIGGERBINDING)) {
            content = tkncli.getTriggerBindingYAML(namespace, resourceName);
        } else if (kind.equalsIgnoreCase(KIND_CLUSTERTRIGGERBINDING)) {
            content = tkncli.getClusterTriggerBindingYAML(resourceName);
        } else if (kind.equalsIgnoreCase(KIND_EVENTLISTENER)) {
            content = tkncli.getEventListenerYAML(namespace, resourceName);
        } else if (kind.equalsIgnoreCase(KIND_TASKRUN)) {
            content = tkncli.getTaskRunYAML(namespace, resourceName);
        } else if (kind.equalsIgnoreCase(KIND_PIPELINERUN)){
            content = tkncli.getPipelineRunYAML(namespace, resourceName);
        }
        if (content.isEmpty()) {
            return null;
        }
        return new TektonVirtualFile(path, content);
    }

    private <T extends HasMetadata> Watcher<T> getWatcher(String path) {
        TektonVirtualFileManager tvfs = this;
        return new Watcher<T>() {
            @Override
            public void eventReceived(Action action, T resource) {
                switch (action) {
                    case MODIFIED: {
                        try {
                            TektonVirtualFile file = tvfs.getResourceRemotely(tvfs.getTkn(), path);
                            tvfs.addFile(path, file);
                        } catch (IOException e) {
                            logger.warn(e.getLocalizedMessage());
                        }
                    }
                    case DELETED: {
                        if (tektonFiles.containsKey(path)) {
                            tektonFiles.remove(path);
                        }
                    }
                }
            }

            @Override
            public void onClose(KubernetesClientException cause) {  }
        };
    }

    private String getSanitizedPath(String path) {
        if (path.startsWith("/")) {
            return path.substring(1);
        }
        return path;
    }

    @Override
    public void refresh(boolean asynchronous) {

    }

    @Nullable
    @Override
    public VirtualFile refreshAndFindFileByPath(@NotNull String path) {
        return null;
    }

    @Override
    public void addVirtualFileListener(@NotNull VirtualFileListener listener) {

    }

    @Override
    public void removeVirtualFileListener(@NotNull VirtualFileListener listener) {

    }

    /**
     * Save a resource on the cluster
     * Url is in form namespace/kind/name for non cluster-scoped resources, kind/name otherwise
     */
    public void saveResource(String namespace, Document document) throws IOException {
        Tkn tkncli = getTkn();
        if (tkncli == null) {
            throw new IOException("Unable to contact the cluster");
        }

        String name = YAMLHelper.getStringValueFromYAML(document.getCharsSequence().toString(), new String[] {"metadata", "name"});
        if (Strings.isNullOrEmpty(name)) {
            throw new IOException("Tekton file has not a valid format. Name field is not valid or found.");
        }
        String kind = YAMLHelper.getStringValueFromYAML(document.getCharsSequence().toString(), new String[] {"kind"});
        if (Strings.isNullOrEmpty(kind)) {
            throw new IOException("Tekton file has not a valid format. Kind field is not found.");
        }
        String apiVersion = YAMLHelper.getStringValueFromYAML(document.getCharsSequence().toString(), new String[] {"apiVersion"});
        if (Strings.isNullOrEmpty(apiVersion)) {
            throw new IOException("Tekton file has not a valid format. ApiVersion field is not found.");
        }
        CustomResourceDefinitionContext crdContext = CRDHelper.getCRDContext(apiVersion, FileDocumentManager.getInstance().getFile(document).getUserData(KIND_PLURAL));
        if (crdContext == null) {
            throw new IOException("Tekton file has not a valid format. ApiVersion field contains an invalid value.");
        }
        JsonNode spec = YAMLHelper.getValueFromYAML(document.getCharsSequence().toString(), new String[] {"spec"});
        if (spec == null) {
            throw new IOException("Tekton file has not a valid format. Spec field is not found.");
        }

        Map<String, Object> resource = tkncli.getCustomResource(namespace, name, crdContext);
        if (resource == null) {
            tkncli.createCustomResource(namespace, crdContext, document.getCharsSequence().toString());
        } else {
            JsonNode customResource = JSONHelper.MapToJSON(resource);
            ((ObjectNode) customResource).set("spec", spec);
            tkncli.editCustomResource(namespace, name, crdContext, customResource.toString());
        }

        String fileUrl = TreeHelper.getTektonResourceUrl(namespace, kind.toLowerCase(), name, false);
        TektonVirtualFile file = new TektonVirtualFile(fileUrl, document.getCharsSequence());
        tektonFiles.put(fileUrl, file);
    }

    @Override
    protected void deleteFile(Object requestor, @NotNull VirtualFile vFile) throws IOException {

    }

    public void deleteResources(List<String> resourcesUrl, boolean deleteRelatedResources) throws IOException {
        if (resourcesUrl.isEmpty()) {
            return;
        }

        Tkn tkncli = getTkn();
        if (tkncli == null) {
            throw new IOException("Unable to contact the cluster");
        }

        String namespace = TreeHelper.getNamespaceFromResourcePath(resourcesUrl.get(0));
        Map<String, List<String>> resourcesByClass = new HashMap<>();
        for (String path: resourcesUrl) {
            String tempNamespace = TreeHelper.getNamespaceFromResourcePath(path);
            // delete action is only enable on resources belonging to the same namespace or cluster-scoped resources.
            if (!tempNamespace.isEmpty() && !tempNamespace.equalsIgnoreCase(namespace)) {
                throw new IOException("Delete action is only enable on resources of the same namespace");
            }
            resourcesByClass.computeIfAbsent(TreeHelper.getKindFromResourcePath(path), value -> new ArrayList<>())
                    .add(TreeHelper.getNameFromResourcePath(path));
            if (tektonFiles.containsKey(path)) {
                tektonFiles.remove(path);
            }
        }

        for (String kind: resourcesByClass.keySet()) {
            List<String> resources = resourcesByClass.get(kind);
            if (kind.equalsIgnoreCase(KIND_PIPELINE)) {
                tkncli.deletePipelines(namespace, resources, deleteRelatedResources);
            } else if (kind.equalsIgnoreCase(KIND_PIPELINERESOURCE)) {
                tkncli.deleteResources(namespace, resources);
            } else if (kind.equalsIgnoreCase(KIND_TASK)) {
                tkncli.deleteTasks(namespace, resources, deleteRelatedResources);
            } else if (kind.equalsIgnoreCase(KIND_CLUSTERTASK)) {
                tkncli.deleteClusterTasks(resources, deleteRelatedResources);
            } else if (kind.equalsIgnoreCase(KIND_CONDITION)) {
                tkncli.deleteConditions(namespace, resources);
            } else if (kind.equalsIgnoreCase(KIND_TRIGGERTEMPLATE)) {
                tkncli.deleteTriggerTemplates(namespace, resources);
            } else if (kind.equalsIgnoreCase(KIND_TRIGGERBINDING)) {
                tkncli.deleteTriggerBindings(namespace, resources);
            } else if (kind.equalsIgnoreCase(KIND_CLUSTERTRIGGERBINDING)) {
                tkncli.deleteClusterTriggerBindings(resources);
            } else if (kind.equalsIgnoreCase(KIND_EVENTLISTENER)) {
                tkncli.deleteEventListeners(namespace, resources);
            } else if (kind.equalsIgnoreCase(KIND_TASKRUN)) {
                tkncli.deleteTaskRuns(namespace, resources);
            } else if (kind.equalsIgnoreCase(KIND_PIPELINERUN)){
                tkncli.deletePipelineRuns(namespace, resources);
            }
        }
    }

    @Override
    protected void moveFile(Object requestor, @NotNull VirtualFile vFile, @NotNull VirtualFile newParent) throws IOException {

    }

    @Override
    protected void renameFile(Object requestor, @NotNull VirtualFile vFile, @NotNull String newName) throws IOException {

    }

    @NotNull
    @Override
    protected VirtualFile createChildFile(Object requestor, @NotNull VirtualFile vDir, @NotNull String fileName) throws IOException {
        return null;
    }

    @NotNull
    @Override
    protected VirtualFile createChildDirectory(Object requestor, @NotNull VirtualFile vDir, @NotNull String dirName) throws IOException {
        return null;
    }

    @NotNull
    @Override
    protected VirtualFile copyFile(Object requestor, @NotNull VirtualFile virtualFile, @NotNull VirtualFile newParent, @NotNull String copyName) throws IOException {
        return null;
    }

    @Override
    public boolean isReadOnly() {
        return false;
    }

    private void addFile(String path, TektonVirtualFile file) {
        if (!path.isEmpty() && file != null) {
            tektonFiles.put(path, file);
        }
    }

    private Tkn getTkn() {
        if (tkncli == null) {
            tkncli = TreeHelper.getTkn(project);
        }
        return tkncli;
    }

    public void setTkn(Tkn tkn) {
        tkncli = tkn;
    }

}
