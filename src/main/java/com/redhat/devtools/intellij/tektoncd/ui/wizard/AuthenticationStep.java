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

import com.redhat.devtools.intellij.tektoncd.utils.model.actions.ActionToRunModel;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.border.EmptyBorder;


import static com.redhat.devtools.intellij.tektoncd.ui.UIConstants.BORDER_LABEL_NAME;
import static com.redhat.devtools.intellij.tektoncd.ui.UIConstants.ROW_DIMENSION;
import static com.redhat.devtools.intellij.tektoncd.ui.UIConstants.TIMES_PLAIN_14;

public class AuthenticationStep extends BaseStep {

    private static final String GLOBALSA = "Global Service Account";

    public AuthenticationStep(ActionToRunModel model) {
        super("Authentication", model);
    }

    @Override
    public boolean isComplete() {
        return true;
    }

    @Override
    public String getHelpId() {
        return "https://github.com/tektoncd/pipeline/blob/master/docs/auth.md";
    }

    public void setContent() {
        final int[] row = {0};

        List<String> serviceAccounts = new ArrayList<>(model.getTaskServiceAccounts().keySet());
        serviceAccounts.add(0, GLOBALSA);

        serviceAccounts.forEach(name -> {
            if (row[0] == 2) {
                String infoText = "<html>The following fields allow you to map a Service Account to<br>a specific Task in the Pipeline. This overrides the global<br> Service Account set above.</html>";
                JLabel lblInfoText = new JLabel(infoText);
                addComponent(lblInfoText, new Font("TimesRoman", Font.PLAIN, 13), new EmptyBorder(30, 0, 10, 0), new Dimension(400, 80), 0, row[0], GridBagConstraints.NORTH);
                row[0] += 1;
            }

            JLabel lblServiceAccount = new JLabel("<html><span style=\\\"font-family:serif;font-size:10px;font-weight:bold;\\\">" + name + "</span>  <span style=\\\"font-family:serif;font-size:10;font-weight:normal;font-style:italic;\\\">(optional)</span></html");
            addComponent(lblServiceAccount, null, BORDER_LABEL_NAME, ROW_DIMENSION, 0, row[0], GridBagConstraints.NORTH);
            row[0] += 1;

            JComboBox cmbValueResource = new JComboBox();
            cmbValueResource = (JComboBox) addComponent(cmbValueResource, TIMES_PLAIN_14, null, ROW_DIMENSION, 0, row[0], GridBagConstraints.NORTH);
            fillComboBox(name, cmbValueResource);
            addListener(name, cmbValueResource);
            row[0] += 1;
        });
    }

    private void fillComboBox(String saAssociatedToComboBox, JComboBox comboBox) {
        comboBox.addItem("");
        for (String value : model.getServiceAccounts()) {
            comboBox.addItem(value);
        }

        if (saAssociatedToComboBox.equals(GLOBALSA)) {
            if (!model.getServiceAccount().isEmpty()) {
                comboBox.setSelectedItem(model.getServiceAccount());
            }
        } else {
            if (model.getTaskServiceAccounts().containsKey(saAssociatedToComboBox) &&
                    !model.getTaskServiceAccounts().get(saAssociatedToComboBox).isEmpty()) {
                comboBox.setSelectedItem(model.getTaskServiceAccounts().get(saAssociatedToComboBox));
            }
        }
    }

    private void addListener(String idParam, JComboBox cmbValueResource) {
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
    }
}

