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

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.redhat.devtools.intellij.common.utils.JSONHelper;
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
    private JTextField inputTxtField;
    private JComboBox inResourcesComboBox;
    private JComboBox inputsComboBox;
    private JLabel resourcesComboLabel;
    private JLabel valueTxtLabel;
    private JComboBox outputsComboBox;
    private JComboBox outResourcesComboBox;
    private JTextArea previewTextArea;
    private JButton previewRefreshBtn;
    private JLabel pathsTxtLabel;
    private JTextField pathsInputTxtField;
    private JTextField pathsOutputTxtField;
    private JLabel outInfoMessage;
    private JLabel inInfoMessage;
    private JLabel outputLbl;
    private JLabel outResourceLbl;
    private JLabel outPathsLbl;
    private JButton startTaskButton;
    private JLabel errorLbl;
    private JButton cancelButton;
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
        Input input = null;
        // if only a resource of the type requested exists, set that resource as value
        for (int i=0; i<inputs.size(); i++) {
            input = inputs.get(i);
            if (input.kind() == Input.Kind.RESOURCE) {
                for (Resource resource: resources) {
                    if (resource.type().equals((input.type()))) {
                        if (defaultValue != null) {
                            defaultValue = null;
                            break;
                        }
                        defaultValue = resource.name();
                        // if first input is a resource, set the value with the first resource of the type requested
                        // this is needed to prevent an issue with combobox (if only one element is visible cannot be selected)
                        if (i == 0) {
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
        Output output = null;
        for (int i=0; i<outputs.size(); i++) {
            output = outputs.get(i);
            for (Resource resource: resources) {
                if (resource.type().equals((output.type()))) {
                    if (defaultValue != null) {
                        defaultValue = null;
                        break;
                    }
                    defaultValue = resource.name();
                    // if first input is a resource, set the value with the first resource of the type requested
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

    private void changeInputComponentVisibility(boolean hasInput, boolean isResource) {
        if (!hasInput) {
            inInfoMessage.setVisible(true);
            valueTxtLabel.setVisible(false);
            inputTxtField.setVisible(false);
            resourcesComboLabel.setVisible(false);
            inResourcesComboBox.setVisible(false);
            pathsTxtLabel.setVisible(false);
            pathsInputTxtField.setVisible(false);
            return;
        }
        if (isResource) {
            resourcesComboLabel.setVisible(true);
            inResourcesComboBox.setVisible(true);
            pathsTxtLabel.setVisible(true);
            pathsInputTxtField.setVisible(true);
            valueTxtLabel.setVisible(false);
            inputTxtField.setVisible(false);
        } else {
            valueTxtLabel.setVisible(true);
            inputTxtField.setVisible(true);
            resourcesComboLabel.setVisible(false);
            inResourcesComboBox.setVisible(false);
            pathsTxtLabel.setVisible(false);
            pathsInputTxtField.setVisible(false);
        }
    }

    private void initInputsArea() {
        if (inputs == null) {
            changeInputComponentVisibility(false, false);
            return;
        }
        // TODO check if there is a way to add multiple items at once???
        for (Input input: inputs) {
            inputsComboBox.addItem(input);
        }
        Input firstInput = inputs.get(0);
        if (firstInput.type().equals("string") ||
                firstInput.type().equals("array")) {
            changeInputComponentVisibility(true, false);
            inputTxtField.setText(firstInput.defaultValue().orElse(""));
        } else {
            changeInputComponentVisibility(true, true);
            fillInputResourceComboBox(firstInput);
        }
    }

    private void fillInputResourceComboBox(Input inputSelected) {
        inResourcesComboBox.removeAll();
        for (Resource resource: resources) {
            if (resource.type().equals(inputSelected.type())) {
                inResourcesComboBox.addItem(resource);
            }
        }
        if (inputSelected.value() != null) {
            inResourcesComboBox.setSelectedItem(inputSelected.value());
        }
    }

    private void initOutputsArea() {
        if (outputs == null) {
            changeOutputComponentVisibility(false);
            return;
        }
        // TODO check if there is a way to add multiple items at once???
        for (Output output: outputs) {
            outputsComboBox.addItem(output);
        }
        Output firstOutput = outputs.get(0);
        fillOutResourcesComboBox(firstOutput);
    }

    private void changeOutputComponentVisibility(boolean hasOutput) {
        if (!hasOutput) {
            outInfoMessage.setVisible(true);
            outputsComboBox.setVisible(false);
            outResourcesComboBox.setVisible(false);
            pathsOutputTxtField.setVisible(false);
            outputLbl.setVisible(false);
            outResourceLbl.setVisible(false);
            outPathsLbl.setVisible(false);
            return;
        }
    }

    private void fillOutResourcesComboBox(Output outputSelected) {
        outResourcesComboBox.removeAll();
        for (Resource resource: resources) {
            if (resource.type().equals(outputSelected.type())) {
                outResourcesComboBox.addItem(resource);
            }
        }
        if (outputSelected.value() != null) {
            outResourcesComboBox.setSelectedItem(outputSelected.value());
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

    private void setPathsResource(String resourceName, String paths) {
        for (Resource resource: resources) {
            if (resource.name().equals(resourceName)) {
                resource.setPaths(paths);
                break;
            }
        }
    }

    private void registerListeners() {
        previewRefreshBtn.addActionListener(actionEvent -> updatePreview());

        inputsComboBox.addItemListener(itemEvent -> {
            if (itemEvent.getStateChange() == 1) {
                Input currentInput = (Input) itemEvent.getItem();
                if (currentInput.type().toString().equals("string") ||
                        currentInput.type().toString().equals("array")) {
                    changeInputComponentVisibility(true, false);
                    String value = currentInput.value() != null ? currentInput.value() : currentInput.defaultValue().orElse("");
                    inputTxtField.setText(value);
                } else {
                    changeInputComponentVisibility(true, true);
                    fillInputResourceComboBox(currentInput);
                    if (currentInput.value() == null) {
                        setInputValue(currentInput.name(), ((Resource) inResourcesComboBox.getItemAt(0)).name());
                    }
                }
            }
        });

        inResourcesComboBox.addItemListener(itemEvent -> {
            if (itemEvent.getStateChange() == 1) {
                Input inputSelected = (Input) inputsComboBox.getSelectedItem();
                Resource resourceSelected = (Resource) itemEvent.getItem();
                setInputValue(inputSelected.name(), resourceSelected.name());
                if (resourceSelected.paths() != null) {
                    pathsInputTxtField.setText(resourceSelected.paths());
                }
            }
        });

        inputTxtField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
            super.focusLost(e);
            Input inputSelected = (Input) inputsComboBox.getSelectedItem();
            setInputValue(inputSelected.name(), inputTxtField.getText());
            updatePreview();
            }
        });

        pathsInputTxtField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
            super.focusLost(e);
            Resource resourceSelected = (Resource) inResourcesComboBox.getSelectedItem();
            setPathsResource(resourceSelected.name(), pathsInputTxtField.getText());
            updatePreview();
            }
        });

        outputsComboBox.addItemListener(itemEvent -> {
            if (itemEvent.getStateChange() == 1) {
                Output currentOutput = (Output) itemEvent.getItem();
                fillOutResourcesComboBox(currentOutput);
            }
        });

        outResourcesComboBox.addItemListener(itemEvent -> {
            if (itemEvent.getStateChange() == 1) {
                int outputSelectedIndex = outputsComboBox.getSelectedIndex();
                Resource resourceSelected = (Resource) itemEvent.getItem();
                outputs.get(outputSelectedIndex).setValue(resourceSelected.name());
                if (resourceSelected.paths() != null) {
                    pathsOutputTxtField.setText(resourceSelected.paths());
                }
            }
        });

        pathsOutputTxtField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
            super.focusLost(e);
            Resource resourceSelected = (Resource) outResourcesComboBox.getSelectedItem();
            setPathsResource(resourceSelected.name(), pathsOutputTxtField.getText());
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
