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
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
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
            List<String> tasksAlreadyAdded = Arrays.asList(parameters.getPosition().getParent().getParent().getParent().getText().split("\n"));
            tasksAlreadyAdded = tasksAlreadyAdded.stream().map(task -> task.trim().replaceFirst("- ", "")).collect(Collectors.toList());

            // get current task node position
            PsiElement currentTask = parameters.getPosition().getParent().getParent().getParent().getParent().getParent().getContext();
            String yamlUntilTask = parameters.getEditor().getDocument().getText(new TextRange(0, currentTask.getTextOffset()));
            long taskPosition = 0;
            try {
                JsonNode tasksNodeUntilSelected = YAMLHelper.getValueFromYAML(yamlUntilTask, new String[]{"spec"} );
                if (tasksNodeUntilSelected.has("tasks")) {
                    taskPosition = StreamSupport.stream(tasksNodeUntilSelected.get("tasks").spliterator(),true).count();
                }
            } catch (IOException e) {
                logger.warn("Error: " + e.getLocalizedMessage());
            }

            // get all tasks node found in the pipeline and add valid options to lookup list
            String yaml = parameters.getEditor().getDocument().getText();
            JsonNode tasksNode = YAMLHelper.getValueFromYAML(yaml, new String[]{"spec", "tasks"} );
            int cont = 0;
            if (tasksNode != null) {
                for (JsonNode item : tasksNode) {
                    if (item != null && cont != taskPosition) {
                        String name = item.has("name") ? item.get("name").asText("") : "";
                        if (!name.isEmpty() && !Arrays.asList(tasksAlreadyAdded).contains(name)) {
                            lookups.add(0, LookupElementBuilder.create(name)
                                    .withPresentableText(name)
                                    .withLookupString(name));
                        }
                    }
                    cont++;
                }
            }
        } catch (IOException e) {
            logger.warn("Error: " + e.getLocalizedMessage());
        }

        return lookups;
    }
}

