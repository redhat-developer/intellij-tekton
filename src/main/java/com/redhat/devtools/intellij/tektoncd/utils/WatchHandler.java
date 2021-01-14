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
import com.intellij.notification.NotificationType;
import com.intellij.openapi.project.Project;
import com.intellij.ui.treeStructure.Tree;
import com.redhat.devtools.intellij.common.utils.DateHelper;
import com.redhat.devtools.intellij.tektoncd.settings.SettingsState;
import com.redhat.devtools.intellij.tektoncd.tkn.Tkn;
import com.redhat.devtools.intellij.tektoncd.tree.ClusterTasksNode;
import com.redhat.devtools.intellij.tektoncd.tree.ClusterTriggerBindingsNode;
import com.redhat.devtools.intellij.tektoncd.tree.ConditionsNode;
import com.redhat.devtools.intellij.tektoncd.tree.EventListenersNode;
import com.redhat.devtools.intellij.tektoncd.tree.ParentableNode;
import com.redhat.devtools.intellij.tektoncd.tree.PipelineNode;
import com.redhat.devtools.intellij.tektoncd.tree.PipelineRunNode;
import com.redhat.devtools.intellij.tektoncd.tree.PipelineRunsNode;
import com.redhat.devtools.intellij.tektoncd.tree.PipelinesNode;
import com.redhat.devtools.intellij.tektoncd.tree.ResourcesNode;
import com.redhat.devtools.intellij.tektoncd.tree.TaskNode;
import com.redhat.devtools.intellij.tektoncd.tree.TaskRunsNode;
import com.redhat.devtools.intellij.tektoncd.tree.TasksNode;
import com.redhat.devtools.intellij.tektoncd.tree.TriggerBindingsNode;
import com.redhat.devtools.intellij.tektoncd.tree.TriggerTemplatesNode;
import io.fabric8.knative.internal.pkg.apis.Condition;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.client.Watch;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.WatcherException;
import io.fabric8.tekton.pipeline.v1beta1.PipelineRun;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.swing.tree.TreePath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_PIPELINERUN;
import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_PIPELINERUNS;
import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_TASK;
import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_TASKRUN;
import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_TASKRUNS;

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

    public void setWatchByResourceName(Tkn tkn, String namespace, String kind, String resourceName, Watcher watcher) {
        String watchId = getWatchId(namespace, kind + "-" + resourceName);
        Watch watch = null;
        WatchNodes wn = null;

        if (this.watches.containsKey(watchId)) {
            return;
        }

        try {
            if (kind.equalsIgnoreCase(KIND_TASK)) {
                watch = tkn.watchTask(namespace, resourceName, watcher);
            }
            wn = new WatchNodes(watch);
        } catch (IOException e) {
            logger.warn("Error: " + e.getLocalizedMessage());
        }

        if (wn != null) {
            watches.put(watchId, wn);
        }

    }

    public void setWatchByKind(Tkn tkn, Project project, String namespace, String kind) {
        String watchId = getWatchId(namespace, kind);
        if (this.watches.containsKey(watchId)) {
            return;
        }

        try {
            Watcher watcher = getWatcher(watchId, project);
            if (kind.equalsIgnoreCase(KIND_PIPELINERUN)) {
                tkn.watchPipelineRuns(namespace, watcher);
            } else if (kind.equalsIgnoreCase(KIND_TASKRUN)) {
                tkn.watchTaskRuns(namespace, watcher);
            }
            watches.put(watchId, null);
        } catch (IOException e) {
            logger.warn(e.getLocalizedMessage());
        }
    }

    public void setWatchByNode(ParentableNode<?> element, TreePath treePath) {
        Tkn tkn = element.getRoot().getTkn();

        String namespace = element.getNamespace();
        String watchId = getWatchId(element);
        Watcher watcher = getWatcher(watchId, element.getRoot().getProject());
        Watch watch = null;
        WatchNodes wn = null;

        // a watch can be associated to multiple nodes
        // (e.g a taskRuns watcher, when a change happens, could update multiple nodes such as a single Task node and the TaskRuns node)
        if (this.watches.containsKey(watchId)) {
            wn = this.watches.get(watchId);
            if (wn != null) {
                wn.addNode(element, treePath);
                return;
            }
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
            } else if (element instanceof TriggerTemplatesNode) {
                watch = tkn.watchTriggerTemplates(namespace, watcher);
            } else if (element instanceof TriggerBindingsNode) {
                watch = tkn.watchTriggerBindings(namespace, watcher);
            } else if (element instanceof ClusterTriggerBindingsNode) {
                watch = tkn.watchClusterTriggerBindings(watcher);
            } else if (element instanceof EventListenersNode) {
                watch = tkn.watchEventListeners(namespace, watcher);
            }
            wn = new WatchNodes(watch, treePath);
        } catch (IOException e) {
            logger.warn("Error: " + e.getLocalizedMessage());
        }

        if (wn != null) {
            watches.put(watchId, wn);
        }
    }

    public void removeWatch(ParentableNode<?> element, TreePath treePath) {
        String watchId = getWatchId(element);
        if (watches.containsKey(watchId)) {
            WatchNodes wn = watches.get(watchId);
            wn.removeNode(element, treePath);
            // kind of temporary hack until we only show the active namespace.
            // Prevent from closing the watch for *runs that must be always active
            if (wn != null && wn.isNodesEmpty() &&
                    !(watchId.equalsIgnoreCase(element.getNamespace() + "-" + KIND_TASKRUNS) ||
                     watchId.equalsIgnoreCase(element.getNamespace() + "-" + KIND_PIPELINERUNS))) {
                wn.getWatch().close();
                watches.remove(watchId);
            }
        }
    }

    private String getWatchId(ParentableNode<?> element) {
        String name = element.getName();
        if (element instanceof TaskNode || element instanceof PipelineRunNode) {
            // we are expanding a single task or pipelinerun node and we want it to refresh if its taskruns change
            name = KIND_TASKRUN;
        } else if (element instanceof PipelineNode) {
            // we are expanding a single pipeline node and we want it to refresh if its pipelineruns change
            name = KIND_PIPELINERUN;
        }
        return getWatchId(element.getNamespace(), name);
    }

    private String getWatchId(String namespace, String name) {
        return (namespace + "-" + name).toLowerCase();
    }

    public <T extends HasMetadata> Watcher<T> getWatcher(String watchId, Project project) {
        Instant watcherStartTime = Instant.now();
        return new Watcher<T>() {
            @Override
            public void eventReceived(Action action, T resource) {
                WatchNodes watchNode = watches.get(watchId);
                if (watchNode != null) {
                    RefreshQueue.get().addAll(watches.get(watchId).getNodes());
                }
                // watches for *runs are always active so there also could be nothing to refresh, only a notification to display
                if (!SettingsState.getInstance().displayPipelineRunResultAsNotification ||
                        !(resource instanceof io.fabric8.tekton.pipeline.v1beta1.PipelineRun) ||
                        ((PipelineRun) resource).getStatus() == null ||
                        Strings.isNullOrEmpty(((PipelineRun) resource).getStatus().getCompletionTime())) {
                    return;
                }
                Instant completion = Instant.parse(((PipelineRun) resource).getStatus().getCompletionTime());
                if (Duration.between(watcherStartTime, completion).getSeconds() > 0) {
                    List<Condition> conditions = ((PipelineRun) resource).getStatus().getConditions();
                    if (!conditions.isEmpty()) {
                        String name = "PipelineRun " + resource.getMetadata().getName();
                        String executionTime = DateHelper.humanizeDate(Instant.parse(((PipelineRun) resource).getStatus().getStartTime()), completion);
                        if (conditions.get(0).getStatus().equalsIgnoreCase("true")) {
                            NotificationHelper.notifyWithBalloon(project, name + " successfully completed in " + executionTime, NotificationType.INFORMATION);
                        } else {
                            NotificationHelper.notifyWithBalloon(project, name + " failed", NotificationType.ERROR);
                        }
                    }
                }
            }

            @Override
            public void onClose(WatcherException cause) {  }
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
               element instanceof ConditionsNode ||
               element instanceof TriggerTemplatesNode ||
               element instanceof TriggerBindingsNode ||
               element instanceof ClusterTriggerBindingsNode ||
               element instanceof EventListenersNode;
    }
}

class WatchNodes {
    private Watch watch;
    private List<TreePath> nodesToBeUpdated;

    public WatchNodes(Watch watch, TreePath... nodes) {
        this.watch = watch;
        this.nodesToBeUpdated = new ArrayList<>(Arrays.asList(nodes));
    }

    public Watch getWatch() {
        return this.watch;
    }

    public List<TreePath> getNodes() {
        return this.nodesToBeUpdated;
    }

    public void addNode(ParentableNode element, TreePath node) {
        removeNode(element, node);
        this.nodesToBeUpdated.add(node);
    }

    public void removeNode(ParentableNode element, TreePath node) {
        this.nodesToBeUpdated.remove(node);
        removeCollapsedNodes(element);
    }

    public void removeCollapsedNodes(ParentableNode element) {
        Tree tree = TreeHelper.getTree(element.getRoot().getProject());
        List<TreePath> nodesToDelete = this.nodesToBeUpdated.stream().filter(path -> !tree.isExpanded(path)).collect(Collectors.toList());
        this.nodesToBeUpdated.removeAll(nodesToDelete);
    }

    public boolean isNodesEmpty() {
        return this.nodesToBeUpdated.isEmpty();
    }
}
