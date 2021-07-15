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

import com.google.common.base.Strings;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.util.IconLoader;
import com.intellij.ui.components.JBOptionButton;
import com.intellij.util.ui.JBUI;
import com.redhat.devtools.intellij.common.utils.function.TriConsumer;
import com.redhat.devtools.intellij.tektoncd.actions.InstallFromHubAction;
import com.redhat.devtools.intellij.tektoncd.hub.model.ResourceData;
import com.redhat.devtools.intellij.tektoncd.tree.TektonTreeStructure;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.function.Consumer;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import org.jetbrains.annotations.NotNull;


import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_CLUSTERTASK;
import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_PIPELINE;
import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_TASK;
import static com.redhat.devtools.intellij.tektoncd.ui.UIConstants.MAIN_BG_COLOR;
import static com.redhat.devtools.intellij.tektoncd.ui.UIConstants.MARGIN_10;

public class HubItem {

    private ResourceData resource;
    private JPanel parent, rightSide, bottomCenterPanel;

    public HubItem(@NotNull ResourceData resource) {
        this.resource = resource;
    }

    public JPanel createPanel(HubModel model, Consumer<HubItem> doSelectAction, TriConsumer<HubItem, String, String> doInstallAction) {

        Font defaultFont = (new JLabel()).getFont();

        JLabel nameHubItem = createCustomizedLabel(this.resource.getName(), null, -1, JBUI.Borders.empty(5), null, null, defaultFont.deriveFont(Font.BOLD), "");

        // bottom central panel - includes version/rating/kind
        bottomCenterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottomCenterPanel.setBackground(MAIN_BG_COLOR);

        JLabel version = createCustomizedLabel("v. " + this.resource.getLatestVersion().getVersion(), null, -1,
                                                                JBUI.Borders.empty(0, 0, 5, 5), null, JBUI.CurrentTheme.Label.disabledForeground(), null, "");
        bottomCenterPanel.add(version);

        JLabel rating = createCustomizedLabel(this.resource.getRating().toString(), AllIcons.Plugins.Rating, SwingConstants.RIGHT,
                                                                JBUI.Borders.empty(0, 5, 5, 5), null, JBUI.CurrentTheme.Label.disabledForeground(), null, "");
        bottomCenterPanel.add(rating);

        Icon kindIcon = getIconByKind(this.resource.getKind());
        if (kindIcon != null) {
            JLabel kind = createCustomizedLabel("", kindIcon, SwingConstants.LEFT,
                                                                JBUI.Borders.empty(0, 5, 5, 5), null, JBUI.CurrentTheme.Label.disabledForeground(), null, this.resource.getKind());
            bottomCenterPanel.add(kind);
        }

        String labelText = null;
        if (model.getIsTaskView()) {
            if (model.getTasksInstalled().contains(resource.getName())) {
                labelText = "A task with this name already exists on the cluster.";
            }
        } else {
            if (model.getClusterTasksInstalled().contains(resource.getName())) {
                labelText = "A clusterTask with this name already exists on the cluster.";
            }
        }
        if (labelText != null) {
            JLabel warningNameAlreadyUsed = createCustomizedLabel("", AllIcons.General.Warning, SwingConstants.CENTER,
                    JBUI.Borders.empty(0, 5, 5, 5), null, JBUI.CurrentTheme.Label.disabledForeground(), null, "A " + resource.getKind() + " with this name already exists on the cluster.");
            bottomCenterPanel.add(warningNameAlreadyUsed);
        }

        JBOptionButton optionButton;
        Action installAsTask = new InstallFromHubAction("Install as Task",
                () -> this,
                () -> KIND_TASK,
                () -> resource.getLatestVersion().getVersion(),
                () -> doInstallAction);
        Action installAsClusterTask = new InstallFromHubAction("Install as ClusterTask",
                () -> this,
                () -> KIND_CLUSTERTASK,
                () -> resource.getLatestVersion().getVersion(),
                () -> doInstallAction);
        if (model.getIsTaskView()) {
            optionButton = new JBOptionButton(installAsTask, new Action[] { installAsClusterTask });
        } else {
            optionButton = new JBOptionButton(installAsClusterTask, new Action[] { installAsTask });
        }

        rightSide = new JPanel(new BorderLayout());
        rightSide.setBackground(MAIN_BG_COLOR);
        rightSide.add(nameHubItem, BorderLayout.CENTER);
        rightSide.add(bottomCenterPanel, BorderLayout.PAGE_END);
        rightSide.add(optionButton, BorderLayout.LINE_END);

        parent = new JPanel(new BorderLayout());
        parent.setBackground(MAIN_BG_COLOR);
        parent.setMaximumSize(new Dimension(Integer.MAX_VALUE,90));
        parent.setBorder(MARGIN_10);
        parent.add(rightSide, BorderLayout.CENTER);

        Icon icon = null; // TODO get icon somewhere
        if (icon != null) {
            JLabel lblIcon = new JLabel("", icon, SwingConstants.LEFT);
            parent.add(lblIcon, BorderLayout.LINE_START);
        }

        paintAsSelected(model, doSelectAction);
        return parent;
    }

    public ResourceData getResource() {
        return this.resource;
    }

    public void repaint(boolean isSelected) {
        if (!isSelected) {
            parent.setBackground(MAIN_BG_COLOR);
            rightSide.setBackground(MAIN_BG_COLOR);
            bottomCenterPanel.setBackground(MAIN_BG_COLOR);
        } else {
            parent.setBackground(JBUI.CurrentTheme.StatusBar.hoverBackground());
            rightSide.setBackground(JBUI.CurrentTheme.StatusBar.hoverBackground());
            bottomCenterPanel.setBackground(JBUI.CurrentTheme.StatusBar.hoverBackground());
        }
    }

    private void paintAsSelected(HubModel model, Consumer<HubItem> doSelectAction) {
        String selected = model.getSelectedHubItem();
        if (!Strings.isNullOrEmpty(selected) && selected.equals(this.getResource().getName())) {
            parent.setBackground(JBUI.CurrentTheme.StatusBar.hoverBackground());
            rightSide.setBackground(JBUI.CurrentTheme.StatusBar.hoverBackground());
            bottomCenterPanel.setBackground(JBUI.CurrentTheme.StatusBar.hoverBackground());
            if (doSelectAction != null) {
                doSelectAction.accept(this);
            }
        }
    }

    private JLabel createCustomizedLabel(String text, Icon icon, int horizontalAlignment, Border border, Color background, Color foreground, Font font, String tooltip) {
        JLabel label = new JLabel(text);
        if (icon != null) {
            label.setIcon(icon);
        }
        if (horizontalAlignment > -1) {
            label.setHorizontalAlignment(horizontalAlignment);
        }
        if (border != null) {
            label.setBorder(border);
        }
        if (background != null) {
            label.setBackground(background);
        }
        if (foreground != null) {
            label.setForeground(foreground);
        }
        if (font != null) {
            label.setFont(font);
        }
        if (!tooltip.isEmpty()) {
            label.setToolTipText(tooltip);
        }
        return label;
    }

    private Icon getIconByKind(String kind) {
        if (kind.toLowerCase().equalsIgnoreCase(KIND_TASK)) {
            return IconLoader.findIcon("/images/task.svg", TektonTreeStructure.class);
        } else if (kind.toLowerCase().equalsIgnoreCase(KIND_PIPELINE)) {
            return IconLoader.findIcon("/images/pipeline.svg", TektonTreeStructure.class);
        }
        return null;
    }

    public void updateBottomPanel(JComponent component) {
        bottomCenterPanel.add(component);
        parent.revalidate();
        parent.repaint();
    }
}