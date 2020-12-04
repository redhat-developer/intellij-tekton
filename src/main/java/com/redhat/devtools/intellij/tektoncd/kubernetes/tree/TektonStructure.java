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
import io.fabric8.tekton.pipeline.v1alpha1.Condition;
import io.fabric8.tekton.pipeline.v1beta1.ClusterTask;
import io.fabric8.tekton.pipeline.v1beta1.Pipeline;
import io.fabric8.tekton.pipeline.v1beta1.PipelineRun;
import io.fabric8.tekton.pipeline.v1beta1.Task;
import io.fabric8.tekton.pipeline.v1beta1.TaskRun;
import io.fabric8.tekton.resource.v1alpha1.PipelineResource;
import io.fabric8.tekton.triggers.v1alpha1.EventListener;
import io.fabric8.tekton.triggers.v1alpha1.TriggerBinding;
import io.fabric8.tekton.triggers.v1alpha1.TriggerTemplate;
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
import java.util.function.Function;
import java.util.function.Supplier;

public class TektonStructure extends AbstractTreeStructureContribution {

	public static class Factory implements ITreeStructureContributionFactory {

		@NotNull
		@Override
		public ITreeStructureContribution create(@NotNull IResourceModel model) {
			return new TektonStructure(model);
		}
	}

	public static final Folder TEKTON_PIPELINES = new TreeStructure.Folder("Tekton Pipelines", null);

	public static final Folder CLUSTER_TASKS = new TreeStructure.Folder("ClusterTasks", ResourceKind.create(ClusterTask.class));
	public static final Folder TASKS = new TreeStructure.Folder("Tasks", ResourceKind.create(Task.class));
	public static final Folder TASK_RUNS = new TreeStructure.Folder("TaskRuns", ResourceKind.create(TaskRun.class));
	public static final Folder PIPELINES = new TreeStructure.Folder("Pipelines", ResourceKind.create(Pipeline.class));
	public static final Folder PIPELINE_RUNS = new TreeStructure.Folder("PipelineRuns", ResourceKind.create(PipelineRun.class));
	public static final Folder PIPELINE_RESOURCES = new TreeStructure.Folder("PipelineResources", ResourceKind.create(PipelineResource.class));
	public static final Folder TRIGGER_TEMPLATES = new TreeStructure.Folder("TriggerTemplates", ResourceKind.create(TriggerTemplate.class));
	public static final Folder TRIGGER_BINDINGS = new TreeStructure.Folder("TriggerBindings", ResourceKind.create(TriggerBinding.class));
	public static final Folder EVENT_LISTENER = new TreeStructure.Folder("EventListener", ResourceKind.create(EventListener.class));
	public static final Folder CONDITIONS = new TreeStructure.Folder("Conditions", ResourceKind.create(Condition.class));

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
			return Collections.singletonList(TEKTON_PIPELINES);
		} else if (TEKTON_PIPELINES.equals(element)) {
			return Arrays.asList(
					CLUSTER_TASKS,
					TASKS,
					TASK_RUNS,
					PIPELINES,
					PIPELINE_RUNS,
					PIPELINE_RESOURCES,
					TRIGGER_TEMPLATES,
					TRIGGER_BINDINGS,
					EVENT_LISTENER,
					CONDITIONS);
		} else if (CLUSTER_TASKS.equals(element)
				|| PIPELINE_RESOURCES.equals(element)
				|| TRIGGER_TEMPLATES.equals(element)
				|| TRIGGER_BINDINGS.equals(element)
				|| EVENT_LISTENER.equals(element)
				|| CONDITIONS.equals(element)) {
			return getResources((Folder) element,
					kind -> getModel().resources(kind).inNoNamespace().list());
		} else if (TASKS.equals(element)
				|| TASK_RUNS.equals(element)
				|| PIPELINES.equals(element)
				|| PIPELINE_RUNS.equals(element)) {
			return getResources((Folder) element,
					kind -> getModel().resources(kind).inCurrentNamespace().list());
		}
		return Collections.emptyList();
	}

	@Nullable
	private Collection<Object> getResources(
			@NotNull Folder element,
			@NotNull Function<ResourceKind<? extends HasMetadata>, Collection<? extends HasMetadata>> supplier) {
		Collection<Object> resources = new ArrayList<>();
		ResourceKind<? extends HasMetadata> kind = element.getKind();
		if (kind != null) {
			resources.addAll(supplier.apply(kind));
		}
		return resources;
	}

	@Nullable
	@Override
	public Object getParentElement(@NotNull Object element) {
		if (TEKTON_PIPELINES.equals(element)) {
			return getRootElement();
		} else if (CLUSTER_TASKS.equals(element)
				|| TASKS.equals(element)
				|| TASK_RUNS.equals(element)
				|| PIPELINES.equals(element)
				|| PIPELINE_RUNS.equals(element)
				|| PIPELINE_RESOURCES.equals(element)
				|| TRIGGER_TEMPLATES.equals(element)
				|| TRIGGER_BINDINGS.equals(element)
				|| EVENT_LISTENER.equals(element)
				|| CONDITIONS.equals(element)) {
			return TEKTON_PIPELINES;
		} else {
			return getRootElement();
		}
	}

	@Nullable
	@Override
	public NodeDescriptor<?> createDescriptor(@NotNull Object element, @Nullable NodeDescriptor<?> parent) {
		if (element instanceof ClusterTask) {
			return new ClusterTaskDescriptor((ClusterTask) element, parent, getModel());
		} else if (element instanceof Task) {
			return new TaskDescriptor((Task) element, parent, getModel());
		} else if (element instanceof TaskRun) {
			return new TaskRunDescriptor((TaskRun) element, parent, getModel());
		} else if (element instanceof Pipeline) {
			return new PipelineDescriptor((Pipeline) element, parent, getModel());
		} else if (element instanceof PipelineRun) {
			return new PipelineRunDescriptor((PipelineRun) element, parent, getModel());
		} else if (element instanceof PipelineResource) {
			return new PipelineResourceDescriptor((PipelineResource) element, parent, getModel());
		} else if (element instanceof TriggerTemplate) {
			return new TriggerTemplateDescriptor((TriggerTemplate) element, parent, getModel());
		} else if (element instanceof TriggerBinding) {
			return new TriggerBindingDescriptor((TriggerBinding) element, parent, getModel());
		} else if (element instanceof EventListener) {
			return new EventListenerDescriptor((EventListener) element, parent, getModel());
		} else if (element instanceof Condition) {
			return new ConditionDescriptor((Condition) element, parent, getModel());
		} else {
			return null;
		}
	}

	private class ClusterTaskDescriptor extends ResourceDescriptor<ClusterTask> {

		private ClusterTaskDescriptor(ClusterTask element, NodeDescriptor<?> parent, IResourceModel model) {
			super(element, parent, model);
		}

		@Nullable
		@Override
		protected Icon getIcon(ClusterTask element) {
			return IconLoader.getIcon("/images/clustertask.svg");
		}
	}

	private class TaskDescriptor extends ResourceDescriptor<Task> {

		private TaskDescriptor(Task element, NodeDescriptor<?> parent, IResourceModel model) {
			super(element, parent, model);
		}

		@Nullable
		@Override
		protected Icon getIcon(Task element) {
			return IconLoader.getIcon("/images/task.svg");
		}
	}

	private class TaskRunDescriptor extends ResourceDescriptor<TaskRun> {

		private TaskRunDescriptor(TaskRun element, NodeDescriptor<?> parent, IResourceModel model) {
			super(element, parent, model);
		}

		@Nullable
		@Override
		protected Icon getIcon(TaskRun element) {
			return IconLoader.getIcon("/images/taskrun.svg");
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
			return IconLoader.getIcon("/images/pipeline.svg");
		}
	}

	private class PipelineRunDescriptor extends ResourceDescriptor<PipelineRun> {

		private PipelineRunDescriptor(PipelineRun element, NodeDescriptor<?> parent, IResourceModel model) {
			super(element, parent, model);
		}

		@Nullable
		@Override
		protected Icon getIcon(PipelineRun element) {
			return IconLoader.getIcon("/images/pipeline.svg");
		}
	}

	private class PipelineResourceDescriptor extends ResourceDescriptor<PipelineResource> {

		private PipelineResourceDescriptor(PipelineResource element, NodeDescriptor<?> parent, IResourceModel model) {
			super(element, parent, model);
		}

		@Nullable
		@Override
		protected Icon getIcon(PipelineResource element) {
			return IconLoader.getIcon("/images/pipelineresource.svg");
		}

		@Nullable
		@Override
		protected String getLabel(PipelineResource element) {
			return element.getMetadata().getName();
		}
	}

	private class TriggerTemplateDescriptor extends ResourceDescriptor<TriggerTemplate> {

		private TriggerTemplateDescriptor(TriggerTemplate element, NodeDescriptor<?> parent, IResourceModel model) {
			super(element, parent, model);
		}

		@Nullable
		@Override
		protected Icon getIcon(TriggerTemplate element) {
			return IconLoader.getIcon("/images/triggertemplate.svg");
		}

		@Nullable
		@Override
		protected String getLabel(TriggerTemplate element) {
			return element.getMetadata().getName();
		}
	}

	private class TriggerBindingDescriptor extends ResourceDescriptor<TriggerBinding> {

		private TriggerBindingDescriptor(TriggerBinding element, NodeDescriptor<?> parent, IResourceModel model) {
			super(element, parent, model);
		}

		@Nullable
		@Override
		protected Icon getIcon(TriggerBinding element) {
			return IconLoader.getIcon("/images/triggerbinding.svg");
		}

		@Nullable
		@Override
		protected String getLabel(TriggerBinding element) {
			return element.getMetadata().getName();
		}
	}

	private class EventListenerDescriptor extends ResourceDescriptor<EventListener> {

		private EventListenerDescriptor(EventListener element, NodeDescriptor<?> parent, IResourceModel model) {
			super(element, parent, model);
		}

		@Nullable
		@Override
		protected Icon getIcon(EventListener element) {
			return IconLoader.getIcon("/images/eventlistener.svg");
		}

		@Nullable
		@Override
		protected String getLabel(EventListener element) {
			return element.getMetadata().getName();
		}
	}

	private class ConditionDescriptor extends ResourceDescriptor<Condition> {

		private ConditionDescriptor(Condition element, NodeDescriptor<?> parent, IResourceModel model) {
			super(element, parent, model);
		}

		@Nullable
		@Override
		protected Icon getIcon(Condition element) {
			return IconLoader.getIcon("/images/condition.svg");
		}

		@Nullable
		@Override
		protected String getLabel(Condition element) {
			return element.getMetadata().getName();
		}
	}

}
