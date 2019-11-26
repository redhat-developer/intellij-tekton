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
     * Return the names of the namespace (projects for OpenShift).
     *
     * @param client the cluster client object
     * @return the list of namespaces names
     * @throws IOException if communication errored
     */
    List<String> getNamespaces(KubernetesClient client) throws IOException;

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
}
