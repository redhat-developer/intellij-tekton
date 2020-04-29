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
package com.redhat.devtools.intellij.tektoncd.tree;

import com.intellij.openapi.project.Project;
import com.redhat.devtools.intellij.tektoncd.tkn.Tkn;
import com.redhat.devtools.intellij.tektoncd.tkn.TknCli;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;

import java.io.IOException;

public class TektonRootNode {
  private KubernetesClient client = loadClient();
  private final Project project;
  private Tkn tkn;

  public TektonRootNode(Project project) {
    this.project = project;
  }

  public KubernetesClient getClient() {
    return client;
  }

  private KubernetesClient loadClient() {
    return new DefaultKubernetesClient(new ConfigBuilder().build());
  }

  public Tkn getTkn() throws IOException {
    if (tkn == null) {
        tkn = TknCli.get(project);
    }
    return tkn;
  }

  public void load() {
      client = loadClient();
  }
}
