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

import com.google.common.base.Strings;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.LightVirtualFile;
import com.redhat.devtools.intellij.tektoncd.tkn.Tkn;
import gnu.trove.THashMap;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.WatcherException;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_CLUSTERTASKS;
import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_CLUSTERTRIGGERBINDINGS;
import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_EVENTLISTENERS;
import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_PIPELINE;
import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_PIPELINERUN;
import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_TASK;
import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_TASKRUN;
import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_TRIGGERBINDINGS;
import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_TRIGGERTEMPLATES;

public class TektonVirtualFileManager {
    static Logger logger = LoggerFactory.getLogger(TektonVirtualFileManager.class);

    private static TektonVirtualFileManager INSTANCE;
    private final Map<String, VirtualFile> tektonFiles = new THashMap<>();
    private Tkn tkncli;

    private TektonVirtualFileManager(Project project) {
        tkncli = TreeHelper.getTkn(project);
    }

    public static TektonVirtualFileManager getInstance(@NotNull Project project) {
        if (INSTANCE == null) {
            INSTANCE = new TektonVirtualFileManager(project);
        }
        return INSTANCE;
    }

    public VirtualFile findResource(String namespace, String kind, String resourceName) throws IOException {
        String id = getId(namespace, kind, resourceName);
        VirtualFile file = tektonFiles.get(id);
        if (file == null) {
            file = getResourceRemotely(namespace, kind, resourceName);
            tkncli.getWatchHandler().setWatchByResourceName(namespace, kind, resourceName, getWatcher(namespace, kind, resourceName));
            tektonFiles.put(id, file);
        }
        return file;
    }

    private <T extends HasMetadata> Watcher<T> getWatcher(String namespace, String kind, String resourceName) {
        String id = getId(namespace, kind, resourceName);
        TektonVirtualFileManager tvfm = this;
        return new Watcher<T>() {
            @Override
            public void eventReceived(Action action, T resource) {
                switch (action) {
                    case MODIFIED: {
                        try {
                            VirtualFile file = tvfm.getResourceRemotely(namespace, kind, resourceName);
                            tektonFiles.put(id, file);
                        } catch (IOException e) {
                            logger.warn(e.getLocalizedMessage(), e);
                        }
                    }
                    case DELETED: {
                        tektonFiles.remove(id);
                    }
                }
            }

            @Override
            public void onClose(WatcherException cause) {  }
        };
    }

    private VirtualFile getResourceRemotely(String namespace, String kind, String resourceName) throws IOException {
        String content = "";
        if (kind.equalsIgnoreCase(KIND_PIPELINE)) {
            content = tkncli.getPipelineYAML(namespace, resourceName);
        } else if (kind.equalsIgnoreCase(KIND_TASK)) {
            content = tkncli.getTaskYAML(namespace, resourceName);
        } else if (kind.equalsIgnoreCase(KIND_CLUSTERTASKS)) {
            content = tkncli.getClusterTaskYAML(resourceName);
        } else if (kind.equalsIgnoreCase(KIND_TRIGGERTEMPLATES)) {
            content = tkncli.getTriggerTemplateYAML(namespace, resourceName);
        } else if (kind.equalsIgnoreCase(KIND_TRIGGERBINDINGS)) {
            content = tkncli.getTriggerBindingYAML(namespace, resourceName);
        } else if (kind.equalsIgnoreCase(KIND_CLUSTERTRIGGERBINDINGS)) {
            content = tkncli.getClusterTriggerBindingYAML(resourceName);
        } else if (kind.equalsIgnoreCase(KIND_EVENTLISTENERS)) {
            content = tkncli.getEventListenerYAML(namespace, resourceName);
        } else if (kind.equalsIgnoreCase(KIND_TASKRUN)) {
            content = tkncli.getTaskRunYAML(namespace, resourceName);
        } else if (kind.equalsIgnoreCase(KIND_PIPELINERUN)){
            content = tkncli.getPipelineRunYAML(namespace, resourceName);
        }
        return new LightVirtualFile(resourceName + ".yaml", content);
    }

    private String getId(String namespace, String kind, String resourceName) {
        String id = "";
        if (!Strings.isNullOrEmpty(namespace)) {
            id += namespace + "-";
        }
        id += kind + "-" + resourceName;
        return id;
    }
}
