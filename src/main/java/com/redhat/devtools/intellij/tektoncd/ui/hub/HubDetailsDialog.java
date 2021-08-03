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
package com.redhat.devtools.intellij.tektoncd.ui.hub;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.redhat.devtools.intellij.common.utils.function.TriConsumer;
import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.jetbrains.annotations.Nullable;

public class HubDetailsDialog extends DialogWrapper {

    private HubDetailsPageComponent hubDetailsPageComponent;

    public HubDetailsDialog(String title, @Nullable Project project, HubModel model) {
        super(project, true);
        setTitle(title);
        this.hubDetailsPageComponent = new HubDetailsPageComponent(model);
        super.init();
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        final JPanel panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(new Dimension(600, 400));
        panel.add(hubDetailsPageComponent, BorderLayout.CENTER);
        return panel;
    }

    public void show(HubItem item, TriConsumer<HubItem, String, String> callback) {
        hubDetailsPageComponent.show(item, callback);
        super.show();
    }
}
