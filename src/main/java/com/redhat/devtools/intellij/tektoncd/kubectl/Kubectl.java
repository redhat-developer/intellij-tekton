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
package com.redhat.devtools.intellij.tektoncd.kubectl;

import java.io.IOException;

public interface Kubectl {
    /**
     * Create a Tekton resource in the cluster. Namespace parameter may be null
     * for CusterTask resources.
     *
     * @param namespace the namespace to create the resource in or null for ClusterTask
     * @param path the path to the resource definition file
     * @throws IOException if communication errored
     */
    void create(String namespace, String path) throws IOException;

    /**
     *  Apply a configuration to Tekton resource in the cluster. Namespace parameter may be null
     *  for CusterTask resources.
     *
     * @param namespace the namespace to update the resource in or null for ClusterTask
     * @param path the path to the resource definition file
     * @throws IOException if communication errored
     */
    void apply(String namespace, String path) throws IOException;
}
