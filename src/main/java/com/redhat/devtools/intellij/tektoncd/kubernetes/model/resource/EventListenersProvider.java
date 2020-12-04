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
import io.fabric8.tekton.triggers.v1alpha1.EventListener;
import org.jboss.tools.intellij.kubernetes.model.resource.IResourcesProviderFactory;
import org.jboss.tools.intellij.kubernetes.model.resource.NonNamespacedResourcesProvider;
import org.jboss.tools.intellij.kubernetes.model.resource.ResourceKind;
import org.jboss.tools.intellij.kubernetes.model.util.Clients;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class EventListenersProvider extends NonNamespacedResourcesProvider<EventListener, TektonClient> {

    static class Factory implements IResourcesProviderFactory<EventListener, KubernetesClient, EventListenersProvider> {

        @NotNull
        @Override
        public EventListenersProvider create(@NotNull final Clients<KubernetesClient> clients) {
            return new EventListenersProvider(clients.get(TektonClient.class));
        }
    }

    public EventListenersProvider(@NotNull TektonClient client) {
        super(client);
    }

    @NotNull
    @Override
    public ResourceKind<EventListener> getKind() {
        return ResourceKind.create(EventListener.class);
    }

    @NotNull
    @Override
    protected Supplier<WatchListDeletable<EventListener, ? extends KubernetesResourceList<EventListener>, Boolean, Watch>> getOperation() {
        return () -> getClient().v1alpha1().eventListeners();
    }

}
