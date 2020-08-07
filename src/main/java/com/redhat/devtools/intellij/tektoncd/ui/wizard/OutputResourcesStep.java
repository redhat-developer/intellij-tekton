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
import com.redhat.devtools.intellij.tektoncd.tkn.component.field.Output;
import com.redhat.devtools.intellij.tektoncd.utils.StartResourceModel;
import java.awt.GridBagConstraints;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import org.jetbrains.annotations.NotNull;

public class OutputResourcesStep extends BaseStep {

    public OutputResourcesStep(StartResourceModel model) {
        super("Output Resources", model);
        setContent(model);
    }

    @NotNull
    @Override
    public Object getStepId() {
        return "OutputResourcesStep";
    }

    @Override
    public boolean isComplete() {
        return true;
    }

    @Override
    public String getHelpId() {
        return "https://github.com/tektoncd/pipeline/blob/master/docs/resources.md";
    }

    private void setContent(StartResourceModel model) {
        initContentPanel();
        final int[] row = {0};

        model.getOutputs().stream().forEach(output -> {
            JLabel lblNameResource = new JLabel("<html><span style=\\\"font-family:serif;font-size:10px;font-weight:bold;\\\">" + output.name() + "</span></html");
            addComponent(lblNameResource, null, defaultBorderName, defaultRowDimension, 0, row[0], defaultAnchor);
            addTooltip(lblNameResource, output.description().orElse(""));
            row[0] += 1;

            JComboBox cmbValueResource = new JComboBox();
            cmbValueResource = (JComboBox) addComponent(cmbValueResource, defaultFontValueComponent, defaultBorderValue, defaultRowDimension, 0, row[0], GridBagConstraints.NORTH);
            for (Resource resource : model.getResources()) {
                if (resource.type().equals(output.type())) {
                    cmbValueResource.addItem(resource);
                }
            }
            if (output.value() != null) {
                cmbValueResource.setSelectedItem(output.value());
            }
            addListener(output.name(), cmbValueResource);
            row[0] += 1;
        });

        adjustContentPanel();
    }

    private void addListener(String idParam, JComboBox cmbValueResource) {
        // listener for when value in output resources combo box changes
        cmbValueResource.addItemListener(itemEvent -> {
            if (itemEvent.getStateChange() == 1) {
                // when outputsResourcesCB combo box value changes, the new value is saved and preview is updated
                Resource resourceSelected = (Resource) itemEvent.getItem();
                setOutputValue(idParam, resourceSelected.name());
                fireStateChanged();
            }
        });
    }

    private void setOutputValue(String outputName, String value) {
        for (Output output : model.getOutputs()) {
            if (output.name().equals(outputName)) {
                output.setValue(value);
                break;
            }
        }
    }
}
