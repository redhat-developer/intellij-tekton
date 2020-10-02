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
package com.redhat.devtools.intellij.tektoncd.ui.wizard.addtrigger;

import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBScrollPane;
import com.redhat.devtools.intellij.tektoncd.ui.wizard.BaseStep;
import com.redhat.devtools.intellij.tektoncd.utils.model.actions.AddTriggerModel;
import com.redhat.devtools.intellij.tektoncd.utils.model.resources.TriggerBindingConfigurationModel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.util.Map;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;


import static com.redhat.devtools.intellij.tektoncd.ui.UIConstants.BORDER_COMPONENT_VALUE;
import static com.redhat.devtools.intellij.tektoncd.ui.UIConstants.BORDER_LABEL_NAME;
import static com.redhat.devtools.intellij.tektoncd.ui.UIConstants.ROMAN_PLAIN_13;
import static com.redhat.devtools.intellij.tektoncd.ui.UIConstants.TIMES_PLAIN_14;

public class TriggerStep extends BaseStep {

    private JCheckBox chkSelectExistingTriggerBinding, chkCreateNewTriggerBinding;
    private JComboBox cmbPreMadeTriggerBindingTemplates;
    private JTextArea textAreaNewTriggerBinding;
    private JScrollPane scrollTriggerBindingAreaPane;
    private JList listBindingsAvailableOnCluster;
    private JLabel lblErrorNewBinding;

    public TriggerStep(AddTriggerModel model, Map<String, String> triggerBindingTemplates) {
        super("Trigger", model);
        setContent(triggerBindingTemplates);
    }

    @Override
    public void setContent() {}

    public void setContent(Map<String, String> triggerBindingTemplates) {
        final int[] row = {0};

        String infoText = "<html>The following options allow you to associate one or more bindings to the event-listener<br>";
               infoText += "which will be created automatically. You are allowed to use either or both of the options<br>";
               infoText += "to associate a newly-created binding and/or existing ones.</html>";
        JLabel lblInfoText = new JLabel(infoText);
        addComponent(lblInfoText, ROMAN_PLAIN_13, new EmptyBorder(10, 0, 30, 0), new Dimension(594, 91), buildGridBagConstraints(0, row[0], 2, GridBagConstraints.NORTHWEST));
        row[0] += 1;

        chkSelectExistingTriggerBinding = new JCheckBox("Select a TriggerBinding");
        chkSelectExistingTriggerBinding.setBounds(100,100, 50,50);
        chkSelectExistingTriggerBinding.setBackground(backgroundTheme);

        addComponent(chkSelectExistingTriggerBinding, ROMAN_PLAIN_13, null, null, 0, row[0], GridBagConstraints.NORTHWEST);

        listBindingsAvailableOnCluster = new JBList();
        listBindingsAvailableOnCluster.setEnabled(false);
        listBindingsAvailableOnCluster.setListData(((AddTriggerModel)model).getBindingsAvailableOnCluster().keySet().toArray());
        listBindingsAvailableOnCluster.setLayoutOrientation(JList.VERTICAL);

        JScrollPane scrollPane = new JBScrollPane();
        scrollPane.setViewportView(listBindingsAvailableOnCluster);
        int width = (int) listBindingsAvailableOnCluster.getPreferredSize().getWidth();
        scrollPane.setPreferredSize(new Dimension(width < 250 ? 250 : width, 150));

        addComponent(scrollPane, TIMES_PLAIN_14, BORDER_COMPONENT_VALUE, null, 1, row[0], GridBagConstraints.NORTHWEST);

        chkSelectExistingTriggerBinding.addItemListener(itemEvent -> {
            if (chkSelectExistingTriggerBinding.isSelected()) {
                listBindingsAvailableOnCluster.setEnabled(true);
            } else {
                listBindingsAvailableOnCluster.setEnabled(false);
            }
        });

        row[0] += 1;

        chkCreateNewTriggerBinding = new JCheckBox("Create a new TriggerBinding");
        chkCreateNewTriggerBinding.setBounds(100,100, 50,50);
        chkCreateNewTriggerBinding.setBackground(backgroundTheme);

        addComponent(chkCreateNewTriggerBinding, ROMAN_PLAIN_13, new EmptyBorder(20, 0, 0, 0), null, 0, row[0], GridBagConstraints.WEST);
        row[0] += 1;

        JLabel lblSelectTemplate = new JLabel("Select a template");
        lblSelectTemplate.setEnabled(false);

        cmbPreMadeTriggerBindingTemplates = new ComboBox();
        cmbPreMadeTriggerBindingTemplates.setEnabled(false);
        cmbPreMadeTriggerBindingTemplates.addItem("");

        triggerBindingTemplates.keySet().stream().forEach(template -> cmbPreMadeTriggerBindingTemplates.addItem(template));

        cmbPreMadeTriggerBindingTemplates.addItemListener(itemEvent -> {
            if (itemEvent.getStateChange() == 1) {
                // when cmbPreMadeTriggerBindingTemplates combo box value changes, the new value is saved and preview is updated
                String templateSelected = (String) itemEvent.getItem();
                if (!templateSelected.isEmpty()) {
                    String content = triggerBindingTemplates.get(templateSelected);
                    textAreaNewTriggerBinding.setText(content);
                }
                fireStateChanged();
            }
        });

        addComponent(lblSelectTemplate, ROMAN_PLAIN_13, BORDER_LABEL_NAME, null, 0, row[0], GridBagConstraints.WEST);
        addComponent(cmbPreMadeTriggerBindingTemplates, TIMES_PLAIN_14, BORDER_LABEL_NAME, null, 1, row[0], GridBagConstraints.NORTHWEST);
        row[0] += 1;

        JLabel lblWarningUniqueName = new JLabel("* If a trigger binding with the same name already exists, it will be overwritten");
        addComponent(lblWarningUniqueName, ROMAN_PLAIN_13, BORDER_LABEL_NAME, null, buildGridBagConstraints(0, row[0], 2, GridBagConstraints.NORTHWEST));
        row[0] += 1;

        lblErrorNewBinding = new JLabel();
        lblErrorNewBinding.setVisible(false);
        lblErrorNewBinding.setForeground(Color.RED);
        addComponent(lblErrorNewBinding, ROMAN_PLAIN_13, BORDER_LABEL_NAME, null, buildGridBagConstraints(0, row[0], 2, GridBagConstraints.NORTHWEST));
        row[0] += 1;

        textAreaNewTriggerBinding = new JTextArea(15, 35);
        scrollTriggerBindingAreaPane = new JBScrollPane(textAreaNewTriggerBinding);
        scrollTriggerBindingAreaPane.setEnabled(false);
        textAreaNewTriggerBinding.setEnabled(false);
        textAreaNewTriggerBinding.setEditable(false);
        textAreaNewTriggerBinding.setText(triggerBindingTemplates.get("empty-binding"));
        textAreaNewTriggerBinding.setFont(ROMAN_PLAIN_13);

        addComponent(scrollTriggerBindingAreaPane, ROMAN_PLAIN_13, BORDER_COMPONENT_VALUE, null, buildGridBagConstraints(0, row[0], 2, GridBagConstraints.NORTHWEST));
        row[0] += 1;

        chkCreateNewTriggerBinding.addItemListener(itemEvent -> {
            if (chkCreateNewTriggerBinding.isSelected()) {
                lblSelectTemplate.setEnabled(true);
                scrollTriggerBindingAreaPane.setEnabled(true);
                cmbPreMadeTriggerBindingTemplates.setEnabled(true);
                textAreaNewTriggerBinding.setEditable(true);
                textAreaNewTriggerBinding.setEnabled(true);
            } else {
                lblSelectTemplate.setEnabled(false);
                scrollTriggerBindingAreaPane.setEnabled(false);
                cmbPreMadeTriggerBindingTemplates.setEnabled(false);
                textAreaNewTriggerBinding.setEditable(false);
                textAreaNewTriggerBinding.setEnabled(false);
            }
        });

        adjustContentPanel();
    }

    private GridBagConstraints buildGridBagConstraints(int col, int row, int gridWidth, int anchor) {
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = col;
        gridBagConstraints.gridy = row;
        gridBagConstraints.anchor = anchor;
        gridBagConstraints.gridwidth = gridWidth;
        return gridBagConstraints;
    }

    @Override
    public boolean isComplete() {
        // trigger bindings are optional in an event listener so users can select none or everything - the result is always valid
        lblErrorNewBinding.setVisible(false);
        ((AddTriggerModel) model).getBindingsSelectedByUser().clear();
        if (chkSelectExistingTriggerBinding.isSelected()) {
            listBindingsAvailableOnCluster.getSelectedValuesList().forEach(binding -> {
                String bindingAsYAML = ((AddTriggerModel) model).getBindingsAvailableOnCluster().get(binding.toString());
                ((AddTriggerModel) model).getBindingsSelectedByUser().put((String) binding, bindingAsYAML);
            });
        }
        if (chkCreateNewTriggerBinding.isSelected()) {
            String configuration = textAreaNewTriggerBinding.getText();
            TriggerBindingConfigurationModel bindingModel = new TriggerBindingConfigurationModel(configuration);
            if (!bindingModel.isValid()) {
                // if the new binding written by the user is not valid, we should show an error message with some info
                lblErrorNewBinding.setText(bindingModel.getErrorMessage());
                lblErrorNewBinding.setVisible(true);
                return false;
            }
            ((AddTriggerModel) model).setNewBindingAdded(configuration);
        }

        return true;
    }

    @Override
    public String getHelpId() {
        return "https://github.com/tektoncd/triggers/blob/master/docs/triggerbindings.md";
    }
}