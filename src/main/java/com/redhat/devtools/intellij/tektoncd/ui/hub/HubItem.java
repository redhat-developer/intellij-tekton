/*******************************************************************************
 * Copyright (c) 2020 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc.
 ******************************************************************************/
package com.redhat.devtools.intellij.tektoncd.ui.hub;

import com.redhat.devtools.intellij.tektoncd.hub.model.ResourceData;
import org.jetbrains.annotations.NotNull;

public class HubItem {

    private ResourceData resource;
    private String kind, version;

    public HubItem(@NotNull ResourceData resource) {
        this(resource, resource.getKind(), resource.getLatestVersion().getVersion());
    }

    public HubItem(@NotNull ResourceData resource, String kind, String version) {
        this.resource = resource;
        this.kind = kind;
        this.version = version;
    }

    public String getVersion() {
        return version;
    }

    public ResourceData getResource() {
        return this.resource;
    }

    public String getKind() {
        return kind;
    }
}