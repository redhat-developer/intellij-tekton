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
package com.redhat.devtools.intellij.tektoncd.ui.wizard.addtrigger;

import com.intellij.openapi.project.Project;
import com.redhat.devtools.intellij.tektoncd.ui.wizard.AuthenticationStep;
import com.redhat.devtools.intellij.tektoncd.ui.wizard.BaseStep;
import com.redhat.devtools.intellij.tektoncd.ui.wizard.BaseWizard;
import com.redhat.devtools.intellij.tektoncd.ui.wizard.InputResourcesStep;
import com.redhat.devtools.intellij.tektoncd.ui.wizard.OutputResourcesStep;
import com.redhat.devtools.intellij.tektoncd.ui.wizard.ParametersStep;
import com.redhat.devtools.intellij.tektoncd.ui.wizard.WorkspacesStep;
import com.redhat.devtools.intellij.tektoncd.utils.model.actions.AddTriggerModel;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.jetbrains.annotations.Nullable;

public class AddTriggerWizard extends BaseWizard {

    private Map<String, String> triggerBindingTemplates;

    public AddTriggerWizard(String title, @Nullable Project project, AddTriggerModel model, Map<String, String> triggerBindingTemplates) {
        super(title, project, model);
        this.triggerBindingTemplates = triggerBindingTemplates;
        init();
    }

    public List<BaseStep> getSteps() {
        List<BaseStep> steps = new ArrayList<>();
        boolean hasParams = !model.getParams().isEmpty();
        boolean hasInputResources = !model.getInputResources().isEmpty();
        boolean hasOutputResources = false;
        boolean hasWorkspaces = !model.getWorkspaces().isEmpty();

        steps.add(buildStepWithListener(new TriggerStep((AddTriggerModel) model, this.triggerBindingTemplates)));

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

    @Override
    public void doBeforeNextStep(int currentStep) {
        if (mySteps.get(currentStep).getTitle().equals("Trigger")) {
            Optional<BaseStep> parameterStep = mySteps.stream().filter(step -> step.getTitle().equals("Parameters")).findFirst();
            if (parameterStep.isPresent()) {
                parameterStep.get().refresh();
            }
        }
    }

    @Override
    public String getLastStepButtonText() {
        return "&Add Trigger";
    }

    public void calculateArgs() {}

}
