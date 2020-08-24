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

import com.fasterxml.jackson.databind.JsonNode;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.util.ProcessingContext;
import com.redhat.devtools.intellij.common.utils.YAMLHelper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RunAfterCompletionProvider extends CompletionProvider<CompletionParameters> {
    Logger logger = LoggerFactory.getLogger(ConditionCompletionProvider.class);

    @Override
    protected void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context, @NotNull CompletionResultSet result) {
        result.addAllElements(getTasksLookups(parameters));
        result.stopHere();
    }

    private List<LookupElementBuilder> getTasksLookups(CompletionParameters parameters) {
        List<LookupElementBuilder> lookups = new ArrayList<>();

        try {
            // get the list of tasks already added in the runAfter array to avoid showing them again
            String[] tasksAlreadyAdded = parameters.getPosition().getParent().getParent().getParent().getText().split("\n");
            tasksAlreadyAdded = Arrays.stream(tasksAlreadyAdded).map(task -> task.trim().replaceFirst("- ", "")).toArray(String[]::new);

            // get current task node
            PsiElement currentTask = parameters.getPosition().getParent().getParent().getParent().getParent().getParent().getContext();

            // get all tasks node found in the pipeline before the selected one
            String yamlUntilTask = parameters.getEditor().getDocument().getText(new TextRange(0, currentTask.getTextOffset()));
            JsonNode tasksNode = YAMLHelper.getValueFromYAML(yamlUntilTask, new String[]{"spec", "tasks"} );
            if (tasksNode != null) {
                for (JsonNode item : tasksNode) {
                    if (item != null) {
                        String name = item.get("name").asText("");
                        if (!name.isEmpty() && !Arrays.asList(tasksAlreadyAdded).contains(name)) {
                            lookups.add(0, LookupElementBuilder.create(name)
                                    .withPresentableText(name)
                                    .withLookupString(name));
                        }
                    }
                }
            }
        } catch (IOException e) {
            logger.warn("Error: " + e.getLocalizedMessage());
        }

        return lookups;
    }
}

