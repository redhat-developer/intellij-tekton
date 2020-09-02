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

import com.intellij.codeInsight.completion.InsertHandler;
import com.intellij.codeInsight.completion.InsertionContext;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.openapi.editor.Document;
import org.jetbrains.annotations.NotNull;

public class VariableCompletionAutoInsertHandler implements InsertHandler<LookupElement> {

    private String variable;
    private int offset;

    public VariableCompletionAutoInsertHandler(String variable, int offset) {
        this.variable = variable;
        this.offset = offset;
    }

    @Override
    public void handleInsert(@NotNull InsertionContext context, @NotNull LookupElement item) {
        Document document = context.getEditor().getDocument();
        String toInsert = item.getObject().toString().substring(0, offset) + variable;
        int startOffset = context.getStartOffset();
        int tailOffset = context.getTailOffset();

        document.replaceString(startOffset, tailOffset, toInsert);
    }
}
