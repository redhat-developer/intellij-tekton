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
import static com.redhat.devtools.intellij.tektoncd.ui.UIConstants.ROMAN_PLAIN_13;
import static com.redhat.devtools.intellij.tektoncd.ui.UIConstants.TIMES_PLAIN_14;

public class TriggerStep extends BaseStep {

    private Map<String, String> triggerBindingTemplates;
    private JCheckBox chkSelectExistingTriggerBinding, chkCreateNewTriggerBinding;
    private JComboBox cmbPreMadeTriggerBindingTemplates;
    private JTextArea textAreaNewTriggerBinding;
    private JScrollPane scrolltriggerBindingAreaPane;
    private JList listBindingsAvailableOnCluster;

    public TriggerStep(AddTriggerModel model, Map<String, String> triggerBindingTemplates) {
        super("Trigger", model);
        this.triggerBindingTemplates = triggerBindingTemplates;
        setContent();
    }

    @Override
    public void setContent(ActionToRunModel model) {

    }

    public void setContent() {
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

        listBindingsAvailableOnCluster = new JBList();
        listBindingsAvailableOnCluster.setEnabled(false);
        listBindingsAvailableOnCluster.setListData(((AddTriggerModel)model).getBindingsAvailableOnCluster().toArray());
        listBindingsAvailableOnCluster.setLayoutOrientation(JList.VERTICAL);



        JScrollPane scrollPane = new JBScrollPane();
        scrollPane.setViewportView(listBindingsAvailableOnCluster);


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
                    textAreaNewTriggerBinding.setText(content);
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

        textAreaNewTriggerBinding = new JTextArea(15, 50);
        scrolltriggerBindingAreaPane = new JBScrollPane(textAreaNewTriggerBinding);
        scrolltriggerBindingAreaPane.setEnabled(false);
        textAreaNewTriggerBinding.setEnabled(false);
        textAreaNewTriggerBinding.setEditable(false);
        textAreaNewTriggerBinding.setText(this.triggerBindingTemplates.get("empty-binding"));
        textAreaNewTriggerBinding.setFont(ROMAN_PLAIN_13);

        //TODO add comment "N.B make sure to use a unique name. If a trigger binding with the same name already exists its content will be overwritten
        addComponent(scrolltriggerBindingAreaPane, ROMAN_PLAIN_13, BORDER_COMPONENT_VALUE, null, gridBagConstraints);
        row[0] += 1;

        chkCreateNewTriggerBinding.addItemListener(itemEvent -> {
            if (chkCreateNewTriggerBinding.isSelected()) {
                scrolltriggerBindingAreaPane.setEnabled(true);
                cmbPreMadeTriggerBindingTemplates.setEnabled(true);
                textAreaNewTriggerBinding.setEditable(true);
                textAreaNewTriggerBinding.setEnabled(true);
            } else {
                scrolltriggerBindingAreaPane.setEnabled(false);
                cmbPreMadeTriggerBindingTemplates.setEnabled(false);
                textAreaNewTriggerBinding.setEditable(false);
                textAreaNewTriggerBinding.setEnabled(false);
            }
        });

        adjustContentPanel();
    }

    @Override
    public boolean isComplete() {
        // TODO verify if i can create an el without any binding
        if (!chkCreateNewTriggerBinding.isSelected() && !chkSelectExistingTriggerBinding.isSelected()) {
            return false;
        }

        ((AddTriggerModel) model).getBindingsSelectedByUser().clear();
        if (chkSelectExistingTriggerBinding.isSelected()) {
            listBindingsAvailableOnCluster.getSelectedValuesList().forEach(binding -> ((AddTriggerModel) model).getBindingsSelectedByUser().put(binding.toString(), null));
        }
        if (chkCreateNewTriggerBinding.isSelected()) {
            String t = textAreaNewTriggerBinding.getText();
            //TODO verify if configuration is valid and extract namespace
            ((AddTriggerModel) model).getBindingsSelectedByUser().put("test", t);
        }

        return true;
    }

    @Override
    public String getHelpId() {
        return "https://github.com/tektoncd/pipeline/blob/master/docs/auth.md";
    }
}