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
package com.redhat.devtools.intellij.tektoncd.ui.component;

import com.google.common.base.Strings;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.redhat.devtools.intellij.tektoncd.utils.JSONHelper;
import com.redhat.devtools.intellij.tektoncd.tkn.Resource;
import com.redhat.devtools.intellij.tektoncd.tkn.component.field.Input;
import com.redhat.devtools.intellij.tektoncd.tkn.component.field.Output;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.List;
import java.util.TimerTask;

public class StartTaskDialog extends DialogWrapper {
    Logger logger = LoggerFactory.getLogger(StartTaskDialog.class);
    private JPanel contentPane;
    private JTextField inputParamValueTxt;
    private JComboBox inputResourceValuesCB;
    private JComboBox inputParamsCB;
    private JComboBox outputsCB;
    private JComboBox outputsResourcesCB;
    private JTextArea previewTextArea;
    private JButton previewRefreshBtn;
    private JLabel outInfoMessage;
    private JLabel inInfoMessage;
    private JButton startTaskButton;
    private JLabel errorLbl;
    private JButton cancelButton;
    private JComboBox inputResourcesCB;
    private JPanel inputParamsPanel;
    private JPanel inputResourcesPanel;
    private JPanel outputsPanel;
    private JLabel inputParamsLbl;
    private JLabel inputParamValueLbl;
    private JLabel outputsLbl;
    private JLabel outputsResourceLbl;
    private JLabel inputResourceValuesLbl;
    private JLabel inputResourcesLbl;
    private List<Input> inputs;
    private List<Resource> resources;
    private List<Output> outputs;

    private String namespace;
    private String taskName;
    private String args;

    public StartTaskDialog(Component parent, String task, List<Resource> resources) {
        super((Project) null, parent, false, IdeModalityType.IDE);
        try {
            this.namespace = JSONHelper.getNamespace(task);
            this.taskName = JSONHelper.getName(task);
            if (Strings.isNullOrEmpty(this.namespace) || Strings.isNullOrEmpty(this.taskName)) {
                throw new IOException("Tekton file has not a valid format. Namespace and/or name properties are invalid.");
            }
        } catch (IOException e) {
            logger.error("Error: " + e.getLocalizedMessage());
            return;
        }
        this.resources = resources;
        this.args = "";
        setTitle("Run Task " + taskName);
        init();

        try {
            inputs = JSONHelper.getInputs(task);
            outputs = JSONHelper.getOutputs(task);
        } catch (IOException e) {
            logger.error("Error: " + e.getLocalizedMessage());
        }

        // check if only one resource of the same input/output resource type exists and set it as value
        setFixedValueInputResources();
        setFixedValueOutputResources();
        // init dialog
        initInputsArea();
        initOutputsArea();
        updatePreview();
        registerListeners();
    }

    public static void main(String[] args) {
        StartTaskDialog dialog = new StartTaskDialog(null, "", null);
        dialog.pack();
        dialog.show();
        System.exit(0);
    }

    public String args() {
        return args;
    }

    private void calculateArgs() {
        String args = "";
        if (inputs != null) {
            for (Input input : inputs) {
                if (input.kind() == Input.Kind.PARAMETER) {
                    String value = input.value() == null ? input.defaultValue().get() : input.value();
                    args += "-p " + input.name() + "=" + value + " ";
                } else {
                    args += "-i " + input.name() + "=" + input.value() + " ";
                }
            }
        }

        if (outputs != null) {
            for (Output output : outputs) {
                args += "-o " + output.name() + "=" + output.value() + " ";
            }
        }

        this.args = args;
    }

    private String validateInputs() {
        if (inputs == null) {
            return null;
        }

        for (Input input: inputs) {
            if (input.value() == null && !input.defaultValue().isPresent()) {
                return input.name();
            }
        }
        return null;
    }

    private String validateOutputs() {
        if (outputs == null) {
            return null;
        }

        for (Output output: outputs) {
            if (output.value() == null) {
                return output.name();
            }
        }
        return null;
    }

    @NotNull
    @Override
    protected Action[] createActions() {
        return new Action[]{};
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return contentPane;
    }

    private void setFixedValueInputResources() {
        if (inputs == null) {
            return;
        }
        String defaultValue = null;
        int n = 0;
        // if only a resource of the type requested exists, set that resource as value
        for (Input input: inputs) {
            if (input.kind() == Input.Kind.RESOURCE) {
                for (Resource resource: resources) {
                    if (resource.type().equals((input.type()))) {
                        if (defaultValue != null) {
                            defaultValue = null;
                            break;
                        }
                        defaultValue = resource.name();
                        // set the value of the first resource
                        // this is needed to prevent an issue with combobox (if only one element is visible cannot be selected)
                        if (n == 0) {
                            n++;
                            break;
                        }
                    }
                }
                if (defaultValue != null) {
                    input.setValue(defaultValue);
                }
            }
        }
    }

    private void setFixedValueOutputResources() {
        if (outputs == null) {
            return;
        }
        String defaultValue = null;
        Output output;
        for (int i=0; i<outputs.size(); i++) {
            output = outputs.get(i);
            for (Resource resource: resources) {
                if (resource.type().equals((output.type()))) {
                    if (defaultValue != null) {
                        defaultValue = null;
                        break;
                    }
                    defaultValue = resource.name();
                    // if first output, set the value with the first resource of the type requested and leave
                    // this is needed to prevent an issue with combobox (if only one element is visible cannot be selected)
                    if (i == 0) {
                        break;
                    }
                }
            }
            if (defaultValue != null) {
                output.setValue(defaultValue);
            }
        }
    }

    private void changeInputComponentVisibility(boolean hasInputs, boolean hasParams, boolean hasResources) {
        if (!hasInputs) {
            inInfoMessage.setVisible(true);
            return;
        }
        if (hasResources) {
            inputResourcesPanel.setVisible(true);
        }
        if (hasParams) {
            inputParamsPanel.setVisible(true);
        }
    }

    private void initInputsArea() {
        if (inputs == null) {
            changeInputComponentVisibility(false, false, false);
            return;
        }
        for (Input input: inputs) {
            if (input.kind() == Input.Kind.PARAMETER) {
                inputParamsCB.addItem(input);
            } else {
                inputResourcesCB.addItem(input);
            }
        }

        if (inputParamsCB.getItemCount() > 0) {
            inputParamValueTxt.setText(((Input)inputParamsCB.getItemAt(0)).defaultValue().orElse(""));
        }

        if (inputResourcesCB.getItemCount() > 0) {
            fillInputResourceComboBox((Input)inputResourcesCB.getItemAt(0));
        }

        changeInputComponentVisibility(true, inputParamsCB.getItemCount() > 0, inputResourcesCB.getItemCount() > 0);
    }

    private void fillInputResourceComboBox(Input inputSelected) {
        inputResourceValuesCB.removeAll();
        for (Resource resource: resources) {
            if (resource.type().equals(inputSelected.type())) {
                inputResourceValuesCB.addItem(resource);
            }
        }
        if (inputSelected.value() != null) {
            inputResourceValuesCB.setSelectedItem(inputSelected.value());
        }
    }

    private void initOutputsArea() {
        changeOutputComponentVisibility(outputs != null);
        if (outputs == null) {
            return;
        }
        // TODO check if there is a way to add multiple items at once???
        for (Output output: outputs) {
            outputsCB.addItem(output);
        }

        fillOutResourcesComboBox(outputs.get(0));
    }

    private void changeOutputComponentVisibility(boolean hasOutput) {
        if (!hasOutput) {
            outInfoMessage.setVisible(true);
        } else {
            outputsPanel.setVisible(true);
        }
    }

    private void fillOutResourcesComboBox(Output outputSelected) {
        outputsResourcesCB.removeAll();
        for (Resource resource: resources) {
            if (resource.type().equals(outputSelected.type())) {
                outputsResourcesCB.addItem(resource);
            }
        }
        if (outputSelected.value() != null) {
            outputsResourcesCB.setSelectedItem(outputSelected.value());
        }
    }

    private void updatePreview() {
        String preview = "";
        try {
            preview = JSONHelper.JSONToYAML(JSONHelper.createPreviewJsonNode(inputs, outputs, resources));
        } catch (IOException e) {
            logger.error("Error: " + e.getLocalizedMessage());
        }
        previewTextArea.setText(preview);
    }

    private void setInputValue(String inputName, String value) {
        for (Input input: inputs) {
            if (input.name().equals(inputName)) {
                if (input.type().equals("array")) {
                    value = value.replace(" ", ",");
                }
                input.setValue(value);
                break;
            }
        }
    }

    private void registerListeners() {
        previewRefreshBtn.addActionListener(actionEvent -> updatePreview());

        inputParamsCB.addItemListener(itemEvent -> {
            if (itemEvent.getStateChange() == 1) {
                Input currentInput = (Input) itemEvent.getItem();
                String value = currentInput.value() != null ? currentInput.value() : currentInput.defaultValue().orElse("");
                inputParamValueTxt.setText(value);
            }
        });

        inputParamValueTxt.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                super.focusLost(e);
                Input inputSelected = (Input) inputParamsCB.getSelectedItem();
                setInputValue(inputSelected.name(), inputParamValueTxt.getText());
                updatePreview();
            }
        });

        inputResourcesCB.addItemListener(itemEvent -> {
            if (itemEvent.getStateChange() == 1) {
                Input currentInput = (Input) itemEvent.getItem();
                fillInputResourceComboBox(currentInput);
                if (currentInput.value() == null && inputResourceValuesCB.getItemCount() > 0) {
                    setInputValue(currentInput.name(), ((Resource) inputResourceValuesCB.getItemAt(0)).name());
                }
            }
        });

        inputResourceValuesCB.addItemListener(itemEvent -> {
            if (itemEvent.getStateChange() == 1) {
                Input inputSelected = (Input) inputResourcesCB.getSelectedItem();
                Resource resourceSelected = (Resource) itemEvent.getItem();
                setInputValue(inputSelected.name(), resourceSelected.name());
                updatePreview();
            }
        });

        outputsCB.addItemListener(itemEvent -> {
            if (itemEvent.getStateChange() == 1) {
                Output currentOutput = (Output) itemEvent.getItem();
                fillOutResourcesComboBox(currentOutput);
                if (currentOutput.value() == null && outputsResourcesCB.getItemCount() > 0) {
                    outputs.get(outputsCB.getSelectedIndex()).setValue(((Resource) outputsResourcesCB.getItemAt(0)).name());
                }
            }
        });

        outputsResourcesCB.addItemListener(itemEvent -> {
            if (itemEvent.getStateChange() == 1) {
                int outputSelectedIndex = outputsCB.getSelectedIndex();
                Resource resourceSelected = (Resource) itemEvent.getItem();
                outputs.get(outputSelectedIndex).setValue(resourceSelected.name());
                updatePreview();
            }
        });

        startTaskButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                // check if all inputs/outputs have been initialized otherwise show an error
                String invalidInput = validateInputs();
                String invalidOutput = validateOutputs();
                if (invalidInput == null && invalidOutput == null) {
                    calculateArgs();
                    StartTaskDialog.super.close(0, true);
                } else {
                    String msg = "Error - No value inserted for ";
                    if (invalidInput != null) {
                        msg += invalidInput;
                    } else {
                        msg += invalidOutput;
                    }
                    errorLbl.setText(msg);

                    errorLbl.setVisible(true);
                    java.util.Timer timer = new java.util.Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            errorLbl.setVisible(false);
                        }
                    }, 4000);

                }
            }
        });

        cancelButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                StartTaskDialog.super.close(0, false);
            }
        });
    }
}
