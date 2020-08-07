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

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.ui.JBCardLayout;
import com.redhat.devtools.intellij.tektoncd.tkn.component.field.Input;
import com.redhat.devtools.intellij.tektoncd.tkn.component.field.Output;
import com.redhat.devtools.intellij.tektoncd.utils.StartResourceModel;
import com.redhat.devtools.intellij.tektoncd.utils.YAMLBuilder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import org.jetbrains.annotations.Nullable;

public class StartWizard extends AbstractWizard<BaseStep> {

    private final String myTitle;
    private List<JLabel> navigationList;
    private JPanel navigationPanel;
    private JTextArea previewTextArea;

    public StartWizard(String title, @Nullable Project project, StartResourceModel model) {
        super(title, project);
        myTitle = title;
        Color white = new Color(255, 255, 255);

        myContentPanel.setBackground(white);
        myContentPanel.setPreferredSize(new Dimension(550, 400));
        myLeftPanel.setBackground(white);
        myLeftPanel.setBorder(new MatteBorder(0, 0, 0, 1, new Color(204, 204, 204)));
        myRightPanel.setBackground(white);
        myRightPanel.setBorder(new MatteBorder(0, 1, 0, 0, new Color(204, 204, 204)));
        myRightPanel.setVisible(false);

        List<BaseStep> steps = getSteps(model);

        navigationList = new ArrayList<>();
        Box box = Box.createVerticalBox();
        navigationPanel = new JPanel();
        navigationPanel.setBackground(white);
        navigationPanel.setBorder(new EmptyBorder(10, 15, 0, 15));
        navigationPanel.add(box);
        steps.stream().forEach(step -> {
            JLabel currentStepLabel = new JLabel(step.getTitle());
            currentStepLabel.setForeground(new Color(0, 0, 0));
            currentStepLabel.setBorder(new EmptyBorder(0, 0, 5, 0));

            box.add(currentStepLabel);
            navigationList.add(currentStepLabel);
        });

        navigationList.get(0).setForeground(new Color(0, 102, 204));


        setLeftPanel(navigationPanel);
        JPanel previewFooterPanel = new JPanel();
        if (SystemInfo.isMac) {
            myFooterPanel.add(previewFooterPanel, BorderLayout.LINE_START);
            previewFooterPanel.setLayout(new BoxLayout(previewFooterPanel, BoxLayout.X_AXIS));
        } else {
            myFooterPanel.add(previewFooterPanel, BorderLayout.LINE_START);
        }

        JCheckBox chk = new JCheckBox("Show Preview");
        chk.setBounds(100,100, 50,50);
        previewFooterPanel.add(chk);
        chk.addItemListener(itemEvent -> {
            if (chk.isSelected()) {
                myRightPanel.setVisible(true);
            } else {
                myRightPanel.setVisible(false);
            }
        });

        previewTextArea = new JTextArea();
        previewTextArea.setEditable(false);
        previewTextArea.setBorder(new EmptyBorder(10, 10, 10, 10));
        previewTextArea.setFont(new Font ("TimesRoman", Font.PLAIN, 15));
        setRightPanel(previewTextArea);
        updatePreview(model);
        addSteps(steps);
        init();
    }

    private List<BaseStep> getSteps(StartResourceModel model) {
        List<BaseStep> steps = new ArrayList<>();
        boolean hasParams = model.getInputs().stream().anyMatch(input -> input.kind() == Input.Kind.PARAMETER);
        boolean hasInputResources = model.getInputs().stream().anyMatch(input -> input.kind() == Input.Kind.RESOURCE);
        boolean hasOutputResources = !model.getOutputs().isEmpty();
        boolean hasWorkspaces = !model.getWorkspaces().isEmpty();
        if (hasParams) {
            steps.add(buildStepWithListener(new ParametersStep(model)));
        }

        if (hasInputResources) {
            steps.add(buildStepWithListener(new InputResourcesStep(model)));
        }

        if (hasOutputResources) {
            steps.add(buildStepWithListener(new OutputResourcesStep(model)));
        }

        if (hasWorkspaces) {
            steps.add(buildStepWithListener(new WorkspacesStep(model)));
        }

        steps.add(buildStepWithListener(new AuthenticationStep(model)));

        return steps;
    }

    private BaseStep buildStepWithListener(BaseStep step) {
        step.addStepListener(new BaseStep.Listener() {

            @Override
            public void stateChanged() {
                StartResourceModel model = getModelCurrentStep();
                updatePreview(model);
                updateButtons();
            }

            @Override
            public void doNextAction() {
                if (getNextButton().isEnabled()) {
                    StartWizard.this.doNextAction();
                }
            }
        });
        return step;
    }

    @Override
    protected void doPreviousAction() {
        int oldStep = myCurrentStep;
        myCurrentStep = getPreviousStep(myCurrentStep);
        setSelectItemNavigationList(oldStep, myCurrentStep);
        updateStep(JBCardLayout.SwipeDirection.BACKWARD);
    }

    @Override
    protected void doNextAction() {
        if (!canGoNext()) return;

        if (isLastStep()) {
            doOKAction();
            return;
        }


        int oldStep = myCurrentStep;
        myCurrentStep = getNextStep(myCurrentStep);
        //((NoSelectionModel)navigationList.getSelectionModel()).setSelectItem(myCurrentStep);

        //navigationList.setSelectedIndex(myCurrentStep);
        setSelectItemNavigationList(oldStep, myCurrentStep);
        updateStep(JBCardLayout.SwipeDirection.FORWARD);
    }

    private void setSelectItemNavigationList(int oldStep, int newStep) {
        if (newStep < 0) {
            newStep = 0;
        }
        if (newStep >= navigationList.size()) {
            newStep = navigationList.size() - 1;
        }
        navigationList.get(oldStep).setForeground(new Color(0, 0, 0));
        navigationList.get(newStep).setForeground(new Color(0, 102, 204));
    }

    private StartResourceModel getModelCurrentStep() {
        final BaseStep currentStep = mySteps.get(myCurrentStep);
        return currentStep.getModel();
    }

    @Override
    protected String getHelpID() {
        return getCurrentStepObject().getHelpId();
    }

    @Override
    protected void updateStep() {
        super.updateStep();
        String stepTitle = getCurrentStepObject().getTitle();
        setTitle(stepTitle != null ? myTitle + ": " + stepTitle : myTitle);
    }

    @Override
    protected void updateButtons() {
        boolean lastStep = isLastStep();
        updateButtons(lastStep, lastStep ? canFinish() : true, isFirstStep());
        getPreviousButton().setEnabled(myCurrentStep != 0);
    }

    @Override
    protected boolean canGoNext() {
        return getCurrentStepObject().isComplete();
    }

    @Override
    protected boolean isLastStep() {
        return getCurrentStep() == mySteps.size() - 1;
    }

    @Override
    protected boolean canFinish() {
        for (BaseStep step : mySteps) {
            if (!step.isComplete()) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected void dispose() {
        super.dispose();
        for (BaseStep step : mySteps) {
            Disposer.dispose(step);
        }
    }

    @Override
    public JPanel getContentComponent() {
        return super.getContentComponent();
    }

    private void updatePreview(StartResourceModel model) {
        String preview = "";
        try {
            preview = YAMLBuilder.createPreview(model);
        } catch (IOException e) {
            //logger.warn("Error: " + e.getLocalizedMessage());
        }
        previewTextArea.setText(preview);
    }

    @Override
    protected void doOKAction() {
        calculateArgs();
        super.doOKAction();
    }

    private void calculateArgs() {
        Map<String, String> parameters = new HashMap<>();
        Map<String, String> inputResources = new HashMap<>();
        Map<String, String> outputResources = new HashMap<>();

        StartResourceModel model = getModelCurrentStep();
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

}
