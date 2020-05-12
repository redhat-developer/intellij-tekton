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
import com.redhat.devtools.intellij.common.tree.LabelAndIconDescriptor;
import com.redhat.devtools.intellij.common.tree.MutableModel;
import com.redhat.devtools.intellij.common.tree.MutableModelSupport;
import com.redhat.devtools.intellij.common.utils.ConfigHelper;
import com.redhat.devtools.intellij.common.utils.ConfigWatcher;
import com.redhat.devtools.intellij.common.utils.ExecHelper;
import com.redhat.devtools.intellij.tektoncd.tkn.Run;
import com.redhat.devtools.intellij.tektoncd.tkn.TaskRun;
import com.redhat.devtools.intellij.tektoncd.tkn.Tkn;
import io.fabric8.kubernetes.api.model.Config;
import io.fabric8.kubernetes.api.model.Context;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.internal.KubeConfigUtils;
import org.apache.commons.codec.binary.StringUtils;
import org.apache.commons.lang.ArrayUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class TektonTreeStructure extends AbstractTreeStructure implements MutableModel<Object>, ConfigWatcher.Listener {
    private final Project project;
    private Config config;
    private TektonRootNode root;

    private static final String ERROR = "Please log in to the cluster";

    private static final String NO_TEKTON = "Tekton not installed on the cluster";

    private static final Icon CLUSTER_ICON = IconLoader.findIcon("/images/cluster.png", TektonTreeStructure.class);

    private static final Icon NAMESPACE_ICON = IconLoader.findIcon("/images/project.png", TektonTreeStructure.class);

    private static final Icon PIPELINE_ICON = IconLoader.findIcon("/images/pipeline.png", TektonTreeStructure.class);

    private static final Icon TASK_ICON = IconLoader.findIcon("/images/task.png", TektonTreeStructure.class);

    private static final Icon CLUSTER_TASK_ICON = IconLoader.findIcon("/images/clustertask.png", TektonTreeStructure.class);

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
                return getNamespaces((TektonRootNode) element);
            }
            if (element instanceof NamespaceNode) {
                Object[] generalNodes = new Object[]{
                        new PipelinesNode(((NamespaceNode) element).getRoot(), (NamespaceNode) element),
                        new PipelineRunsNode(((NamespaceNode) element).getRoot(), (NamespaceNode) element),
                        new TasksNode(((NamespaceNode) element).getRoot(), (NamespaceNode) element),
                        new ClusterTasksNode(((NamespaceNode) element).getRoot(), (NamespaceNode) element),
                        new TaskRunsNode(((NamespaceNode) element).getRoot(), (NamespaceNode) element),
                        new ResourcesNode(((NamespaceNode) element).getRoot(), (NamespaceNode) element),
                        new ConditionsNode(((NamespaceNode) element).getRoot(), (NamespaceNode) element)
                };
                if (!tkn.isTektonTriggersAware(root.getClient())) {
                    return generalNodes;
                }
                Object[] triggersNode = new Object[] {
                        new TriggerTemplatesNode(((NamespaceNode) element).getRoot(), (NamespaceNode) element),
                        new TriggerBindingsNode(((NamespaceNode) element).getRoot(), (NamespaceNode) element),
                        new ClusterTriggerBindingsNode(((NamespaceNode) element).getRoot(), (NamespaceNode) element),
                        new EventListenersNode(((NamespaceNode) element).getRoot(), (NamespaceNode) element)
                };
                return ArrayUtils.addAll(generalNodes, triggersNode);

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
                return getTaskRuns((PipelineRunNode)element, ((PipelineRunNode) element).getRun().getChildren());
            }
            if (element instanceof TasksNode) {
                return getTasks((TasksNode) element);
            }
            if (element instanceof TaskNode) {
                return getTaskRuns((TaskNode) element, ((TaskNode) element).getName());
            }
            if (element instanceof TaskRunNode) {
                return getTaskRuns((TaskRunNode)element, ((TaskRunNode) element).getRun().getChildren());
            }
            if (element instanceof ClusterTasksNode) {
                return getClusterTasks((ClusterTasksNode) element);
            }
            if (element instanceof TaskRunsNode) {
                return getTaskRuns((TaskRunsNode) element, "");
            }
            if (element instanceof ResourcesNode) {
                return getResources((ResourcesNode) element);
            }
            if (element instanceof ConditionsNode) {
                return getConditions((ConditionsNode)element);
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

    private Object[] getConditions(ConditionsNode element) {
        List<Object> conditions = new ArrayList<>();
        try {
            Tkn tkn = element.getRoot().getTkn();
            tkn.getConditions(element.getParent().getName()).forEach(condition -> conditions.add(new ConditionNode(element.getRoot(), element, condition)));
        } catch (IOException e) {
            conditions.add(new MessageNode(element.getRoot(), element, "Failed to load conditions"));
        }
        return conditions.toArray(new Object[conditions.size()]);
    }

    private Object[] getEventListenersNode(EventListenersNode element) {
        List<Object> eventListeners = new ArrayList<>();
        try {
            Tkn tkn = element.getRoot().getTkn();
            tkn.getEventListeners(element.getNamespace()).forEach(template -> eventListeners.add(new EventListenerNode(element.getRoot(), element, template)));
        } catch (IOException e) {
            eventListeners.add(new MessageNode(element.getRoot(), element, "Failed to load event listeners"));
        }
        return eventListeners.toArray(new Object[eventListeners.size()]);
    }

    private Object[] getClusterTriggerBindingsNode(ClusterTriggerBindingsNode element) {
        List<Object> ctbs = new ArrayList<>();
        try {
            Tkn tkn = element.getRoot().getTkn();
            tkn.getClusterTriggerBindings(element.getNamespace()).forEach(template -> ctbs.add(new ClusterTriggerBindingNode(element.getRoot(), element, template)));
        } catch (IOException e) {
            ctbs.add(new MessageNode(element.getRoot(), element, "Failed to load cluster trigger bindings"));
        }
        return ctbs.toArray(new Object[ctbs.size()]);
    }

    private Object[] getTriggerBindings(TriggerBindingsNode element) {
        List<Object> triggerBindings = new ArrayList<>();
        try {
            Tkn tkn = element.getRoot().getTkn();
            tkn.getTriggerBindings(element.getNamespace()).forEach(template -> triggerBindings.add(new TriggerBindingNode(element.getRoot(), element, template)));
        } catch (IOException e) {
            triggerBindings.add(new MessageNode(element.getRoot(), element, "Failed to load triggerbindings"));
        }
        return triggerBindings.toArray(new Object[triggerBindings.size()]);
    }

    private Object[] getTriggerTemplates(TriggerTemplatesNode element) {
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
            taskRunsNodes = getTaskRuns(element, taskRuns);
        } catch (IOException e) {
            taskRunsNodes = new Object[] { new MessageNode(element.getRoot(), element, "Failed to load task runs") };
        }
        return taskRunsNodes;
    }

    private Object[] getTaskRuns(ParentableNode element, List<TaskRun> taskRuns)  {
        List<Object> taskRunsNodes = new ArrayList<>();
        if (taskRuns != null) {
            taskRuns.forEach(run -> taskRunsNodes.add(new TaskRunNode(element.getRoot(), (ParentableNode) element, run)));
        }
        return taskRunsNodes.toArray(new Object[taskRunsNodes.size()]);
    }

    private Object[] getPipelineRuns(ParentableNode element, String pipeline) {
        List<Object> pipelineRuns = new ArrayList<>();
        try {
            Tkn tkn = element.getRoot().getTkn();
            tkn.getPipelineRuns(element.getNamespace(), pipeline).forEach(pipelinerun -> pipelineRuns.add(new PipelineRunNode(element.getRoot(), element, pipelinerun)));
        } catch (IOException e) {
            pipelineRuns.add(new MessageNode(element.getRoot(), element, "Failed to load pipeline runs"));
        }
        return pipelineRuns.toArray(new Object[pipelineRuns.size()]);
    }

    private Object[] getResources(ResourcesNode element) {
        List<Object> resources = new ArrayList<>();
        try {
            Tkn tkn = element.getRoot().getTkn();
            tkn.getResources(element.getParent().getName()).forEach(resource -> resources.add(new ResourceNode(element.getRoot(), element, resource.name())));
        } catch (IOException e) {
            resources.add(new MessageNode(element.getRoot(), element, "Failed to load resources"));
        }
        return resources.toArray(new Object[resources.size()]);
    }

    private Object[] getClusterTasks(ClusterTasksNode element) {
        List<Object> tasks = new ArrayList<>();
        try {
            Tkn tkn = element.getRoot().getTkn();
            tkn.getClusterTasks(element.getParent().getName()).forEach(name -> tasks.add(new TaskNode(element.getRoot(), element, name, true)));
        } catch (IOException e) {
            tasks.add(new MessageNode(element.getRoot(), element, "Failed to load cluster tasks"));
        }
        return tasks.toArray(new Object[tasks.size()]);
    }

    private Object[] getPipelines(PipelinesNode element) {
        List<Object> pipelines = new ArrayList<>();
        try {
            Tkn tkn = element.getRoot().getTkn();
            tkn.getPipelines(element.getParent().getName()).forEach(name -> pipelines.add(new PipelineNode(element.getRoot(), element, name)));
        } catch (IOException e) {
            pipelines.add(new MessageNode(element.getRoot(), element, "Failed to load pipelines"));
        }
        return pipelines.toArray(new Object[pipelines.size()]);
    }

    private Object[] getTasks(TasksNode element) {
        List<Object> tasks = new ArrayList<>();
        try {
            Tkn tkn = element.getRoot().getTkn();
            tkn.getTasks(element.getParent().getName()).forEach(name -> tasks.add(new TaskNode(element.getRoot(), element, name, false)));
        } catch (IOException e) {
            tasks.add(new MessageNode(element.getRoot(), element, "Failed to load tasks"));
        }
        return tasks.toArray(new Object[tasks.size()]);
    }

    private Object[] getNamespaces(TektonRootNode element) {
        List<Object> namespaces = new ArrayList<>();
        try {
            Tkn tkn = element.getTkn();
            KubernetesClient client = element.getClient();
            if (tkn.isTektonAware(client)) {
                element.getTkn().getNamespaces(element.getClient()).forEach(name -> namespaces.add(new NamespaceNode(element, name)));
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
            return new LabelAndIconDescriptor(project, element, ((TektonRootNode)element).getClient().getMasterUrl().toString(), CLUSTER_ICON, parentDescriptor);
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
            return new LabelAndIconDescriptor(project, element, ((PipelineRunNode)element).getName(), ((PipelineRunNode)element).getInfoText(), getIcon(((PipelineRunNode)element).getRun()), parentDescriptor);
        }
        if (element instanceof PipelineRunsNode) {
            return new LabelAndIconDescriptor(project, element, ((PipelineRunsNode)element).getName(), PIPELINE_ICON, parentDescriptor);
        }
        if (element instanceof TasksNode) {
            return new LabelAndIconDescriptor(project, element, ((TasksNode)element).getName(), TASK_ICON, parentDescriptor);
        }
        if (element instanceof TaskNode) {
            return new LabelAndIconDescriptor(project, element, ((TaskNode)element).getName(), TASK_ICON, parentDescriptor);
        }
        if (element instanceof TaskRunNode) {
            return new LabelAndIconDescriptor(project, element, ((TaskRunNode)element).getDisplayName(), ((TaskRunNode)element).getInfoText(), getIcon(((TaskRunNode)element).getRun()), parentDescriptor);
        }
        if (element instanceof TaskRunsNode) {
            return new LabelAndIconDescriptor(project, element, ((TaskRunsNode) element).getName(), TASK_ICON, parentDescriptor);
        }
        if (element instanceof ClusterTasksNode) {
            return new LabelAndIconDescriptor(project, element, ((ClusterTasksNode)element).getName(), CLUSTER_TASK_ICON, parentDescriptor);
        }
        if (element instanceof ResourcesNode) {
            return new LabelAndIconDescriptor(project, element, ((ResourcesNode)element).getName(), PIPELINE_ICON, parentDescriptor);
        }
        if (element instanceof ResourceNode) {
            return new LabelAndIconDescriptor(project, element, ((ResourceNode)element).getName(), PIPELINE_ICON, parentDescriptor);
        }
        if (element instanceof ConditionsNode) {
            return new LabelAndIconDescriptor(project, element, ((ConditionsNode)element).getName(), PIPELINE_ICON, parentDescriptor);
        }
        if (element instanceof ConditionNode) {
            return new LabelAndIconDescriptor(project, element, ((ConditionNode)element).getName(), PIPELINE_ICON, parentDescriptor);
        }
        if (element instanceof TriggerTemplatesNode) {
            return new LabelAndIconDescriptor(project, element, ((TriggerTemplatesNode)element).getName(), PIPELINE_ICON, parentDescriptor);
        }
        if (element instanceof TriggerTemplateNode) {
            return new LabelAndIconDescriptor(project, element, ((TriggerTemplateNode)element).getName(), PIPELINE_ICON, parentDescriptor);
        }
        if (element instanceof TriggerBindingsNode) {
            return new LabelAndIconDescriptor(project, element, ((TriggerBindingsNode)element).getName(), PIPELINE_ICON, parentDescriptor);
        }
        if (element instanceof TriggerBindingNode) {
            return new LabelAndIconDescriptor(project, element, ((TriggerBindingNode)element).getName(), PIPELINE_ICON, parentDescriptor);
        }
        if (element instanceof ClusterTriggerBindingsNode) {
            return new LabelAndIconDescriptor(project, element, ((ClusterTriggerBindingsNode)element).getName(), PIPELINE_ICON, parentDescriptor);
        }
        if (element instanceof ClusterTriggerBindingNode) {
            return new LabelAndIconDescriptor(project, element, ((ClusterTriggerBindingNode)element).getName(), PIPELINE_ICON, parentDescriptor);
        }
        if (element instanceof EventListenersNode) {
            return new LabelAndIconDescriptor(project, element, ((EventListenersNode)element).getName(), PIPELINE_ICON, parentDescriptor);
        }
        if (element instanceof EventListenerNode) {
            return new LabelAndIconDescriptor(project, element, ((EventListenerNode)element).getName(), PIPELINE_ICON, parentDescriptor);
        }
        if (element instanceof MessageNode) {
            return new LabelAndIconDescriptor(project, element, ((MessageNode)element).getName(), AllIcons.General.Warning, parentDescriptor);
        }
        return null;
    }

    private Icon getIcon(Run run) {
        return run.isCompleted().isPresent()?run.isCompleted().get()?SUCCESS_ICON:FAILED_ICON:RUNNING_ICON;
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
        if (hasContextChanged(config, this.config)) {
            refresh();
        }
        this.config = config;
    }

    private boolean hasContextChanged(Config newConfig, Config currentConfig) {
        Context currentContext = KubeConfigUtils.getCurrentContext(currentConfig);
        Context newContext = KubeConfigUtils.getCurrentContext(currentConfig);
        return hasServerChanged(newContext, currentContext)
                || hasNewToken(newContext, newConfig, currentContext, currentConfig);
    }

    private boolean hasServerChanged(Context newContext, Context currentContext) {
        return newContext == null
                || currentContext == null
                || !StringUtils.equals(currentContext.getCluster(), newContext.getCluster())
                || !StringUtils.equals(currentContext.getUser(), newContext.getUser());
    }

    private boolean hasNewToken(Context newContext, Config newConfig, Context currentContext, Config currentConfig) {
        if (newContext == null) {
            return false;
        }
        if (currentContext == null) {
            return true;
        }
        String newToken = KubeConfigUtils.getUserToken(newConfig, newContext);
        if (newToken == null) {
            // logout, do not refresh, LogoutAction already refreshes
            return false;
        }
        String currentToken = KubeConfigUtils.getUserToken(currentConfig, currentContext);
        return !StringUtils.equals(newToken, currentToken);
    }

    protected void refresh() {
        try {
            root.load();
            mutableModelSupport.fireModified(root);
        } catch (Exception e) {
        }
    }
}
