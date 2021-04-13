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
import com.redhat.devtools.intellij.tektoncd.utils.SnippetHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class TknAutoInsertHandler implements InsertHandler<LookupElement> {
    Logger logger = LoggerFactory.getLogger(TknAutoInsertHandler.class);

    @Override
    public void handleInsert(InsertionContext context, LookupElement item) {
        Document document = context.getEditor().getDocument();
        int startOffset = context.getStartOffset();
        int tailOffset = context.getTailOffset();
        try {
            String elementText = SnippetHelper.getBody(item.getLookupString());
            document.replaceString(startOffset, tailOffset, elementText);
        } catch (IOException ex) {
            logger.warn("Error: " + ex.getLocalizedMessage(), ex);
        }
    }
}
