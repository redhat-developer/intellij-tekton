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
import io.fabric8.tekton.pipeline.v1beta1.TaskResource;
import io.fabric8.tekton.pipeline.v1beta1.WorkspaceDeclaration;
import java.util.stream.Collectors;

public class SingleInputInTaskInsertHandler extends BaseAutoInsertHandler {
    @Override
    public String getParentName() {
        return "- name:";
    }

    @Override
    public String getCompletionText(LookupElement item, int indentationSize, int indentationParent) {
        String completionText = "";

        if (item.getObject() instanceof ParamSpec) {
            completionText = getParamCompletionText((ParamSpec) item.getObject(), indentationSize, indentationParent);
        } else if (item.getObject() instanceof TaskResource) {
            completionText = getResourceCompletionText((TaskResource) item.getObject(), indentationSize, indentationParent);
        } else if (item.getObject() instanceof WorkspaceDeclaration) {
            completionText = getWorkspaceCompletionText((WorkspaceDeclaration) item.getObject(), indentationSize, indentationParent);
        }

        return completionText;
    }

    private String getParamCompletionText(ParamSpec param, int indentationSize, int indentationParent) {
        String defaultValue = param.getDefault() == null
                                ? ""
                                : param.getDefault().getType().equalsIgnoreCase("string")
                                    ? param.getDefault().getStringVal()
                                    : param.getDefault().getArrayVal().stream().collect(Collectors.joining(","));
        return buildCompletionText(param.getName(), "value", defaultValue, indentationSize, indentationParent, 1);
    }

    private String getResourceCompletionText(TaskResource input, int indentationSize, int indentationParent) {
        return buildCompletionText(input.getName(), "resource", "", indentationSize, indentationParent, 2);
    }

    private String getWorkspaceCompletionText(WorkspaceDeclaration workspace, int indentationSize, int indentationParent) {
        return buildCompletionText(workspace.getName(), "workspace", "", indentationSize, indentationParent, 1);
    }

    private String buildCompletionText(String name, String valueKey, String value, int indentationSize, int indentationParent, int indentationLevel) {
        String completionText = name + "\n";
        completionText += getIndentationAsText(indentationParent, indentationSize, indentationLevel) + valueKey + ": " + value + "\n";
        return completionText;
    }
}
