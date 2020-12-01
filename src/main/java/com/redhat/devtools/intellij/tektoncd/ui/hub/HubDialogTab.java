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
import com.intellij.openapi.ui.Divider;
import com.intellij.ui.Gray;
import com.intellij.ui.JBColor;
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
import com.intellij.util.ui.UIUtil;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.KeyEvent;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.ScrollPaneConstants;
import org.jetbrains.annotations.NotNull;

public abstract class HubDialogTab {

    protected final Alarm mySearchUpdateAlarm = new Alarm();
    protected HubDetailsPageComponent myDetailsPage;
    protected JBPanelWithEmptyText myEmptyPanel;
    private JComponent myContentPanel;
    protected JPanel innerContentPanel;
    protected SearchTextField mySearchTextField;
    private OnePixelSplitter tabPanel;
    public static final Color MAIN_BG_COLOR =
            JBColor.namedColor("Plugins.background", new JBColor(() -> JBColor.isBright() ? UIUtil.getListBackground() : new Color(0x313335)));
    public static final Color GRAY_COLOR = JBColor.namedColor("Label.infoForeground", new JBColor(Gray._120, Gray._135));
    public static final Color SEARCH_FIELD_BORDER_COLOR =
            JBColor.namedColor("Plugins.SearchField.borderColor", new JBColor(0xC5C5C5, 0x515151));
    public static final Color SEARCH_BG_COLOR = JBColor.namedColor("Plugins.SearchField.background", MAIN_BG_COLOR);

    public HubDialogTab(Project project, String namespace, List<String> tasks) {
        createPanel(project, namespace, tasks);
    }

    private void createPanel(Project project, String namespace, List<String> tasks) {
        // empty panel to be shown if no results is available
        myEmptyPanel = new JBPanelWithEmptyText();
        myEmptyPanel.setBorder(new CustomLineBorder(SEARCH_FIELD_BORDER_COLOR, JBUI.insets(1, 0, 0, 0)));
        myEmptyPanel.setOpaque(true);
        myEmptyPanel.setBackground(MAIN_BG_COLOR);
        myEmptyPanel.getEmptyText().setText("Nothing found.");

        // main content panel
        this.createSearchTextField(250);
        myContentPanel = createContentPanel();

        JPanel listPanel = new JPanel(new BorderLayout());
        listPanel.add(this.mySearchTextField, "North");
        listPanel.add(this.myContentPanel);
        tabPanel = new OnePixelSplitter(false, 0.45F) {
            protected Divider createDivider() {
                Divider divider = super.createDivider();
                divider.setBackground(SEARCH_FIELD_BORDER_COLOR);
                return divider;
            }
        };
        tabPanel.setFirstComponent(listPanel);

        myDetailsPage = new HubDetailsPageComponent(project, namespace, tasks);
        tabPanel.setSecondComponent(myDetailsPage);
    }

    @NotNull
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

    @NotNull
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
