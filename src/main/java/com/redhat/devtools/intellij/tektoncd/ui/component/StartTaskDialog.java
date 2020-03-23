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

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Strings;
import com.intellij.openapi.ui.DialogWrapper;
import com.redhat.devtools.intellij.common.utils.YAMLHelper;
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
import java.util.*;
import java.util.stream.Collectors;

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
    private List<String> args;

    public StartTaskDialog(Component parent, String task, List<Resource> resources) {
        super(null, parent, false, IdeModalityType.IDE);
        try {
            this.namespace = YAMLHelper.getStringValueFromYAML(task, new String[] {"metadata", "namespace"});
            if (Strings.isNullOrEmpty(namespace)) {
                throw new IOException("Tekton task has not a valid format. Namespace field is not found or its value is not valid.");
            }
            this.taskName = YAMLHelper.getStringValueFromYAML(task, new String[] {"metadata", "name"});
            if (Strings.isNullOrEmpty(this.taskName)) {
                throw new IOException("Tekton task has not a valid format. Name field is not found or its value is not valid.");
            }
        } catch (IOException e) {
            logger.error("Error: " + e.getLocalizedMessage());
            return;
        }
        this.resources = resources;
        this.args = new ArrayList<>();
        setTitle("Run Task " + taskName);
        init();

        try {
            JsonNode inputsNode = YAMLHelper.getValueFromYAML(task, new String[] {"spec", "inputs"});
            if (inputsNode != null) {
                inputs = getInputs(inputsNode);
            }
            JsonNode outputsNode = YAMLHelper.getValueFromYAML(task, new String[] {"spec", "outputs"});
            if (outputsNode != null) {
                outputs = getOutputs(outputsNode);
            }
        } catch (IOException e) {
            logger.error("Error: " + e.getLocalizedMessage());
        }

        // if for a specific input/output type (git, image, ...) only a resource exists, set that resource as default value for input/output
        setDefaultValueResources();
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

    public List<String> args() {
        return args;
    }

    private void calculateArgs() {
        if (inputs != null) {
            for (Input input : inputs) {
                if (input.kind() == Input.Kind.PARAMETER) {
                    String value = input.value() == null ? input.defaultValue().get() : input.value();
                    args.add("-p");
                    args.add(input.name() + "=" + value);
                } else {
                    args.add("-i");
                    args.add(input.name() + "=" + input.value());
                }
            }
        }

        if (outputs != null) {
            for (Output output : outputs) {
                args.add("-o");
                args.add(output.name() + "=" + output.value());
            }
        }
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

    private List<Input> getInputs(JsonNode inputsNode) {
        List<Input> result = new ArrayList<>();
        List<JsonNode> params = inputsNode.findValues("params");
        List<JsonNode> resources = inputsNode.findValues("resources");

        if (params != null) {
            for (Iterator<JsonNode> it = params.iterator(); it.hasNext(); ) {
                JsonNode item = it.next();
                result.add(new Input().fromJson(item.get(0), Input.Kind.PARAMETER));
            }
        }

        if (resources != null) {
            for (Iterator<JsonNode> it = resources.iterator(); it.hasNext(); ) {
                JsonNode item = it.next();
                result.add(new Input().fromJson(item.get(0), Input.Kind.RESOURCE));
            }
        }

        return result;
    }

    private List<Output> getOutputs(JsonNode outputsNode) {
        List<Output> result = new ArrayList<>();
        List<JsonNode> resources = outputsNode.findValues("resources");

        if (resources != null) {
            for (Iterator<JsonNode> it = resources.iterator(); it.hasNext(); ) {
                JsonNode item = it.next();
                result.add(new Output().fromJson(item.get(0)));
            }
        }

        return result;
    }

    private void setDefaultValueResources() {
        if (inputs == null && outputs == null) {
            return;
        }
        int resourcesCount;
        // if for a specific type (git, image, ...) only a resource exists, set that resource as default value for input/output
        Map<String, List<Resource>> resourceGroupedByType = resources.stream().collect(Collectors.groupingBy(Resource::type));

        if (inputs != null) {
            Input[] resourceInputs = inputs.stream().filter(input -> input.kind() == Input.Kind.RESOURCE).toArray(Input[]::new);
            for (Input input: resourceInputs) {
                resourcesCount = resourceGroupedByType.get(input.type()).size();
                if (resourcesCount == 1) {
                    input.setValue(resourceGroupedByType.get(input.type()).get(0).name());
                }
            }
        }

        if (outputs != null) {
            for (Output output: outputs) {
                resourcesCount = resourceGroupedByType.get(output.type()).size();
                if (resourcesCount == 1) {
                    output.setValue(resourceGroupedByType.get(output.type()).get(0).name());
                }
            }
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
        if (outputs == null) return;

        for (Output output: outputs) {
            outputsCB.addItem(output);
        }
        fillOutResourcesComboBox(outputs.get(0));
    }

    private void changeOutputComponentVisibility(boolean hasOutput) {
        outInfoMessage.setVisible(!hasOutput);
        outputsPanel.setVisible(hasOutput);
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
            preview = YAMLHelper.createPreviewInYAML(inputs, outputs);
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
        // listener for when preview refresh button is clicked
        previewRefreshBtn.addActionListener(actionEvent -> updatePreview());

        // listener for when value in parameters input combo box changes
        inputParamsCB.addItemListener(itemEvent -> {
            // when combo box value change update input value textbox
            if (itemEvent.getStateChange() == 1) {
                Input currentInput = (Input) itemEvent.getItem();
                String value = currentInput.value() != null ? currentInput.value() : currentInput.defaultValue().orElse("");
                inputParamValueTxt.setText(value);
            }
        });

        // listener for when focus is lost in textbox with parameter input's value
        inputParamValueTxt.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                super.focusLost(e);
                // when focus is lost, value in textbox is saved in input's value and preview is updated
                Input inputSelected = (Input) inputParamsCB.getSelectedItem();
                setInputValue(inputSelected.name(), inputParamValueTxt.getText());
                updatePreview();
            }
        });

        // listener for when value in resources input combo box changes
        inputResourcesCB.addItemListener(itemEvent -> {
            if (itemEvent.getStateChange() == 1) {
                // when inputResourcesCB combo box value changes, inputResourceValuesCB combo box is filled with all resources of the same type as the currentInput
                Input currentInput = (Input) itemEvent.getItem();
                fillInputResourceComboBox(currentInput);
                if (currentInput.value() == null && inputResourceValuesCB.getItemCount() > 0) {
                    setInputValue(currentInput.name(), ((Resource) inputResourceValuesCB.getItemAt(0)).name());
                }
            }
        });

        // listener for when value in resources value input combo box changes
        inputResourceValuesCB.addItemListener(itemEvent -> {
            if (itemEvent.getStateChange() == 1) {
                // when inputResourceValuesCB combo box value changes, the new value is saved and preview is updated
                Input inputSelected = (Input) inputResourcesCB.getSelectedItem();
                Resource resourceSelected = (Resource) itemEvent.getItem();
                setInputValue(inputSelected.name(), resourceSelected.name());
                updatePreview();
            }
        });

        // listener for when value in outputs combo box changes
        outputsCB.addItemListener(itemEvent -> {
            if (itemEvent.getStateChange() == 1) {
                // when outputsCB combo box value changes, outputsResourcesCB combo box is filled with all resources of the same type as the currentOutput
                Output currentOutput = (Output) itemEvent.getItem();
                fillOutResourcesComboBox(currentOutput);
                if (currentOutput.value() == null && outputsResourcesCB.getItemCount() > 0) {
                    outputs.get(outputsCB.getSelectedIndex()).setValue(((Resource) outputsResourcesCB.getItemAt(0)).name());
                }
            }
        });

        // listener for when value in output resources combo box changes
        outputsResourcesCB.addItemListener(itemEvent -> {
            if (itemEvent.getStateChange() == 1) {
                // when outputsResourcesCB combo box value changes, the new value is saved and preview is updated
                int outputSelectedIndex = outputsCB.getSelectedIndex();
                Resource resourceSelected = (Resource) itemEvent.getItem();
                outputs.get(outputSelectedIndex).setValue(resourceSelected.name());
                updatePreview();
            }
        });

        // listener for when the button startTask is clicked
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

        // listener for when the button cancel is clicked
        cancelButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                StartTaskDialog.super.close(0, false);
            }
        });
    }
}
