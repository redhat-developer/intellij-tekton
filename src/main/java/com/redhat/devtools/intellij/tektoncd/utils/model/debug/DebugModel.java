/*******************************************************************************
 * Copyright (c) 2021 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc.
 ******************************************************************************/
package com.redhat.devtools.intellij.tektoncd.utils.model.debug;

import io.fabric8.kubernetes.api.model.Pod;

public class DebugModel {

    private Pod pod;
    private String containerId, step, resource, image;
    private DebugResourceState resourceStatus;

    public DebugModel(String resource) {
        this(null, resource);
    }

    public DebugModel(Pod pod, String resource) {
        this(pod, "", resource);
    }

    public DebugModel(Pod pod, String containerId, String resource) {
        this(pod, containerId, "", resource, "");
    }

    public DebugModel(Pod pod, String containerId, String step, String resource, String image) {
        this.pod = pod;
        this.containerId = containerId;
        this.step = step;
        this.resource = resource;
        this.image = image;
        this.resourceStatus = DebugResourceState.RUNNING;
    }

    public Pod getPod() {
        return pod;
    }

    public void setPod(Pod pod) {
        this.pod = pod;
    }

    public String getContainerId() {
        return containerId;
    }

    public void setContainerId(String containerId) {
        this.containerId = containerId;
    }

    public String getStep() {
        return step;
    }

    public void setStep(String step) {
        this.step = step;
    }

    public String getResource() {
        return resource;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public DebugResourceState getResourceStatus() {
        return resourceStatus;
    }

    public void setResourceStatus(DebugResourceState resourceStatus) {
        this.resourceStatus = resourceStatus;
    }
}
