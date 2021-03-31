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
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.redhat.devtools.intellij.common.utils.YAMLHelper;
import com.redhat.devtools.intellij.tektoncd.tkn.Tkn;
import com.redhat.devtools.intellij.tektoncd.utils.TreeHelper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.StreamSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BaseCompletionProvider extends CompletionProvider<CompletionParameters> {
    Logger logger = LoggerFactory.getLogger(BaseCompletionProvider.class);

    protected Tkn getClient(CompletionParameters parameters) {
        return TreeHelper.getTkn(parameters.getEditor().getProject());
    }

    /**
     * Return a list of tasks present in the pipeline except the current task and others if specified
     *
     * @param parameters data related to the current document
     * @param currentTaskElement the psielement representing the task where the user is typing in
     * @param tasksToExclude additional tasks to exclude from the resulting list
     * @return
     */
    protected List<String> getFilteredTasksInPipeline(CompletionParameters parameters, PsiElement currentTaskElement, List<String> tasksToExclude) {
        List<String> tasks = new ArrayList<>();

        try {
            String yamlUntilTask = parameters.getEditor().getDocument().getText(new TextRange(0, currentTaskElement.getTextOffset()));
            long taskPosition = 0;
            try {
                JsonNode tasksNodeUntilSelected = YAMLHelper.getValueFromYAML(yamlUntilTask, new String[]{"spec"} );
                if (tasksNodeUntilSelected.has("tasks")) {
                    taskPosition = StreamSupport.stream(tasksNodeUntilSelected.get("tasks").spliterator(), true).count();
                }
            } catch (IOException e) {
                logger.warn("Error: " + e.getLocalizedMessage(), e);
            }

            // get all tasks node found in the pipeline and add valid options to lookup list
            String yaml = parameters.getEditor().getDocument().getText();
            JsonNode tasksNode = YAMLHelper.getValueFromYAML(yaml, new String[]{"spec", "tasks"} );
            int cont = 0;
            if (tasksNode != null) {
                for (JsonNode item : tasksNode) {
                    if (item != null && cont != taskPosition) {
                        String name = item.has("name") ? item.get("name").asText("") : "";
                        if (!name.isEmpty() && !tasksToExclude.contains(name)) {
                            tasks.add(name);
                        }
                    }
                    cont++;
                }
            }
        } catch (IOException e) {
            logger.warn("Error: " + e.getLocalizedMessage(), e);
        }

        return tasks;
    }
}
