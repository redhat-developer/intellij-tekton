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
package com.redhat.devtools.intellij.tektoncd.ui.wizard;

import com.intellij.ide.wizard.CommitStepException;
import com.intellij.ui.components.JBScrollPane;
import com.redhat.devtools.intellij.tektoncd.tkn.component.field.Input;
import com.redhat.devtools.intellij.tektoncd.utils.StartResourceModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Collection;

public abstract class BaseStep extends AbstractWizardStep {
    protected StartResourceModel model;
    protected JPanel mainPanel;
    protected JPanel contentPanel;
    protected GridBagLayout gridBagLayout;
    protected GridBagConstraints gridBagConstraints;
    protected Dimension defaultRowDimension;
    protected Font defaultFontLblName;
    protected Border defaultBorderName;
    protected Font defaultFontValueComponent;
    protected Border defaultBorderValue;
    protected Border defaultErrorBorderValue;
    protected int defaultAnchor;

    public BaseStep(@Nullable String title) {
        super(title);
    }

    public BaseStep(@Nullable String title, StartResourceModel model) {
        super(title);
        this.model = model;
    }

    public StartResourceModel getModel() {
        return this.model;
    }

    @Override
    public void commit(CommitType commitType) throws CommitStepException {
        switch(commitType) {
            case Next:

        }
    }

    @Override
    public JComponent getComponent() {
        return mainPanel;
    }

    @Nullable
    @Override
    public JComponent getPreferredFocusedComponent() {
        return mainPanel;
    }

    protected void fillComboBox(JComboBox comboBox, Collection<String> values, String defaultValue) {
        if (defaultValue != null) comboBox.addItem(defaultValue);
        for (String value : values) {
            comboBox.addItem(value);
        }
    }

    protected void initContentPanel() {
        // mainPanel is the panel displayed in the wizard as it is
        mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(255, 255, 255));

        gridBagLayout = new GridBagLayout();
        contentPanel = new JPanel(gridBagLayout);
        gridBagConstraints = new GridBagConstraints();

        contentPanel.setBackground(new Color(255, 255, 255));
        contentPanel.setBorder(new EmptyBorder(0, 10, 10, 10));

        JBScrollPane scroll = new JBScrollPane(contentPanel);
        scroll.setBorder(new EmptyBorder(0, 0, 0, 0));
        scroll.setBackground(new Color(255, 255, 255));

        mainPanel.add(scroll);

        initGraphics();
    }

    private void initGraphics() {
        defaultRowDimension = new Dimension(400, 33);
        defaultFontLblName = new Font("TimesRoman", Font.BOLD, 11);
        defaultBorderName = new EmptyBorder(10, 0, 0, 0);

        defaultFontValueComponent = new Font("TimesRoman", Font.PLAIN, 14);
        defaultBorderValue = new MatteBorder(1, 1, 2, 1, new Color(212, 212, 212));
        defaultAnchor = GridBagConstraints.NORTH;

        defaultErrorBorderValue = new MatteBorder(1, 1, 2, 1, new Color(229, 77, 4));
    }

    protected void adjustContentPanel() {
        // this code allows content to stick to top left corner even when the window is resized manually
        // add an extra row consuming vertical extra space
        int nRows = contentPanel.getComponentCount();
        gridBagLayout.rowHeights = new int[nRows + 1];
        gridBagLayout.rowWeights = new double[nRows + 1];
        gridBagLayout.rowWeights[nRows] = 1;
        // add an extra column consuming extra horizontal space
        gridBagLayout.columnWidths = new int[]{0, 0};
        gridBagLayout.columnWeights = new double[]{0, 1};
    }

    protected JComponent addComponent(@NotNull JComponent component, Font font, Border border, Dimension preferredSize, @NotNull int col, @NotNull int row, @NotNull int anchor) {
        if (font != null) component.setFont(font);
        if (border != null) component.setBorder(border);
        if (preferredSize != null) component.setPreferredSize(preferredSize);
        gridBagConstraints.gridx = col;
        gridBagConstraints.gridy = row;
        gridBagConstraints.anchor = anchor;
        contentPanel.add(component, gridBagConstraints);
        return component;
    }

    protected void addTooltip(@NotNull JComponent component, String textToDisplay) {
        if (!textToDisplay.isEmpty()) {
            component.setToolTipText(textToDisplay);
        }
    }

    protected void setInputValue(String inputName, String value) {
        for (Input input: model.getInputs()) {
            if (input.name().equals(inputName)) {
                input.setValue(value);
                break;
            }
        }
    }
}
