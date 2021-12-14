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
package com.redhat.devtools.intellij.tektoncd.inspector;

import com.intellij.codeInspection.InspectionManager;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.lang.ASTNode;
import com.intellij.lang.FileASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.redhat.devtools.intellij.common.utils.StringHelper;
import com.redhat.devtools.intellij.tektoncd.tkn.component.field.Input;
import com.redhat.devtools.intellij.tektoncd.tkn.component.field.Output;
import com.redhat.devtools.intellij.tektoncd.utils.model.ConfigurationModel;
import com.redhat.devtools.intellij.tektoncd.utils.model.resources.PipelineConfigurationModel;
import com.redhat.devtools.intellij.tektoncd.utils.model.resources.TaskConfigurationModel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

public class VariableReferencesInspector extends BaseInspector {

    @Nullable
    @Override
    public ProblemDescriptor[] checkFile(@NotNull PsiFile file, @NotNull InspectionManager manager, boolean isOnTheFly) {
        ConfigurationModel model = getTektonModelFromFile(file);
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
                manager.createProblemDescriptor(item, item, "Variable " + StringHelper.getUnquotedValueFromPsi(item.getContext()) + " is never used", ProblemHighlightType.WEAK_WARNING, isOnTheFly, LocalQuickFix.EMPTY_ARRAY)
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
            List<Integer> variableUsageIndexes = indexesOfByPattern(Pattern.compile("\\$\\(params\\." + param.name()), spec);
            if (variableUsageIndexes.isEmpty()) {
                PsiElement psiNode = getVariablePsiElement(file, "params:", param.name());
                if (psiNode != null) {
                    unusedPsiElements.add(psiNode);
                }
            }
        });

        inputResource.forEach(resource -> {
            Pattern pattern = isPipeline(file) ? Pattern.compile("resource:\\s+[\"\']?" + resource.name() + "(?=\\s|\"|')") : Pattern.compile("\\$\\(resources\\.inputs\\." + resource.name());
            List<Integer> variableUsageIndexes = indexesOfByPattern(pattern, spec);
            if (variableUsageIndexes.isEmpty()) {
                PsiElement psiNode;
                if (isPipeline(file)) {
                    psiNode = getVariablePsiElement(file, "resources:", resource.name());
                } else {
                    psiNode = getVariablePsiElement(file, "inputs:", resource.name());
                }
                if (psiNode != null) {
                    unusedPsiElements.add(psiNode);
                }
            }
        });

        outputResources.forEach(resource -> {
            List<Integer> variableUsageIndexes = indexesOfByPattern(Pattern.compile("\\$\\(resources\\.outputs\\." + resource.name()), spec);
            if (variableUsageIndexes.isEmpty()) {
                PsiElement psiNode = getVariablePsiElement(file, "outputs:", resource.name());
                if (psiNode != null) {
                    unusedPsiElements.add(psiNode);
                }
            }
        });

        workspaces.forEach(workspace -> {
            Pattern pattern = isPipeline(file) ? Pattern.compile("workspace:\\s+[\"\']?" + workspace + "(?=\\s|\"|')") : Pattern.compile("\\$\\(workspaces\\." + workspace);
            List<Integer> variableUsageIndexes = indexesOfByPattern(pattern, spec);
            if (variableUsageIndexes.isEmpty()) {
                PsiElement psiNode = getVariablePsiElement(file, "workspaces:", workspace);
                if (psiNode != null) {
                    unusedPsiElements.add(psiNode);
                }
            }
        });

        return unusedPsiElements;
    }

    private PsiElement getVariablePsiElement(PsiFile file, String inputType, String variable) {
        int specIndex = file.getText().indexOf("\nspec:");
        String spec = file.getText().substring(specIndex);
        final FileASTNode node = file.getNode();
        // it needs to find the correct section index. it may happen there are two section with the same name. e.g workspaces as direct spec child or taskRef child
        List<Integer> inputTypeIndexes = indexesOfByPattern(Pattern.compile(inputType), spec);
        int variableTypeIndex = getActualTypeSectionIndex(file, inputTypeIndexes);
        if (variableTypeIndex == -1) {
            return null;
        }

        String variableTypeSection = spec.substring(variableTypeIndex);
        List<Integer> variableLineIndex = indexesOfByPattern(Pattern.compile("name:\\s*[\"']?" + variable + "(?=\\s|\"|')"), variableTypeSection);
        if (variableLineIndex.isEmpty()) {
            return null;
        }
        int actualVariableIndex = variableTypeSection.substring(variableLineIndex.get(0)).indexOf(variable);
        int absoluteIndex = specIndex + variableTypeIndex + variableLineIndex.get(0) + actualVariableIndex;
        PsiElement psiNode = node.findLeafElementAt(absoluteIndex).getPsi();
        return psiNode;
    }

    private int getActualTypeSectionIndex(PsiFile file, List<Integer> indexes) {
        int index = -1;
        int specIndex = file.getText().indexOf("\nspec:");
        FileASTNode node = file.getNode();
        for (int tmpIndex: indexes) {
            ASTNode psiNode = node.findLeafElementAt(specIndex + tmpIndex);
            if (isSpecDirectChild(psiNode)) {
                index = tmpIndex;
                break;
            }
        }
        return index;
    }

    private boolean isSpecDirectChild(ASTNode node) {
        return node.getTreeParent().getTreeParent().getTreeParent().getText().startsWith("spec:") ||
                node.getTreeParent().getTreeParent().getTreeParent().getFirstChildNode().getText().startsWith("resources");
    }
}
