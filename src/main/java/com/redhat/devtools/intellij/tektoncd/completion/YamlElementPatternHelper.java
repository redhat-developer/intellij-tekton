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

import com.intellij.patterns.ElementPattern;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import org.jetbrains.yaml.YAMLLanguage;
import org.jetbrains.yaml.YAMLTokenTypes;
import org.jetbrains.yaml.psi.YAMLKeyValue;
import org.jetbrains.yaml.psi.YAMLScalar;

public class YamlElementPatternHelper {
    public static ElementPattern<PsiElement> getSingleLineScalarKey(String... keyName) {
        // it finds key: "value" fields
        return PlatformPatterns
                .psiElement(YAMLTokenTypes.TEXT)
                .withParent(
                        PlatformPatterns
                             .psiElement(YAMLScalar.class)
                             .withParent(
                                     PlatformPatterns
                                             .psiElement(YAMLKeyValue.class)
                                             .withName(
                                                     PlatformPatterns.string().oneOf(keyName)
                                             )
                             )
                )
                .withLanguage(YAMLLanguage.INSTANCE);

    }

}
