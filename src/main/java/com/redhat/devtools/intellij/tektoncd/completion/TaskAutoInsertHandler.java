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
import io.fabric8.tekton.pipeline.v1beta1.ArrayOrString;
import io.fabric8.tekton.pipeline.v1beta1.ParamSpec;
import io.fabric8.tekton.pipeline.v1beta1.Task;
import io.fabric8.tekton.pipeline.v1beta1.TaskResource;
import io.fabric8.tekton.pipeline.v1beta1.TaskResources;
import java.util.stream.Collectors;

public class TaskAutoInsertHandler extends BaseAutoInsertHandler {

    @Override
    public String getParentName() {
        return "taskRef";
    }

    @Override
    public String getCompletionText(LookupElement item, int indentationSize, int indentationParent) {
        Task task = (Task) item.getObject();

        String completionText = task.getMetadata().getName() + "\n";
        if (task.getSpec().getParams() != null && task.getSpec().getParams().size() > 0) {
            completionText += getIndentationAsText(indentationParent, indentationSize, 0) + "params:\n";
            for (ParamSpec param: task.getSpec().getParams()) {
                completionText += getIndentationAsText(indentationParent, indentationSize, 0) + "- name: " + param.getName() + "\n";
                String defaultText = "";
                ArrayOrString defaultValue = param.getDefault();
                if (defaultValue != null) {
                    if (defaultValue.getType().equalsIgnoreCase("string")) {
                        defaultText = defaultValue.getStringVal();
                    } else {
                        defaultText = defaultValue.getArrayVal().stream().collect(Collectors.joining(","));
                    }
                }
                completionText += getIndentationAsText(indentationParent, indentationSize, 1) + "value: " + defaultText + "\n";
            }
        }
        TaskResources resources = task.getSpec().getResources();
        if (resources!= null) {
            completionText += getIndentationAsText(indentationParent, indentationSize, 0) + "resources:\n";
            // inputs resources
            if (resources.getInputs() != null && !resources.getInputs().isEmpty()) {
                completionText += getIndentationAsText(indentationParent, indentationSize, 1) + "inputs:\n";
                for (TaskResource resource: resources.getInputs()) {
                    completionText += getIndentationAsText(indentationParent, indentationSize, 1) + "- name: " + resource.getName() + "\n";
                    completionText += getIndentationAsText(indentationParent, indentationSize, 2) + "resource: \n";
                }
            }
            // outputs resources
            if (resources.getOutputs() != null && !resources.getOutputs().isEmpty()) {
                completionText += getIndentationAsText(indentationParent, indentationSize, 1) + "outputs:\n";
                for (TaskResource resource: resources.getOutputs()) {
                    completionText += getIndentationAsText(indentationParent, indentationSize, 1) + "- name: " + resource.getName() + "\n";
                    completionText += getIndentationAsText(indentationParent, indentationSize, 2) + "resource: \n";
                }
            }

        }
        return completionText;
    }
}
