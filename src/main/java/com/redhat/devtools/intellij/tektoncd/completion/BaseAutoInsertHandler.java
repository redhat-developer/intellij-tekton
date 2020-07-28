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

import com.intellij.application.options.CodeStyle;
import com.intellij.codeInsight.completion.InsertHandler;
import com.intellij.codeInsight.completion.InsertionContext;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.openapi.editor.Document;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;

public abstract class BaseAutoInsertHandler implements InsertHandler<LookupElement> {
    @Override
    public void handleInsert(@NotNull InsertionContext context, @NotNull LookupElement item) {
        Document document = context.getEditor().getDocument();
        int startOffset = context.getStartOffset();
        int tailOffset = context.getTailOffset();
        int indentationSize = CodeStyle.getIndentOptions(context.getProject(), document).INDENT_SIZE;
        int indentationParent = getParentIndentation(document, getParentName(), startOffset);
        String completionText = getCompletionText(item, indentationSize, indentationParent);
        document.replaceString(startOffset, tailOffset, completionText);
    }

    protected int getParentIndentation(Document document, String label, int offset) {
        int positionNewLine = document.getText().substring(0, offset).lastIndexOf("\n");
        int nSpaces = document.getText().indexOf(label, positionNewLine);
        return (nSpaces + 1) - (positionNewLine + 2);
    }

    protected String getIndentationAsText(int indentationParent, int indentSize, int level) {
        return String.join("", Collections.nCopies(indentationParent + (indentSize * level), " "));
    }

    public abstract String getParentName();

    public abstract String getCompletionText(LookupElement item, int indentationSize, int indentationParent);
}
