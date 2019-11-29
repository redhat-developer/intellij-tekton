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
package com.redhat.devtools.intellij.tektoncd.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;
import com.redhat.devtools.intellij.common.actions.TreeAction;
import com.redhat.devtools.intellij.tektoncd.kubectl.Kubectl;
import com.redhat.devtools.intellij.tektoncd.kubectl.KubectlCli;

import javax.swing.tree.TreePath;
import java.io.IOException;

public class KubectlAction extends TreeAction {
  public KubectlAction(Class... filters) {
    super(filters);
  }

  @Override
  public void actionPerformed(AnActionEvent anActionEvent, TreePath path, Object selected) {
    try {
      this.actionPerformed(anActionEvent, path, selected, getKubectl(anActionEvent));
    } catch (IOException e) {
      Messages.showErrorDialog("Error: " + e.getLocalizedMessage(), "Error");
    }
  }

    private Kubectl getKubectl(AnActionEvent anActionEvent) throws IOException {
        return KubectlCli.get();
    }

    public void actionPerformed(AnActionEvent anActionEvent, TreePath path, Object selected, Kubectl kubectl) {
  }
}
