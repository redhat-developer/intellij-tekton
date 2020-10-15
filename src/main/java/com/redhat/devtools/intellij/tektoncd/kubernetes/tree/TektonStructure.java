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
package com.redhat.devtools.intellij.tektoncd.kubernetes.tree;


import com.intellij.ide.util.treeView.NodeDescriptor;
import com.intellij.openapi.util.IconLoader;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.tekton.pipeline.v1beta1.ClusterTask;
import io.fabric8.tekton.pipeline.v1beta1.Pipeline;
import io.fabric8.tekton.pipeline.v1beta1.PipelineRun;
import io.fabric8.tekton.pipeline.v1beta1.Task;
import io.fabric8.tekton.pipeline.v1beta1.TaskRun;
import io.fabric8.tekton.resource.v1alpha1.PipelineResource;
import org.jboss.tools.intellij.kubernetes.model.IResourceModel;
import org.jboss.tools.intellij.kubernetes.model.resource.ResourceKind;
import org.jboss.tools.intellij.kubernetes.tree.AbstractTreeStructureContribution;
import org.jboss.tools.intellij.kubernetes.tree.ITreeStructureContribution;
import org.jboss.tools.intellij.kubernetes.tree.ITreeStructureContributionFactory;
import org.jboss.tools.intellij.kubernetes.tree.TreeStructure;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

import static org.jboss.tools.intellij.kubernetes.tree.TreeStructure.Folder;
import static org.jboss.tools.intellij.kubernetes.tree.TreeStructure.ResourceDescriptor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

public class TektonStructure extends AbstractTreeStructureContribution {

	public static class Factory implements ITreeStructureContributionFactory {

		@NotNull
		@Override
		public ITreeStructureContribution create(@NotNull IResourceModel model) {
			return new TektonStructure(model);
		}
	}

	public static final Folder TEKTON = new TreeStructure.Folder("Tekton Pipelines", null);
	public static final Folder PIPELINES = new TreeStructure.Folder("Pipelines", ResourceKind.create(Pipeline.class));
	public static final Folder PIPELINE_RUNS = new TreeStructure.Folder("Pipeline Runs", ResourceKind.create(PipelineRun.class));
	public static final Folder PIPELINE_RESOURCES = new TreeStructure.Folder("Pipeline Resources", ResourceKind.create(PipelineResource.class));
	public static final Folder TASKS = new TreeStructure.Folder("Tasks", ResourceKind.create(Task.class));
	public static final Folder TASK_RUNS = new TreeStructure.Folder("Task Runs", ResourceKind.create(TaskRun.class));
	public static final Folder CLUSTER_TASKS = new TreeStructure.Folder("Cluster Tasks", ResourceKind.create(ClusterTask.class));

	public TektonStructure(@NotNull IResourceModel model) {
		super(model);
	}

	@Override
	public boolean canContribute() {
		return true;
	}

	@NotNull
	@Override
	public Collection<Object> getChildElements(@NotNull Object element) {
		if (getRootElement().equals(element)) {
			return Collections.singletonList(TEKTON);
		} else if (TEKTON.equals(element)) {
			return Arrays.asList(
					PIPELINES,
					PIPELINE_RUNS,
					PIPELINE_RESOURCES,
					TASKS,
					TASK_RUNS,
					CLUSTER_TASKS);
		} else if (PIPELINES.equals(element)
				|| PIPELINE_RUNS.equals(element)
				|| PIPELINE_RESOURCES.equals(element)
				|| TASKS.equals(element)
				|| TASK_RUNS.equals(element)
				|| CLUSTER_TASKS.equals(element)) {
			ResourceKind<? extends HasMetadata> kind = ((Folder) element).getKind();
			if (kind != null) {
				return new ArrayList<>(getModel().resources(kind).inCurrentNamespace().list());
			}
		}
		return Collections.emptyList();
	}

	@Nullable
	@Override
	public Object getParentElement(@NotNull Object element) {
		if (TEKTON.equals(element)) {
			return getRootElement();
		} else if (PIPELINES.equals(element)
				|| PIPELINE_RUNS.equals(element)
				|| PIPELINE_RESOURCES.equals(element)
				|| TASKS.equals(element)
				|| TASK_RUNS.equals(element)
				|| CLUSTER_TASKS.equals(element)) {
			return TEKTON;
		} else {
			return getRootElement();
		}
	}

	@Nullable
	@Override
	public NodeDescriptor<?> createDescriptor(@NotNull Object element, @Nullable NodeDescriptor<?> parent) {
		if (element instanceof Pipeline) {
			return new PipelineDescriptor((Pipeline) element, parent, getModel());
		} else if (element instanceof PipelineResource) {
			return new PipelineResourceDescriptor((PipelineResource) element, parent, getModel());
		} else if (element instanceof PipelineRun) {
			return new PipelineRunDescriptor((PipelineRun) element, parent, getModel());
		} else if (element instanceof Task) {
			return new TaskDescriptor((Task) element, parent, getModel());
		} else if (element instanceof TaskRun) {
			return new TaskRunDescriptor((TaskRun) element, parent, getModel());
		} else if (element instanceof ClusterTask) {
			return new ClusterTaskDescriptor((ClusterTask) element, parent, getModel());
		} else {
			return null;
		}
	}

	private class PipelineDescriptor extends ResourceDescriptor<Pipeline> {

		private PipelineDescriptor(Pipeline element, NodeDescriptor<?> parent, IResourceModel model) {
			super(element, parent, model);
		}

		@Nullable
		@Override
		protected String getLabel(Pipeline pipeline) {
			return pipeline.getMetadata().getName();
		}

		@Nullable
		@Override
		protected Icon getIcon(Pipeline pipeline) {
			return IconLoader.getIcon("/images/pipeline.png");
		}
	}

	private class PipelineResourceDescriptor extends ResourceDescriptor<PipelineResource> {

		private PipelineResourceDescriptor(PipelineResource element, NodeDescriptor<?> parent, IResourceModel model) {
			super(element, parent, model);
		}

		@Nullable
		@Override
		protected Icon getIcon(PipelineResource element) {
			return IconLoader.getIcon("/images/pipeline.png");
		}

		@Nullable
		@Override
		protected String getLabel(PipelineResource element) {
			return element.getMetadata().getName();
		}
	}

	private class PipelineRunDescriptor extends ResourceDescriptor<PipelineRun> {

		private PipelineRunDescriptor(PipelineRun element, NodeDescriptor<?> parent, IResourceModel model) {
			super(element, parent, model);
		}

		@Nullable
		@Override
		protected Icon getIcon(PipelineRun element) {
			return IconLoader.getIcon("/images/pipeline.png");
		}
	}

	private class TaskDescriptor extends ResourceDescriptor<Task> {

		private TaskDescriptor(Task element, NodeDescriptor<?> parent, IResourceModel model) {
			super(element, parent, model);
		}

		@Nullable
		@Override
		protected Icon getIcon(Task element) {
			return IconLoader.getIcon("/images/task.png");
		}
	}

	private class TaskRunDescriptor extends ResourceDescriptor<TaskRun> {

		private TaskRunDescriptor(TaskRun element, NodeDescriptor<?> parent, IResourceModel model) {
			super(element, parent, model);
		}

		@Nullable
		@Override
		protected Icon getIcon(TaskRun element) {
			return IconLoader.getIcon("/images/task.png");
		}
	}

	private class ClusterTaskDescriptor extends ResourceDescriptor<ClusterTask> {

		private ClusterTaskDescriptor(ClusterTask element, NodeDescriptor<?> parent, IResourceModel model) {
			super(element, parent, model);
		}

		@Nullable
		@Override
		protected Icon getIcon(ClusterTask element) {
			return IconLoader.getIcon("/images/clustertask.png");
		}
	}
}
