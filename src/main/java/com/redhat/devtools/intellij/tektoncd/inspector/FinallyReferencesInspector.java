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
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.redhat.devtools.intellij.tektoncd.utils.model.ConfigurationModel;
import com.redhat.devtools.intellij.tektoncd.utils.model.resources.PipelineConfigurationModel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

public class FinallyReferencesInspector extends BaseInspector {

    private static final String FINALLY_TAG = "finally:";
    private static final String RUN_AFTER_TAG = "runAfter:";

    @Nullable
    @Override
    public ProblemDescriptor[] checkFile(@NotNull PsiFile file, @NotNull InspectionManager manager, boolean isOnTheFly) {
        ConfigurationModel model = getTektonModelFromFile(file);
        if (model == null) {
            return ProblemDescriptor.EMPTY_ARRAY;
        }


        List<PsiElement> errorPsiElements = new ArrayList<>();
        if (model instanceof PipelineConfigurationModel) {
            errorPsiElements = findFinallySectionErrors(file);
        }

        return errorPsiElements.stream().map(item ->
                manager.createProblemDescriptor(item, item, "No runAfter can be specified in final tasks.", ProblemHighlightType.GENERIC_ERROR, isOnTheFly)
        ).toArray(ProblemDescriptor[]::new);
    }

    private List<PsiElement> findFinallySectionErrors(PsiFile file) {
        if (!file.getText().contains(FINALLY_TAG)) {
            return Collections.emptyList();
        }

        List<Integer> finallyNodeIndexMatches = indexesOfByPattern(Pattern.compile(START_ROW + FINALLY_TAG), file.getText());
        for (int index: finallyNodeIndexMatches) {
            // if this finally node is a direct child of spec
            if (file.getNode().findLeafElementAt(index).getTreeParent().getTreeParent().getText().startsWith("spec:")) {
                return hasRunAfterSection(file, index);
            }
        }
        return Collections.emptyList();
    }

    private List<PsiElement> hasRunAfterSection(PsiFile file, int finallyNodeIndex) {
        String textAfterFinally = file.getText().substring(finallyNodeIndex);
        if (!textAfterFinally.contains(RUN_AFTER_TAG)) {
            return Collections.emptyList();
        }

        List<PsiElement> runAfterNodes = new ArrayList<>();
        List<Integer> indexesRunAfterNodes = indexesOfByPattern(Pattern.compile(START_ROW + RUN_AFTER_TAG), textAfterFinally);
        for (int tmpIndex: indexesRunAfterNodes) {
            runAfterNodes.add(file.findElementAt(finallyNodeIndex + tmpIndex).getNextSibling().getNextSibling());
        }
        return runAfterNodes;
    }

}
