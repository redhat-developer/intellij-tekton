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
import io.fabric8.tekton.resource.v1alpha1.PipelineResource;
import org.jboss.tools.intellij.kubernetes.model.resource.IResourcesProviderFactory;
import org.jboss.tools.intellij.kubernetes.model.resource.NonNamespacedResourcesProvider;
import org.jboss.tools.intellij.kubernetes.model.resource.ResourceKind;
import org.jboss.tools.intellij.kubernetes.model.util.Clients;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class PipelineResourcesProvider extends NonNamespacedResourcesProvider<PipelineResource, TektonClient> {

    static class Factory implements IResourcesProviderFactory<PipelineResource, KubernetesClient, PipelineResourcesProvider> {

        @NotNull
        @Override
        public PipelineResourcesProvider create(@NotNull final Clients<KubernetesClient> clients) {
            return new PipelineResourcesProvider(clients.get(TektonClient.class));
        }
    }

    public PipelineResourcesProvider(@NotNull TektonClient client) {
        super(client);
    }

    @NotNull
    @Override
    public ResourceKind<PipelineResource> getKind() {
        return ResourceKind.create(PipelineResource.class);
    }

    @NotNull
    @Override
    protected Supplier<WatchListDeletable<PipelineResource, ? extends KubernetesResourceList<PipelineResource>, Boolean, Watch>> getOperation() {
        return () -> getClient().v1alpha1().pipelineResources();
    }

}
