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

import com.redhat.devtools.intellij.common.tree.IconTreeNode;
import com.redhat.devtools.intellij.common.tree.LazyMutableTreeNode;
import com.redhat.devtools.intellij.tektoncd.tkn.Tkn;
import com.redhat.devtools.intellij.tektoncd.tkn.TknCli;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;

import javax.swing.tree.DefaultMutableTreeNode;
import java.io.IOException;

public class TektonRootNode extends LazyMutableTreeNode implements IconTreeNode {
  private KubernetesClient client = loadClient();
  private boolean logged;
  private final TektonTreeModel model;
  private Tkn tkn;

  private static final String ERROR = "Please log in to the cluster";

  private static final String NO_TEKTON = "Tekton not installed on the cluster";

  public TektonRootNode(TektonTreeModel model) {
    setUserObject(client.getMasterUrl());
    this.model = model;
  }

  public KubernetesClient getClient() {
    return client;
  }

  private KubernetesClient loadClient() {
    return new DefaultKubernetesClient(new ConfigBuilder().build());
  }

  public boolean isLogged() {
    return logged;
  }

  public void setLogged(boolean logged) {
    this.logged = logged;
  }

  public Tkn getTkn() throws IOException {
    if (tkn == null) {
        tkn = TknCli.get();
    }
    return tkn;
  }

  public TektonTreeModel getModel() {
    return model;
  }

  @Override
  public void load() {
    super.load();
    try {
      Tkn tkn = getTkn();
      if (tkn.isTektonAware(getClient())) {
        tkn.getNamespaces(getClient()).forEach(name -> add(new NamespaceNode(name)));
        setLogged(true);
      } else {
        add(new DefaultMutableTreeNode(NO_TEKTON));
      }
    } catch (Exception e) {
      add(new DefaultMutableTreeNode(ERROR));
    }
  }

  @Override
  public void reload() {
    client = loadClient();
    super.reload();
  }

  @Override
  public String getIconName() {
    return "/images/cluster.png";
  }
}
