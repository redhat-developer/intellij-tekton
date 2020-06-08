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

import com.intellij.ui.treeStructure.Tree;
import com.redhat.devtools.intellij.tektoncd.Constants;
import com.redhat.devtools.intellij.tektoncd.tree.ClusterTasksNode;
import com.redhat.devtools.intellij.tektoncd.tree.ClusterTriggerBindingsNode;
import com.redhat.devtools.intellij.tektoncd.tree.ConditionsNode;
import com.redhat.devtools.intellij.tektoncd.tree.EventListenersNode;
import com.redhat.devtools.intellij.tektoncd.tree.ParentableNode;
import com.redhat.devtools.intellij.tektoncd.tree.PipelineRunsNode;
import com.redhat.devtools.intellij.tektoncd.tree.PipelinesNode;
import com.redhat.devtools.intellij.tektoncd.tree.ResourcesNode;
import com.redhat.devtools.intellij.tektoncd.tree.TaskRunsNode;
import com.redhat.devtools.intellij.tektoncd.tree.TasksNode;
import com.redhat.devtools.intellij.tektoncd.tree.TektonTreeStructure;
import com.redhat.devtools.intellij.tektoncd.tree.TriggerBindingsNode;
import com.redhat.devtools.intellij.tektoncd.tree.TriggerTemplatesNode;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.Watch;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.tekton.client.TektonClient;

import java.util.HashMap;
import java.util.Map;

public class WatchHandler {
    private Map<String, Watch> watches;

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

    public void setWatch(ParentableNode<? extends ParentableNode<?>> element) {
        TektonClient client = element.getRoot().getTektonClient();

        String namespace = element.getNamespace();
        String watchId = getWatchId(element);
        Watch watch = null;
        if (element instanceof PipelinesNode) {
            watch = client.v1beta1().pipelines().inNamespace(namespace).watch(getWatcher(element));
        } else if (element instanceof PipelineRunsNode) {
            watch = client.v1beta1().pipelineRuns().inNamespace(namespace).watch(getWatcher(element));
        } else if (element instanceof ResourcesNode) {
            watch = client.v1alpha1().pipelineResources().inNamespace(namespace).watch(getWatcher(element));
        } else if (element instanceof TasksNode) {
            watch = client.v1beta1().tasks().inNamespace(namespace).watch(getWatcher(element));
        } else if (element instanceof TaskRunsNode) {
            watch = client.v1beta1().taskRuns().inNamespace(namespace).watch(getWatcher(element));
        } else if (element instanceof ClusterTasksNode) {
            watch = client.v1beta1().clusterTasks().watch(getWatcher(element));
        } else if (element instanceof ConditionsNode) {
            watch = client.v1alpha1().conditions().inNamespace(namespace).watch(getWatcher(element));
        }

        if (watch != null) {
            watches.put(watchId, watch);
        }
    }

    public void removeWatch(ParentableNode<? extends ParentableNode<?>> element) {
        String watchId =  getWatchId(element);
        if (watches.containsKey(watchId)) {
            Watch watch = watches.get(watchId);
            watch.close();
        }
    }

    private String getWatchId(ParentableNode<? extends ParentableNode<?>> element) {
        return element.getNamespace() + "-" + element.getName();
    }

    public <T extends HasMetadata> Watcher<T> getWatcher(ParentableNode<? extends ParentableNode<?>> element) {
        return new Watcher<T>() {
            @Override
            public void eventReceived(Action action, T resource) {
                Tree tree = TreeHelper.getTree(element.getRoot().getProject());
                TektonTreeStructure treeStructure = (TektonTreeStructure)tree.getClientProperty(Constants.STRUCTURE_PROPERTY);
                treeStructure.fireModified(element);
            }

            @Override
            public void onClose(KubernetesClientException cause) {  }
        };
    }

    public boolean canBeWatched(ParentableNode<? extends ParentableNode<?>> element) {
        return element instanceof PipelinesNode ||
               element instanceof PipelineRunsNode ||
               element instanceof ResourcesNode ||
               element instanceof TasksNode ||
               element instanceof TaskRunsNode ||
               element instanceof ClusterTasksNode ||
               element instanceof ConditionsNode ||
               element instanceof TriggerTemplatesNode ||
               element instanceof TriggerBindingsNode ||
               element instanceof ClusterTriggerBindingsNode ||
               element instanceof EventListenersNode;
    }
}
