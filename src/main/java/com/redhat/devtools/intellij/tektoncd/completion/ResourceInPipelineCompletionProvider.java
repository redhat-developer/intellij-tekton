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
import com.redhat.devtools.intellij.tektoncd.utils.model.ConfigurationModel;
import com.redhat.devtools.intellij.tektoncd.utils.model.ConfigurationModelFactory;
import com.redhat.devtools.intellij.tektoncd.utils.model.resources.PipelineConfigurationModel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class ResourceInPipelineCompletionProvider extends CompletionProvider<CompletionParameters> {

    @Override
    protected void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context, @NotNull CompletionResultSet result) {
        result.addAllElements(getResourcesLookups(parameters));
        result.stopHere();
    }

    private List<LookupElementBuilder> getResourcesLookups(CompletionParameters parameters) {
        String configuration = parameters.getEditor().getDocument().getText();
        ConfigurationModel model = ConfigurationModelFactory.getModel(configuration);
        if (model == null) return Collections.emptyList();

        List<LookupElementBuilder> lookups = new ArrayList<>();
        ((PipelineConfigurationModel)model).getInputResources().forEach(resource -> {
            lookups.add(LookupElementBuilder.create(resource.name())
                    .withPresentableText(resource.name())
                    .withLookupString(resource.name()));
        });
        return lookups;
    }
}