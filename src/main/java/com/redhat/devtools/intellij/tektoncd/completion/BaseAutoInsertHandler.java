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

import com.intellij.openapi.editor.Document;

import java.util.Collections;

public class BaseAutoInsertHandler {
    protected int getParentIndentation(Document document, String label, int offset) {
        int positionNewLine = document.getText().substring(0, offset).lastIndexOf("\n");
        int nSpaces = document.getText().indexOf(label, positionNewLine);
        return (nSpaces + 1) - (positionNewLine + 2);
    }

    protected String getIndentationAsText(int indentationParent, int indentSize, int level) {
        return String.join("", Collections.nCopies(indentationParent + (indentSize * level), " "));
    }
}
