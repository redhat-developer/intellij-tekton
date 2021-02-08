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

import com.redhat.devtools.intellij.tektoncd.tkn.Resource;
import com.redhat.devtools.intellij.tektoncd.tkn.component.field.Input;
import com.redhat.devtools.intellij.tektoncd.utils.model.actions.ActionToRunModel;
import java.awt.GridBagConstraints;
import javax.swing.JComboBox;
import javax.swing.JLabel;


import static com.redhat.devtools.intellij.tektoncd.ui.UIConstants.BORDER_LABEL_NAME;
import static com.redhat.devtools.intellij.tektoncd.ui.UIConstants.ROW_DIMENSION;
import static com.redhat.devtools.intellij.tektoncd.ui.UIConstants.TIMES_PLAIN_14;

public class InputResourcesStep extends BaseStep {

    public InputResourcesStep(ActionToRunModel model) {
        super("Input Resources", model);
    }

    @Override
    public boolean isComplete() {
        return true;
    }

    @Override
    public String getHelpId() {
        return "https://github.com/tektoncd/pipeline/blob/master/docs/resources.md";
    }

    public void setContent() {
        final int[] row = {0};

        model.getInputResources().stream().filter(input -> input.kind() == Input.Kind.RESOURCE).forEach(input -> {
            JLabel lblNameResource = new JLabel("<html><span style=\\\"font-family:serif;font-size:10px;font-weight:bold;\\\">" + input.name() + "</span></html");
            addComponent(lblNameResource, null, BORDER_LABEL_NAME, ROW_DIMENSION, 0, row[0], GridBagConstraints.NORTH);
            addTooltip(lblNameResource, input.description().orElse(""));
            row[0] += 1;

            JComboBox cmbValueResource = new JComboBox();
            cmbValueResource = (JComboBox) addComponent(cmbValueResource, TIMES_PLAIN_14, null, ROW_DIMENSION, 0, row[0], GridBagConstraints.NORTH);
            fillComboBox(cmbValueResource, input);
            addListener(input.name(), cmbValueResource);
            row[0] += 1;
        });
    }

    private void fillComboBox(JComboBox comboBox, Input input) {
        for (Resource resource: model.getPipelineResources()) {
            if (resource.type().equals(input.type())) {
                comboBox.addItem(resource.name());
            }
        }
        if (input.value() != null) {
            comboBox.setSelectedItem(input.value());
        }
    }

    private void addListener(String idParam, JComboBox cmbValueResource) {
        // listener for when value in resources value input combo box changes
        cmbValueResource.addItemListener(itemEvent -> {
            if (itemEvent.getStateChange() == 1) {
                // when inputResourceValuesCB combo box value changes, the new value is saveds and preview is updated
                String resourceSelected = (String) itemEvent.getItem();
                setInputValue(model.getInputResources(), idParam, resourceSelected);
                fireStateChanged();
            }
        });
    }
}
