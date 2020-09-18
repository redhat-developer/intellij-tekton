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
import com.intellij.util.ProcessingContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class OperatorInWhenClauseCodeCompletion extends CompletionProvider<CompletionParameters> {

    private final static String[] operators = new String[] { "in", "notin" };

    @Override
    protected void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context, @NotNull CompletionResultSet result) {
        result.addAllElements(getOperatorsLookups());
        result.stopHere();
    }

    private List<LookupElementBuilder> getOperatorsLookups() {
        List<LookupElementBuilder> lookups = new ArrayList<>();
        Arrays.stream(operators).forEach(operator -> {
            lookups.add(LookupElementBuilder.create(operator)
                    .withPresentableText(operator)
                    .withLookupString(operator));
        });
        return lookups;
    }
}