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

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.util.ui.JBDimension;
import com.redhat.devtools.intellij.tektoncd.tree.ParentableNode;
import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.jetbrains.annotations.Nullable;

public class HubDialog extends DialogWrapper {

    private JComponent myContentPanel;

    public HubDialog(Project project, HubModel model) {
        super(project, true);
        setTitle("Import from Tekton Hub");
        createComponent(model);
        super.init();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        final JPanel panel = new JPanel(new BorderLayout());
        panel.add(myContentPanel, BorderLayout.CENTER);
        panel.setMinimumSize(new JBDimension(800, 600));
        panel.setPreferredSize(new Dimension(800, 600));
        return panel;
    }

    public void createComponent(HubModel model) {
        HubMarketplaceTab marketplaceTab = new HubMarketplaceTab(model);
        myContentPanel = marketplaceTab.getTabPanel();
    }
}
