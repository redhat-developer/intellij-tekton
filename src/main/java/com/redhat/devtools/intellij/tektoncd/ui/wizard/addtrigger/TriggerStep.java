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
import com.redhat.devtools.intellij.tektoncd.utils.model.actions.ActionToRunModel;
import com.redhat.devtools.intellij.tektoncd.utils.model.actions.AddTriggerModel;
import com.redhat.devtools.intellij.tektoncd.utils.model.actions.StartResourceModel;
import java.awt.GridBagConstraints;
import java.util.List;
import java.util.Map;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;


import static com.redhat.devtools.intellij.tektoncd.ui.UIConstants.BORDER_COMPONENT_VALUE;
import static com.redhat.devtools.intellij.tektoncd.ui.UIConstants.ROMAN_PLAIN_13;
import static com.redhat.devtools.intellij.tektoncd.ui.UIConstants.TIMES_PLAIN_14;

public class TriggerStep extends BaseStep {

    private Map<String, String> triggerBindingTemplates;
    private JCheckBox chkSelectExistingTriggerBinding, chkCreateNewTriggerBinding;
    private JComboBox cmbExistingTriggerBindings, cmbPreMadeTriggerBindingTemplates;
    private JTextArea triggerBindingArea;
    private JScrollPane scrolltriggerBindingAreaPane;

    public TriggerStep(AddTriggerModel model, Map<String, String> triggerBindingTemplates) {
        super("Trigger", model);
        this.triggerBindingTemplates = triggerBindingTemplates;
        setContent();
    }

    @Override
    public void setContent(ActionToRunModel model) {
        final int[] row = {0};

        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = row[0];
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.gridwidth = 2;

        String infoText = "<html>The following options allow you to associate one or more bindings to the <br> event-listener which will be created automatically.<br> You are allowed to use both options at the same time to associate a <br> newly-created binding and/or existing ones.</html>";
        JLabel lblInfoText = new JLabel(infoText);
        addComponent(lblInfoText, ROMAN_PLAIN_13, new EmptyBorder(10, 0, 30, 0), null, gridBagConstraints);
        row[0] += 1;

        chkSelectExistingTriggerBinding = new JCheckBox("Select a TriggerBinding");
        chkSelectExistingTriggerBinding.setBounds(100,100, 50,50);
        chkSelectExistingTriggerBinding.setBackground(backgroundTheme);

        addComponent(chkSelectExistingTriggerBinding, ROMAN_PLAIN_13, null, null, 0, row[0], GridBagConstraints.NORTHWEST);

        JList existingTriggerBindingsList = new JBList();
        existingTriggerBindingsList.setEnabled(false);
        existingTriggerBindingsList.setListData(((AddTriggerModel)model).getTriggerBindings().keySet().toArray());
        existingTriggerBindingsList.setLayoutOrientation(JList.VERTICAL);

        JScrollPane scrollPane = new JBScrollPane();
        scrollPane.setViewportView(existingTriggerBindingsList);


        addComponent(scrollPane, TIMES_PLAIN_14, BORDER_COMPONENT_VALUE, null, 1, row[0], GridBagConstraints.NORTHWEST);
        /*cmbExistingTriggerBindings = new ComboBox();
        cmbExistingTriggerBindings.setEnabled(false);
        if (this.triggerBindings != null) {
            for (String el : this.triggerBindings) {

            }
        }*/
        chkSelectExistingTriggerBinding.addItemListener(itemEvent -> {
            if (chkSelectExistingTriggerBinding.isSelected()) {
                existingTriggerBindingsList.setEnabled(true);
            } else {
                existingTriggerBindingsList.setEnabled(false);
            }
        });

        //addComponent(cmbExistingTriggerBindings, TIMES_PLAIN_14, null, ROW_DIMENSION, 1, row[0], GridBagConstraints.NORTHWEST);
        row[0] += 1;

        chkCreateNewTriggerBinding = new JCheckBox("Create a new TriggerBinding");
        chkCreateNewTriggerBinding.setBounds(100,100, 50,50);
        chkCreateNewTriggerBinding.setBackground(backgroundTheme);

        addComponent(chkCreateNewTriggerBinding, ROMAN_PLAIN_13, null, null, 0, row[0], GridBagConstraints.WEST);
        row[0] += 1;

        JLabel lblSelectTemplate = new JLabel("Select a template");
        lblSelectTemplate.setEnabled(false);

        cmbPreMadeTriggerBindingTemplates = new ComboBox();
        cmbPreMadeTriggerBindingTemplates.setEnabled(false);
        cmbPreMadeTriggerBindingTemplates.addItem("");

        this.triggerBindingTemplates.keySet().stream().forEach(template -> cmbPreMadeTriggerBindingTemplates.addItem(template));

        cmbPreMadeTriggerBindingTemplates.addItemListener(itemEvent -> {
            if (itemEvent.getStateChange() == 1) {
                // when cmbPreMadeTriggerBindingTemplates combo box value changes, the new value is saved and preview is updated
                String templateSelected = (String) itemEvent.getItem();
                if (!templateSelected.isEmpty()) {
                    String content = this.triggerBindingTemplates.get(templateSelected);
                    triggerBindingArea.setText(content);
                }
                fireStateChanged();
            }
        });

        addComponent(lblSelectTemplate, ROMAN_PLAIN_13, null, null, 0, row[0], GridBagConstraints.WEST);
        addComponent(cmbPreMadeTriggerBindingTemplates, TIMES_PLAIN_14, null, null, 1, row[0], GridBagConstraints.NORTHWEST);
        row[0] += 1;

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = row[0];
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.gridwidth = 2;

        triggerBindingArea = new JTextArea(15, 50);
        scrolltriggerBindingAreaPane = new JBScrollPane(triggerBindingArea);
        scrolltriggerBindingAreaPane.setEnabled(false);
        triggerBindingArea.setEnabled(false);
        triggerBindingArea.setEditable(false);
        triggerBindingArea.setText(this.triggerBindingTemplates.get("empty-binding"));
        triggerBindingArea.setFont(ROMAN_PLAIN_13);

        addComponent(scrolltriggerBindingAreaPane, ROMAN_PLAIN_13, BORDER_COMPONENT_VALUE, null, gridBagConstraints);
        row[0] += 1;

        chkCreateNewTriggerBinding.addItemListener(itemEvent -> {
            if (chkCreateNewTriggerBinding.isSelected()) {
                scrolltriggerBindingAreaPane.setEnabled(true);
                cmbPreMadeTriggerBindingTemplates.setEnabled(true);
                triggerBindingArea.setEditable(true);
                triggerBindingArea.setEnabled(true);
            } else {
                scrolltriggerBindingAreaPane.setEnabled(false);
                cmbPreMadeTriggerBindingTemplates.setEnabled(false);
                triggerBindingArea.setEditable(false);
                triggerBindingArea.setEnabled(false);
            }
        });

        adjustContentPanel();
    }

    public void setContent() {

    }

    @Override
    public boolean isComplete() {
        if (!chkCreateNewTriggerBinding.isSelected() && !chkSelectExistingTriggerBinding.isSelected()) {
            return false;
        }

        return true;
    }

    @Override
    public String getHelpId() {
        return "https://github.com/tektoncd/pipeline/blob/master/docs/auth.md";
    }
}