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
import com.redhat.devtools.intellij.tektoncd.tkn.Tkn;
import io.fabric8.kubernetes.api.model.Config;
import io.fabric8.kubernetes.api.model.Context;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.internal.KubeConfigUtils;
import org.apache.commons.codec.binary.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

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
        return root;
    }

    @Override
    public Object[] getChildElements(Object element) {
        if (element instanceof TektonRootNode) {
            return getNamespaces((TektonRootNode) element);
        }
        if (element instanceof NamespaceNode) {
            return new Object[]{
                    new PipelinesNode(((NamespaceNode) element).getRoot(), (NamespaceNode) element),
                    new TasksNode(((NamespaceNode) element).getRoot(), (NamespaceNode) element),
                    new ClusterTasksNode(((NamespaceNode) element).getRoot(), (NamespaceNode) element),
                    new ResourcesNode(((NamespaceNode) element).getRoot(), (NamespaceNode) element)};
        }
        if (element instanceof PipelinesNode) {
            return getPipelines((PipelinesNode) element);
        }
        if (element instanceof PipelineNode) {
            return getPipelineRuns((PipelineNode) element);
        }
        if (element instanceof PipelineRunNode) {
            return ((PipelineRunNode)element).getRun().getTaskRuns().stream().map(run -> new TaskRunNode(((PipelineRunNode) element).getRoot(), (ParentableNode<Object>) element, run)).toArray();
        }

        if (element instanceof TasksNode) {
            return getTasks((TasksNode) element);
        }
        if (element instanceof TaskNode) {
            return getTaskRuns((TaskNode) element);
        }
        if (element instanceof ClusterTasksNode) {
            return getClusterTasks((ClusterTasksNode) element);
        }
        if (element instanceof ResourcesNode) {
            return getResources((ResourcesNode) element);
        }
        return new Object[0];
    }

    private Object[] getTaskRuns(TaskNode element) {
        List<Object> taskruns = new ArrayList<>();
        try {
            Tkn tkn = element.getRoot().getTkn();
            tkn.getTaskRuns(element.getParent().getParent().getName(), element.getName()).forEach(run -> taskruns.add(new TaskRunNode(element.getRoot(), (ParentableNode) element, run)));
        } catch (IOException e) {
            taskruns.add(new MessageNode(element.getRoot(), element, "Failed to load task runs"));
        }
        return taskruns.toArray(new Object[taskruns.size()]);

    }

    private Object[] getPipelineRuns(PipelineNode element) {
        List<Object> pipelineRuns = new ArrayList<>();
        try {
            Tkn tkn = element.getRoot().getTkn();
            tkn.getPipelineRuns(element.getParent().getParent().getName(), element.getName()).forEach(pipelinerun -> pipelineRuns.add(new PipelineRunNode(element.getRoot(), element, pipelinerun)));
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
            return new LabelAndIconDescriptor(project, element, ((PipelineRunNode)element).getName(), ((PipelineRunNode)element).getTimeInfoText(), getIcon(((PipelineRunNode)element).getRun()), parentDescriptor);
        }
        if (element instanceof TasksNode) {
            return new LabelAndIconDescriptor(project, element, ((TasksNode)element).getName(), TASK_ICON, parentDescriptor);
        }
        if (element instanceof TaskNode) {
            return new LabelAndIconDescriptor(project, element, ((TaskNode)element).getName(), TASK_ICON, parentDescriptor);
        }
        if (element instanceof TaskRunNode) {
            return new LabelAndIconDescriptor(project, element, ((TaskRunNode)element).getDisplayName(), ((TaskRunNode)element).getTimeInfoText(), getIcon(((TaskRunNode)element).getRun()), parentDescriptor);
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
        if (element instanceof MessageNode) {
            return new LabelAndIconDescriptor(project, element, ((MessageNode)element).getName(), AllIcons.Ide.Warning_notifications, parentDescriptor);
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
