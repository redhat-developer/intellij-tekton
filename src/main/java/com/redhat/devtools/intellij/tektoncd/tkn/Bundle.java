/*******************************************************************************
 *  Copyright (c) 2022 Red Hat, Inc.
 *  Distributed under license by Red Hat, Inc. All rights reserved.
 *  This program is made available under the terms of the
 *  Eclipse Public License v2.0 which accompanies this distribution,
 *  and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 *  Contributors:
 *  Red Hat, Inc.
 ******************************************************************************/
package com.redhat.devtools.intellij.tektoncd.tkn;

import java.util.ArrayList;
import java.util.List;

public class Bundle {

    private final List<Resource> resources;
    private String name;
    private final static int BUNDLE_CAPACITY = 10;

    public Bundle() {
        resources = new ArrayList<>();
    }

    public void addResource(Resource resource) {
        if (hasSpace()) {
            resources.add(resource);
        }
    }

    public int getLayersSize() {
        return resources.size();
    }

    public boolean hasSpace() {
        return resources.size() < BUNDLE_CAPACITY;
    }

    public void removeResource(Resource resource) {
        resources.remove(resource);
    }

    public boolean hasResource(Resource resource) {
        return resources.stream().anyMatch(resource1 -> resource1.equals(resource));
    }

    public List<Resource> getResources() {
        return resources;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
