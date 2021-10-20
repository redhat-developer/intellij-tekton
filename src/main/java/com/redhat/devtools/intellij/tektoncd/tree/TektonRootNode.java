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
import com.redhat.devtools.intellij.tektoncd.tkn.TknCliFactory;

import java.util.concurrent.CompletableFuture;

public class TektonRootNode {
  private final Project project;
  private Tkn tkn;

  public TektonRootNode(Project project) {
    this.project = project;
  }

  public CompletableFuture<Tkn> initializeTkn() {
    TknCliFactory.getInstance().resetTkn(project);
    return TknCliFactory.getInstance().getTkn(project).whenComplete((tkn, err) -> this.tkn = tkn);
  }

  public Tkn getTkn() {
    return tkn;
  }

  public CompletableFuture<Tkn> loadTkn() {
    return TknCliFactory.getInstance().getTkn(project);
  }

  public Project getProject() {
    return project;
  }
}
