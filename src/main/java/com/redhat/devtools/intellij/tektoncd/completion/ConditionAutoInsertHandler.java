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

import com.intellij.codeInsight.lookup.LookupElement;
import io.fabric8.tekton.pipeline.v1alpha1.Condition;
import io.fabric8.tekton.resource.v1alpha1.ResourceDeclaration;
import io.fabric8.tekton.v1alpha1.internal.pipeline.pkg.apis.pipeline.v1alpha2.ParamSpec;

public class ConditionAutoInsertHandler extends BaseAutoInsertHandler {

    @Override
    public String getParentName() {
        return "- conditionRef";
    }

    @Override
    public String getCompletionText(LookupElement item, int indentationSize, int indentationParent) {
        Condition condition = (Condition) item.getObject();

        String completionText = condition.getMetadata().getName() + "\n";
        if (condition.getSpec().getParams() != null && condition.getSpec().getParams().size() > 0) {
            completionText += getIndentationAsText(indentationParent, indentationSize, 1) + "params:\n";
            for (ParamSpec param: condition.getSpec().getParams()) {
                completionText += getIndentationAsText(indentationParent, indentationSize, 1) + "- name: " + param.getName() + "\n";
                completionText += getIndentationAsText(indentationParent, indentationSize, 2) + "value: \n";
            }
        }
        if (condition.getSpec().getResources() != null && condition.getSpec().getResources().size() > 0) {
            completionText += getIndentationAsText(indentationParent, indentationSize, 1) + "resources:\n";
            for (ResourceDeclaration resource: condition.getSpec().getResources()) {
                completionText += getIndentationAsText(indentationParent, indentationSize, 1) + "- name: " + resource.getName() + "\n";
                completionText += getIndentationAsText(indentationParent, indentationSize, 2) + "resource: \n";
            }
        }

        return completionText;
    }
}
