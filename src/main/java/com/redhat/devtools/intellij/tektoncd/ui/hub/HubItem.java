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
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.util.ui.JBUI;
import com.redhat.devtools.intellij.tektoncd.hub.model.ResourceData;
import com.redhat.devtools.intellij.tektoncd.tree.TektonTreeStructure;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import org.jetbrains.annotations.NotNull;


import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_PIPELINE;
import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_TASK;
import static com.redhat.devtools.intellij.tektoncd.ui.hub.HubDialogTab.MAIN_BG_COLOR;

public class HubItem {

    private ResourceData resource;
    public JPanel parent, rightSide, bottomCenterPanel;

    public HubItem(@NotNull ResourceData resource) {
        this.resource = resource;
    }

    public JPanel createPanel(Project project, String namespace, boolean alreadyOnCluster, Consumer<HubItem> doSelectAction) {

        JLabel lblNameHubItem = new JLabel(this.resource.getName());
        lblNameHubItem.setBorder(new EmptyBorder(5, 5, 5, 5));
        lblNameHubItem.setFont(lblNameHubItem.getFont().deriveFont(Font.BOLD));

        // bottom central panel - includes version/rating/kind
        bottomCenterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottomCenterPanel.setBackground(MAIN_BG_COLOR);

        JLabel version = new JLabel("v. " + this.resource.getLatestVersion().getVersion());
        version.setBorder(new EmptyBorder(0, 0, 5, 5));
        version.setForeground(JBUI.CurrentTheme.Label.disabledForeground());
        bottomCenterPanel.add(version);

        JLabel rating = new JLabel(this.resource.getRating().toString(), AllIcons.Plugins.Rating, SwingConstants.RIGHT);
        rating.setBorder(new EmptyBorder(0, 5, 5, 5));
        rating.setForeground(JBUI.CurrentTheme.Label.disabledForeground());
        bottomCenterPanel.add(rating);

        Icon kindIcon = getIconByKind(this.resource.getKind());
        if (kindIcon != null) {
            JLabel kind = new JLabel("", kindIcon, SwingConstants.LEFT);
            kind.setBorder(new EmptyBorder(0, 5, 5, 5));
            kind.setForeground(JBUI.CurrentTheme.Label.disabledForeground());
            kind.setToolTipText(this.resource.getKind());
            bottomCenterPanel.add(kind);
        }

        if (alreadyOnCluster) {
            JLabel warningNameAlreadyUsed = new JLabel("", AllIcons.General.Warning, SwingConstants.CENTER);
            warningNameAlreadyUsed.setToolTipText("A " + resource.getKind() + " with this name already exists on the cluster.");
            bottomCenterPanel.add(warningNameAlreadyUsed);
        }

        JLabel installBtn = new JLabel("Install", SwingConstants.CENTER);
        installBtn.setForeground(JBUI.CurrentTheme.Link.linkColor());
        installBtn.setPreferredSize(new Dimension(80, 60));
        Border outside = new MatteBorder(1, 1, 1, 1, JBUI.CurrentTheme.Link.linkColor());
        Border inside = new EmptyBorder(3, 0, 3, 0);
        CompoundBorder cb = BorderFactory.createCompoundBorder(outside, inside);
        installBtn.setBorder(cb);
        installBtn.setBackground(MAIN_BG_COLOR);
        installBtn.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {

            }

            @Override
            public void mousePressed(MouseEvent e) {
                String confirmationMessage = "";
                if (alreadyOnCluster) {
                    confirmationMessage = "A " + resource.getKind() + " with this name already exists on the cluster. By installing this " + resource.getKind() + " the one on the cluster will be overwritten. Do you want to install it?";
                } else {
                    confirmationMessage = "Do you want to install this " + resource.getKind() + " to the cluster?";
                }
                try {
                    boolean installed = HubModel.getInstance().installHubItem(project, namespace, resource.getLatestVersion().getRawURL().toString(), confirmationMessage);
                    if (!installed) {
                        // TODO show a warning/error message??
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {

            }

            @Override
            public void mouseEntered(MouseEvent e) {

            }

            @Override
            public void mouseExited(MouseEvent e) {

            }
        });

        rightSide = new JPanel(new BorderLayout());
        rightSide.setBackground(MAIN_BG_COLOR);
        rightSide.add(lblNameHubItem, BorderLayout.CENTER);
        rightSide.add(bottomCenterPanel, BorderLayout.PAGE_END);
        rightSide.add(installBtn, BorderLayout.LINE_END);

        parent = new JPanel(new BorderLayout());
        parent.setBackground(MAIN_BG_COLOR);
        parent.setMaximumSize(new Dimension(Integer.MAX_VALUE,80));
        parent.setBorder(new EmptyBorder(10, 10, 10, 10));
        parent.add(rightSide, BorderLayout.CENTER);

        Icon icon = null; // TODO get icon somewhere
        if (icon != null) {
            JLabel lblIcon = new JLabel("", icon, SwingConstants.LEFT);
            parent.add(lblIcon, BorderLayout.LINE_START);
        }

        String selected = HubModel.getInstance().getSelectedHubItem();
        if (!Strings.isNullOrEmpty(selected) && selected.equals(this.getResource().getName())) {
            parent.setBackground(JBUI.CurrentTheme.StatusBar.hoverBackground());
            rightSide.setBackground(JBUI.CurrentTheme.StatusBar.hoverBackground());
            bottomCenterPanel.setBackground(JBUI.CurrentTheme.StatusBar.hoverBackground());
            doSelectAction.accept(this);
        }
        return parent;
    }

    public ResourceData getResource() {
        return this.resource;
    }

    private Icon getIconByKind(String kind) {
        if (kind.toLowerCase().equalsIgnoreCase(KIND_TASK)) {
            return IconLoader.findIcon("/images/task.svg", TektonTreeStructure.class);
        } else if (kind.toLowerCase().equalsIgnoreCase(KIND_PIPELINE)) {
            return IconLoader.findIcon("/images/pipeline.svg", TektonTreeStructure.class);
        }
        return null;
    }
}