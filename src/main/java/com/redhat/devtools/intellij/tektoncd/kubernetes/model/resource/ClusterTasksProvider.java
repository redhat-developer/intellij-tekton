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
package com.redhat.devtools.intellij.tektoncd.kubernetes.model.resource;

import io.fabric8.kubernetes.api.model.KubernetesResourceList;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.Watch;
import io.fabric8.kubernetes.client.dsl.WatchListDeletable;
import io.fabric8.tekton.client.TektonClient;
import io.fabric8.tekton.pipeline.v1beta1.ClusterTask;
import io.fabric8.tekton.pipeline.v1beta1.TaskRun;
import org.jboss.tools.intellij.kubernetes.model.resource.IResourcesProviderFactory;
import org.jboss.tools.intellij.kubernetes.model.resource.NamespacedResourcesProvider;
import org.jboss.tools.intellij.kubernetes.model.resource.NonNamespacedResourcesProvider;
import org.jboss.tools.intellij.kubernetes.model.resource.ResourceKind;
import org.jboss.tools.intellij.kubernetes.model.util.Clients;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class ClusterTasksProvider extends NonNamespacedResourcesProvider<ClusterTask, TektonClient> {

    static class Factory implements IResourcesProviderFactory<ClusterTask, KubernetesClient, ClusterTasksProvider> {

        @NotNull
        @Override
        public ClusterTasksProvider create(@NotNull final Clients<KubernetesClient> clients) {
            return new ClusterTasksProvider(clients.get(TektonClient.class));
        }
    }

    public ClusterTasksProvider(@NotNull TektonClient client) {
        super(client);
    }

    @NotNull
    @Override
    public ResourceKind<ClusterTask> getKind() {
        return ResourceKind.create(ClusterTask.class);
    }

    @NotNull
    @Override
    protected Supplier<WatchListDeletable<ClusterTask, ? extends KubernetesResourceList<ClusterTask>, Boolean, Watch>> getOperation() {
        return () -> getClient().v1beta1().clusterTasks();
    }

}
