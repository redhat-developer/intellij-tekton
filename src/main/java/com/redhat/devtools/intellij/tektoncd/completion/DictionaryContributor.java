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

import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PlainTextTokenTypes;
import org.jetbrains.yaml.YAMLLanguage;

public class DictionaryContributor extends CompletionContributor {
    public DictionaryContributor() {
        // conditions
        extend(CompletionType.BASIC,
                YamlElementPatternHelper.getSingleLineScalarKey("conditionRef"),
                new ConditionCompletionProvider());
        // runAfter
        extend(CompletionType.BASIC,
                YamlElementPatternHelper.getMultipleLineScalarKey("runAfter"),
                new RunAfterCompletionProvider());
        // general tekton snippets
        extend(CompletionType.BASIC,
                PlatformPatterns.psiElement(PlainTextTokenTypes.PLAIN_TEXT),
                new DictionaryCompletionProvider());
        extend(CompletionType.BASIC,
                PlatformPatterns.psiElement().withLanguage(YAMLLanguage.INSTANCE),
                new DictionaryCompletionProvider());
    }
}
