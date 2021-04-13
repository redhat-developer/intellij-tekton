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
package com.redhat.devtools.intellij.tektoncd.ui;

import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.components.JBScrollPane;
import com.redhat.devtools.intellij.tektoncd.settings.SettingsState;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.Arrays;
import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DeleteDialog extends DialogWrapper{
    private JCheckBox deleteRelatedResourcesChb;
    private JPanel myContentPanel;
    private JLabel deleteText;
    private GridBagConstraints gridBagConstraints;
    private boolean deleteRelatedResources;

    public DeleteDialog(Component parent, String title, String mainDeleteText, String deleteChkText) {
        super(null, parent, false, DialogWrapper.IdeModalityType.IDE);
        GridBagLayout gridBagLayout = new GridBagLayout();
        gridBagConstraints = new GridBagConstraints();
        this.myContentPanel = new JPanel(gridBagLayout);
        this.deleteRelatedResources = false;

        setTitle(title);
        fillContainer(mainDeleteText, deleteChkText);
        setOKButtonText("Delete");
        init();
    }

    public static void main(String[] args) {
        DeleteDialog dialog = new DeleteDialog(null, "", "", "");
        dialog.pack();
        dialog.show();
        System.exit(0);
    }

    @Override
    protected void doOKAction() {
        this.deleteRelatedResources = deleteRelatedResourcesChb != null ? deleteRelatedResourcesChb.isSelected() : false;
        super.doOKAction();
    }

    public boolean hasToDeleteRelatedResources() {
        return this.deleteRelatedResources;
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        final JPanel panel = new JPanel(new BorderLayout());
        panel.add(myContentPanel, BorderLayout.CENTER);
        return panel;
    }

    private void fillContainer(String mainDeleteText, String deleteChkText) {
        String[] itemsToDelete = mainDeleteText.split("\n");
        deleteText = new JLabel(itemsToDelete[0]);
        addComponent(deleteText, new EmptyBorder(10, 10, 5, 10), null, 0, 1, GridBagConstraints.NORTHWEST);

        if (itemsToDelete.length > 1) {
            Box box = Box.createVerticalBox();
            Arrays.stream(itemsToDelete).skip(1).forEach(item -> {
                if (!item.isEmpty()) {
                    JLabel currentStepLabel = new JLabel(item);
                    currentStepLabel.setBorder(new EmptyBorder(0, 0, 5, 0));
                    box.add(currentStepLabel);
                }
            });
            JScrollPane scroll = new JBScrollPane(box);
            Dimension scrollSize = itemsToDelete.length > 16 ? new Dimension(450, 400) : null;
            addComponent(scroll, new EmptyBorder(10, 10, 5, 10), scrollSize, 0, 2, GridBagConstraints.NORTHWEST);
        }

        if (!deleteChkText.isEmpty()) {
            deleteRelatedResourcesChb = new JCheckBox(deleteChkText);
            deleteRelatedResourcesChb.setSelected(SettingsState.getInstance().enableDeleteAllRelatedResourcesAsDefault);
            addComponent(deleteRelatedResourcesChb, new EmptyBorder(5, 10, 10, 10), null, 0, 4, GridBagConstraints.NORTHWEST);
        }
    }

    private JComponent addComponent(@NotNull JComponent component, Border border, Dimension preferredSize, @NotNull int col, @NotNull int row, @NotNull int anchor) {
        if (border != null) component.setBorder(border);
        if (preferredSize != null) component.setPreferredSize(preferredSize);
        gridBagConstraints.gridx = col;
        gridBagConstraints.gridy = row;
        gridBagConstraints.anchor = anchor;
        myContentPanel.add(component, gridBagConstraints);
        return component;
    }
}
