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
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.JLabel;
import javax.swing.JTextField;
import org.jetbrains.annotations.NotNull;

public class ParametersStep extends BaseStep {

    List<JTextField> textFields;

    public ParametersStep(StartResourceModel model) {
        super("Parameters", model);
        textFields = new ArrayList<>();
        setContent(model);
    }

    @NotNull
    @Override
    public Object getStepId() {
        return "ParametersStep";
    }

    @Override
    public boolean isComplete() {
        AtomicBoolean isComplete = new AtomicBoolean(true);
        textFields.stream().forEach(field -> {
            if (field.getText().isEmpty()) {
                field.setBorder(defaultErrorBorderValue);
                isComplete.set(false);
            }
        });
        return isComplete.get();
    }

    @Override
    public String getHelpId() {
        return "https://github.com/tektoncd/pipeline/blob/master/docs/pipelines.md#specifying-parameters";
    }

    private void setContent(StartResourceModel model) {
        initContentPanel();
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
            addComponent(lblNameParam, null, defaultBorderName, defaultRowDimension, 0, row[0], defaultAnchor);
            addTooltip(lblNameParam, tooltip);
            row[0] += 1;

            JTextField txtValueParam = new JTextField(input.defaultValue().orElse(""));
            textFields.add(txtValueParam);
            txtValueParam = (JTextField) addComponent(txtValueParam, defaultFontValueComponent, defaultBorderValue, defaultRowDimension, 0, row[0], defaultAnchor);
            addListener(input.name(), txtValueParam);
            row[0] += 1;
        });

        adjustContentPanel();
    }

    private void addListener(String idParam, JTextField txtValueParam) {
        // listen to when the focus is lost from the textbox then the model is updated so the preview shows the current model
        txtValueParam.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                super.focusLost(e);
                setInputValue(idParam, txtValueParam.getText());
                // reset the border in case an error occured before and the border is red
                if (!txtValueParam.getText().isEmpty()) {
                    txtValueParam.setBorder(defaultBorderValue);
                }
                fireStateChanged();
            }
        });
    }
}
