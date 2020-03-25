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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.redhat.devtools.intellij.tektoncd.utils.SnippetHelper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TknDictionary {
    private List<LookupElementBuilder> lookups;
    private static TknDictionary INSTANCE;

    private TknDictionary() throws IOException {
        load();
    }

    public static final TknDictionary get() throws IOException {
        if (INSTANCE == null) {
            INSTANCE = new TknDictionary();
        }
        return INSTANCE;
    }

    private void load() throws IOException {
        lookups = new ArrayList<>();
        JsonNode snippetsNode = SnippetHelper.getSnippetJSON();
        if (snippetsNode != null) {
            for (Iterator<JsonNode> it = snippetsNode.iterator(); it.hasNext(); ) {
                JsonNode item = it.next();
                LookupElementBuilder elem = LookupElementBuilder.create(item.get("prefix").asText()).
                        withTailText(" " + item.get("description").asText(),true).
                        withInsertHandler(new TknAutoInsertHandler());
                ArrayNode keywordsNode = (ArrayNode) item.get("keywords");
                if (keywordsNode != null) {
                    for (Iterator<JsonNode> keyword = keywordsNode.iterator(); keyword.hasNext(); ) {
                        elem = elem.withLookupString(keyword.next().asText());
                    }
                }
                lookups.add(elem);
            }
        }
    }

    public List<LookupElementBuilder> getLookups() {
        return lookups;
    }
}