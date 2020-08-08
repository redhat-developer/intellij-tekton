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

import com.redhat.devtools.intellij.tektoncd.tkn.component.field.Input;
import com.redhat.devtools.intellij.tektoncd.utils.StartResourceModel;
import java.awt.GridBagConstraints;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.JLabel;
import javax.swing.JTextField;

import static com.redhat.devtools.intellij.tektoncd.ui.UIContants.BORDER_COMPONENT_VALUE;
import static com.redhat.devtools.intellij.tektoncd.ui.UIContants.BORDER_LABEL_NAME;
import static com.redhat.devtools.intellij.tektoncd.ui.UIContants.FONT_COMPONENT_VALUE;
import static com.redhat.devtools.intellij.tektoncd.ui.UIContants.RED_BORDER_SHOW_ERROR;
import static com.redhat.devtools.intellij.tektoncd.ui.UIContants.ROW_DIMENSION;

public class ParametersStep extends BaseStep {

    List<JTextField> textFields;

    public ParametersStep(StartResourceModel model) {
        super("Parameters", model);
    }

    @Override
    public boolean isComplete() {
        if (textFields == null) return false;
        AtomicBoolean isComplete = new AtomicBoolean(true);
        textFields.stream().forEach(field -> {
            if (field.getText().isEmpty()) {
                field.setBorder(RED_BORDER_SHOW_ERROR);
                isComplete.set(false);
            }
        });
        return isComplete.get();
    }

    @Override
    public String getHelpId() {
        return "https://github.com/tektoncd/pipeline/blob/master/docs/pipelines.md#specifying-parameters";
    }

    public void setContent(StartResourceModel model) {
        textFields = new ArrayList<>();
        final int[] row = {0};

        model.getInputs().stream().filter(input -> input.kind() == Input.Kind.PARAMETER).forEach(input -> {
            String label = "<html><span style=\\\"font-family:serif;font-size:10px;font-weight:bold;\\\">" + input.name() + "</span>  <span style=\\\"font-family:serif;font-size:10;font-weight:normal;font-style:italic;\\\">(" + input.type() + ")</span></html>";
            String tooltip = input.description().isPresent() ? input.description().get() + "\n" : "";
            if (input.type().equals("string")) {
                tooltip += "The parameter " + input.name() + " expects a string value.";
            } else {
                tooltip += "The parameter " + input.name() + " expects an array value (e.g. val1,val2,val3 ...).";
            }
            JLabel lblNameParam = new JLabel(label);
            addComponent(lblNameParam, null, BORDER_LABEL_NAME, ROW_DIMENSION, 0, row[0], GridBagConstraints.NORTH);
            addTooltip(lblNameParam, tooltip);
            row[0] += 1;

            JTextField txtValueParam = new JTextField(input.defaultValue().orElse(""));
            textFields.add(txtValueParam);
            txtValueParam = (JTextField) addComponent(txtValueParam, FONT_COMPONENT_VALUE, BORDER_COMPONENT_VALUE, ROW_DIMENSION, 0, row[0], GridBagConstraints.NORTH);
            addListener(input.name(), txtValueParam);
            row[0] += 1;
        });
    }

    private void addListener(String idParam, JTextField txtValueParam) {
        // listen to when the focus is lost by the textbox and update the model so the preview shows the updated value
        txtValueParam.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                super.focusLost(e);
                setInputValue(idParam, txtValueParam.getText());
                // reset the border in case an error occured before and the border is red
                if (!txtValueParam.getText().isEmpty()) {
                    txtValueParam.setBorder(BORDER_COMPONENT_VALUE);
                }
                fireStateChanged();
            }
        });
    }
}
