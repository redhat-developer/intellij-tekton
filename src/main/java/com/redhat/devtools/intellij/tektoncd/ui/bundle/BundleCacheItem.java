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
package com.redhat.devtools.intellij.tektoncd.ui.bundle;

import com.redhat.devtools.intellij.tektoncd.tkn.Authenticator;
import com.redhat.devtools.intellij.tektoncd.tkn.Resource;

import java.util.List;

public class BundleCacheItem {
    private Authenticator authenticator;
    private List<Resource> resources;

    public BundleCacheItem(Authenticator authenticator, List<Resource> resources) {
        this.authenticator = authenticator;
        this.resources = resources;
    }

    public Authenticator getAuthenticator() {
        return authenticator;
    }

    public List<Resource> getResources() {
        return resources;
    }
}
