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
import org.jetbrains.yaml.psi.YAMLMapping;
import org.jetbrains.yaml.psi.YAMLScalar;
import org.jetbrains.yaml.psi.YAMLSequence;
import org.jetbrains.yaml.psi.YAMLSequenceItem;

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

    public static ElementPattern<PsiElement> getMultipleLineScalarKey(String keyName) {
        // it finds
        // key:
        //  - "value"
        //  - "value 1"
        return PlatformPatterns
                .psiElement(YAMLTokenTypes.TEXT)
                .withParent(
                        PlatformPatterns
                                .psiElement(YAMLScalar.class)
                                .withParent(
                                        PlatformPatterns.
                                                psiElement(YAMLSequenceItem.class)
                                                .withParent(
                                                        PlatformPatterns.
                                                                psiElement(YAMLSequence.class)
                                                                .withParent(
                                                                        PlatformPatterns
                                                                                .psiElement(YAMLKeyValue.class)
                                                                                .withName(
                                                                                        PlatformPatterns.string().equalTo(keyName)
                                                                                )
                                                                )
                                                )
                                )
                )
                .withLanguage(YAMLLanguage.INSTANCE);

    }

    public static ElementPattern<PsiElement> getAfterParentScalarKey(String parent, String keyName) {
        // it finds parentKey: key: "value" fields
        return PlatformPatterns
                .psiElement(YAMLTokenTypes.TEXT)
                .withParent(
                        PlatformPatterns
                            .psiElement(YAMLScalar.class)
                            .withParent(
                                    PlatformPatterns
                                            .psiElement(YAMLKeyValue.class)
                                            .withName(
                                                    PlatformPatterns.string().equalTo(keyName)
                                            )
                                            .withParent(
                                                    PlatformPatterns
                                                            .psiElement(YAMLMapping.class)
                                                            .withParent(
                                                                    PlatformPatterns
                                                                            .psiElement(YAMLKeyValue.class)
                                                                            .withName(
                                                                                    PlatformPatterns.string().equalTo(parent)
                                                                            )
                                                            )
                                            )
                            )
            )
            .withLanguage(YAMLLanguage.INSTANCE);
    }

    public static ElementPattern<PsiElement> getAfterParentScalarKeyInSequence(String keyName, String... parent) {
        // it finds parentKey: key: "value" fields
        return PlatformPatterns
                .psiElement(YAMLTokenTypes.TEXT)
                .withParent(
                        PlatformPatterns
                                .psiElement(YAMLScalar.class)
                                .withParent(
                                        PlatformPatterns
                                                .psiElement(YAMLKeyValue.class)
                                                .withName(
                                                        PlatformPatterns.string().equalTo(keyName)
                                                )
                                                .withParent(
                                                        PlatformPatterns
                                                                .psiElement(YAMLMapping.class)
                                                                .withParent(
                                                                        PlatformPatterns
                                                                                .psiElement(YAMLSequenceItem.class)
                                                                                .withParent(
                                                                                        PlatformPatterns
                                                                                                .psiElement(YAMLSequence.class)
                                                                                                .withParent(
                                                                                                        PlatformPatterns
                                                                                                                .psiElement(YAMLKeyValue.class)
                                                                                                                .withName(
                                                                                                                        PlatformPatterns.string().oneOf(parent)
                                                                                                                )
                                                                                                )
                                                                                )
                                                                )
                                                )
                                )
                )
                .withLanguage(YAMLLanguage.INSTANCE);
    }

    public static ElementPattern<PsiElement> getAfterParentScalarKeyInSequenceFromRoot(String keyName, String root, String... parent) {
        // it finds parentKey: key: "value" fields
        return PlatformPatterns
                .psiElement(YAMLTokenTypes.TEXT)
                .withParent(
                        PlatformPatterns
                                .psiElement(YAMLScalar.class)
                                .withParent(
                                        PlatformPatterns
                                                .psiElement(YAMLKeyValue.class)
                                                .withName(
                                                        PlatformPatterns.string().equalTo(keyName)
                                                )
                                                .withParent(
                                                        PlatformPatterns
                                                                .psiElement(YAMLMapping.class)
                                                                .withParent(
                                                                        PlatformPatterns
                                                                                .psiElement(YAMLSequenceItem.class)
                                                                                .withParent(
                                                                                        PlatformPatterns
                                                                                                .psiElement(YAMLSequence.class)
                                                                                                .withParent(
                                                                                                        PlatformPatterns
                                                                                                                .psiElement(YAMLKeyValue.class)
                                                                                                                .withName(
                                                                                                                        PlatformPatterns.string().oneOf(parent)
                                                                                                                )
                                                                                                                .withParent(
                                                                                                                        PlatformPatterns
                                                                                                                                .psiElement(YAMLMapping.class)
                                                                                                                                .withParent(
                                                                                                                                        PlatformPatterns
                                                                                                                                                .psiElement(YAMLSequenceItem.class)
                                                                                                                                                .withParent(
                                                                                                                                                        PlatformPatterns
                                                                                                                                                                .psiElement(YAMLSequence.class)
                                                                                                                                                                .withParent(
                                                                                                                                                                        PlatformPatterns
                                                                                                                                                                                .psiElement(YAMLKeyValue.class)
                                                                                                                                                                                .withName(
                                                                                                                                                                                        PlatformPatterns.string().equalTo(root)
                                                                                                                                                                                )
                                                                                                                                                                )
                                                                                                                                                )
                                                                                                                                )
                                                                                                                )
                                                                                                )
                                                                                )
                                                                )
                                                )
                                )
                )
                .withLanguage(YAMLLanguage.INSTANCE);
    }

    public static ElementPattern<PsiElement> getAfterParentScalarKeyInSequenceFromRootWithParents(String keyName, String root, String secondLevelParent, String... firstLevelParent) {
        // it finds parentKey: key: "value" fields
        return PlatformPatterns
                .psiElement(YAMLTokenTypes.TEXT)
                .withParent(
                        PlatformPatterns
                                .psiElement(YAMLScalar.class)
                                .withParent(
                                        PlatformPatterns
                                                .psiElement(YAMLKeyValue.class)
                                                .withName(
                                                        PlatformPatterns.string().equalTo(keyName)
                                                )
                                                .withParent(
                                                        PlatformPatterns
                                                                .psiElement(YAMLMapping.class)
                                                                .withParent(
                                                                        PlatformPatterns
                                                                                .psiElement(YAMLSequenceItem.class)
                                                                                .withParent(
                                                                                        PlatformPatterns
                                                                                                .psiElement(YAMLSequence.class)
                                                                                                .withParent(
                                                                                                        PlatformPatterns
                                                                                                                .psiElement(YAMLKeyValue.class)
                                                                                                                .withName(
                                                                                                                        PlatformPatterns.string().oneOf(firstLevelParent)
                                                                                                                )
                                                                                                                .withParent(
                                                                                                                        PlatformPatterns
                                                                                                                                .psiElement(YAMLMapping.class)
                                                                                                                                .withParent(
                                                                                                                                        PlatformPatterns
                                                                                                                                                .psiElement(YAMLKeyValue.class)
                                                                                                                                                .withName(
                                                                                                                                                        PlatformPatterns.string().equalTo(secondLevelParent)
                                                                                                                                                )
                                                                                                                                                .withParent(
                                                                                                                                                        PlatformPatterns
                                                                                                                                                                .psiElement(YAMLMapping.class)
                                                                                                                                                                .withParent(
                                                                                                                                                                        PlatformPatterns
                                                                                                                                                                                .psiElement(YAMLSequenceItem.class)
                                                                                                                                                                                .withParent(
                                                                                                                                                                                        PlatformPatterns
                                                                                                                                                                                                .psiElement(YAMLSequence.class)
                                                                                                                                                                                                .withParent(
                                                                                                                                                                                                        PlatformPatterns
                                                                                                                                                                                                                .psiElement(YAMLKeyValue.class)
                                                                                                                                                                                                                .withName(
                                                                                                                                                                                                                        PlatformPatterns.string().equalTo(root)
                                                                                                                                                                                                                )
                                                                                                                                                                                                )
                                                                                                                                                                                )
                                                                                                                                                                )

                                                                                                                                                )

                                                                                                                                )
                                                                                                                )
                                                                                                )
                                                                                )
                                                                )
                                                )
                                )
                )
                .withLanguage(YAMLLanguage.INSTANCE);
    }


}
