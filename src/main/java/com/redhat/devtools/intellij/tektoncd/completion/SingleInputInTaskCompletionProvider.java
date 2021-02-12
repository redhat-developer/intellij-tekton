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

import com.fasterxml.jackson.databind.JsonNode;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.util.ProcessingContext;
import com.redhat.devtools.intellij.common.utils.YAMLHelper;
import com.redhat.devtools.intellij.tektoncd.tkn.Tkn;
import com.redhat.devtools.intellij.tektoncd.utils.TreeHelper;
import com.redhat.devtools.intellij.tektoncd.utils.model.ConfigurationModel;
import com.redhat.devtools.intellij.tektoncd.utils.model.ConfigurationModelFactory;
import io.fabric8.tekton.pipeline.v1beta1.ClusterTask;
import io.fabric8.tekton.pipeline.v1beta1.Task;
import io.fabric8.tekton.pipeline.v1beta1.TaskSpec;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_CLUSTERTASK;
import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_TASK;
import static com.redhat.devtools.intellij.tektoncd.Constants.NAMESPACE;

public class SingleInputInTaskCompletionProvider extends BaseCompletionProvider {
    private static final Logger logger = LoggerFactory.getLogger(SingleInputInTaskCompletionProvider.class);
    @Override
    protected void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context, @NotNull CompletionResultSet result) {
        result.addAllElements(getSingleInputLookups(parameters));
        result.stopHere();
    }

    /**
     * Get lookups for all possible variables matching the prefix
     *
     * @param parameters
     * @return
     */
    private List<LookupElementBuilder> getSingleInputLookups(CompletionParameters parameters) {
        String configuration = parameters.getEditor().getDocument().getText();
        ConfigurationModel model = ConfigurationModelFactory.getModel(configuration);
        if (model == null) return Collections.emptyList();

        return getLookupsByKind(parameters, model);
    }

    /**
     * Get lookups for the current parameter based on the kind
     *
     * @param parameters
     * @param model the model built by the configuration
     * @return
     */
    private List<LookupElementBuilder> getLookupsByKind(CompletionParameters parameters, ConfigurationModel model) {
        switch (model.getKind().toLowerCase()) {
            case "pipeline":
                return getLookupsPipeline(parameters);
            default:
                return Collections.emptyList();
        }
    }

    private List<LookupElementBuilder> getLookupsPipeline(CompletionParameters parameters) {
        Project project = parameters.getEditor().getProject();
        String currentInputType = parameters.getPosition().getParent().getParent().getParent().getParent().getParent().getParent().getNode().getFirstChildNode().getText();
        JsonNode currentTaskNode = getCurrentTaskRefNameInPipeline(parameters, parameters.getPosition());
        Tkn tkn = TreeHelper.getTkn(project);
        String ns = parameters.getOriginalFile().getVirtualFile().getUserData(NAMESPACE);
        try {
            JsonNode taskRef = currentTaskNode.get("taskRef");
            if (taskRef == null) {
                return Collections.emptyList();
            }
            String nameTask = taskRef.has("name") ? taskRef.get("name").asText("") : "";
            if (nameTask.isEmpty()) {
                return Collections.emptyList();
            }
            String kind = taskRef.has("kind") ? taskRef.get("kind").asText(KIND_TASK) : KIND_TASK;
            Optional<Task> task = Optional.empty();
            Optional<ClusterTask> cTask = Optional.empty();

            if (kind.equalsIgnoreCase(KIND_TASK)) {
                task = tkn.getTasks(ns).stream().filter(t -> t.getMetadata().getName().equalsIgnoreCase(nameTask)).findFirst();
            } else if (kind.equalsIgnoreCase(KIND_CLUSTERTASK)){
                cTask = tkn.getClusterTasks().stream().filter(t -> t.getMetadata().getName().equalsIgnoreCase(nameTask)).findFirst();
            }

            TaskSpec spec = task.isPresent() ? task.get().getSpec() :
                            cTask.isPresent() ? cTask.get().getSpec() :
                            null;
            if (spec != null) {
                switch (currentInputType.toLowerCase()) {
                    case "params": {
                        return getParamsLookups(spec, currentTaskNode);
                    }
                    case "inputs": {
                        return getInputResourcesLookups(spec, currentTaskNode);
                    }
                    case "outputs": {
                        return getOutputResourcesLookups(spec, currentTaskNode);
                    }
                    case "workspaces": {
                        return getWorkspacesLookups(spec, currentTaskNode);
                    }
                }
            }
        } catch (IOException e) {
            logger.warn(e.getLocalizedMessage());
        }
        return Collections.emptyList();
    }

    private List<LookupElementBuilder> getParamsLookups(TaskSpec spec, JsonNode taskNode) {
        List<LookupElementBuilder> singleParamInputLookups = new ArrayList<>();

        if (taskNode.has("params")) {
            List<String> existingParams = getInputsInNode(taskNode.get("params"));
            spec.getParams().stream().filter(param -> !existingParams.contains(param.getName())).forEach(param -> {
                singleParamInputLookups.add(0, LookupElementBuilder.create(param)
                        .withPresentableText(param.getName())
                        .withLookupString(param.getName())
                        .withInsertHandler(new SingleInputInTaskInsertHandler()));
            });
        }

        return singleParamInputLookups;
    }

    private List<LookupElementBuilder> getInputResourcesLookups(TaskSpec spec, JsonNode taskNode) {
        List<LookupElementBuilder> singleInputResourcesLookups = new ArrayList<>();

        if (taskNode.has("resources") &&
            taskNode.get("resources").has("inputs")) {
            List<String> existingInputs = getInputsInNode(taskNode.get("resources").get("inputs"));
            spec.getResources().getInputs().stream().filter(input -> !existingInputs.contains(input.getName())).forEach(input -> {
                singleInputResourcesLookups.add(0, LookupElementBuilder.create(input)
                        .withPresentableText(input.getName())
                        .withLookupString(input.getName())
                        .withInsertHandler(new SingleInputInTaskInsertHandler()));
            });
        }

        return singleInputResourcesLookups;
    }

    private List<LookupElementBuilder> getOutputResourcesLookups(TaskSpec spec, JsonNode taskNode) {
        List<LookupElementBuilder> singleOutputResourcesLookups = new ArrayList<>();

        if (taskNode.has("resources") &&
            taskNode.get("resources").has("outputs")) {
            List<String> existingOutputs = getInputsInNode(taskNode.get("resources").get("outputs"));
            spec.getResources().getOutputs().stream().filter(output -> !existingOutputs.contains(output.getName())).forEach(output -> {
                singleOutputResourcesLookups.add(0, LookupElementBuilder.create(output)
                        .withPresentableText(output.getName())
                        .withLookupString(output.getName())
                        .withInsertHandler(new SingleInputInTaskInsertHandler()));
            });
        }

        return singleOutputResourcesLookups;
    }

    private List<LookupElementBuilder> getWorkspacesLookups(TaskSpec spec, JsonNode taskNode) {
        List<LookupElementBuilder> singleWorkspaceInputLookups = new ArrayList<>();

        if (taskNode.has("workspaces")) {
            List<String> existingWorkspaces = getInputsInNode(taskNode.get("workspaces"));

            spec.getWorkspaces().stream().filter(workspace -> !existingWorkspaces.contains(workspace.getName())).forEach(workspace -> {
                singleWorkspaceInputLookups.add(0, LookupElementBuilder.create(workspace)
                        .withPresentableText(workspace.getName())
                        .withLookupString(workspace.getName())
                        .withInsertHandler(new SingleInputInTaskInsertHandler()));
            });
        }

        return singleWorkspaceInputLookups;
    }

    private List<String> getInputsInNode(JsonNode inputsNode) {
        List<String> inputs = new ArrayList<>();
        for(JsonNode inputNode: inputsNode) {
            if (inputNode.has("name") && !inputNode.get("name").asText().isEmpty()) {
                inputs.add(inputNode.get("name").asText());
            }
        }
        return inputs;
    }

    private JsonNode getCurrentTaskRefNameInPipeline(CompletionParameters parameters, PsiElement currentTaskElement) {
        try {
            String yamlUntilTask = parameters.getEditor().getDocument().getText(new TextRange(0, currentTaskElement.getTextOffset()));
            long taskPosition = 0;
            try {
                JsonNode tasksNodeUntilSelected = YAMLHelper.getValueFromYAML(yamlUntilTask, new String[]{"spec"} );
                if (tasksNodeUntilSelected.has("tasks")) {
                    taskPosition = StreamSupport.stream(tasksNodeUntilSelected.get("tasks").spliterator(), true).count();
                }
            } catch (IOException e) {
                logger.warn("Error: " + e.getLocalizedMessage());
            }

            // get all tasks node found in the pipeline and add valid options to lookup list
            String yaml = parameters.getEditor().getDocument().getText();
            JsonNode tasksNode = YAMLHelper.getValueFromYAML(yaml, new String[]{"spec", "tasks"} );
            int cont = 1;
            if (tasksNode != null) {
                for (JsonNode item : tasksNode) {
                    if (item != null &&
                        cont == taskPosition &&
                        item.has("taskRef")) {
                        String name = item.get("taskRef").has("name") ? item.get("taskRef").get("name").asText("") : "";
                        if (!name.isEmpty()) {
                            return item;
                        }
                    }
                    cont++;
                }
            }
        } catch (IOException e) {
            logger.warn("Error: " + e.getLocalizedMessage());
        }

        return null;
    }
}
