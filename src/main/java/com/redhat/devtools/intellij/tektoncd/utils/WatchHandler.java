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

import com.redhat.devtools.intellij.tektoncd.tkn.Tkn;
import com.redhat.devtools.intellij.tektoncd.tree.ClusterTasksNode;
import com.redhat.devtools.intellij.tektoncd.tree.ConditionsNode;
import com.redhat.devtools.intellij.tektoncd.tree.ParentableNode;
import com.redhat.devtools.intellij.tektoncd.tree.PipelineNode;
import com.redhat.devtools.intellij.tektoncd.tree.PipelineRunNode;
import com.redhat.devtools.intellij.tektoncd.tree.PipelineRunsNode;
import com.redhat.devtools.intellij.tektoncd.tree.PipelinesNode;
import com.redhat.devtools.intellij.tektoncd.tree.ResourcesNode;
import com.redhat.devtools.intellij.tektoncd.tree.TaskNode;
import com.redhat.devtools.intellij.tektoncd.tree.TaskRunsNode;
import com.redhat.devtools.intellij.tektoncd.tree.TasksNode;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.Watch;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.tekton.client.TektonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WatchHandler {
    private static final Logger logger = LoggerFactory.getLogger(WatchHandler.class);
    private Map<String, WatchNodes> watches;

    private static WatchHandler instance;

    private WatchHandler() {
        watches = new HashMap<>();
    }

    public static WatchHandler get() {
        if (instance == null) {
            instance = new WatchHandler();
        }
        return instance;
    }

    public void setWatch(ParentableNode<?> element) {
        Tkn tkn = element.getRoot().getTkn();

        String namespace = element.getNamespace();
        String watchId = getWatchId(element);
        Watcher watcher = getWatcher(watchId);
        Watch watch = null;
        WatchNodes wn = null;

        if (this.watches.containsKey(watchId)) {
            wn = this.watches.get(watchId);
            wn.addNode(element);
            return;
        }

        try {
            if (element instanceof PipelinesNode) {
                watch = tkn.watchPipelines(namespace, watcher);
            } else if (element instanceof PipelineNode) {
                // we are expanding a single pipeline node and we want it to refresh if its children (pipelineruns) change
                watch = tkn.watchPipelineRuns(namespace, watcher);
            } else if (element instanceof PipelineRunsNode) {
                watch = tkn.watchPipelineRuns(namespace, watcher);
            } else if (element instanceof PipelineRunNode) {
                // we are expanding a single pipelinerun node and we want it to refresh if its children (taskruns) change
                watch = tkn.watchTaskRuns(namespace, watcher);
            } else if (element instanceof ResourcesNode) {
                watch = tkn.watchPipelineResources(namespace, watcher);
            } else if (element instanceof TasksNode) {
                watch = tkn.watchTasks(namespace, watcher);
            } else if (element instanceof TaskNode) {
                // we are expanding a single task node and we want it to refresh if its children (taskruns) change
                watch = tkn.watchTaskRuns(namespace, watcher);
            } else if (element instanceof TaskRunsNode) {
                watch = tkn.watchTaskRuns(namespace, watcher);
            } else if (element instanceof ClusterTasksNode) {
                watch = tkn.watchClusterTasks(watcher);
            } else if (element instanceof ConditionsNode) {
                watch = tkn.watchConditions(namespace, watcher);
            }
            wn = new WatchNodes(watch, element);
        } catch (IOException e) {
            logger.warn("Error: " + e.getLocalizedMessage());
        }

        if (wn != null) {
            watches.put(watchId, wn);
        }
    }

    public void removeWatch(ParentableNode<?> element) {
        String watchId = getWatchId(element);
        if (watches.containsKey(watchId)) {
            WatchNodes wn = watches.get(watchId);
            if (wn.removeNodeAndStopWatchIfLast(element)) {
                watches.remove(watchId);
            }
        }
    }

    private String getWatchId(ParentableNode<?> element) {
        String name = element.getName();
        if (element instanceof TaskNode || element instanceof PipelineRunNode) {
            // we are expanding a single task or pipelinerun node and we want it to refresh if its taskruns change
            name = "TaskRuns";
        } else if (element instanceof PipelineNode) {
            // we are expanding a single pipeline node and we want it to refresh if its pipelineruns change
            name = "PipelineRuns";
        }
        return getWatchId(element.getNamespace(), name);
    }

    private String getWatchId(String namespace, String name) {
        return (namespace + "-" + name).toLowerCase();
    }

    public <T extends HasMetadata> Watcher<T> getWatcher(String watchId) {
        return new Watcher<T>() {
            @Override
            public void eventReceived(Action action, T resource) {
                RefreshQueue.get().addAll(watches.get(watchId).getNodes());
            }

            @Override
            public void onClose(KubernetesClientException cause) {  }
        };
    }

    public boolean canBeWatched(ParentableNode<?> element) {
        return element instanceof PipelinesNode ||
               element instanceof PipelineNode ||
               element instanceof PipelineRunsNode ||
               element instanceof PipelineRunNode ||
               element instanceof ResourcesNode ||
               element instanceof TasksNode ||
               element instanceof TaskNode ||
               element instanceof TaskRunsNode ||
               element instanceof ClusterTasksNode ||
               element instanceof ConditionsNode;
    }
}

class WatchNodes {
    private Watch watch;
    private List<ParentableNode> nodes;

    public WatchNodes(Watch watch, ParentableNode... nodes) {
        this.watch = watch;
        this.nodes = new ArrayList<>();
        for(ParentableNode node: nodes) {
            this.nodes.add(node);
        }
    }

    public List<ParentableNode> getNodes() {
        return this.nodes;
    }

    public void addNode(ParentableNode node) {
        if (!this.nodes.contains(node)) {
            this.nodes.add(node);
        }
    }

    public boolean removeNodeAndStopWatchIfLast(ParentableNode node) {
        this.nodes.remove(node);
        if (this.nodes.isEmpty()) {
            this.watch.close();
            return true;
        }
        return false;
    }
}
