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
package com.redhat.devtools.intellij.tektoncd.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.intellij.openapi.util.Pair;
import com.redhat.devtools.intellij.common.utils.YAMLHelper;
import com.redhat.devtools.intellij.tektoncd.tkn.component.field.Input;
import com.redhat.devtools.intellij.tektoncd.tkn.component.field.Output;
import com.redhat.devtools.intellij.tektoncd.tkn.component.field.Workspace;
import com.redhat.devtools.intellij.tektoncd.utils.model.ConfigurationModel;
import com.redhat.devtools.intellij.tektoncd.utils.model.actions.ActionToRunModel;
import com.redhat.devtools.intellij.tektoncd.utils.model.resources.TaskConfigurationModel;
import io.fabric8.kubernetes.api.model.HasMetadata;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.fasterxml.jackson.dataformat.yaml.YAMLGenerator.Feature.WRITE_DOC_START_MARKER;
import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_PIPELINE;
import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_PIPELINERUN;
import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_PVC;
import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_VCT;

public class YAMLBuilder {
    private static final ObjectMapper YAML_MAPPER = new ObjectMapper(new com.fasterxml.jackson.dataformat.yaml.YAMLFactory());

    public static String createPreview(ActionToRunModel model) throws IOException {
        ObjectNode rootNode = YAML_MAPPER.createObjectNode();

        if (!model.getServiceAccount().isEmpty()) {
            rootNode.put("serviceAccountName", model.getServiceAccount());
        }

        ArrayNode tsaNode = createTaskServiceAccountNode(model.getTaskServiceAccounts());
        if (tsaNode.size() > 0) {
            rootNode.set("serviceAccountNames", tsaNode);
        }

        ArrayNode workspacesNode = createWorkspaceNode(model.getWorkspaces());
        if (workspacesNode.size() > 0) {
            rootNode.set("workspaces", workspacesNode);
        }

        ArrayNode paramsNode = createParamsNodeFromInput(model.getParams());
        if (paramsNode.size() > 0) {
            rootNode.set("params", paramsNode);
        }

        return new YAMLMapper().writeValueAsString(rootNode);
    }

    private static ArrayNode createWorkspaceNode(Map<String, Workspace> workspaces) {
        ArrayNode workspacesNode = YAML_MAPPER.createArrayNode();
        for (Workspace workspace: workspaces.values()) {
            if (workspace.getKind() != null) {
                ObjectNode workspaceNode = createWorkspaceResourceNode(workspace);
                // TODO need to add subpath and items
                workspacesNode.add(workspaceNode);
            }
        }
        return workspacesNode;
    }

    private static ObjectNode createWorkspaceResourceNode(Workspace workspace) {
        ObjectNode workspaceNode = YAML_MAPPER.createObjectNode();
        workspaceNode.put("name", workspace.getName());

        ObjectNode workspaceResourceNode = YAML_MAPPER.createObjectNode();
        switch(workspace.getKind()) {
            case CONFIGMAP: {
                workspaceResourceNode.put("name", workspace.getResource());
                workspaceNode.set(workspace.getKind().toString(), workspaceResourceNode);
                break;
            }
            case SECRET: {
                workspaceResourceNode.put("secretName", workspace.getResource());
                workspaceNode.set(workspace.getKind().toString(), workspaceResourceNode);
                break;
            }
            case PVC: {
                Pair<String, ObjectNode> workspaceNodeValues = createPVCNode(workspace);
                workspaceNode.set(workspaceNodeValues.getFirst(), workspaceNodeValues.getSecond());
                break;
            }
            case EMPTYDIR: {
                workspaceNode.putObject(workspace.getKind().toString());
                break;
            }
            default:
                workspaceNode.put("Error", "An error occurred while building the preview");
                break;
        }
        return workspaceNode;
    }

    private static Pair<String, ObjectNode> createPVCNode(Workspace workspace) {
        Map<String, String> items = workspace.getItems();
        if (items.size() > 0) {
            if (KIND_VCT.equals(items.get("type"))) {
                ObjectNode vctNode = createVCT(items.get("name"), items.get("accessMode"), items.get("size"), items.get("unit"));
                return new Pair<>(KIND_VCT, vctNode);
            } else {
                return createPVCNodeInternal(items.get("name"));
            }
        }
        return createPVCNodeInternal(workspace.getResource());

    }

    private static Pair<String, ObjectNode> createPVCNodeInternal(String resource) {
        ObjectNode workspaceResourceNode = YAML_MAPPER.createObjectNode();
        workspaceResourceNode.put("claimName", resource);
        return new Pair<>(KIND_PVC, workspaceResourceNode);
    }

    private static ArrayNode createTaskServiceAccountNode(Map<String, String> tsa) {
        ArrayNode serviceAccountNamesNode = YAML_MAPPER.createArrayNode();
        for (String task: tsa.keySet()) {
            if (!tsa.get(task).isEmpty()) {
                ObjectNode tsaNode = YAML_MAPPER.createObjectNode();
                tsaNode.put("taskName", task);
                tsaNode.put("serviceAccountName", tsa.get(task));
                serviceAccountNamesNode.add(tsaNode);
            }
        }
        return serviceAccountNamesNode;
    }

    private static ArrayNode createParamsNodeFromInput(List<Input> params) {
        ArrayNode paramsNode = YAML_MAPPER.createArrayNode();
        ObjectNode inputNode;
        for (Input param: params) {
            inputNode = YAML_MAPPER.createObjectNode();
            inputNode.put("name", param.name());
            String value = param.value() == null ? param.defaultValue().orElse("") : param.value();
            if (param.type().equals("array")) {
                ArrayNode paramValuesNode = YAML_MAPPER.valueToTree(value.split(","));
                inputNode.set("value", paramValuesNode);
            } else {
                inputNode.put("value", value);
            }
            paramsNode.add(inputNode);
        }

        return paramsNode;
    }

    private static ArrayNode createParamsNodeFromNames(List<String> params) {
        ArrayNode paramsNode = YAML_MAPPER.createArrayNode();
        ObjectNode inputNode;
        for (String param: params) {
            inputNode = YAML_MAPPER.createObjectNode();
            inputNode.put("name", param);
            paramsNode.add(inputNode);
        }
        return paramsNode;
    }

    private static ArrayNode createInputResourcesNode(List<Input> inputResources) {
        ArrayNode resourcesNode = YAML_MAPPER.createArrayNode();
        ObjectNode inputNode;
        for (Input input: inputResources) {
            inputNode = YAML_MAPPER.createObjectNode();
            inputNode.put("name", input.name());
            // resourceRef node
            ObjectNode resourceRefNode = YAML_MAPPER.createObjectNode();
            resourceRefNode.put("name", input.value() == null ? "Resource has not yet been inserted" : input.value());
            inputNode.set("resourceRef", resourceRefNode);
            resourcesNode.add(inputNode);
        }

        return resourcesNode;
    }

    private static ArrayNode createOutputResourcesNode(List<Output> outputs) {
        ArrayNode resourcesNode = YAML_MAPPER.createArrayNode();
        ObjectNode outputNode;
        for (Output output: outputs) {
            outputNode = YAML_MAPPER.createObjectNode();
            outputNode.put("name", output.name());

            // resourceRef node
            ObjectNode resourceRefNode = YAML_MAPPER.createObjectNode();
            resourceRefNode.put("name", output.value() == null ? "Resource has not yet been inserted" : output.value());
            outputNode.set("resourceRef", resourceRefNode);
            resourcesNode.add(outputNode);
        }

        return resourcesNode;
    }

    private static ArrayNode createRunsNode(List<ObjectNode> runs) {
        ArrayNode runsNode = YAML_MAPPER.createArrayNode();
        runs.forEach(run -> runsNode.add(run));

        return runsNode;
    }

    private static ArrayNode createTriggersNode(String name, List<String> triggerBindings, String triggerTemplate) {
        ArrayNode triggersNode = YAML_MAPPER.createArrayNode();
        ObjectNode triggerNode = YAML_MAPPER.createObjectNode();

        ArrayNode bindingsNode = YAML_MAPPER.createArrayNode();
        triggerBindings.forEach(binding -> {
            ObjectNode bindingNode = YAML_MAPPER.createObjectNode();
            bindingNode.put("ref", binding);
            bindingsNode.add(bindingNode);
        });

        ObjectNode templateNode = YAML_MAPPER.createObjectNode();
        templateNode.put("ref", triggerTemplate);

        triggerNode.put("name", name);
        if (bindingsNode.size() > 0) {
            triggerNode.set("bindings", bindingsNode);
        }
        triggerNode.set("template", templateNode);

        triggersNode.add(triggerNode);

        return triggersNode;
    }

    public static ObjectNode createRun(ActionToRunModel model) {
        return createRun(model, false);
    }

    public static ObjectNode createRun(ActionToRunModel model, boolean toDebug) {
        if (model.getKind().equalsIgnoreCase(KIND_PIPELINE)) {
            return createPipelineRun(model);
        } else {
            return createTaskRun(model, toDebug);
        }
    }

    public static ObjectNode createPipelineRun(ActionToRunModel model) {
        return createRunInternal("PipelineRun", model, false);
    }

    public static ObjectNode createTaskRun(ConfigurationModel model, boolean toDebug) {
        return createRunInternal("TaskRun", model, toDebug);
    }

    public static ObjectNode createTaskRun(ConfigurationModel model) {
        return createTaskRun(model, false);
    }

    public static ObjectNode createRunInternal(String kind, ConfigurationModel model, boolean toDebug) {
        ObjectNode rootNode = YAML_MAPPER.createObjectNode();

        rootNode.put("apiVersion", "tekton.dev/v1beta1");
        rootNode.put("kind", kind);

        ObjectNode metadataNode = YAML_MAPPER.createObjectNode();
        metadataNode.put("generateName", model.getName() + "-");

        rootNode.set("metadata", metadataNode);

        ObjectNode spec = YAML_MAPPER.createObjectNode();
        if (model instanceof ActionToRunModel) {
            ActionToRunModel actionModel = (ActionToRunModel)model;
            if (kind.equalsIgnoreCase(KIND_PIPELINERUN)) {
                spec = createPipelineRunSpec(actionModel);
            } else {
                spec = createTaskRunSpec(actionModel.getResource().getName(),
                        actionModel.getParams(),
                        actionModel.getWorkspaces(),
                        actionModel.getServiceAccount().isEmpty() ? null : actionModel.getServiceAccount(),
                        toDebug
                );
            }
        } else if (model instanceof TaskConfigurationModel) {
            Map<String, Workspace> workspaces = new HashMap<>();
            ((TaskConfigurationModel)model).getWorkspaces().stream().forEach(workspace -> {
                workspace.setKind(Workspace.Kind.EMPTYDIR);
                workspaces.put(workspace.getName(), workspace);
            });
            spec = createTaskRunSpec(model.getName(),
                    ((TaskConfigurationModel)model).getParams(),
                    workspaces,
                    "",
                    toDebug);
        }

        rootNode.set("spec", spec);

        return rootNode;
    }

    private static ObjectNode createPipelineRunSpec(ActionToRunModel model) {
        ObjectNode specNode = YAML_MAPPER.createObjectNode();

        ObjectNode pipelineRefNode = YAML_MAPPER.createObjectNode();
        pipelineRefNode.put("name", model.getResource().getName());

        specNode.set("pipelineRef", pipelineRefNode);

        if (!model.getServiceAccount().isEmpty()) {
            specNode.put("serviceAccountName", model.getServiceAccount());
        }

        ArrayNode tsaNode = createTaskServiceAccountNode(model.getTaskServiceAccounts());
        if (tsaNode.size() > 0) {
            specNode.set("serviceAccountNames", tsaNode);
        }

        ArrayNode paramsNode = createParamsNodeFromInput(model.getParams());
        if (paramsNode.size() > 0) {
            specNode.set("params", paramsNode);
        }

        ArrayNode workspacesNode = createWorkspaceNode(model.getWorkspaces());
        if (workspacesNode.size() > 0) {
            specNode.set("workspaces", workspacesNode);
        }

        return specNode;
    }

    private static ObjectNode createTaskRunSpec(String name, List<Input> params, Map<String, Workspace> workspaces, String serviceAccount, boolean toDebug) {
        ObjectNode specNode = YAML_MAPPER.createObjectNode();

        if (toDebug) {
            ObjectNode breakpoint = YAMLBuilder.createBreakpointSection();
            specNode.set("debug", breakpoint);
        }

        ObjectNode pipelineRefNode = YAML_MAPPER.createObjectNode();
        pipelineRefNode.put("name", name);

        specNode.set("taskRef", pipelineRefNode);

        if (serviceAccount != null) {
            specNode.put("serviceAccountName", serviceAccount);
        }

        ArrayNode paramsNode = createParamsNodeFromInput(params);
        if (paramsNode.size() > 0) {
            specNode.set("params", paramsNode);
        }

        ArrayNode workspacesNode = createWorkspaceNode(workspaces);
        if (workspacesNode.size() > 0) {
            specNode.set("workspaces", workspacesNode);
        }

        return specNode;
    }

    public static String addBreakPointToResource(String resourceAsYAML) throws IOException {
        ObjectNode run = YAMLBuilder.convertToObjectNode(resourceAsYAML);
        ObjectNode breakpoint = YAMLBuilder.createBreakpointSection();
        ((ObjectNode)run.get("spec")).set("debug", breakpoint);
        return YAMLHelper.JSONToYAML(run, false);
    }

    public static ObjectNode createTriggerTemplate(String name, String apiVersion, List<String> params, List<ObjectNode> runs) {
        ObjectNode rootNode = YAML_MAPPER.createObjectNode();

        rootNode.put("apiVersion", apiVersion);
        rootNode.put("kind", "TriggerTemplate");

        ObjectNode metadataNode = YAML_MAPPER.createObjectNode();
        metadataNode.put("name", name);

        rootNode.set("metadata", metadataNode);

        ObjectNode specNode = YAML_MAPPER.createObjectNode();

        ArrayNode paramsNode = createParamsNodeFromNames(params);
        if (paramsNode.size() > 0) {
            specNode.set("params", paramsNode);
        }

        ArrayNode runsNode = createRunsNode(runs);
        specNode.set("resourcetemplates", runsNode);

        rootNode.set("spec", specNode);

        return rootNode;
    }

    public static ObjectNode createEventListener(String name, String apiVersion, String serviceAccount, List<String> triggerBindings, String triggerTemplate) {
        ObjectNode rootNode = YAML_MAPPER.createObjectNode();

        rootNode.put("apiVersion", apiVersion);
        rootNode.put("kind", "EventListener");

        ObjectNode metadataNode = YAML_MAPPER.createObjectNode();
        metadataNode.put("name", name);

        rootNode.set("metadata", metadataNode);

        ObjectNode specNode = YAML_MAPPER.createObjectNode();

        specNode.put("serviceAccountName", serviceAccount);

        ArrayNode triggersNode = createTriggersNode("trig", triggerBindings, triggerTemplate);
        specNode.set("triggers", triggersNode);

        rootNode.set("spec", specNode);

        return rootNode;
    }

    public static ObjectNode createTask(String name, String kind, ObjectNode taskSpec) {
        JsonNode metadataNode = taskSpec.remove("metadata");
        if (metadataNode == null) {
            metadataNode = YAML_MAPPER.createObjectNode();
        }
        if (!metadataNode.has("name")) {
            ((ObjectNode) metadataNode).put("name", name);
        }

        ObjectNode task = YAML_MAPPER.createObjectNode();
        task.put("apiVersion", "tekton.dev/v1beta1");
        task.put("kind", kind);
        task.set("metadata", metadataNode);
        task.set("spec", taskSpec);
        return task;
    }

    public static ObjectNode createVCT(String name, String accessMode, String size, String unit) {
        ObjectNode metadataNode = YAML_MAPPER.createObjectNode();
        metadataNode.put("name", name);

        ObjectNode spec = YAML_MAPPER.createObjectNode();

        ObjectNode requests = YAML_MAPPER.createObjectNode();
        ObjectNode resources = YAML_MAPPER.createObjectNode();
        requests.put("storage", size + unit);
        resources.set("requests", requests);

        ArrayNode accessModes = YAML_MAPPER.createArrayNode();
        accessModes.add(accessMode);

        spec.set("resources", resources);
        spec.put("volumeMode", "Filesystem");
        spec.set("accessModes", accessModes);

        ObjectNode vct = YAML_MAPPER.createObjectNode();
        vct.set("metadata", metadataNode);
        vct.set("spec", spec);
        return vct;
    }

    public static ObjectNode createTaskRef(String name, String kind) {
        ObjectNode taskRef = YAML_MAPPER.createObjectNode();
        ObjectNode metadataTaskRef = YAML_MAPPER.createObjectNode();
        metadataTaskRef.put("name", name);
        metadataTaskRef.put("kind", kind);
        taskRef.set("taskRef", metadataTaskRef);
        return taskRef;
    }

    public static ObjectNode createBreakpointSection() {
        ArrayNode breakpoints = YAML_MAPPER.createArrayNode();
        breakpoints.add("onFailure");
        ObjectNode breakpoint = YAML_MAPPER.createObjectNode();
        breakpoint.set("breakpoint", breakpoints);
        return breakpoint;
    }

    public static String writeValueAsString(Map<String, Object> value) throws IOException {
        try {
            return new YAMLMapper().writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IOException(e);
        }
    }

    public static String writeValueAsString(HasMetadata value) throws IOException {
        try {
            return new YAMLMapper().configure(WRITE_DOC_START_MARKER, false).writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IOException(e);
        }
    }

    public static ObjectNode convertToObjectNode(String yaml) throws IOException {
        try {
            return (ObjectNode) YAML_MAPPER.readTree(yaml);
        } catch (JsonProcessingException e) {
            throw new IOException(e);
        }
    }
}
