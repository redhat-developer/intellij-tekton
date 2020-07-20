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

import com.redhat.devtools.intellij.tektoncd.tkn.component.field.Workspace;
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext;
import io.fabric8.tekton.client.TektonClient;

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
     * Returns the names of ClusterTask for a namespace
     * @return the list of ClusterTasks names
     * @throws IOException if communication errored
     */
    List<String> getClusterTasks() throws IOException;

    /**
     * Return the names of the namespace (projects for OpenShift).
     *
     * @return the list of namespaces names
     * @throws IOException if communication errored
     */
    List<String> getNamespaces() throws IOException;

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
     * Return the names of Tekton pipelines for a namespace
     *
     * @param namespace the namespace to use
     * @return the list of pipelines names
     * @throws IOException if communication errored
     */
    List<String> getPipelines(String namespace) throws IOException;

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
     * Return the list of resources that can be used in a pipeline
     *
     * @param namespace the namespace to use
     * @return the list of resources
     * @throws IOException if communication errored
     */
    List<Resource> getResources(String namespace) throws IOException;

    /**
     * Return the names of Tekton tasks for a namespace.
     *
     * @param namespace the namespace to use
     * @return the list of tasks names
     * @throws IOException if communication errored
     */
    List<String> getTasks(String namespace) throws IOException;

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
     * Return the list of conditions for a namespace.
     *
     * @param namespace the namespace of the task
     * @return the list of conditions
     * @throws IOException if communication errored
     */
    List<Condition> getConditions(String namespace) throws IOException;

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
     * Return the list of clusterTriggerbindings in a namespace
     *
     * @param namespace the namespace to use
     * @return the list of clusterTriggerbindings
     * @throws IOException if communication errored
     */
    List<String> getClusterTriggerBindings(String namespace) throws IOException;

    /**
     * Return the list of eventListeners in a namespace
     *
     * @param namespace the namespace to use
     * @return the list of eventListeners
     * @throws IOException if communication errored
     */
    List<String> getEventListeners(String namespace) throws IOException;

    /**
     * Get pipeline configuration in YAML
     *
     * @param namespace the namespace of the task
     * @param pipeline the pipeline to use
     * @throws IOException if communication errored
     */
    String getPipelineYAML(String namespace, String pipeline) throws IOException;

    /**
     * Get pipeline resource configuration in YAML
     *
     * @param namespace the namespace of the task
     * @param resource the pipeline resource to use
     * @throws IOException if communication errored
     */
    String getResourceYAML(String namespace, String resource) throws IOException;

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
     * Get condition configuration in YAML
     *
     * @param namespace the namespace of the condition
     * @param condition the condition to use
     * @throws IOException if communication errored
     */
    String getConditionYAML(String namespace, String condition) throws IOException ;

    /**
     *
     * @param namespace the namespace of the task
     * @param triggerTemplate the triggerTemplate to use
     * @return triggerTemplate configuration
     * @throws IOException if communication errored
     */
    String getTriggerTemplateYAML(String namespace, String triggerTemplate) throws IOException;

    /**
     * Get triggerBinding configuration in YAML
     *
     * @param namespace the namespace of the task
     * @param triggerBinding the triggerBinding to use
     * @return triggerBinding configuration
     * @throws IOException if communication errored
     */
    String getTriggerBindingYAML(String namespace, String triggerBinding) throws IOException;

    /**
     * Get clusterTriggerBinding configuration in YAML
     *
     * @param namespace the namespace of the task
     * @param ctb the clusterTriggerBinding to use
     * @return clusterTriggerBinding configuration
     * @throws IOException if communication errored
     */
    String getClusterTriggerBindingYAML(String namespace, String ctb) throws IOException;

    /**
     * Get eventListener configuration in YAML
     *
     * @param namespace the namespace of the task
     * @param eventListener the eventListener to use
     * @return eventListener configuration
     * @throws IOException if communication errored
     */
    String getEventListenerYAML(String namespace, String eventListener) throws IOException;

    /**
     * Delete a list of pipelines
     *
     * @param namespace the namespace to use
     * @param pipelines the list of pipelines to delete
     * @throws IOException if communication errored
     */
    void deletePipelines(String namespace, List<String> pipelines) throws IOException;

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
     * @throws IOException if communication errored
     */
    void deleteTasks(String namespace, List<String> tasks) throws IOException;

    /**
     * Delete a list of clusterTasks
     *
     * @param tasks the list of tasks to delete
     * @throws IOException if communication errored
     */
    void deleteClusterTasks(List<String> tasks) throws IOException;

    /**
     * Delete a list of taskruns
     *
     * @param namespace the namespace to use
     * @param trs the list of taskRuns to delete
     * @throws IOException if communication errored
     */
    void deleteTaskRuns(String namespace, List<String> trs) throws IOException;

    /**
     * Delete a list of resources
     *
     * @param namespace the namespace to use
     * @param resources the list of resources to delete
     * @throws IOException if communication errored
     */
    void deleteResources(String namespace, List<String> resources) throws IOException;

    /**
     * Delete a list of conditions
     *
     * @param namespace the namespace to use
     * @param conditions the list of conditions to delete
     * @throws IOException if communication errored
     */
    void deleteConditions(String namespace, List<String> conditions) throws IOException;

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
     * Get a custom resource from the cluster which is namespaced.
     *
     * @param namespace the namespace to use
     * @param name name of custom resource
     * @param crdContext the custom resource definition context of the resource kind
     * @return Object as HashMap, null if no resource was found
     */
    Map<String, Object> getCustomResource(String namespace, String name, CustomResourceDefinitionContext crdContext);

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
     */
    void createCustomResource(String namespace, CustomResourceDefinitionContext crdContext, String objectAsString) throws IOException;

    /**
     * Start the execution of a pipeline
     *
     * @param namespace the namespace of the pipeline
     * @param pipeline the pipeline that has to be run
     * @param parameters the parameters to start pipeline
     * @param inputResources the input resources to start pipeline
     * @param serviceAccount the service account to use when running the pipeline
     * @param taskServiceAccount the service account corresponding to the task
     * @param workspaces the workspaces to start pipeline
     * @throws IOException if communication errored
     */
    void startPipeline(String namespace, String pipeline, Map<String, String> parameters, Map<String, String> inputResources, String serviceAccount, Map<String, String> taskServiceAccount, Map<String, Workspace> workspaces) throws IOException;

    /**
     * Re-run the pipeline using last pipelinerun values
     *
     * @param namespace the namespace of the task
     * @param pipeline the pipeline that has to be run
     * @throws IOException if communication errored
     */
    void startLastPipeline(String namespace, String pipeline) throws IOException;

    /**
     * Start the execution of a task
     *
     * @param namespace the namespace of the task
     * @param task the task that has to be run
     * @param parameters the parameters to start task
     * @param inputResources the input resources to start task
     * @param outputResources the output resources to start task
     * @param serviceAccount the service account to use when running the task
     * @param workspaces the workspaces to start the task
     * @throws IOException if communication errored
     */
    void startTask(String namespace, String task, Map<String, String> parameters, Map<String, String> inputResources, Map<String, String> outputResources, String serviceAccount, Map<String, Workspace> workspaces) throws IOException;

    /**
     * Re-run the task using last taskrun values
     *
     * @param namespace the namespace of the task
     * @param task the task that has to be run
     * @throws IOException if communication errored
     */
    void startLastTask(String namespace, String task) throws IOException;

    /**
     * Get logs for a PipelineRun
     *
     * @param namespace the namespace to use
     * @param pipelineRun name of the PipelineRun
     */
    void showLogsPipelineRun(String namespace, String pipelineRun) throws IOException;

    /**
     * Get logs for a TaskRun
     * @param namespace the namespace to use
     * @param taskRun name of the TaskRun
     */
    void showLogsTaskRun(String namespace, String taskRun) throws IOException;

    /**
     * Follow logs for a PipelineRun
     *
     * @param namespace the namespace to use
     * @param pipelineRun name of the PipelineRun
     */
    void followLogsPipelineRun(String namespace, String pipelineRun) throws IOException;

    /**
     * Follow logs for a TaskRun
     * @param namespace the namespace to use
     * @param taskRun name of the TaskRun
     */
    void followLogsTaskRun(String namespace, String taskRun) throws IOException;

    /**
     * Get TaskRun configuration in YAML
     *
     * @param namespace the namespace to use
     * @param taskRun name of the TaskRun
     */
    String getTaskRunYAML(String namespace, String taskRun) throws IOException;

    /**
     * Get PipelineRun configuration in YAML
     *
     * @param namespace the namespace to use
     * @param pipelineRun name of the PipelineRun
     */
    String getPipelineRunYAML(String namespace, String pipelineRun) throws IOException;

    public URL getMasterUrl();

    public <T> T getClient(Class<T> clazz);
}
