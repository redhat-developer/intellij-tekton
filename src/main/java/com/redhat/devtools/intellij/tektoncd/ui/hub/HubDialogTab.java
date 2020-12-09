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
import com.intellij.ui.SearchTextField;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.ui.border.CustomLineBorder;
import com.intellij.ui.components.JBPanelWithEmptyText;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextField;
import com.intellij.ui.scale.JBUIScale;
import com.intellij.util.Alarm;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.StatusText;
import java.awt.BorderLayout;
import java.awt.event.KeyEvent;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.ScrollPaneConstants;
import org.jetbrains.annotations.NotNull;


import static com.redhat.devtools.intellij.tektoncd.ui.UIConstants.GRAY_COLOR;
import static com.redhat.devtools.intellij.tektoncd.ui.UIConstants.MAIN_BG_COLOR;
import static com.redhat.devtools.intellij.tektoncd.ui.UIConstants.SEARCH_BG_COLOR;
import static com.redhat.devtools.intellij.tektoncd.ui.UIConstants.SEARCH_FIELD_BORDER_COLOR;

public abstract class HubDialogTab {

    protected final Alarm mySearchUpdateAlarm = new Alarm();
    protected HubModel model;
    protected HubDetailsPageComponent myDetailsPage;
    protected JBPanelWithEmptyText myEmptyPanel;
    private JComponent myContentPanel;
    protected JPanel innerContentPanel;
    protected SearchTextField mySearchTextField;
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
        // main content panel
        this.createSearchTextField(250);
        myContentPanel = createContentPanel();

        JPanel leftColumnPanel = new JPanel(new BorderLayout());
        leftColumnPanel.add(mySearchTextField, "North");
        leftColumnPanel.add(myContentPanel);
        tabPanel = new OnePixelSplitter(false, 0.30F) {
            protected Divider createDivider() {
                Divider divider = super.createDivider();
                divider.setBackground(SEARCH_FIELD_BORDER_COLOR);
                return divider;
            }
        };
        tabPanel.setFirstComponent(leftColumnPanel);

        myDetailsPage = new HubDetailsPageComponent(model);
        tabPanel.setSecondComponent(myDetailsPage);
    }

    protected void createSearchTextField(final int flyDelay) {
        mySearchTextField = new SearchTextField() {
            @Override
            protected boolean preprocessEventForTextField(KeyEvent event) {
                int keyCode = event.getKeyCode();
                int id = event.getID();

                if (keyCode == KeyEvent.VK_ENTER || event.getKeyChar() == '\n') {
                    return true;
                }
                if ((keyCode == KeyEvent.VK_DOWN || keyCode == KeyEvent.VK_UP) && id == KeyEvent.KEY_PRESSED) {
                    return true;
                }
                return super.preprocessEventForTextField(event);
            }
        };
        mySearchTextField.setBorder(JBUI.Borders.customLine(SEARCH_FIELD_BORDER_COLOR));
        JBTextField editor = mySearchTextField.getTextEditor();
        editor.putClientProperty("JTextField.Search.Gap", JBUIScale.scale(6));
        editor.putClientProperty("JTextField.Search.GapEmptyText", JBUIScale.scale(-1));
        editor.setBorder(JBUI.Borders.empty(6, 6));
        editor.setOpaque(true);
        editor.setBackground(SEARCH_BG_COLOR);
        String text = "Search for task or pipeline"; //"Type / to see options";
        StatusText emptyText = mySearchTextField.getTextEditor().getEmptyText();
        emptyText.appendText(text, new SimpleTextAttributes(0, GRAY_COLOR));
    }

    protected abstract void updateDetailsPanel(HubItem item);

    @NotNull
    protected abstract JComponent createContentPanel();

    protected JComponent createScrollPane(@NotNull JPanel panel) {
        JBScrollPane pane = new JBScrollPane(panel, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        pane.setBackground(MAIN_BG_COLOR);
        pane.setBorder(JBUI.Borders.empty());
        return pane;
    }

    public JComponent getTabPanel() {
        return tabPanel;
    }
}
