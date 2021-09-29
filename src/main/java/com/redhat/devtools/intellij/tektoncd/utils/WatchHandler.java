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
import io.fabric8.tekton.pipeline.v1beta1.TaskRun;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_PIPELINE;
import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_PIPELINERUNS;
import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_POD;
import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_TASK;
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
        Supplier<Watch> watchSupplier = () -> {
            Watch watch = null;
            try {
                if (kind.equalsIgnoreCase(KIND_PIPELINE)) {
                    watch = tkn.watchPipeline(namespace, resourceName, watcher);
                } else if (kind.equalsIgnoreCase(KIND_TASK)) {
                    watch = tkn.watchTask(namespace, resourceName, watcher);
                }
            } catch (IOException e) {
                logger.warn("Error: " + e.getLocalizedMessage(), e);
            }
            return watch;
        };
        setWatch(watchId, watchSupplier, false);
    }

    public void setWatchByLabel(Tkn tkn, String namespace, String kind, String keyLabel, String valueLabel, Watcher watcher, boolean updateIfAlreadyExisting) {
        String watchId = getWatchId(namespace, kind + "-" + keyLabel + "-" + valueLabel);
        Supplier<Watch> watchSupplier = () -> {
            try {
                if (kind.equalsIgnoreCase(KIND_POD)) {
                    return tkn.watchPodsWithLabel(namespace, keyLabel, valueLabel, watcher);
                }
            } catch (IOException e) {
                logger.warn("Error: " + e.getLocalizedMessage(), e);
            }
            return null;
        };
        setWatch(watchId, watchSupplier, updateIfAlreadyExisting);
    }

    private void setWatch(String watchId, Supplier<Watch> watchSupplier, boolean updateIfAlreadyExisting) {
        if (!updateIfAlreadyExisting && this.watches.containsKey(watchId)) {
            return;
        }

        Watch watch = watchSupplier.get();

        if (watch != null) {
            removeWatch(watchId);
            WatchNodes wn = new WatchNodes(watch);
            watches.put(watchId, wn);
        }
    }

    public void setWatchByNode(ParentableNode<?> element) {
        Tkn tkn = element.getRoot().getTkn();

        String namespace = element.getNamespace();
        String watchId = getWatchId(element);
        Watch watch = null;
        WatchNodes wn = null;

        // a watch can be associated to multiple nodes
        // (e.g a taskRuns watcher, when a change happens, could update multiple nodes such as a single Task node and the TaskRuns node)
        if (this.watches.containsKey(watchId)) {
            wn = this.watches.get(watchId);
            if (wn.getNodes().stream().noneMatch(item -> item.getName().equalsIgnoreCase(element.getName()) &&
                    ((ParentableNode)item.getParent()).getName().equalsIgnoreCase(((ParentableNode)element.getParent()).getName()))) {
                wn.getNodes().add(element);
            }
            return;
        }


        Watcher watcher = getWatcher(watchId, element.getRoot().getProject());

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
            wn = new WatchNodes(watch, element);
        } catch (IOException e) {
            logger.warn("Error: " + e.getLocalizedMessage(), e);
        }

        if (wn != null) {
            watches.put(watchId, wn);
        }
    }

    public void removeAll() {
        this.watches.values().stream().forEach(item -> {
            item.getWatch().close();
        });
        this.watches.clear();
    }

    public void removeWatchByLabel(String namespace, String kind, String keyLabel, String valueLabel) {
        String watchId = getWatchId(namespace, kind + "-" + keyLabel + "-" + valueLabel);
        removeWatch(watchId);
    }

    public void removeWatch(String watchId) {
        if (watches.containsKey(watchId)) {
            WatchNodes watchNodes = watches.remove(watchId);
            watchNodes.getWatch().close();
        }
    }

    private String getWatchId(ParentableNode<?> element) {
        String name = element.getName();
        if (element instanceof TaskNode || element instanceof PipelineRunNode) {
            // we are expanding a single task or pipelinerun node and we want it to refresh if its taskruns change
            name = KIND_TASKRUNS;
        } else if (element instanceof PipelineNode) {
            // we are expanding a single pipeline node and we want it to refresh if its pipelineruns change
            name = KIND_PIPELINERUNS;
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
                    List<ParentableNode> nodesById = watches.get(watchId).getNodes();
                    if (!nodesById.isEmpty()) {
                        refreshNodesByType(resource, nodesById);
                    }
                }

                if (resource instanceof PipelineRun) {
                    notifyRunCompletion(project, (PipelineRun) resource, watcherStartTime);
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

    private <T extends HasMetadata> void refreshNodesByType(T resource, List<ParentableNode> nodes) {
        if (resource instanceof PipelineRun) {
            List<ParentableNode> nodesToRefresh = new ArrayList<>(Arrays.asList(nodes.get(0)));
            String pipeline = resource.getMetadata().getLabels() == null ? null : resource.getMetadata().getLabels().get("tekton.dev/pipeline");
            if (pipeline != null) {
                Optional<ParentableNode> pNode = nodes.stream().filter(node -> node.getName().equalsIgnoreCase(pipeline)).findFirst();
                if (pNode.isPresent()) {
                    nodesToRefresh.add(pNode.get());
                }
            }
            RefreshQueue.get().addAll(nodesToRefresh);
        } else if (resource instanceof TaskRun) {
            List<ParentableNode> nodesToRefresh = new ArrayList<>(Arrays.asList(nodes.get(0)));
            String task = resource.getMetadata().getLabels() == null ? null : resource.getMetadata().getLabels().get("tekton.dev/task");
            if (task != null) {
                Optional<ParentableNode> pNode = nodes.stream().filter(node -> node.getName().equalsIgnoreCase(task)).findFirst();
                if (pNode.isPresent()) {
                    nodesToRefresh.add(pNode.get());
                }
                String pipelineRun = resource.getMetadata().getLabels() == null ? null : resource.getMetadata().getLabels().get("tekton.dev/pipelineRun");
                if (pipelineRun != null) {
                    List<ParentableNode> pNodes = nodes.stream().filter(node -> node.getName().equalsIgnoreCase(pipelineRun)).collect(Collectors.toList());
                    if (!pNodes.isEmpty()) {
                        nodesToRefresh.addAll(pNodes);
                    }
                }
            }
            RefreshQueue.get().addAll(nodesToRefresh);
        } else {
            RefreshQueue.get().addAll(nodes);
        }
    }

    private <T extends HasMetadata> void notifyRunCompletion(Project project, PipelineRun resource, Instant watcherStartTime) {
        // watches for *runs are always active so there also could be nothing to refresh, only a notification to display
        if (!SettingsState.getInstance().displayPipelineRunResultAsNotification ||
                (resource).getStatus() == null ||
                Strings.isNullOrEmpty(resource.getStatus().getCompletionTime())) {
            return;
        }

        Instant completion = Instant.parse(resource.getStatus().getCompletionTime());
        if (Duration.between(watcherStartTime, completion).getSeconds() > 0) {
            List<Condition> conditions = resource.getStatus().getConditions();
            if (!conditions.isEmpty()) {
                String name = "PipelineRun " + resource.getMetadata().getName();
                String executionTime = DateHelper.humanizeDate(Instant.parse(resource.getStatus().getStartTime()), completion);
                if (conditions.get(0).getStatus().equalsIgnoreCase("true")) {
                    NotificationHelper.notifyWithBalloon(project, name + " successfully completed in " + executionTime, NotificationType.INFORMATION);
                } else {
                    NotificationHelper.notifyWithBalloon(project, name + " failed", NotificationType.ERROR);
                }
            }
        }
    }
}


class WatchNodes {
    private Watch watch;
    private List<ParentableNode> nodesToBeUpdated;

    public WatchNodes(Watch watch, ParentableNode... nodes) {
        this.watch = watch;
        this.nodesToBeUpdated = new ArrayList<>(Arrays.asList(nodes));
    }

    public Watch getWatch() {
        return this.watch;
    }

    public List<ParentableNode> getNodes() {
        return this.nodesToBeUpdated;
    }
}
