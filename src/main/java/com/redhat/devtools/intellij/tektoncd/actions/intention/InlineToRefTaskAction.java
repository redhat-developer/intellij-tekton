/*******************************************************************************
 * Copyright (c) 2021 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc.
 ******************************************************************************/
package com.redhat.devtools.intellij.tektoncd.actions.intention;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.yaml.psi.impl.YAMLKeyValueImpl;


import static com.redhat.devtools.intellij.tektoncd.Constants.NOTIFICATION_ID;

public class InlineToRefTaskAction extends PsiElementBaseIntentionAction implements IntentionAction {

    @Override
    public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement element) throws IncorrectOperationException {
        if (project.isDisposed()) {
            return;
        }
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement element) {
        // Quick sanity check
        if (element == null) {
            return false;
        }

        AtomicReference<String> name = new AtomicReference<>("");
        AtomicBoolean isAvailable = new AtomicBoolean(false);
        PsiElement taskSpec = getTaskSpecElement(element);
        Arrays.stream(element.getParent().getParent().getChildren()).forEach(psiElement -> {
            String firstChild = psiElement.getFirstChild().getText();
            if (firstChild.equalsIgnoreCase("name")) {
                name.set(((YAMLKeyValueImpl) psiElement).getValue().toString());
            } else if (firstChild.equalsIgnoreCase("taskSpec")) {
                isAvailable.set(true);
            }
        });
        // Is this a token of type representing a "?" character?
        super.setText(!name.get().isEmpty() ? "vuoto" : name.get());
        return true;
    }

    private PsiElement getTaskSpecElement(PsiElement element) {
        PsiElement tmpElement = element;
        PsiElement taskSpecElement = null;
        boolean doParse = true;
        while (doParse) {
            if (tmpElement == null) {
                break;
            }
            PsiElement currentNode = tmpElement.getFirstChild();


            if (currentNode != null) {
                if (currentNode.getText().equals("spec")) {
                    break;
                } else if(currentNode.getText().equals("taskSpec")) {
                    doParse = false;
                    taskSpecElement = currentNode;
                }
            }
            tmpElement = tmpElement.getParent();
        }

        return taskSpecElement;
    }

    @Override
    public @NotNull @Nls(capitalization = Nls.Capitalization.Sentence) String getFamilyName() {
        return NOTIFICATION_ID;
    }

    @Override
    public boolean checkFile(@Nullable PsiFile file) {
        return true;
    }
}
