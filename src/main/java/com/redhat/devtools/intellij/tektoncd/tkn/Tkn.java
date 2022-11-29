/*******************************************************************************
 * Copyright (c) 2019 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package com.redhat.devtools.intellij.tektoncd.tkn;

import com.redhat.devtools.intellij.tektoncd.tkn.component.field.Input;
import com.redhat.devtools.intellij.tektoncd.tkn.component.field.Workspace;
import com.redhat.devtools.intellij.tektoncd.ui.toolwindow.debug.DebugTabPanelFactory;
import com.redhat.devtools.intellij.tektoncd.ui.toolwindow.findusage.RefUsage;
import com.redhat.devtools.intellij.tektoncd.utils.PollingHelper;
import com.redhat.devtools.intellij.tektoncd.utils.WatchHandler;
import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.GenericKubernetesResource;
import io.fabric8.kubernetes.api.model.GenericKubernetesResourceList;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.Watch;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.dsl.ExecWatch;
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext;
import io.fabric8.tekton.pipeline.v1beta1.ClusterTask;
import io.fabric8.tekton.pipeline.v1beta1.Pipeline;
import io.fabric8.tekton.pipeline.v1beta1.PipelineRun;
import io.fabric8.tekton.pipeline.v1beta1.Task;
import io.fabric8.tekton.pipeline.v1beta1.TaskRun;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;

public interface Tkn {
    /**
     * Check if the cluster is Tekton aware.
     *
     * @return true if Tekton is installed on cluster false otherwise
     */
    boolean isTektonAware() throws IOException;

    /**
     * Check if the cluster is Tekton Triggers aware.
     *
     * @return true if Tekton triggers are installed on cluster false otherwise
     */
    boolean isTektonTriggersAware();

    /**
     * Return the newest apiVersion for tekton triggers
     *
     * @return the newest tekton triggers apiVersion
     * @throws IOException if communication errored
     */
    String getTektonTriggersApiVersion() throws IOException;

    /**
     * Return the tekton version used on active cluster
     * @return tekton version
     */
    String getTektonVersion();

    /**
     * Check if alpha features are enabled on active cluster
     * @return true if alpha features are enabled
     */
    boolean isTektonAlphaFeatureEnabled();

    /**
     * Return the name of the current active namespace (project for OpenShift).
     *
     * @return the active namespace name
     */
    String getNamespace();

    /**
     * Return the name of the namespace containing the core features of Tekton
     * openshift-pipelines for Ocp cluster
     * tekton-pipelines for K8s cluster
     *
     * @return the core namespace name
     */
    String getTektonCoreNamespace();

    /**
     * Return the names of the serviceAccounts for a namespace
     *
     * @param namespace the namespace to use
     * @return the list of service account names
     */
    List<String> getServiceAccounts(String namespace);

    /**
     * Return the names of the secrets for a namespace
     *
     * @param namespace the namespace to use
     * @return the list of secrets names
     */
    List<String> getSecrets(String namespace);

    /**
     * Return the names of the configmaps for a namespace
     *
     * @param namespace the namespace to use
     * @return the list of configmaps names
     */
    List<String> getConfigMaps(String namespace);

    /**
     * Return the names of the persistentVolumeClaims for a namespace
     *
     * @param namespace the namespace to use
     * @return the list of persistentVolumeClaims names
     */
    List<String> getPersistentVolumeClaim(String namespace);

    /**
     * Return the list of Tekton pipelines for a namespace
     *
     * @param namespace the namespace to use
     * @return the list of pipelines
     * @throws IOException if communication errored
     */
    List<Pipeline> getPipelines(String namespace) throws IOException;

    /**
     * Return the list of pipeline runs for a pipeline
     *
     * @param namespace the namespace of the pipeline
     * @param pipeline  the pipeline to look pipeline runs for
     * @return the list of pipeline runs
     * @throws IOException if communication errored
     */
    List<PipelineRun> getPipelineRuns(String namespace, String pipeline) throws IOException;

    /**
     * Return the names of Tekton tasks for a namespace.
     *
     * @param namespace the namespace to use
     * @return the list of tasks
     * @throws IOException if communication errored
     */
    List<Task> getTasks(String namespace) throws IOException;

    /**
     * Return the names of Tekton clusterTasks
     *
     * @return the list of clustertasks
     * @throws IOException if communication errored
     */
    List<ClusterTask> getClusterTasks() throws IOException;

    /**
     * Return the list of task runs for a task.
     *
     * @param namespace the namespace of the task
     * @param task the task to look task runs for
     * @return the list of task runs
     * @throws IOException if communication errored
     */
    List<TaskRun> getTaskRuns(String namespace, String task) throws IOException;

    /**
     * Return the list of triggertemplate in a namespace
     *
     * @param namespace the namespace to use
     * @return the list of triggertemplate
     * @throws IOException if communication errored
     */
    List<String> getTriggerTemplates(String namespace) throws IOException;

    /**
     * Return the list of triggerbindings in a namespace
     *
     * @param namespace the namespace to use
     * @return the list of triggerbindings
     * @throws IOException if communication errored
     */
    List<String> getTriggerBindings(String namespace) throws IOException;

    /**
     * Return the list of clusterTriggerbindings in the cluster
     *
     * @return the list of clusterTriggerbindings
     * @throws IOException if communication errored
     */
    List<String> getClusterTriggerBindings() throws IOException;

    /**
     * Return the list of eventListeners in a namespace
     *
     * @param namespace the namespace to use
     * @return the list of eventListeners
     * @throws IOException if communication errored
     */
    List<String> getEventListeners(String namespace) throws IOException;

    /**
     * Get configmap by name
     *
     * @param namespace the namespace of the configmap
     * @param name configmap name
     * @return configmap object
     */
    ConfigMap getConfigMap(String namespace, String name);

    /**
     * Get pipeline configuration in YAML
     *
     * @param namespace the namespace of the task
     * @param pipeline the pipeline to use
     * @throws IOException if communication errored
     */
    String getPipelineYAML(String namespace, String pipeline) throws IOException;

    /**
     * Get task configuration in YAML
     *
     * @param namespace the namespace of the task
     * @param task the task to use
     * @throws IOException if communication errored
     */
    String getTaskYAML(String namespace, String task) throws IOException;

    /**
     * Get clusterTask configuration in YAML
     *
     * @param task the task to use
     * @throws IOException if communication errored
     */
    String getClusterTaskYAML(String task) throws IOException;

    /**
     *
     * @param namespace the namespace of the triggerTemplate
     * @param triggerTemplate the triggerTemplate to use
     * @return triggerTemplate configuration
     * @throws IOException if communication errored
     */
    String getTriggerTemplateYAML(String namespace, String triggerTemplate) throws IOException;

    /**
     * Get triggerBinding configuration in YAML
     *
     * @param namespace the namespace of the triggerBinding
     * @param triggerBinding the triggerBinding to use
     * @return triggerBinding configuration
     * @throws IOException if communication errored
     */
    String getTriggerBindingYAML(String namespace, String triggerBinding) throws IOException;

    /**
     * Get clusterTriggerBinding configuration in YAML
     *
     * @param ctb the clusterTriggerBinding to use
     * @return clusterTriggerBinding configuration
     * @throws IOException if communication errored
     */
    String getClusterTriggerBindingYAML(String ctb) throws IOException;

    /**
     * Get eventListener configuration in YAML
     *
     * @param namespace the namespace of the eventListener
     * @param eventListener the eventListener to use
     * @return eventListener configuration
     * @throws IOException if communication errored
     */
    String getEventListenerYAML(String namespace, String eventListener) throws IOException;

    /**
     * Return all pipelines where the task is used
     *
     * @param kind the kind of task (task or clustertask)
     * @param task the name of the task
     * @return list of pipelines where the task is used
     * @throws IOException if communication errored
     */
    List<RefUsage> findTaskUsages(String kind, String task) throws IOException;

    /**
     * Delete a list of pipelines
     *
     * @param namespace the namespace to use
     * @param pipelines the list of pipelines to delete
     * @param deleteRelatedResources boolean to delete related resources (e.g pipelinerun)
     * @throws IOException if communication errored
     */
    void deletePipelines(String namespace, List<String> pipelines, boolean deleteRelatedResources) throws IOException;

    /**
     * Delete a list of pipelineruns
     *
     * @param namespace the namespace to use
     * @param prs the list of pipelineRuns to delete
     * @throws IOException if communication errored
     */
    void deletePipelineRuns(String namespace, List<String> prs) throws IOException;

    /**
     * Delete a list of tasks
     *
     * @param namespace the namespace to use
     * @param tasks the list of tasks to delete
     * @param deleteRelatedResources boolean to delete related resources (e.g taskrun)
     * @throws IOException if communication errored
     */
    void deleteTasks(String namespace, List<String> tasks, boolean deleteRelatedResources) throws IOException;

    /**
     * Delete a list of clusterTasks
     *
     * @param tasks the list of tasks to delete
     * @param deleteRelatedResources boolean to delete related resources (e.g taskrun)
     * @throws IOException if communication errored
     */
    void deleteClusterTasks(List<String> tasks, boolean deleteRelatedResources) throws IOException;

    /**
     * Delete a list of taskruns
     *
     * @param namespace the namespace to use
     * @param trs the list of taskRuns to delete
     * @throws IOException if communication errored
     */
    void deleteTaskRuns(String namespace, List<String> trs) throws IOException;

    /**
     * Delete a list of triggerTemplates
     *
     * @param namespace the namespace to use
     * @param triggerTemplates the list of triggerTemplates to delete
     * @throws IOException if communication errored
     */
    void deleteTriggerTemplates(String namespace, List<String> triggerTemplates) throws IOException;

    /**
     * Delete a list of triggerBindings
     *
     * @param namespace the namespace to use
     * @param triggerBindings the list of triggerBindings to delete
     * @throws IOException if communication errored
     */
    void deleteTriggerBindings(String namespace, List<String> triggerBindings) throws IOException;

    /**
     * Delete a list of clusterTriggerBindings
     *
     * @param ctbs the list of clusterTriggerBindings to delete
     * @throws IOException if communication errored
     */
    void deleteClusterTriggerBindings(List<String> ctbs) throws IOException;

    /**
     * Delete a list of eventListeners
     *
     * @param namespace the namespace to use
     * @param eventListeners the list of eventListeners to delete
     * @throws IOException if communication errored
     */
    void deleteEventListeners(String namespace, List<String> eventListeners) throws IOException;

    /**
     * Get all custom resources from the cluster which is namespaced.
     *
     * @param namespace the namespace to use
     * @param crdContext the custom resource definition context of the resource kind
     * @return GenericKubernetesResourceList or null if no resource was found
     */
    GenericKubernetesResourceList getCustomResources(String namespace, CustomResourceDefinitionContext crdContext);

    /**
     * Get a custom resource from the cluster which is namespaced.
     *
     * @param namespace the namespace to use
     * @param name name of custom resource
     * @param crdContext the custom resource definition context of the resource kind
     * @return GenericKubernetesResource or null if no resource was found
     */
    GenericKubernetesResource getCustomResource(String namespace, String name, CustomResourceDefinitionContext crdContext);

    /**
     * Edit a custom resource object which is a namespaced object
     *
     * @param namespace the namespace to use
     * @param name name of custom resource
     * @param crdContext the custom resource definition context of the resource kind
     * @param objectAsString new object as a JSON string
     * @throws IOException
     */
    void editCustomResource(String namespace, String name, CustomResourceDefinitionContext crdContext, String objectAsString) throws IOException;

    /**
     * Create a custom resource which is a namespaced object.
     *
     * @param namespace the namespace to use
     * @param crdContext the custom resource definition context of the resource kind
     * @param objectAsString new object as a JSON string
     * @throws IOException
     * @return resource name
     */
    String createCustomResource(String namespace, CustomResourceDefinitionContext crdContext, String objectAsString) throws IOException;

    /**
     * Create a PVC
     *
     * @param name PVC name
     * @param accessMode PVC accessMode
     * @param size PVC size
     * @param unit PVC size format (MB, GB or TB)
     * @throws IOException if communication errored
     */
    void createPVC(String name, String accessMode, String size, String unit) throws IOException;

    /**
     * Start the execution of a pipeline
     *
     * @param namespace the namespace of the pipeline
     * @param pipeline the pipeline that has to be run
     * @param parameters the parameters to start pipeline
     * @param serviceAccount the service account to use when running the pipeline
     * @param taskServiceAccount the service account corresponding to the task
     * @param workspaces the workspaces to start pipeline
     * @param runPrefixName the name to use as a prefix for the pipelinerun
     * @throws IOException if communication errored
     * @return PipelineRun name
     */
    String startPipeline(String namespace, String pipeline, Map<String, Input> parameters, String serviceAccount, Map<String, String> taskServiceAccount, Map<String, Workspace> workspaces, String runPrefixName) throws IOException;

    /**
     * Re-run the pipeline using last pipelinerun values
     *
     * @param namespace the namespace of the task
     * @param pipeline the pipeline that has to be run
     * @throws IOException if communication errored
     * @return PipelineRun name
     */
    String startLastPipeline(String namespace, String pipeline) throws IOException;

    /**
     * Start the execution of a task
     *
     * @param namespace the namespace of the task
     * @param task the task that has to be run
     * @param parameters the parameters to start task
     * @param serviceAccount the service account to use when running the task
     * @param workspaces the workspaces to start the task
     * @param runPrefixName the name to use as a prefix for the taskrun
     * @throws IOException if communication errored
     * @return TaskRun name
     */
    String startTask(String namespace, String task, Map<String, Input> parameters, String serviceAccount, Map<String, Workspace> workspaces, String runPrefixName) throws IOException;

    /**
     * Preview the TaskRun without running it
     *
     * @param namespace the namespace of the task
     * @param task the task that has to be run
     * @param parameters the parameters to start task
     * @param serviceAccount the service account to use when running the task
     * @param workspaces the workspaces to start the task
     * @param runPrefixName the name to use as a prefix for the taskrun
     * @throws IOException if communication errored
     * @return TaskRun
     */
    String createRunFromTask(String namespace, String task, Map<String, Input> parameters, String serviceAccount, Map<String, Workspace> workspaces, String runPrefixName) throws IOException;

    /**
     * Start the execution of a task
     *
     * @param namespace the namespace where to run the clusterTask, useful to retrieve the input/output resources
     * @param clusterTask the task that has to be run
     * @param parameters the parameters to start task
     * @param serviceAccount the service account to use when running the task
     * @param workspaces the workspaces to start the task
     * @param runPrefixName the name to use as a prefix for the taskrun
     * @throws IOException if communication errored
     * @return TaskRun name
     */
    String startClusterTask(String namespace, String clusterTask, Map<String, Input> parameters, String serviceAccount, Map<String, Workspace> workspaces, String runPrefixName) throws IOException;

    /**
     * Preview the ClusterTaskRun without running it
     *
     * @param namespace the namespace where to run the clusterTask, useful to retrieve the input/output resources
     * @param clusterTask the task that has to be run
     * @param parameters the parameters to start task
     * @param serviceAccount the service account to use when running the task
     * @param workspaces the workspaces to start the task
     * @param runPrefixName the name to use as a prefix for the taskrun
     * @throws IOException if communication errored
     * @return ClusterTaskRun
     */
    String createRunFromClusterTask(String namespace, String clusterTask, Map<String, Input> parameters, String serviceAccount, Map<String, Workspace> workspaces, String runPrefixName) throws IOException;

    /**
     * Re-run the task using last taskrun values
     *
     * @param namespace the namespace of the task
     * @param task the task that has to be run
     * @throws IOException if communication errored
     * @return TaskRun name
     */
    String startLastTask(String namespace, String task) throws IOException;

    /**
     * Show logs for a PipelineRun
     *
     * @param namespace the namespace to use
     * @param pipelineRun name of the PipelineRun
     * @param toEditor true if logs has to be redirected and displayed in the editor
     * @throws IOException if communication errored
     */
    void showLogsPipelineRun(String namespace, String pipelineRun, boolean toEditor) throws IOException;

    /**
     * Get logs for a TaskRun
     * @param namespace the namespace to use
     * @param taskRun name of the TaskRun
     * @param toEditor true if logs has to be redirected and displayed in the editor
     * @throws IOException if communication errored
     */
    void showLogsTaskRun(String namespace, String taskRun, boolean toEditor) throws IOException;

    /**
     * Get logs for an eventListener
     * @param namespace the namespace to use
     * @param el name of the eventListener
     * @throws IOException if communication errored
     */
    void showLogsEventListener(String namespace, String el) throws IOException;

    /**
     * Follow logs for a PipelineRun
     *
     * @param namespace the namespace to use
     * @param pipelineRun name of the PipelineRun
     * @param toEditor true if logs has to be redirected and displayed in the editor
     * @throws IOException if communication errored
     */
    void followLogsPipelineRun(String namespace, String pipelineRun, boolean toEditor) throws IOException;

    /**
     * Follow logs for a TaskRun
     * @param namespace the namespace to use
     * @param taskRun name of the TaskRun
     * @param toEditor true if logs has to be redirected and displayed in the editor
     * @throws IOException if communication errored
     */
    void followLogsTaskRun(String namespace, String taskRun, boolean toEditor) throws IOException;

    /**
     * Get TaskRun configuration in YAML
     *
     * @param namespace the namespace to use
     * @param taskRun name of the TaskRun
     * @throws IOException if communication errored
     */
    String getTaskRunYAML(String namespace, String taskRun) throws IOException;

    /**
     * Get PipelineRun configuration in YAML
     *
     * @param namespace the namespace to use
     * @param pipelineRun name of the PipelineRun
     */
    String getPipelineRunYAML(String namespace, String pipelineRun) throws IOException;

    /**
     *  Cancel the pipelineRun
     *
     * @param namespace the namespace to use
     * @param pipelineRun name of the PipelineRun
     * @throws IOException if communication errored
     */
    void cancelPipelineRun(String namespace, String pipelineRun) throws IOException;

    /**
     *  Cancel the taskRun
     *
     * @param namespace the namespace to use
     * @param taskRun name of the TaskRun
     * @throws IOException if communication errored
     */
    void cancelTaskRun(String namespace, String taskRun) throws IOException;

    /**
     * Set a watch on Pipeline resource
     *
     * @param namespace the namespace to use
     * @param pipeline the name of the pipeline
     * @param watcher the watcher to call when a new event is received
     * @return the watch object
     * @throws IOException if communication errored
     */
    Watch watchPipeline(String namespace, String pipeline, Watcher<Pipeline> watcher) throws IOException;

    /**
     * Set a watch on Pipeline resources
     *
     * @param namespace the namespace to use
     * @param watcher the watcher to call when a new event is received
     * @return the watch object
     * @throws IOException if communication errored
     */
    Watch watchPipelines(String namespace, Watcher<Pipeline> watcher) throws IOException;

    /**
     * Set a watch on PipelineRun resources
     *
     * @param namespace the namespace to use
     * @param watcher the watcher to call when a new event is received
     * @return the watch object
     * @throws IOException if communication errored
     */
    Watch watchPipelineRuns(String namespace, Watcher<io.fabric8.tekton.pipeline.v1beta1.PipelineRun> watcher) throws IOException;

    /**
     * Set a watch on a specific Task resource
     *
     * @param namespace the namespace to use
     * @param task the name of the task
     * @param watcher the watcher to call when a new event is received
     * @return the watch object
     * @throws IOException if communication errored
     */
    Watch watchTask(String namespace, String task, Watcher<Task> watcher) throws IOException;

    /**
     * Set a watch on Task resources
     *
     * @param namespace the namespace to use
     * @param watcher the watcher to call when a new event is received
     * @return the watch object
     * @throws IOException if communication errored
     */
    Watch watchTasks(String namespace, Watcher<Task> watcher) throws IOException;

    /**
     * Set a watch on TaskRun resources
     *
     * @param namespace the namespace to use
     * @param watcher the watcher to call when a new event is received
     * @return the watch object
     * @throws IOException if communication errored
     */
    Watch watchTaskRuns(String namespace, Watcher<TaskRun> watcher) throws IOException;

    /**
     * Set a watch on ClusterTask resources
     *
     * @param watcher the watcher to call when a new event is received
     * @return the watch object
     * @throws IOException if communication errored
     */
    Watch watchClusterTasks(Watcher<ClusterTask> watcher) throws IOException;

    /**
     * Set a watch on TriggerTemplate resources
     *
     * @param namespace the namespace to use
     * @param watcher the watcher to call when a new event is received
     * @return the watch object
     * @throws IOException if communication errored
     */
    Watch watchTriggerTemplates(String namespace, Watcher<io.fabric8.tekton.triggers.v1alpha1.TriggerTemplate> watcher) throws IOException;

    /**
     * Set a watch on TriggerBinding resources
     *
     * @param namespace the namespace to use
     * @param watcher the watcher to call when a new event is received
     * @return the watch object
     * @throws IOException if communication errored
     */
    Watch watchTriggerBindings(String namespace, Watcher<io.fabric8.tekton.triggers.v1alpha1.TriggerBinding> watcher) throws IOException;

    /**
     * Set a watch on ClusterTriggerBinding resources
     *
     * @param watcher the watcher to call when a new event is received
     * @return the watch object
     * @throws IOException if communication errored
     */
    Watch watchClusterTriggerBindings(Watcher<io.fabric8.tekton.triggers.v1alpha1.ClusterTriggerBinding> watcher) throws IOException;


    /**
     * Set a watch on EventListener resources
     *
     * @param namespace the namespace to use
     * @param watcher the watcher to call when a new event is received
     * @return the watch object
     * @throws IOException if communication errored
     */
    Watch watchEventListeners(String namespace, Watcher<io.fabric8.tekton.triggers.v1alpha1.EventListener> watcher) throws IOException;

    /**
     * Get diagnostic data related to objects with the field=value pair
     *
     * @param namespace the namespace to use
     * @param keyLabel the key to use to retrieve the objects
     * @param valueLabel the value to use to retrieve the objects
     * @return if the search succeeded or not
     * @throws IOException if communication errored
     */
    boolean getDiagnosticData(String namespace, String keyLabel, String valueLabel) throws IOException;

    /**
     * Set a watch on a pod by using a label ket-value
     * @param namespace namespace where the pod exists
     * @param key key of the label
     * @param value value of the label
     * @return the watch object
     * @throws IOException if communication errored
     */
    Watch watchPodsWithLabel(String namespace, String key, String value, Watcher<Pod> watcher) throws IOException;

    /**
     * Get  the pod with name
     * @param namespace namesapce where the pod exists
     * @param name pod name
     * @return pod resource
     * @throws IOException if communication errored
     */
    Pod getPod(String namespace, String name) throws IOException;

    /**
     * Check if container is stopped in debug mode
     * @param namespace namespace where the pod exists
     * @param name name of the pod
     * @param container container name
     * @return true if the container is stopped in debug mode, false otherwise
     * @throws IOException if communication errored
     */
    boolean isContainerStoppedOnDebug(String namespace, String name, String container, Pod resource) throws IOException;

    /**
     * Execute a command within the container
     * @param pod pod where the container exists
     * @param containerId container where to execute the command
     * @param command command to execute
     * @return watch to check command execution
     */
    ExecWatch execCommandInContainer(Pod pod, String containerId, String... command);

    /**
     * Execute a command within the container by using a custom execWebSocketListener which
     * prevents lagging within the embedded terminal
     * @param pod pod where the container exists
     * @param containerId container where to execute the command
     * @param command command to execute
     * @return watch to check command execution
     */
    ExecWatch customExecCommandInContainer(Pod pod, String containerId, String... command);

    /**
     * Install task from Tekton Hub
     * @param task task name
     * @param version version of the task
     * @param overwrite if the task is already installed and we want to overwrite that
     * @throws IOException if communication errored
     */
    void installTaskFromHub(String task, String version, boolean overwrite) throws IOException;

    /**
     * Get the task yaml from Tekton Hub
     * @param task task name
     * @param version version of the task
     * @return the task yaml
     * @throws IOException if communication errored
     */
    String getTaskYAMLFromHub(String task, String version) throws IOException;

    /**
     * Get the pipeline yaml from Tekton Hub
     * @param pipeline pipeline name
     * @param version version of the pipeline
     * @return the pipeline yaml
     * @throws IOException if communication errored
     */
    String getPipelineYAMLFromHub(String pipeline, String version) throws IOException;

    List<String> getResourcesAsYaml(List<Resource> resources) throws IOException;

    /**
     * Deploy bundle to registry
     * @param image image name
     * @param resources resources that are included in the bundle
     * @param authenticator object which includes registry authentication data
     * @throws IOException if communication errored
     */
    void deployBundle(String image, List<String> resources, Authenticator authenticator) throws IOException;

    /**
     *
     * @param bundle bundle name
     * @param authenticator object which includes registry authentication data
     * @return list of resources belonging to bundle
     * @throws IOException if communication errored
     */
    List<Resource> listResourceFromBundle(String bundle, Authenticator authenticator) throws IOException;

    /**
     *
     * @param bundle bundle name
     * @param resource resource to get the YAML
     * @param authenticator object which includes registry authentication data
     * @return resource YAML
     * @throws IOException if communication errored
     */
    String getBundleResourceYAML(String bundle, Resource resource, Authenticator authenticator) throws IOException;

    URL getMasterUrl();

    <T> T getClient(Class<T> clazz);

    DebugTabPanelFactory getDebugTabPanelFactory();

    WatchHandler getWatchHandler();

    PollingHelper getPollingHelper();

    void dispose();
}
