/*******************************************************************************
 * Copyright (c) 2020 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package com.redhat.devtools.intellij.tektoncd.tree;

import com.intellij.icons.AllIcons;
import com.intellij.ide.util.treeView.AbstractTreeStructure;
import com.intellij.ide.util.treeView.NodeDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.util.ArrayUtil;
import com.redhat.devtools.intellij.common.tree.LabelAndIconDescriptor;
import com.redhat.devtools.intellij.common.tree.MutableModel;
import com.redhat.devtools.intellij.common.tree.MutableModelSupport;
import com.redhat.devtools.intellij.common.utils.ConfigHelper;
import com.redhat.devtools.intellij.common.utils.ConfigWatcher;
import com.redhat.devtools.intellij.common.utils.ExecHelper;
import com.redhat.devtools.intellij.tektoncd.tkn.Tkn;
import io.fabric8.kubernetes.api.model.Config;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.tekton.pipeline.v1beta1.PipelineRun;
import io.fabric8.tekton.pipeline.v1beta1.PipelineRunTaskRunStatus;
import io.fabric8.tekton.pipeline.v1beta1.TaskRun;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class TektonTreeStructure extends AbstractTreeStructure implements MutableModel<Object>, ConfigWatcher.Listener {
    private final Project project;
    private Config config;
    protected TektonRootNode root;

    protected static final String ERROR = "Please log in to the cluster";

    protected static final String NO_TEKTON = "Tekton not installed on the cluster";

    private static final Icon CLUSTER_ICON = IconLoader.findIcon("/images/cluster.png", TektonTreeStructure.class);

    private static final Icon NAMESPACE_ICON = IconLoader.findIcon("/images/project.png", TektonTreeStructure.class);

    private static final Icon PIPELINE_ICON = IconLoader.findIcon("/images/pipeline.svg", TektonTreeStructure.class);

    private static final Icon TASK_ICON = IconLoader.findIcon("/images/task.svg", TektonTreeStructure.class);

    private static final Icon CLUSTER_TASK_ICON = IconLoader.findIcon("/images/clustertask.svg", TektonTreeStructure.class);

    private static final Icon PIPELINE_RUN_ICON = IconLoader.findIcon("/images/pipelinerun.svg", TektonTreeStructure.class);

    private static final Icon TASK_RUN_ICON = IconLoader.findIcon("/images/taskrun.svg", TektonTreeStructure.class);

    private static final Icon TRIGGER_TEMPLATE_ICON = IconLoader.findIcon("/images/triggertemplate.svg", TektonTreeStructure.class);

    private static final Icon TRIGGER_BINDING_ICON = IconLoader.findIcon("/images/triggerbinding.svg", TektonTreeStructure.class);

    private static final Icon CLUSTER_TRIGGER_BINDING_ICON = IconLoader.findIcon("/images/clustertriggerbinding.svg", TektonTreeStructure.class);

    private static final Icon EVENT_LISTENER_ICON = IconLoader.findIcon("/images/eventlistener.svg", TektonTreeStructure.class);

    private static final Icon SUCCESS_ICON = IconLoader.findIcon("/images/success.png", TektonTreeStructure.class);

    private static final Icon FAILED_ICON = IconLoader.findIcon("/images/failed.png", TektonTreeStructure.class);

    private static final Icon RUNNING_ICON = IconLoader.findIcon("/images/running.png", TektonTreeStructure.class);

    private MutableModel<Object> mutableModelSupport = new MutableModelSupport<>();

    private final AtomicBoolean initialized = new AtomicBoolean(false);

    public TektonTreeStructure(Project project) {
        this.project = project;
        this.root = new TektonRootNode(project);
        this.config = loadConfig();
        initConfigWatcher();
    }

    private void initConfigWatcher() {
        ExecHelper.submit(new ConfigWatcher(Paths.get(ConfigHelper.getKubeConfigPath()), this));
    }

    protected Config loadConfig() {
        return ConfigHelper.safeLoadKubeConfig();
    }

    @Override
    public Object getRootElement() {
        if (!initialized.getAndSet(true)) {
            root.initializeTkn().thenAccept(tkn -> fireModified(root));
        }
        return root;
    }

    @Override
    public Object[] getChildElements(Object element) {
        Tkn tkn = root.getTkn();
        if (tkn != null) {
            if (element instanceof TektonRootNode) {
                return getFirstLevelNodes((TektonRootNode) element);
            }
            if (element instanceof NamespaceNode) {
                Object[] generalNodes = new Object[]{
                        new PipelinesNode(((NamespaceNode) element).getRoot(), (NamespaceNode) element),
                        new PipelineRunsNode(((NamespaceNode) element).getRoot(), (NamespaceNode) element),
                        new TasksNode(((NamespaceNode) element).getRoot(), (NamespaceNode) element),
                        new ClusterTasksNode(((NamespaceNode) element).getRoot(), (NamespaceNode) element),
                        new TaskRunsNode(((NamespaceNode) element).getRoot(), (NamespaceNode) element)
                };
                if (!tkn.isTektonTriggersAware()) {
                    watchNodes(tkn, generalNodes);
                    return generalNodes;
                }
                Object[] triggersNode = new Object[] {
                        new TriggerTemplatesNode(((NamespaceNode) element).getRoot(), (NamespaceNode) element),
                        new TriggerBindingsNode(((NamespaceNode) element).getRoot(), (NamespaceNode) element),
                        new ClusterTriggerBindingsNode(((NamespaceNode) element).getRoot(), (NamespaceNode) element),
                        new EventListenersNode(((NamespaceNode) element).getRoot(), (NamespaceNode) element)
                };
                Object[] secondLevelNodes = ArrayUtil.append(generalNodes, triggersNode);
                watchNodes(tkn, secondLevelNodes);
                return secondLevelNodes;

            }
            if (element instanceof ConfigurationsNode) {
                return getConfigurationNodes((ConfigurationsNode) element);
            }
            if (element instanceof PipelinesNode) {
                return getPipelines((PipelinesNode) element);
            }
            if (element instanceof PipelineNode) {
                return getPipelineRuns((PipelineNode) element, ((PipelineNode) element).getName());
            }
            if (element instanceof PipelineRunsNode) {
                return getPipelineRuns((PipelineRunsNode) element, "");
            }
            if (element instanceof PipelineRunNode) {
                return getTaskRuns((PipelineRunNode) element);
            }
            if (element instanceof TasksNode) {
                return getTasks((TasksNode) element);
            }
            if (element instanceof TaskNode) {
                return getTaskRuns((TaskNode) element, ((TaskNode) element).getName());
            }
            if (element instanceof ClusterTasksNode) {
                return getClusterTasks((ClusterTasksNode) element);
            }
            if (element instanceof TaskRunsNode) {
                return getTaskRuns((TaskRunsNode) element, "");
            }
            if (element instanceof TriggerTemplatesNode) {
                return getTriggerTemplates((TriggerTemplatesNode) element);
            }
            if (element instanceof TriggerBindingsNode) {
                return getTriggerBindings((TriggerBindingsNode) element);
            }
            if (element instanceof ClusterTriggerBindingsNode) {
                return getClusterTriggerBindingsNode((ClusterTriggerBindingsNode) element);
            }
            if (element instanceof EventListenersNode) {
                return getEventListenersNode((EventListenersNode) element);
            }
        }
        return new Object[0];
    }

    private void watchNodes(Tkn tkn, Object[] nodes) {
        for (Object node: nodes) {
            if (node instanceof ParentableNode) {
                if (tkn.getWatchHandler().canBeWatched((ParentableNode<?>) node)) {
                    tkn.getWatchHandler().setWatchByNode((ParentableNode<?>) node);
                }
            }
        }
    }

    protected Object[] getEventListenersNode(EventListenersNode element) {
        List<Object> eventListeners = new ArrayList<>();
        try {
            Tkn tkn = element.getRoot().getTkn();
            tkn.getEventListeners(element.getNamespace()).forEach(template -> eventListeners.add(new EventListenerNode(element.getRoot(), element, template)));
        } catch (IOException e) {
            eventListeners.add(new MessageNode(element.getRoot(), element, "Failed to load event listeners"));
        }
        return eventListeners.toArray(new Object[eventListeners.size()]);
    }

    protected Object[] getClusterTriggerBindingsNode(ClusterTriggerBindingsNode element) {
        List<Object> ctbs = new ArrayList<>();
        try {
            Tkn tkn = element.getRoot().getTkn();
            tkn.getClusterTriggerBindings().forEach(template -> ctbs.add(new ClusterTriggerBindingNode(element.getRoot(), element, template)));
        } catch (IOException e) {
            ctbs.add(new MessageNode(element.getRoot(), element, "Failed to load cluster trigger bindings"));
        }
        return ctbs.toArray(new Object[ctbs.size()]);
    }

    protected Object[] getTriggerBindings(TriggerBindingsNode element) {
        List<Object> triggerBindings = new ArrayList<>();
        try {
            Tkn tkn = element.getRoot().getTkn();
            tkn.getTriggerBindings(element.getNamespace()).forEach(template -> triggerBindings.add(new TriggerBindingNode(element.getRoot(), element, template)));
        } catch (IOException e) {
            triggerBindings.add(new MessageNode(element.getRoot(), element, "Failed to load triggerbindings"));
        }
        return triggerBindings.toArray(new Object[triggerBindings.size()]);
    }

    protected Object[] getTriggerTemplates(TriggerTemplatesNode element) {
        List<Object> triggerTemplates = new ArrayList<>();
        try {
            Tkn tkn = element.getRoot().getTkn();
            tkn.getTriggerTemplates(element.getNamespace()).forEach(template -> triggerTemplates.add(new TriggerTemplateNode(element.getRoot(), element, template)));
        } catch (IOException e) {
            triggerTemplates.add(new MessageNode(element.getRoot(), element, "Failed to load triggertemplates"));
        }
        return triggerTemplates.toArray(new Object[triggerTemplates.size()]);
    }

    private Object[] getTaskRuns(ParentableNode element, String task) {
        Object[] taskRunsNodes;
        try {
            Tkn tkn = element.getRoot().getTkn();
            List<TaskRun> taskRuns = tkn.getTaskRuns(element.getNamespace(), task);
            taskRunsNodes = getTaskRuns(element, taskRuns, true);
            watchNodes(tkn, taskRunsNodes);
        } catch (IOException e) {
            taskRunsNodes = new Object[] { new MessageNode(element.getRoot(), element, "Failed to load task runs") };
        }
        return taskRunsNodes;
    }

    private Object[] getTaskRuns(PipelineRunNode element) {
        List<TaskRun> taskRuns = new ArrayList<>();
        PipelineRun run = element.getRun();
        Map<String, PipelineRunTaskRunStatus> pipelineRunTaskRunStatusMap = run.getStatus() != null ?
                run.getStatus().getTaskRuns() != null ?
                    run.getStatus().getTaskRuns() :
                    Collections.emptyMap() :
                Collections.emptyMap();
        pipelineRunTaskRunStatusMap.forEach((nameTaskRun, pipelineRunTaskRunStatus) -> {
            TaskRun taskRun = new TaskRun();
            taskRun.setStatus(pipelineRunTaskRunStatus.getStatus());
            ObjectMeta taskRunMetadata = new ObjectMeta();
            taskRunMetadata.setName(nameTaskRun);
            Map<String, String> labels = new HashMap<>();
            labels.put("tekton.dev/pipeline", run.getMetadata().getLabels().get("tekton.dev/pipeline"));
            labels.put("tekton.dev/pipelineRun", run.getMetadata().getName());
            labels.put("tekton.dev/pipelineTask", pipelineRunTaskRunStatus.getPipelineTaskName());
            taskRunMetadata.setLabels(labels);
            taskRun.setMetadata(taskRunMetadata);
            taskRuns.add(taskRun);
        });
        return getTaskRuns(element, taskRuns, true);
    }

    private Object[] getTaskRuns(ParentableNode element, List<TaskRun> taskRuns, boolean orderNewestToOldest)  {
        List<Object> taskRunsNodes = new ArrayList<>();
        if (taskRuns != null) {
            if (orderNewestToOldest) {
                taskRuns.stream().sorted(Comparator.comparing(TaskRunNode::getStartTime, Comparator.nullsLast(Comparator.reverseOrder())))
                        .forEachOrdered(run -> taskRunsNodes.add(new TaskRunNode(element.getRoot(), (ParentableNode) element, run)));
            } else {
                taskRuns.forEach(run -> taskRunsNodes.add(new TaskRunNode(element.getRoot(), (ParentableNode) element, run)));
            }
        }
        return taskRunsNodes.toArray(new Object[taskRunsNodes.size()]);
    }

    private Object[] getPipelineRuns(ParentableNode element, String pipeline) {
        List<Object> pipelineRuns = new ArrayList<>();
        try {
            Tkn tkn = element.getRoot().getTkn();
            tkn.getPipelineRuns(element.getNamespace(), pipeline)
                    .stream()
                    .sorted(Comparator.comparing(PipelineRunNode::getStartTime, Comparator.nullsLast(Comparator.reverseOrder())))
                    .forEachOrdered(pipelinerun -> pipelineRuns.add(new PipelineRunNode(element.getRoot(), element, pipelinerun)));
            watchNodes(tkn, pipelineRuns.toArray());
        } catch (IOException e) {
            pipelineRuns.add(new MessageNode(element.getRoot(), element, "Failed to load pipeline runs"));
        }
        return pipelineRuns.toArray(new Object[pipelineRuns.size()]);
    }

    protected Object[] getClusterTasks(ClusterTasksNode element) {
        List<Object> tasks = new ArrayList<>();
        try {
            Tkn tkn = element.getRoot().getTkn();
            tkn.getClusterTasks().forEach(clusterTask -> tasks.add(new ClusterTaskNode(element.getRoot(), element, clusterTask.getMetadata().getName())));
            watchNodes(tkn, tasks.toArray());
        } catch (IOException e) {
            tasks.add(new MessageNode(element.getRoot(), element, "Failed to load cluster tasks"));
        }
        return tasks.toArray(new Object[tasks.size()]);
    }

    protected Object[] getPipelines(PipelinesNode element) {
        List<Object> pipelines = new ArrayList<>();
        try {
            Tkn tkn = element.getRoot().getTkn();
            tkn.getPipelines(element.getParent().getName()).forEach(pipeline -> pipelines.add(new PipelineNode(element.getRoot(), element, pipeline.getMetadata().getName())));
            watchNodes(tkn, pipelines.toArray());
        } catch (IOException e) {
            pipelines.add(new MessageNode(element.getRoot(), element, "Failed to load pipelines"));
        }
        return pipelines.toArray(new Object[pipelines.size()]);
    }

    protected Object[] getTasks(TasksNode element) {
        List<Object> tasks = new ArrayList<>();
        try {
            Tkn tkn = element.getRoot().getTkn();
            tkn.getTasks(element.getParent().getName()).forEach(task -> tasks.add(new TaskNode(element.getRoot(), element, task.getMetadata().getName(), false)));
            watchNodes(tkn, tasks.toArray());
        } catch (IOException e) {
            tasks.add(new MessageNode(element.getRoot(), element, "Failed to load tasks"));
        }
        return tasks.toArray(new Object[tasks.size()]);
    }

    private Object[] getConfigurationNodes(ConfigurationsNode element) {
        List<Object> configurations = new ArrayList<>();
        configurations.add(new ConfigurationNode(element.getRoot(), element, "config-defaults", "Defaults"));
        configurations.add(new ConfigurationNode(element.getRoot(), element, "feature-flags", "Features"));
        return configurations.toArray(new Object[configurations.size()]);
    }

    private Object[] getFirstLevelNodes(TektonRootNode element) {
        List<Object> namespaces = new ArrayList<>();
        try {
            Tkn tkn = element.getTkn();
            if (tkn.isTektonAware()) {
                String namespace = element.getTkn().getNamespace();
                namespaces.add(new NamespaceNode(element, namespace));
                namespaces.add(new ConfigurationsNode(element, "Configurations"));
            } else {
                namespaces.add(new MessageNode(element, element, NO_TEKTON));
            }
        } catch (IOException e) {
            namespaces.add(new MessageNode(element, element, ERROR));
        }
        return namespaces.toArray(new Object[namespaces.size()]);
    }

    @Nullable
    @Override
    public Object getParentElement(Object element) {
        if (element instanceof ParentableNode) {
            return ((ParentableNode)element).getParent();
        }
        return null;
    }

    @NotNull
    @Override
    public NodeDescriptor createDescriptor(Object element, NodeDescriptor parentDescriptor) {
        if (element instanceof TektonRootNode) {
            Tkn tkn = ((TektonRootNode)element).getTkn();
            return new LabelAndIconDescriptor(project, element, tkn != null?tkn.getMasterUrl().toString():"Loading", CLUSTER_ICON, parentDescriptor);
        }
        if (element instanceof NamespaceNode) {
            return new LabelAndIconDescriptor(project, element, ((NamespaceNode)element).getName(), NAMESPACE_ICON, parentDescriptor);
        }
        if (element instanceof PipelinesNode) {
            return new LabelAndIconDescriptor(project, element, ((PipelinesNode)element).getName(), PIPELINE_ICON, parentDescriptor);
        }
        if (element instanceof PipelineNode) {
            return new LabelAndIconDescriptor(project, element, ((PipelineNode)element).getName(), PIPELINE_ICON, parentDescriptor);
        }
        if (element instanceof PipelineRunNode) {
            return new LabelAndIconDescriptor(project, element, ((PipelineRunNode)element).getName(), ((PipelineRunNode)element).getInfoText(), getIcon((PipelineRunNode) element), parentDescriptor);
        }
        if (element instanceof PipelineRunsNode) {
            return new LabelAndIconDescriptor(project, element, ((PipelineRunsNode)element).getName(), PIPELINE_RUN_ICON, parentDescriptor);
        }
        if (element instanceof TasksNode) {
            return new LabelAndIconDescriptor(project, element, ((TasksNode)element).getName(), TASK_ICON, parentDescriptor);
        }
        if (element instanceof TaskNode) {
            return new LabelAndIconDescriptor(project, element, ((TaskNode)element).getName(), TASK_ICON, parentDescriptor);
        }
        if (element instanceof TaskRunNode) {
            return new LabelAndIconDescriptor(project, element, ((TaskRunNode)element).getDisplayName(), ((TaskRunNode)element).getInfoText(), getIcon((TaskRunNode) element), parentDescriptor);
        }
        if (element instanceof TaskRunsNode) {
            return new LabelAndIconDescriptor(project, element, ((TaskRunsNode) element).getName(), TASK_RUN_ICON, parentDescriptor);
        }
        if (element instanceof ClusterTasksNode) {
            return new LabelAndIconDescriptor(project, element, ((ClusterTasksNode)element).getName(), CLUSTER_TASK_ICON, parentDescriptor);
        }
        if (element instanceof ClusterTaskNode) {
            return new LabelAndIconDescriptor(project, element, ((ClusterTaskNode)element).getName(), CLUSTER_TASK_ICON, parentDescriptor);
        }
        if (element instanceof TriggerTemplatesNode) {
            return new LabelAndIconDescriptor(project, element, ((TriggerTemplatesNode)element).getName(), TRIGGER_TEMPLATE_ICON, parentDescriptor);
        }
        if (element instanceof TriggerTemplateNode) {
            return new LabelAndIconDescriptor(project, element, ((TriggerTemplateNode)element).getName(), TRIGGER_TEMPLATE_ICON, parentDescriptor);
        }
        if (element instanceof TriggerBindingsNode) {
            return new LabelAndIconDescriptor(project, element, ((TriggerBindingsNode)element).getName(), TRIGGER_BINDING_ICON, parentDescriptor);
        }
        if (element instanceof TriggerBindingNode) {
            return new LabelAndIconDescriptor(project, element, ((TriggerBindingNode)element).getName(), TRIGGER_BINDING_ICON, parentDescriptor);
        }
        if (element instanceof ClusterTriggerBindingsNode) {
            return new LabelAndIconDescriptor(project, element, ((ClusterTriggerBindingsNode)element).getName(), CLUSTER_TRIGGER_BINDING_ICON, parentDescriptor);
        }
        if (element instanceof ClusterTriggerBindingNode) {
            return new LabelAndIconDescriptor(project, element, ((ClusterTriggerBindingNode)element).getName(), CLUSTER_TRIGGER_BINDING_ICON, parentDescriptor);
        }
        if (element instanceof EventListenersNode) {
            return new LabelAndIconDescriptor(project, element, ((EventListenersNode)element).getName(), EVENT_LISTENER_ICON, parentDescriptor);
        }
        if (element instanceof EventListenerNode) {
            return new LabelAndIconDescriptor(project, element, ((EventListenerNode)element).getName(), EVENT_LISTENER_ICON, parentDescriptor);
        }
        if (element instanceof ConfigurationsNode) {
            return new LabelAndIconDescriptor(project, element, ((ConfigurationsNode)element).getName(), AllIcons.General.ExternalTools, parentDescriptor);
        }
        if (element instanceof ConfigurationNode) {
            return new LabelAndIconDescriptor(project, element, ((ConfigurationNode)element).getDisplayName(), AllIcons.General.Settings, parentDescriptor);
        }
        if (element instanceof MessageNode) {
            return new LabelAndIconDescriptor(project, element, ((MessageNode)element).getName(), AllIcons.General.Warning, parentDescriptor);
        }
        return null;
    }

    private Icon getIcon(RunNode run) {
        return run.isCompleted().isPresent()?
                ((boolean)run.isCompleted().get()) ? SUCCESS_ICON:FAILED_ICON
                :RUNNING_ICON;
    }

    @Override
    public void commit() {

    }

    @Override
    public boolean hasSomethingToCommit() {
        return false;
    }

    @Override
    public void fireAdded(Object element) {
        mutableModelSupport.fireAdded(element);
    }

    @Override
    public void fireModified(Object element) {
        mutableModelSupport.fireModified(element);
    }

    @Override
    public void fireRemoved(Object element) {
        mutableModelSupport.fireRemoved(element);
    }

    @Override
    public void addListener(Listener<Object> listener) {
        mutableModelSupport.addListener(listener);
    }

    @Override
    public void removeListener(Listener<Object> listener) {
        mutableModelSupport.removeListener(listener);
    }

    @Override
    public void onUpdate(ConfigWatcher source, Config config) {
        if (!ConfigHelper.areEqualCurrentContext(this.config, config)) {
            refresh();
        }
        this.config = config;
    }

    protected void refresh() {
        try {
            dispose();
            root.initializeTkn().whenComplete((tkn, err) ->
                    mutableModelSupport.fireModified(root)
            );
        } catch (Exception e) {
        }
    }

    public void dispose() {
        root.getTkn().dispose();
    }
}
