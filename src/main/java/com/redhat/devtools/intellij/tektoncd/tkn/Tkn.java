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

import io.fabric8.kubernetes.client.KubernetesClient;

import java.io.IOException;
import java.util.List;

public interface Tkn {
    /**
     * Check if the cluster is Tekton aware.
     *
     * @param client the cluster client object
     * @return true if Tekton is installed on cluster false otherwise
     */
    boolean isTektonAware(KubernetesClient client);

    /**
     * Returns the names of ClusterTask for a namespace
     * @param namespace the namespace to use
     * @return the list of ClusterTasks names
     * @throws IOException if communication errored
     */
    List<String> getClusterTasks(String namespace) throws IOException;

    /**
     * Return the names of the namespace (projects for OpenShift).
     *
     * @param client the cluster client object
     * @return the list of namespaces names
     * @throws IOException if communication errored
     */
    List<String> getNamespaces(KubernetesClient client) throws IOException;

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
    List<String> getResources(String namespace) throws IOException;

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
     * Get pipeline configuration in JSON
     *
     * @param namespace the namespace of the task
     * @param pipeline the pipeline to use
     * @throws IOException if communication errored
     */
    String getPipelineJSON(String namespace, String pipeline) throws IOException;

    /**
     * Get pipeline resource configuration in JSON
     *
     * @param namespace the namespace of the task
     * @param resource the pipeline resource to use
     * @throws IOException if communication errored
     */
    String getResourceJSON(String namespace, String resource) throws IOException;

    /**
     * Get task configuration in JSON
     *
     * @param namespace the namespace of the task
     * @param task the task to use
     * @throws IOException if communication errored
     */
    String getTaskJSON(String namespace, String task) throws IOException;

    /**
     * Delete a pipeline
     *
     * @param namespace the namespace to use
     * @param pipeline the pipeline to delete
     * @throws IOException if communication errored
     */
    void deletePipeline(String namespace, String pipeline) throws IOException;

    /**
     * Delete a task
     *
     * @param namespace the namespace to use
     * @param task the task to delete
     * @throws IOException if communication errored
     */
    void deleteTask(String namespace, String task) throws IOException;

    /**
     * Delete a resource
     *
     * @param namespace the namespace to use
     * @param resource the resource to delete
     * @throws IOException if communication errored
     */
    void deleteResource(String namespace, String resource) throws IOException;
}
