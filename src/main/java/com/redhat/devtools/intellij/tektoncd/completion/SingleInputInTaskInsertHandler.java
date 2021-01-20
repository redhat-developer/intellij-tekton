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
package com.redhat.devtools.intellij.tektoncd.completion;

import com.intellij.codeInsight.lookup.LookupElement;
import io.fabric8.tekton.pipeline.v1beta1.ParamSpec;
import java.util.stream.Collectors;

public class SingleInputInTaskInsertHandler extends BaseAutoInsertHandler {
    @Override
    public String getParentName() {
        return "taskRef";
    }

    @Override
    public String getCompletionText(LookupElement item, int indentationSize, int indentationParent) {
        ParamSpec inputItem = (ParamSpec) item.getObject();
        String defaultValue = inputItem.getDefault().getType().equalsIgnoreCase("string") ?
                inputItem.getDefault().getStringVal() :
                inputItem.getDefault().getArrayVal().stream().collect(Collectors.joining(","));
        String completionText = inputItem.getName() + "\n";
        completionText += getIndentationAsText(indentationParent, indentationSize, 1) + "value: " + defaultValue + "\n";

        return completionText;
    }
}
