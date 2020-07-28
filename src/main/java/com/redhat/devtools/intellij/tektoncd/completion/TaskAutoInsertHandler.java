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
import io.fabric8.tekton.pipeline.v1beta1.ParamSpec;
import io.fabric8.tekton.pipeline.v1beta1.Task;
import io.fabric8.tekton.pipeline.v1beta1.TaskResource;
import io.fabric8.tekton.pipeline.v1beta1.TaskResources;
import org.jetbrains.annotations.NotNull;

public class TaskAutoInsertHandler extends BaseAutoInsertHandler implements InsertHandler<LookupElement> {

    @Override
    public void handleInsert(@NotNull InsertionContext context, @NotNull LookupElement item) {
        Document document = context.getEditor().getDocument();
        Task task = (Task) item.getObject();
        int startOffset = context.getStartOffset();
        int tailOffset = context.getTailOffset();
        int indentationSize = CodeStyle.getIndentOptions(context.getProject(), document).INDENT_SIZE;
        int indentationParent = getParentIndentation(document, "taskRef", startOffset);

        String completionText = task.getMetadata().getName() + "\n";
        if (task.getSpec().getParams() != null && task.getSpec().getParams().size() > 0) {
            completionText += getIndentationAsText(indentationParent, indentationSize, 0) + "params:\n";
            for (ParamSpec param: task.getSpec().getParams()) {
                completionText += getIndentationAsText(indentationParent, indentationSize, 0) + "- name: " + param.getName() + "\n";
                completionText += getIndentationAsText(indentationParent, indentationSize, 1) + "value: \n";
            }
        }
        TaskResources resources = task.getSpec().getResources();
        if (resources!= null) {
            completionText += getIndentationAsText(indentationParent, indentationSize, 0) + "resources:\n";
            // inputs resources
            if (resources.getInputs() != null) {
                completionText += getIndentationAsText(indentationParent, indentationSize, 1) + "inputs:\n";
                for (TaskResource resource: resources.getInputs()) {
                    completionText += getIndentationAsText(indentationParent, indentationSize, 1) + "- name: " + resource.getName() + "\n";
                    completionText += getIndentationAsText(indentationParent, indentationSize, 2) + "resource: \n";
                }
            }
            // outputs resources
            if (resources.getOutputs() != null) {
                completionText += getIndentationAsText(indentationParent, indentationSize, 1) + "outputs:\n";
                for (TaskResource resource: resources.getOutputs()) {
                    completionText += getIndentationAsText(indentationParent, indentationSize, 1) + "- name: " + resource.getName() + "\n";
                    completionText += getIndentationAsText(indentationParent, indentationSize, 2) + "resource: \n";
                }
            }

        }
        document.replaceString(startOffset, tailOffset, completionText);
    }
}
