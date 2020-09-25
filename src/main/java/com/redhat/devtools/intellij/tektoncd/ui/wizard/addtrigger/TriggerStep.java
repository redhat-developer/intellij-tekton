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
import com.redhat.devtools.intellij.tektoncd.ui.wizard.BaseStep;
import com.redhat.devtools.intellij.tektoncd.utils.StartResourceModel;
import java.awt.GridBagConstraints;
import java.util.List;
import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JRadioButton;


import static com.redhat.devtools.intellij.tektoncd.ui.UIConstants.BORDER_LABEL_NAME;
import static com.redhat.devtools.intellij.tektoncd.ui.UIConstants.ROW_DIMENSION;
import static com.redhat.devtools.intellij.tektoncd.ui.UIConstants.TIMES_PLAIN_14;

public class TriggerStep extends BaseStep {

    private List<String> triggerBindings;

    public TriggerStep(StartResourceModel model, List<String> triggerBindings) {
        super("Trigger", model);
        this.triggerBindings = triggerBindings;
        setContent();
    }

    @Override
    public void setContent(StartResourceModel model) {
    }

    public void setContent() {
        final int[] row = {0};

        JLabel lblServiceAccount = new JLabel("<html><span style=\\\"font-family:serif;font-size:10px;font-weight:bold;\\\">Select an EventListener</span>  <span style=\\\"font-family:serif;font-size:10;font-weight:normal;font-style:italic;\\\">(optional)</span></html");
        addComponent(lblServiceAccount, null, BORDER_LABEL_NAME, ROW_DIMENSION, 0, row[0], GridBagConstraints.NORTH);
        row[0] += 1;

        JRadioButton btnUseExistingEventListener = new JRadioButton("Use existing EventListener");
        btnUseExistingEventListener.setActionCommand("bb");
        btnUseExistingEventListener.setSelected(true);

        JComboBox cmbValueResource = new ComboBox();
        cmbValueResource = (JComboBox) addComponent(cmbValueResource, TIMES_PLAIN_14, null, ROW_DIMENSION, 0, row[0], GridBagConstraints.NORTH);
        if (this.triggerBindings != null) {
            for (String el : this.triggerBindings) {
                cmbValueResource.addItem(el);
            }
        }
        //addListener(name, cmbValueResource);
        row[0] += 1;

        JRadioButton btnCreateNewEventListener = new JRadioButton("Create new EventListener");
        btnCreateNewEventListener.setActionCommand("cc");

        ButtonGroup group = new ButtonGroup();
        // if no el is available don't show first option
        if (!this.triggerBindings.isEmpty()) {
            group.add(btnUseExistingEventListener);
        }
        group.add(btnCreateNewEventListener);
        adjustContentPanel();
    }

    @Override
    public boolean isComplete() {
        return true;
    }

    @Override
    public String getHelpId() {
        return "https://github.com/tektoncd/pipeline/blob/master/docs/auth.md";
    }

    /* private void addListener(String idParam, JComboBox cmbValueResource) {
        // listener for when value in tsa combo box changes
        cmbValueResource.addItemListener(itemEvent -> {
            // when combo box value change update sa value
            if (itemEvent.getStateChange() == 1) {
                String serviceAccountSelected = (String) itemEvent.getItem();
                if (idParam.equals(GLOBALSA)) {
                    model.setServiceAccount(serviceAccountSelected);
                } else {
                    model.getTaskServiceAccounts().put(idParam, serviceAccountSelected);
                }
                fireStateChanged();
            }
        });
    } */
}