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
package com.redhat.devtools.intellij.tektoncd.ui;

import com.intellij.openapi.ui.DialogWrapper;
import com.redhat.devtools.intellij.tektoncd.tkn.Resource;
import com.redhat.devtools.intellij.tektoncd.tkn.component.field.Input;
import com.redhat.devtools.intellij.tektoncd.tkn.component.field.Output;
import com.redhat.devtools.intellij.tektoncd.tkn.component.field.Workspace;
import com.redhat.devtools.intellij.tektoncd.utils.StartResourceModel;
import com.redhat.devtools.intellij.tektoncd.utils.YAMLBuilder;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;

import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_PIPELINE;
import static com.redhat.devtools.intellij.tektoncd.tkn.component.field.Workspace.Kind.CONFIGMAP;
import static com.redhat.devtools.intellij.tektoncd.tkn.component.field.Workspace.Kind.PVC;
import static com.redhat.devtools.intellij.tektoncd.tkn.component.field.Workspace.Kind.SECRET;

public class StartDialog extends DialogWrapper {
    Logger logger = LoggerFactory.getLogger(StartDialog.class);
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
    private JLabel errorLbl;
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
    private JLabel outputsTitle;
    private JPanel serviceAccountsPanel;
    private JLabel saLbl;
    private JComboBox tsaCB;
    private JLabel tsaLbl;
    private JComboBox saCB;
    private JComboBox saForTaskCB;
    private JComboBox wsCB;
    private JComboBox wsTypeCB;
    private JComboBox wsTypeOptionsCB;
    private JPanel workspacesPanel;
    private JLabel wsLabel;
    private JLabel noResFoundLbl;

    private StartResourceModel model;

    public StartDialog(Component parent, StartResourceModel model) {
        super(null, parent, false, IdeModalityType.IDE);

        this.model = model;
        setTitle("Start " + model.getName());
        setOKButtonText("Start");
        init();

        boolean isPipeline = KIND_PIPELINE.equalsIgnoreCase(model.getKind());

        // init dialog
        initServiceAccountArea(isPipeline);
        initWorkspacesArea();
        initInputsArea();
        initOutputsArea(isPipeline);
        // fill serviceAccount ComboBox
        fillComboBox(saCB, model.getServiceAccounts(), "");
        updatePreview();
        registerListeners();
    }

    public static void main(String[] args) {
        StartDialog dialog = new StartDialog(null, null);
        dialog.pack();
        dialog.show();
        System.exit(0);
    }

    private void calculateArgs() {
        Map<String, String> parameters = new HashMap<>();
        Map<String, String> inputResources = new HashMap<>();
        Map<String, String> outputResources = new HashMap<>();

        for (Input input : model.getInputs()) {
            if (input.kind() == Input.Kind.PARAMETER) {
                String value = input.value() == null ? input.defaultValue().orElse("") : input.value();
                parameters.put(input.name(), value);
            } else {
                inputResources.put(input.name(), input.value());
            }
        }

        for (Output output : model.getOutputs()) {
            outputResources.put(output.name(), output.value());
        }


        model.setParameters(parameters);
        model.setInputResources(inputResources);
        model.setOutputResources(outputResources);
    }

    private String validateInputs() {
        for (Input input: model.getInputs()) {
            if (input.value() == null && !input.defaultValue().isPresent()) {
                return input.name();
            }
        }
        return null;
    }

    private String validateOutputs() {
        for (Output output: model.getOutputs()) {
            if (output.value() == null) {
                return output.name();
            }
        }
        return null;
    }

    @Override
    protected void doOKAction() {
        // check if all inputs/outputs have been initialized otherwise show an error
        String invalidInput = validateInputs();
        String invalidOutput = validateOutputs();
        if (invalidInput == null && invalidOutput == null) {
            calculateArgs();
            super.doOKAction();
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

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return contentPane;
    }

    private void initWorkspacesArea() {
        if (model.getWorkspaces().isEmpty()) {
            return;
        }

        workspacesPanel.setVisible(true);
        for (String workspace: model.getWorkspaces().keySet()) {
            wsCB.addItem(workspace);
        }
        setWorkspaceTypesCombo(null);
    }

    private void setWorkspaceTypesCombo(String workspaceName) {
        if (wsTypeCB.getItemCount() == 0) {
            wsTypeCB.addItem("");
            wsTypeCB.addItem(Workspace.Kind.EMPTYDIR);
            wsTypeCB.addItem(CONFIGMAP);
            wsTypeCB.addItem(Workspace.Kind.SECRET);
            wsTypeCB.addItem(Workspace.Kind.PVC);
        }

        Workspace.Kind typeToBeSelected = workspaceName == null ? null : model.getWorkspaces().get(workspaceName) == null ? null : model.getWorkspaces().get(workspaceName).getKind();
        if (typeToBeSelected != null) {
            wsTypeCB.setSelectedItem(typeToBeSelected);
        } else {
            wsTypeCB.setSelectedItem("");
        }

    }

    private void setWorkspaceTypeOptionsCombo(String workspaceName, Workspace.Kind kind) {
        noResFoundLbl.setVisible(false);
        wsTypeOptionsCB.setVisible(false);

        List<String> options;
        if (kind == CONFIGMAP) {
            options = model.getConfigMaps();
        } else if(kind == SECRET) {
            options = model.getSecrets();
        } else if ( kind == PVC) {
            options = model.getPersistenceVolumeClaims();
        } else {
            return;
        }

        if (options.isEmpty()) {
            // show message no resource exists for this type
            noResFoundLbl.setVisible(true);
            return;
        }

        wsTypeOptionsCB.setVisible(true);
        wsTypeOptionsCB.removeAllItems();
        for (String option: options) {
            wsTypeOptionsCB.addItem(option);
        }

        String resource = model.getWorkspaces().get(workspaceName) == null ? null : model.getWorkspaces().get(workspaceName).getKind() == kind ? model.getWorkspaces().get(workspaceName).getResource() : null;
        if (resource != null) {
            wsTypeOptionsCB.setSelectedItem(resource);
        } else {
            wsTypeOptionsCB.setSelectedIndex(0);
        }
    }

    private void updateWorkspaceModel(String workspaceName, Workspace.Kind kind, String resource) {
        if (resource == null && kind != Workspace.Kind.EMPTYDIR) {
            model.getWorkspaces().put(workspaceName, null);
        } else {
            Workspace workspace = new Workspace(workspaceName, kind, resource);
            model.getWorkspaces().put(workspaceName, workspace);
        }
    }

    private void initServiceAccountArea(boolean isPipeline) {
        if (isPipeline) {
            tsaLbl.setVisible(true);
            tsaCB.setVisible(true);
            saForTaskCB.setVisible(true);
            // fill taskServiceAccount combobox
            fillComboBox(tsaCB, model.getTaskServiceAccounts().keySet(), null);
            // fill service Account combobox for task
            fillComboBox(saForTaskCB, model.getServiceAccounts(), "");
        }
    }

    private void fillComboBox(JComboBox comboBox, Collection<String> values, String defaultValue) {
        comboBox.removeAll();
        if (defaultValue != null) comboBox.addItem(defaultValue);
        for (String value: values) {
            comboBox.addItem(value);
        }
    }


    private void initInputsArea() {
        if (model.getInputs().isEmpty()) {
            changeInputComponentVisibility(false, false, false);
            return;
        }
        for (Input input: model.getInputs()) {
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
        for (Resource resource: model.getResources()) {
            if (resource.type().equals(inputSelected.type())) {
                inputResourceValuesCB.addItem(resource);
            }
        }
        if (inputSelected.value() != null) {
            inputResourceValuesCB.setSelectedItem(inputSelected.value());
        }
    }

    private void initOutputsArea(boolean isPipeline) {
        List<Output> outputs = model.getOutputs();
        changeOutputComponentVisibility(!outputs.isEmpty(), isPipeline);
        if (outputs.isEmpty()) {
            return;
        }

        for (Output output: outputs) {
            outputsCB.addItem(output);
        }
        fillOutResourcesComboBox(outputs.get(0));
    }

    private void changeOutputComponentVisibility(boolean hasOutput, boolean isPipeline) {
        outputsTitle.setVisible(!isPipeline);
        outInfoMessage.setVisible(!hasOutput && !isPipeline);
        outputsPanel.setVisible(hasOutput && !isPipeline);
    }

    private void fillOutResourcesComboBox(Output outputSelected) {
        outputsResourcesCB.removeAll();
        for (Resource resource: model.getResources()) {
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
            preview = YAMLBuilder.createPreview(model);
        } catch (IOException e) {
            logger.warn("Error: " + e.getLocalizedMessage());
        }
        previewTextArea.setText(preview);
    }

    private void setInputValue(String inputName, String value) {
        for (Input input: model.getInputs()) {
            if (input.name().equals(inputName)) {
                input.setValue(value);
                break;
            }
        }
    }

    private void registerListeners() {
        // listener for when preview refresh button is clicked
        previewRefreshBtn.addActionListener(actionEvent -> updatePreview());

        // listener for when value in tsa combo box changes
        saCB.addItemListener(itemEvent -> {
            // when combo box value change update sa value
            if (itemEvent.getStateChange() == 1) {
                String serviceAccountSelected = (String) itemEvent.getItem();
                model.setServiceAccount(serviceAccountSelected);
                updatePreview();
            }
        });

        // listener for when value in tsa combo box changes
        tsaCB.addItemListener(itemEvent -> {
            // when combo box value change update tsa value textbox
            if (itemEvent.getStateChange() == 1) {
                String taskSelected = (String) itemEvent.getItem();
                String value = model.getTaskServiceAccounts().get(taskSelected);
                saForTaskCB.setSelectedItem(value);
            }
        });

        // listener for when value in tsa combo box changes
        saForTaskCB.addItemListener(itemEvent -> {
            // when combo box value change update tsa value textbox
            if (itemEvent.getStateChange() == 1) {
                String saSelected = (String) itemEvent.getItem();
                String taskSelected = tsaCB.getSelectedItem().toString();
                model.getTaskServiceAccounts().put(taskSelected, saSelected);
                updatePreview();
            }
        });

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
                    model.getOutputs().get(outputsCB.getSelectedIndex()).setValue(((Resource) outputsResourcesCB.getItemAt(0)).name());
                }
            }
        });

        // listener for when value in output resources combo box changes
        outputsResourcesCB.addItemListener(itemEvent -> {
            if (itemEvent.getStateChange() == 1) {
                // when outputsResourcesCB combo box value changes, the new value is saved and preview is updated
                int outputSelectedIndex = outputsCB.getSelectedIndex();
                Resource resourceSelected = (Resource) itemEvent.getItem();
                model.getOutputs().get(outputSelectedIndex).setValue(resourceSelected.name());
                updatePreview();
            }
        });

        wsCB.addItemListener(itemEvent -> {
            if (itemEvent.getStateChange() == 1) {
                // when wsCB combo box value changes, wsTypesCB combo box is filled with all possible options
                setWorkspaceTypesCombo(wsCB.getSelectedItem().toString());
            }
        });

        wsTypeCB.addItemListener(itemEvent -> {
            if (itemEvent.getStateChange() == 1) {
                // when wsTypesCB combo box value changes, wsTypeOptions combo box is filled with all possible options
                String workspaceName = wsCB.getSelectedItem().toString();
                Workspace.Kind kindSelected = wsTypeCB.getSelectedItem().equals("") ? null : (Workspace.Kind) wsTypeCB.getSelectedItem();
                setWorkspaceTypeOptionsCombo(workspaceName, kindSelected);
                String resource = wsTypeOptionsCB.isVisible() && wsTypeOptionsCB.getItemCount() > 0 ? wsTypeOptionsCB.getSelectedItem().toString() : null;
                updateWorkspaceModel(workspaceName, kindSelected, resource);
                updatePreview();
            }
        });

        wsTypeOptionsCB.addItemListener(itemEvent -> {
            if (itemEvent.getStateChange() == 1) {
                // when wsCB combo box value changes, wsTypesCB combo box is filled with all possible options
                updateWorkspaceModel(wsCB.getSelectedItem().toString(), (Workspace.Kind) wsTypeCB.getSelectedItem(), itemEvent.getItem().toString());
                updatePreview();
            }
        });
    }
}
