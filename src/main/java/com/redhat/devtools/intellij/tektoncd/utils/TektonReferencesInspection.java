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
package com.redhat.devtools.intellij.tektoncd.utils;

import com.intellij.codeInspection.InspectionManager;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.lang.FileASTNode;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.redhat.devtools.intellij.tektoncd.tkn.component.field.Input;
import com.redhat.devtools.intellij.tektoncd.tkn.component.field.Output;
import com.redhat.devtools.intellij.tektoncd.utils.model.ConfigurationModel;
import com.redhat.devtools.intellij.tektoncd.utils.model.ConfigurationModelFactory;
import com.redhat.devtools.intellij.tektoncd.utils.model.resources.PipelineConfigurationModel;
import com.redhat.devtools.intellij.tektoncd.utils.model.resources.TaskConfigurationModel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


import static com.redhat.devtools.intellij.common.CommonConstants.PROJECT;

public class TektonReferencesInspection extends LocalInspectionTool {

    @Nullable
    @Override
    public ProblemDescriptor[] checkFile(@NotNull PsiFile file, @NotNull InspectionManager manager, boolean isOnTheFly) {
        if (!file.getLanguage().getID().equalsIgnoreCase("yaml")) {
            return ProblemDescriptor.EMPTY_ARRAY;
        }

        final VirtualFile virtualFile = file.getVirtualFile();
        final Project project = virtualFile.getUserData(PROJECT);
        if (project == null) {
            return ProblemDescriptor.EMPTY_ARRAY;
        }
        ConfigurationModel model = ConfigurationModelFactory.getModel(file.getText());
        if (model == null) {
            return ProblemDescriptor.EMPTY_ARRAY;
        }

        List<PsiElement> unusedPsiElements = new ArrayList<>();
        if (model instanceof PipelineConfigurationModel) {
            unusedPsiElements = highlightInPipeline(file, (PipelineConfigurationModel) model);
        } else if (model instanceof TaskConfigurationModel) {
            unusedPsiElements = highlightInTask(file, (TaskConfigurationModel) model);
        }

        return unusedPsiElements.stream().map(item ->
                manager.createProblemDescriptor(item, item, "Variable " + item.getText() + " is never used", ProblemHighlightType.LIKE_UNUSED_SYMBOL, isOnTheFly, LocalQuickFix.EMPTY_ARRAY)
        ).toArray(ProblemDescriptor[]::new);
    }

    private List<PsiElement> highlightInPipeline(PsiFile file, PipelineConfigurationModel model) {
        List<Input> params = model.getParams();
        List<Input> inputResources = model.getInputResources();
        List<String> workspaces = model.getWorkspaces();

        if (params.isEmpty() && inputResources.isEmpty() && workspaces.isEmpty()) {
            return Collections.emptyList();
        }

        return findUnusedVariables(file, params, inputResources, Collections.emptyList(), workspaces);
    }

    private List<PsiElement> highlightInTask(PsiFile file, TaskConfigurationModel model) {
        List<Input> params = model.getParams();
        List<Input> inputResources = model.getInputResources();
        List<Output> outputResources = model.getOutputResources();
        List<String> workspaces = model.getWorkspaces();

        if (params.isEmpty() && inputResources.isEmpty() && outputResources.isEmpty() && workspaces.isEmpty()) {
            return Collections.emptyList();
        }

        return findUnusedVariables(file, params, inputResources, outputResources, workspaces);
    }

    private List<PsiElement> findUnusedVariables(PsiFile file, List<Input> params, List<Input> inputResource, List<Output> outputResources, List<String> workspaces) {
        if (!file.getText().contains("\nspec:")) {
            return Collections.emptyList();
        }

        String spec = file.getText().substring(file.getText().indexOf("\nspec:"));
        List<PsiElement> unusedPsiElements = new ArrayList<>();

        params.forEach(param -> {
            int variableFirstUsageIndex = indexOfByPattern(Pattern.compile("\\$\\(params\\." + param.name()), spec);
            if (variableFirstUsageIndex == -1) {
                PsiElement psiNode = getVariablePsiElement(file, "params:", param.name());
                unusedPsiElements.add(psiNode);
            }
        });

        inputResource.forEach(resource -> {
            int variableFirstUsageIndex = indexOfByPattern(Pattern.compile("\\$\\(resources\\.inputs\\." + resource.name()), spec);
            if (variableFirstUsageIndex == -1) {
                PsiElement psiNode;
                if (isPipeline(file)) {
                    psiNode = getVariablePsiElement(file, "resources:", resource.name());
                } else {
                    psiNode = getVariablePsiElement(file, "inputs:", resource.name());
                }
                unusedPsiElements.add(psiNode);
            }
        });

        outputResources.forEach(resource -> {
            int variableFirstUsageIndex = indexOfByPattern(Pattern.compile("\\$\\(resources\\.outputs\\." + resource.name()), spec);
            if (variableFirstUsageIndex == -1) {
                PsiElement psiNode = getVariablePsiElement(file, "outputs:", resource.name());
                unusedPsiElements.add(psiNode);
            }
        });

        workspaces.forEach(workspace -> {
            int variableFirstUsageIndex = indexOfByPattern(Pattern.compile("\\$\\(workspaces\\." + workspace), spec);
            if (variableFirstUsageIndex == -1) {
                PsiElement psiNode = getVariablePsiElement(file, "workspaces:", workspace);
                unusedPsiElements.add(psiNode);
            }
        });

        return unusedPsiElements;
    }

    private PsiElement getVariablePsiElement(PsiFile file, String inputType, String variable) {
        int specIndex = file.getText().indexOf("\nspec:");
        String spec = file.getText().substring(specIndex);
        final FileASTNode node = file.getNode();
        int variableTypeIndex = spec.indexOf(inputType);
        String variableTypeSection = spec.substring(variableTypeIndex);
        int variableLineIndex = indexOfByPattern(Pattern.compile("name:\\s*" + variable), variableTypeSection);
        int actualVariableIndex = variableTypeSection.substring(variableLineIndex).indexOf(variable);
        int absoluteIndex = specIndex + variableTypeIndex + variableLineIndex + actualVariableIndex;
        PsiElement psiNode = node.findLeafElementAt(absoluteIndex).getPsi();
        return psiNode;
    }

    private boolean isPipeline(PsiFile file) {
        int pipelineIndex = indexOfByPattern(Pattern.compile("kind:\\s*Pipeline"), file.getText());
        return pipelineIndex != -1;
    }

    private int indexOfByPattern(Pattern pattern, String textWhereToSearch) {
        Matcher matcher = pattern.matcher(textWhereToSearch);
        return matcher.find() ? matcher.start() : -1;
    }
}