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
package com.redhat.devtools.intellij.tektoncd.completion;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.psi.PsiElement;
import com.intellij.util.ProcessingContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

public class RunAfterCompletionProvider extends BaseCompletionProvider {

    @Override
    protected void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context, @NotNull CompletionResultSet result) {
        result.addAllElements(getTasksLookups(parameters));
        result.stopHere();
    }

    private List<LookupElementBuilder> getTasksLookups(CompletionParameters parameters) {
        List<LookupElementBuilder> tasksLookups = new ArrayList<>();
        // get the list of tasks already added in the runAfter array to avoid showing them again
        List<String> tasksAlreadyAdded = Arrays.asList(parameters.getPosition().getParent().getParent().getParent().getText().split("\n"));
        tasksAlreadyAdded = tasksAlreadyAdded.stream().map(task -> task.trim().replaceFirst("- ", "")).collect(Collectors.toList());

        // get current task node position
        PsiElement currentTask = parameters.getPosition().getParent().getParent().getParent().getParent().getParent().getContext();

        List<String> tasksInPipeline = getFilteredTasksInPipeline(parameters, currentTask, tasksAlreadyAdded);
        tasksInPipeline.stream().forEach(task -> {
            tasksLookups.add(0, LookupElementBuilder.create(task)
                    .withPresentableText(task)
                    .withLookupString(task));
        });
        return tasksLookups;
    }
}

