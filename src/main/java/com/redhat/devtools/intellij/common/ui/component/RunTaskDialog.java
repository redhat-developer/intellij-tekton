package com.redhat.devtools.intellij.common.ui.component;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.redhat.devtools.intellij.common.utils.JSONHelper;
import com.redhat.devtools.intellij.tektoncd.tkn.Resource;
import com.redhat.devtools.intellij.tektoncd.tkn.component.field.Input;
import com.redhat.devtools.intellij.tektoncd.tkn.component.field.Output;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.IOException;
import java.util.List;

public class RunTaskDialog extends DialogWrapper {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
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
    private List<Input> inputs;
    private List<Resource> resources;
    private List<Output> outputs;

    public RunTaskDialog(Component parent, String task, List<Resource> resources) {
        super((Project) null, parent, false, IdeModalityType.IDE);
        this.resources = resources;
        init();
        try {
            String taskName = JSONHelper.getName(task);
            setTitle("Run Task " + taskName);
            inputs = JSONHelper.getInputs(task);
            outputs = JSONHelper.getOutputs(task);
        } catch (IOException e) {
            e.printStackTrace();
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
        RunTaskDialog dialog = new RunTaskDialog(null, "", null);
        dialog.pack();
        dialog.show();
        System.exit(0);
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return contentPane;
    }

    private void setFixedValueInputResources() {
        String defaultValue = null;
        for(Input input: inputs) {
            if (input.kind() == Input.Kind.RESOURCE) {
                for (Resource resource: resources) {
                    if (resource.type().equals((input.type()))) {
                        if (defaultValue != null) {
                            defaultValue = null;
                            break;
                        }
                        defaultValue = resource.name();
                    }
                }
                if (defaultValue != null) {
                    input.setValue(defaultValue);
                }
            }
        }
    }

    private void setFixedValueOutputResources() {
        String defaultValue = null;
        for(Output output: outputs) {
            for (Resource resource: resources) {
                if (resource.type().equals((output.type()))) {
                    if (defaultValue != null) {
                        defaultValue = null;
                        break;
                    }
                    defaultValue = resource.name();
                }
            }
            if (defaultValue != null) {
                output.setValue(defaultValue);
            }
        }
    }

    private void changeInputComponentVisibility(boolean isResource) {
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
        // TODO check if there is a way to add multiple items at once???
        for (Input input: inputs) {
            inputsComboBox.addItem(input);
        }
        Input firstInput = inputs.get(0);
        if (firstInput.type().equals("string") ||
                firstInput.type().equals("array")) {
            changeInputComponentVisibility(false);
            inputTxtField.setText(firstInput.defaultValue().orElse(""));
        } else {
            changeInputComponentVisibility(true);
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

    private void fillResourcePathsTxtBox(String paths) {
        pathsInputTxtField.setText(paths);
    }

    private void initOutputsArea() {
        // TODO check if there is a way to add multiple items at once???
        for (Output output: outputs) {
            outputsComboBox.addItem(output);
        }
        Output firstOutput = outputs.get(0);
        fillOutResourcesComboBox(firstOutput);
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
            preview = JSONHelper.createPreviewJson(inputs, outputs, resources);
        } catch (IOException e) {
            e.printStackTrace();
        }
        previewTextArea.setText(preview);
    }

    private void setInputValue(String inputName, String value) {
        for (Input input: inputs) {
            if (input.name().equals(inputName)) {
                input.setValue(value);
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
                    changeInputComponentVisibility(false);
                    String value = currentInput.value() != null ? currentInput.value() : currentInput.defaultValue().orElse("");
                    inputTxtField.setText(value);
                } else {
                    changeInputComponentVisibility(true);
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
            int resourceSelectedIndex = inResourcesComboBox.getSelectedIndex();
            resources.get(resourceSelectedIndex).setPaths(pathsInputTxtField.getText());
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
                int resourceSelectedIndex = outResourcesComboBox.getSelectedIndex();
                resources.get(resourceSelectedIndex).setPaths(pathsOutputTxtField.getText());
            }
        });
    }
}
