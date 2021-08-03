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

import com.intellij.icons.AllIcons;
import com.intellij.openapi.util.IconLoader;
import com.intellij.ui.components.JBOptionButton;
import com.intellij.util.ui.JBUI;
import com.redhat.devtools.intellij.common.utils.function.TriConsumer;
import com.redhat.devtools.intellij.tektoncd.actions.InstallFromHubAction;
import com.redhat.devtools.intellij.tektoncd.tree.TektonTreeStructure;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.Border;


import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_CLUSTERTASK;
import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_PIPELINE;
import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_TASK;
import static com.redhat.devtools.intellij.tektoncd.ui.UIConstants.MAIN_BG_COLOR;

public class HubItemPanel {

    private HubItemPanelsBoard board;
    private HubItem hubItem;
    private HubModel model;
    private TriConsumer<HubItem, String, String> doInstallAction;
    private JPanel parent;


    public HubItemPanel(HubItem hubItem, HubItemPanelsBoard board, TriConsumer<HubItem, String, String> doInstallAction) {
        this.hubItem = hubItem;
        this.board = board;
        this.model = board.getModel();
        this.doInstallAction = doInstallAction;
    }

    public void setActive(boolean isActive) {
        repaint(isActive);
    }

    public HubItem getHubItem() {
        return hubItem;
    }

    public JPanel build() {
        JComponent centerComponent = createCenterSideComponent();
        JComponent leftComponent = createLeftSideComponent();

        parent = new JPanel(new BorderLayout());
        parent.setBackground(MAIN_BG_COLOR);
        parent.setMaximumSize(new Dimension(Integer.MAX_VALUE,70));
        parent.setBorder(JBUI.Borders.empty(3));
        parent.add(centerComponent, BorderLayout.CENTER);
        if (leftComponent != null) {
            parent.add(leftComponent, BorderLayout.LINE_START);
        }

        parent.addMouseListener(getMouseAdapter(this));

        return parent;
    }

    private MouseAdapter getMouseAdapter(HubItemPanel self) {
        return new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                board.updateActiveHubItemPanel(self);
                setActive(true);
                board.openItemDetailsPanel(hubItem);
            }
        };
    }

    private JComponent createCenterSideComponent() {
        Font defaultFont = (new JLabel()).getFont();
        JLabel nameHubItem = createCustomizedLabel(this.hubItem.getResource().getName(), null, -1, JBUI.Borders.empty(2, 5, 1, 2), null, null, defaultFont.deriveFont(Font.BOLD), "");

        JPanel bottomCenterPanel = createBottomComponent();

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(MAIN_BG_COLOR);
        panel.add(nameHubItem, BorderLayout.CENTER);
        panel.add(bottomCenterPanel, BorderLayout.PAGE_END);

        if (doInstallAction != null) {
            JBOptionButton installOptionsButton = createInstallOptionButton();
            panel.add(installOptionsButton, BorderLayout.LINE_END);
        }

        return panel;
    }

    private JPanel createBottomComponent() {
        // bottom central panel - includes version/rating/kind
        JPanel bottomCenterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottomCenterPanel.setBackground(MAIN_BG_COLOR);

        JLabel lblVersion = createCustomizedLabel("v. " + hubItem.getVersion(), null, -1,
                JBUI.Borders.empty(0, 0, 1, 5), null, JBUI.CurrentTheme.Label.disabledForeground(), null, "");
        bottomCenterPanel.add(lblVersion);

        JLabel rating = createCustomizedLabel(hubItem.getResource().getRating().toString(), AllIcons.Plugins.Rating, SwingConstants.RIGHT,
                JBUI.Borders.empty(0, 5, 1, 5), null, JBUI.CurrentTheme.Label.disabledForeground(), null, "");
        bottomCenterPanel.add(rating);

        Icon kindIcon = getIconByKind(hubItem.getKind());
        if (kindIcon != null) {
            JLabel kind = createCustomizedLabel("", kindIcon, SwingConstants.LEFT,
                    JBUI.Borders.empty(0, 5, 1, 5), null, JBUI.CurrentTheme.Label.disabledForeground(), null, hubItem.getResource().getKind());
            bottomCenterPanel.add(kind);
        }
        String labelText = getLabelForExistingResource();
        if (!labelText.isEmpty()) {
            JLabel warningNameAlreadyUsed = createCustomizedLabel("", AllIcons.General.Warning, SwingConstants.CENTER,
                    JBUI.Borders.empty(0, 5, 1, 5), null, JBUI.CurrentTheme.Label.disabledForeground(), null, "A " + hubItem.getResource().getKind() + " with this name already exists on the cluster.");
            bottomCenterPanel.add(warningNameAlreadyUsed);
        }
        return bottomCenterPanel;
    }

    private JComponent createLeftSideComponent() {
        Icon icon = null; // TODO get icon somewhere
        if (icon != null) {
            return new JLabel("", icon, SwingConstants.LEFT);
        }
        return null;
    }

    private JBOptionButton createInstallOptionButton() {
        JBOptionButton optionButton;
        Action install = new InstallFromHubAction("Install",
                () -> hubItem,
                () -> hubItem.getResource().getKind(),
                () -> hubItem.getVersion(),
                () -> doInstallAction);

        if (hubItem.getResource().getKind().equalsIgnoreCase(KIND_TASK)) {
            Action installAsClusterTask = new InstallFromHubAction("Install as ClusterTask",
                    () -> hubItem,
                    () -> KIND_CLUSTERTASK,
                    () -> hubItem.getVersion(),
                    () -> doInstallAction);
            if (model.getIsClusterTaskView()) {
                ((InstallFromHubAction)install).setText("Install as Task");
                optionButton = new JBOptionButton(installAsClusterTask, new Action[]{install});
            } else {
                optionButton = new JBOptionButton(install, new Action[]{installAsClusterTask});
            }
        } else {
            optionButton = new JBOptionButton(install, null);
        }
        return optionButton;
    }

    private void repaint(boolean isActive) {
        if (!isActive) {
            setBackground(parent, MAIN_BG_COLOR);
        } else {
            setBackground(parent, JBUI.CurrentTheme.StatusBar.hoverBackground());
        }
        parent.repaint();
        parent.revalidate();
    }

    private void setBackground(JPanel parent, Color background) {
        parent.setBackground(background);
        Arrays.stream(parent.getComponents()).forEach(component -> {
            if (component instanceof JPanel) {
                component.setBackground(background);
                setBackground((JPanel) component, background);
            }
        });
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
        } else if (kind.toLowerCase().equalsIgnoreCase(KIND_CLUSTERTASK)) {
            return IconLoader.findIcon("/images/clustertask.svg", TektonTreeStructure.class);
        } else if (kind.toLowerCase().equalsIgnoreCase(KIND_PIPELINE)) {
            return IconLoader.findIcon("/images/pipeline.svg", TektonTreeStructure.class);
        }
        return null;
    }

    private String getLabelForExistingResource() {
        String labelText = "";
        String resource = hubItem.getResource().getName();
        switch(hubItem.getKind().toLowerCase()) {
            case KIND_TASK: {
                if (board.getModel().getTasksInstalled().stream().anyMatch(pp -> pp.getMetadata().getName().equalsIgnoreCase(resource))) {
                    labelText = "A task with this name already exists on the cluster.";
                }
                break;
            }
            case KIND_CLUSTERTASK: {
                if (board.getModel().getClusterTasksInstalled().stream().anyMatch(pp -> pp.getMetadata().getName().equalsIgnoreCase(resource))) {
                    labelText = "A clusterTask with this name already exists on the cluster.";
                }
                break;
            }
            case KIND_PIPELINE: {
                if (board.getModel().getPipelinesInstalled().stream().anyMatch(pp -> pp.getMetadata().getName().equalsIgnoreCase(resource))) {
                    labelText = "A pipeline with this name already exists on the cluster.";
                }
                break;
            }
            default: {
                break;
            }
        }
        return labelText;
    }
}
