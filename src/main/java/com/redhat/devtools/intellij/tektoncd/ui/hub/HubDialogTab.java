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

import com.intellij.openapi.ui.Divider;
import com.intellij.ui.OnePixelSplitter;
import com.intellij.ui.border.CustomLineBorder;
import com.intellij.ui.components.JBPanelWithEmptyText;
import com.intellij.util.ui.JBUI;
import javax.swing.JComponent;
import org.jetbrains.annotations.NotNull;


import static com.redhat.devtools.intellij.tektoncd.ui.UIConstants.MAIN_BG_COLOR;
import static com.redhat.devtools.intellij.tektoncd.ui.UIConstants.SEARCH_FIELD_BORDER_COLOR;

public abstract class HubDialogTab {

    protected HubModel model;
    protected HubDetailsPageComponent myDetailsPage;
    protected JBPanelWithEmptyText myEmptyPanel;
    private JComponent myContentPanel;
    private OnePixelSplitter tabPanel;

    public HubDialogTab(HubModel model) {
        this.model = model;
        createEmptyPanel();
        createPanel();
    }

    private void createEmptyPanel() {
        // empty panel to be shown if no results is available
        myEmptyPanel = new JBPanelWithEmptyText();
        myEmptyPanel.setBorder(new CustomLineBorder(SEARCH_FIELD_BORDER_COLOR, JBUI.insets(1, 0, 0, 0)));
        myEmptyPanel.setOpaque(true);
        myEmptyPanel.setBackground(MAIN_BG_COLOR);
        myEmptyPanel.getEmptyText().setText("Nothing found.");
    }

    private void createPanel() {
        myDetailsPage = new HubDetailsPageComponent(model);
        myContentPanel = createContentPanel();

        tabPanel = new OnePixelSplitter(false, 0.37F) {
            protected Divider createDivider() {
                Divider divider = super.createDivider();
                divider.setBackground(SEARCH_FIELD_BORDER_COLOR);
                return divider;
            }
        };
        tabPanel.setFirstComponent(myContentPanel);
        tabPanel.setSecondComponent(myDetailsPage);
    }

    @NotNull
    protected abstract JComponent createContentPanel();

    public JComponent getTabPanel() {
        return tabPanel;
    }
}
