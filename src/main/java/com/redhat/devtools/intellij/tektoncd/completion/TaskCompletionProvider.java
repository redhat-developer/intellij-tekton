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
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.util.ProcessingContext;
import com.redhat.devtools.intellij.tektoncd.tkn.Tkn;
import com.redhat.devtools.intellij.tektoncd.utils.TreeHelper;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.redhat.devtools.intellij.tektoncd.Constants.NAMESPACE;

public class TaskCompletionProvider extends CompletionProvider<CompletionParameters> {
    Logger logger = LoggerFactory.getLogger(TaskCompletionProvider.class);

    @Override
    protected void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context, @NotNull CompletionResultSet result) {
        String ns = parameters.getOriginalFile().getVirtualFile().getUserData(NAMESPACE);
        result.addAllElements(getTasksLookups(parameters.getEditor().getProject(), ns));
        result.stopHere();
    }

    private List<LookupElementBuilder> getTasksLookups(Project project, String namespace) {
        Tkn tkn = TreeHelper.getTkn(project);
        List<LookupElementBuilder> lookups = Collections.emptyList();
        try {
            lookups = tkn.getTasks(namespace)
                    .stream()
                    .map(task -> LookupElementBuilder.create(task)
                                                        .withPresentableText(task.getMetadata().getName())
                                                        .withLookupString(task.getMetadata().getName())
                                                        .withInsertHandler(new TaskAutoInsertHandler()))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            logger.warn("Error: " + e.getLocalizedMessage());
        }
        return lookups;
    }
}
