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
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.tekton.pipeline.v1beta1.ClusterTask;
import io.fabric8.tekton.pipeline.v1beta1.ParamSpec;
import io.fabric8.tekton.pipeline.v1beta1.Task;
import io.fabric8.tekton.pipeline.v1beta1.TaskResource;
import io.fabric8.tekton.pipeline.v1beta1.TaskResources;
import java.util.stream.Collectors;
import io.fabric8.tekton.pipeline.v1beta1.WorkspaceDeclaration;
import java.util.Collections;
import java.util.List;

public class TaskAutoInsertHandler extends BaseAutoInsertHandler {

    @Override
    public String getParentName() {
        return "taskRef";
    }

    @Override
    public String getCompletionText(LookupElement item, int indentationSize, int indentationParent) {
        HasMetadata taskRefItem = (HasMetadata) item.getObject();

        String completionText = taskRefItem.getMetadata().getName() + "\n";
        completionText += getIndentationAsText(indentationParent, indentationSize, 1) + "kind: " + (isTask(taskRefItem) ? "Task" : "ClusterTask") + "\n";

        List<ParamSpec> params = getParams(taskRefItem);
        if (params != null && params.size() > 0) {
            completionText += getIndentationAsText(indentationParent, indentationSize, 0) + "params:\n";
            for (ParamSpec param: params) {
                completionText += getIndentationAsText(indentationParent, indentationSize, 0) + "- name: " + param.getName() + "\n";
                String defaultText = "";
                ArrayOrString defaultValue = param.getDefault();
                if (defaultValue != null) {
                    if (defaultValue.getType().equalsIgnoreCase("string")) {
                        defaultText = defaultValue.getStringVal();
                    } else {
                        defaultText = "[" + defaultValue.getArrayVal().stream().map(val -> "\"" + val + "\"").collect(Collectors.joining(",")) + "]";
                    }
                }
                completionText += getIndentationAsText(indentationParent, indentationSize, 1) + "value: " + defaultText + "\n";
            }
        }
        TaskResources resources = getResources(taskRefItem);
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

        //workspaces
        List<WorkspaceDeclaration> workspaces = getWorkspaces(taskRefItem);
        if (!workspaces.isEmpty()) {
            completionText += getIndentationAsText(indentationParent, indentationSize, 0) + "workspaces:\n";
            for (WorkspaceDeclaration workspace: workspaces) {
                completionText += getIndentationAsText(indentationParent, indentationSize, 0) + "- name: " + workspace.getName() + "\n";
                completionText += getIndentationAsText(indentationParent, indentationSize, 1) + "workspace: \n";
            }
        }

        return completionText;
    }

    private boolean isTask(HasMetadata item) {
        return item instanceof  Task;
    }

    private List<ParamSpec> getParams(HasMetadata item) {
        if (item instanceof Task) {
            return ((Task) item).getSpec().getParams();
        } else if (item instanceof ClusterTask) {
            return ((ClusterTask) item).getSpec().getParams();
        } else {
            return Collections.emptyList();
        }
    }

    private TaskResources getResources(HasMetadata item) {
        if (item instanceof Task) {
            return ((Task) item).getSpec().getResources();
        } else if (item instanceof ClusterTask) {
            return ((ClusterTask) item).getSpec().getResources();
        } else {
            return null;
        }
    }

    private List<WorkspaceDeclaration> getWorkspaces(HasMetadata item) {
        if (item instanceof Task) {
            return ((Task) item).getSpec().getWorkspaces();
        } else if (item instanceof ClusterTask) {
            return ((ClusterTask) item).getSpec().getWorkspaces();
        } else {
            return Collections.emptyList();
        }
    }
}
