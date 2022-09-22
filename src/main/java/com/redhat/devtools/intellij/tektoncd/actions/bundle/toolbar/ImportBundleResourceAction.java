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
package com.redhat.devtools.intellij.tektoncd.actions.bundle.toolbar;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.ui.components.JBList;
import com.redhat.devtools.intellij.tektoncd.tkn.Resource;
import com.redhat.devtools.intellij.tektoncd.tkn.Tkn;
import org.jetbrains.annotations.NotNull;

import javax.swing.Icon;

public class ImportBundleResourceAction extends DumbAwareAction {

    private Tkn tkn;
    private JBList<Resource> resourcePanel;

    public ImportBundleResourceAction(String text, String description, Icon icon, JBList<Resource> resourcePanel, Tkn tkn) {
        super(text, description, icon);
        this.tkn = tkn;
        this.resourcePanel = resourcePanel;
    }
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {

    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        Resource selected = resourcePanel.getSelectedValue();
        e.getPresentation().setEnabled(selected!=null);
    }
}
