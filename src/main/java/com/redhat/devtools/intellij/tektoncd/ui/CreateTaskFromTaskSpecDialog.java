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
package com.redhat.devtools.intellij.tektoncd.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.redhat.devtools.intellij.tektoncd.utils.CRDHelper;
import com.redhat.devtools.intellij.tektoncd.utils.DeployHelper;
import com.redhat.devtools.intellij.tektoncd.utils.TreeHelper;
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.IOException;
import java.util.Enumeration;
import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


import static com.redhat.devtools.intellij.tektoncd.Constants.APIVERSION_BETA;
import static com.redhat.devtools.intellij.tektoncd.ui.UIConstants.ROW_DIMENSION;
import static com.redhat.devtools.intellij.tektoncd.ui.UIConstants.TIMES_PLAIN_10;

public class CreateTaskFromTaskSpecDialog extends DialogWrapper {
    private Project project;
    private JPanel myContentPanel;
    private GridBagConstraints gridBagConstraints;
    private JTextField txtName;
    private ButtonGroup group;
    private JLabel lblError;

    public CreateTaskFromTaskSpecDialog(Project project) {
        super(null, null, false, DialogWrapper.IdeModalityType.IDE);
        GridBagLayout gridBagLayout = new GridBagLayout();
        gridBagConstraints = new GridBagConstraints();
        this.myContentPanel = new JPanel(gridBagLayout);
        this.project = project;

        setTitle("Create Task from TaskSpec");
        fillContainer();
        setOKButtonText("Create");
        myOKAction.setEnabled(false);
        init();
        lblError.setVisible(false);
    }

    public static void main(String[] args) {
        CreateTaskFromTaskSpecDialog dialog = new CreateTaskFromTaskSpecDialog(null);
        dialog.pack();
        dialog.show();
        System.exit(0);
    }

    @Override
    protected void doOKAction() {
        if (existsTask()) {
            lblError.setText("A " + getKind().toLowerCase() + " with this name already exists. Please change it and try again.");
            lblError.setVisible(true);
        } else {
            super.doOKAction();
        }
    }

    private boolean existsTask() {
        CustomResourceDefinitionContext crdContext = CRDHelper.getCRDContext(APIVERSION_BETA, TreeHelper.getPluralKind(getKind()));
        if (crdContext == null) {
            return false;
        }
        try {
            return DeployHelper.existsResource(this.project, getName(), crdContext);
        } catch (IOException e) {
            return false;
        }
    }

    public String getName() {
        return txtName.getText();
    }

    public String getKind() {
        for (Enumeration<AbstractButton> buttons = group.getElements(); buttons.hasMoreElements();) {
            AbstractButton button = buttons.nextElement();

            if (button.isSelected()) {
                return button.getText();
            }
        }
        return "";
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        final JPanel panel = new JPanel(new BorderLayout());
        panel.add(myContentPanel, BorderLayout.CENTER);
        panel.setPreferredSize(new Dimension(430, 150));
        return panel;
    }

    @Override
    public @Nullable JComponent getPreferredFocusedComponent() {
        return txtName;
    }

    private void fillContainer() {
        JLabel lnlName = new JLabel("Name:");
        addComponent(lnlName, new EmptyBorder(10, 10, 5, 0), null, 0, 1, GridBagConstraints.NORTHWEST);

        txtName = new JTextField("");
        addListener(txtName);
        addComponent(txtName, null, ROW_DIMENSION, 0, 2, GridBagConstraints.NORTHWEST);

        lblError = new JLabel("");
        lblError.setForeground(Color.red);
        lblError.setFont(TIMES_PLAIN_10);
        addComponent(lblError, new EmptyBorder(3, 10, 5, 0), null, 0, 3, GridBagConstraints.NORTHWEST);

        JRadioButton option1 = new JRadioButton("Task");
        option1.setSelected(true);
        JRadioButton option2 = new JRadioButton("ClusterTask");
        group = new ButtonGroup();
        group.add(option1);
        group.add(option2);
        JPanel radioButtonsPanel = new JPanel(new FlowLayout());
        radioButtonsPanel.add(new JLabel("Save as "));
        radioButtonsPanel.add(option1);
        radioButtonsPanel.add(option2);
        addComponent(radioButtonsPanel, new EmptyBorder(10, 10, 5, 0), null, 0, 4, GridBagConstraints.NORTHWEST);
    }

    private void addListener(JTextField txtValueParam) {
        txtValueParam.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                update();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                update();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                update();
            }

            public void update() {
                lblError.setVisible(false);
                myOKAction.setEnabled(!txtValueParam.getText().isEmpty());
            }
        });
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
