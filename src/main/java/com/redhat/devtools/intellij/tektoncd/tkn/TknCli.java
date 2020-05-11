/*******************************************************************************
 * Copyright (c) 2019 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package com.redhat.devtools.intellij.tektoncd.tkn;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.intellij.openapi.project.Project;
import com.redhat.devtools.intellij.common.utils.ExecHelper;
import com.redhat.devtools.intellij.tektoncd.Constants;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext;
import io.fabric8.openshift.client.OpenShiftClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import static com.redhat.devtools.intellij.tektoncd.Constants.FLAG_INPUTRESOURCEPIPELINE;
import static com.redhat.devtools.intellij.tektoncd.Constants.FLAG_INPUTRESOURCETASK;
import static com.redhat.devtools.intellij.tektoncd.Constants.FLAG_OUTPUTRESOURCE;
import static com.redhat.devtools.intellij.tektoncd.Constants.FLAG_PARAMETER;

public class TknCli implements Tkn {
    private static final ObjectMapper RUN_JSON_MAPPER = new ObjectMapper(new JsonFactory());
    private static final ObjectMapper RESOURCE_JSON_MAPPER = new ObjectMapper(new JsonFactory());
    private static final ObjectMapper JSON_MAPPER = new ObjectMapper(new JsonFactory());
    private static final YAMLMapper YAML_MAPPER = new YAMLMapper().configure(YAMLGenerator.Feature.WRITE_DOC_START_MARKER, false);

    static {
        SimpleModule pr_module = new SimpleModule();
        pr_module.addDeserializer(List.class, new RunDeserializer());
        RUN_JSON_MAPPER.registerModule(pr_module);

        SimpleModule rs_module = new SimpleModule();
        rs_module.addDeserializer(List.class, new ResourceDeserializer());
        RESOURCE_JSON_MAPPER.registerModule(rs_module);
    }

    private String command;
    private final Project project;

    TknCli(Project project, String command) {
        this.command = command;
        this.project = project;
    }

    @Override
    public boolean isTektonAware(KubernetesClient client) {
        return client.rootPaths().getPaths().stream().filter(path -> path.endsWith("tekton.dev")).findFirst().isPresent();
    }

    @Override
    public boolean isTektonTriggersAware(KubernetesClient client) {
        return client.rootPaths().getPaths().stream().filter(path -> path.endsWith("triggers.tekton.dev")).findFirst().isPresent();
    }

    @Override
    public List<String> getClusterTasks(String namespace) throws IOException {
        String output = ExecHelper.execute(command, "clustertask", "ls", "-o", "jsonpath={.items[*].metadata.name}");
        return Arrays.stream(output.split("\\s+")).filter(item -> !item.isEmpty()).collect(Collectors.toList());
    }

    @Override
    public List<String> getNamespaces(KubernetesClient client) throws IOException {
        if (client.isAdaptable(OpenShiftClient.class)) {
            return client.adapt(OpenShiftClient.class).projects().list().getItems().stream().map(project -> project.getMetadata().getName()).collect(Collectors.toList());
        } else {
            return client.namespaces().list().getItems().stream().map(namespace -> namespace.getMetadata().getName()).collect(Collectors.toList());
        }
    }

    @Override
    public List<String> getPipelines(String namespace) throws IOException {
        String output = ExecHelper.execute(command, "pipeline", "ls", "-n", namespace, "-o", "jsonpath={.items[*].metadata.name}");
        return Arrays.stream(output.split("\\s+")).filter(item -> !item.isEmpty()).collect(Collectors.toList());
    }

    @Override
    public List<PipelineRun> getPipelineRuns(String namespace, String pipeline) throws IOException {
        String json = ExecHelper.execute(command, "pipelinerun", "ls", pipeline, "-n", namespace, "-o", "json");
        return RUN_JSON_MAPPER.readValue(json, new TypeReference<List<PipelineRun>>() {});
    }

    @Override
    public List<Resource> getResources(String namespace) throws IOException {
        String json = ExecHelper.execute(command, "resource", "ls", "-n", namespace, "-o", "json");
        return RESOURCE_JSON_MAPPER.readValue(json, new TypeReference<List<Resource>>() {});
    }

    @Override
    public List<String> getTasks(String namespace) throws IOException {
        String output = ExecHelper.execute(command, "task", "ls", "-n", namespace, "-o", "jsonpath={.items[*].metadata.name}");
        return Arrays.stream(output.split("\\s+")).filter(item -> !item.isEmpty()).collect(Collectors.toList());
    }

    @Override
    public List<TaskRun> getTaskRuns(String namespace, String task) throws IOException {
        String json = ExecHelper.execute(command, "taskrun", "ls", task, "-n", namespace, "-o", "json");
        return RUN_JSON_MAPPER.readValue(json, new TypeReference<List<TaskRun>>() {});
    }

    @Override
    public List<Condition> getConditions(String namespace) throws IOException, NullPointerException {
        String conditionListJson = ExecHelper.execute(command, "conditions", "ls", "-n", namespace, "-o", "json");
        if (!JSON_MAPPER.readTree(conditionListJson).has("items")) {
            return Collections.emptyList();
        }
        JavaType customClassCollection = JSON_MAPPER.getTypeFactory().constructCollectionType(List.class, Condition.class);
        return JSON_MAPPER.readValue(JSON_MAPPER.readTree(conditionListJson).get("items").toString(), customClassCollection);
    }

    @Override
    public List<String> getTriggerTemplates(String namespace) throws IOException {
        String output = ExecHelper.execute(command, "triggertemplates", "ls", "-n", namespace, "-o", "jsonpath={.items[*].metadata.name}");
        return Arrays.stream(output.split("\\s+")).filter(item -> !item.isEmpty()).collect(Collectors.toList());
    }

    @Override
    public List<String> getTriggerBindings(String namespace) throws IOException {
        String output = ExecHelper.execute(command, "triggerbindings", "ls", "-n", namespace, "-o", "jsonpath={.items[*].metadata.name}");
        return Arrays.stream(output.split("\\s+")).filter(item -> !item.isEmpty()).collect(Collectors.toList());
    }

    @Override
    public List<String> getClusterTriggerBindings(String namespace) throws IOException {
        String output = ExecHelper.execute(command, "ctb", "ls", "-n", namespace, "-o", "jsonpath={.items[*].metadata.name}");
        return Arrays.stream(output.split("\\s+")).filter(item -> !item.isEmpty()).collect(Collectors.toList());
    }

    @Override
    public List<String> getEventListeners(String namespace) throws IOException {
        String output = ExecHelper.execute(command, "eventlisteners", "ls", "-n", namespace, "-o", "jsonpath={.items[*].metadata.name}");
        return Arrays.stream(output.split("\\s+")).filter(item -> !item.isEmpty()).collect(Collectors.toList());
    }

    @Override
    public String getPipelineYAML(String namespace, String pipeline) throws IOException {
        return ExecHelper.execute(command, "pipeline", "describe", pipeline, "-n", namespace, "-o", "yaml");
    }

    @Override
    public String getResourceYAML(String namespace, String resource) throws IOException {
        return ExecHelper.execute(command, "resource", "describe", resource, "-n", namespace, "-o", "yaml");
    }

    @Override
    public String getTaskYAML(String namespace, String task) throws IOException {
        return ExecHelper.execute(command, "task", "describe", task, "-n", namespace, "-o", "yaml");
    }

    @Override
    public String getConditionYAML(String namespace, String condition) throws IOException {
        return ExecHelper.execute(command, "condition", "describe", condition, "-n", namespace, "-o", "yaml");
    }

    @Override
    public String getTriggerTemplateYAML(String namespace, String triggerTemplate) throws IOException {
        return ExecHelper.execute(command, "triggertemplate", "describe", triggerTemplate, "-n", namespace, "-o", "yaml");
    }

    @Override
    public String getTriggerBindingYAML(String namespace, String triggerBinding) throws IOException {
        return ExecHelper.execute(command, "triggerbinding", "describe", triggerBinding, "-n", namespace, "-o", "yaml");
    }

    @Override
    public String getClusterTriggerBindingYAML(String namespace, String ctb) throws IOException {
        return ExecHelper.execute(command, "ctb", "describe", ctb, "-n", namespace, "-o", "yaml");
    }

    @Override
    public String getEventListenerYAML(String namespace, String eventListener) throws IOException {
        return ExecHelper.execute(command, "eventlistener", "describe", eventListener, "-n", namespace, "-o", "yaml");
    }

    @Override
    public void deletePipeline(String namespace, String pipeline) throws IOException {
        ExecHelper.execute(command, "pipeline", "delete", "-f", pipeline, "-n", namespace);
    }

    @Override
    public void deleteTask(String namespace, String task) throws IOException {
        ExecHelper.execute(command, "task", "delete", "-f", task, "-n", namespace);
    }

    @Override
    public void deleteResource(String namespace, String resource) throws IOException {
        ExecHelper.execute(command, "resource", "delete", "-f", resource, "-n", namespace);
    }

    @Override
    public void deleteCondition(String namespace, String condition) throws IOException {
        ExecHelper.execute(command, "conditions", "delete", "-f", condition, "-n", namespace);
    }

    @Override
    public void deleteTriggerTemplate(String namespace, String triggerTemplate) throws IOException {
        ExecHelper.execute(command, "triggertemplate", "delete", "-f", triggerTemplate, "-n", namespace);
    }

    @Override
    public void deleteTriggerBinding(String namespace, String triggerBinding) throws IOException {
        ExecHelper.execute(command, "triggerbinding", "delete", "-f", triggerBinding, "-n", namespace);
    }

    @Override
    public void deleteClusterTriggerBinding(String namespace, String ctb) throws IOException {
        ExecHelper.execute(command, "ctb", "delete", "-f", ctb, "-n", namespace);
    }

    @Override
    public void deleteEventListener(String namespace, String eventListener) throws IOException {
        ExecHelper.execute(command, "eventlistener", "delete", "-f", eventListener, "-n", namespace);
    }

    @Override
    public Map<String, Object> getCustomResource(KubernetesClient client, String namespace, String name, CustomResourceDefinitionContext crdContext) {
        try {
            return new TreeMap<>(client.customResource(crdContext).get(namespace, name));
        } catch(KubernetesClientException e) {
            // call failed bc resource doesn't exist - 404
            return null;
        }
    }

    @Override
    public void editCustomResource(KubernetesClient client, String namespace, String name, CustomResourceDefinitionContext crdContext, String objectAsString) throws IOException {
        client.customResource(crdContext).edit(namespace, name, objectAsString);
    }

    @Override
    public void createCustomResource(KubernetesClient client, String namespace, CustomResourceDefinitionContext crdContext, String objectAsString) throws IOException {
        client.customResource(crdContext).create(namespace, objectAsString);
    }

    @Override
    public void startPipeline(String namespace, String pipeline, Map<String, String> parameters, Map<String, String> resources) throws IOException {
        List<String> args = new ArrayList<>(Arrays.asList("pipeline", "start", pipeline, "-n", namespace));
        args.addAll(argsToList(parameters, FLAG_PARAMETER));
        args.addAll(argsToList(resources, FLAG_INPUTRESOURCEPIPELINE));
        ExecHelper.execute(command, args.toArray(new String[0]));
    }

    @Override
    public void startLastPipeline(String namespace, String pipeline) throws IOException {
        ExecHelper.execute(command, "pipeline", "start", pipeline, "--last", "-n", namespace);
    }

    public void startTask(String namespace, String task, Map<String, String> parameters, Map<String, String> inputResources, Map<String, String> outputResources) throws IOException {
        List<String> args = new ArrayList<>(Arrays.asList("task", "start", task, "-n", namespace));
        args.addAll(argsToList(parameters, FLAG_PARAMETER));
        args.addAll(argsToList(inputResources, FLAG_INPUTRESOURCETASK));
        args.addAll(argsToList(outputResources, FLAG_OUTPUTRESOURCE));
        ExecHelper.execute(command, args.toArray(new String[0]));
    }

    @Override
    public void startLastTask(String namespace, String task) throws IOException {
        ExecHelper.execute(command, "task", "start", task, "--last", "-n", namespace);
    }

    private List<String> argsToList(Map<String, String> argMap, String flag) {
        List<String> args = new ArrayList<>();
        if (argMap != null) {
            argMap.entrySet().stream().forEach(param -> {
                args.add(flag);
                args.add(param.getKey() + "=" + param.getValue());
            });
        }
        return args;
    }

    @Override
    public void showLogsPipelineRun(String namespace, String pipelineRun) throws IOException {
        ExecHelper.executeWithTerminal(project, Constants.TERMINAL_TITLE,false, command, "pipelinerun", "logs", pipelineRun, "-n", namespace);
    }

    @Override
    public void showLogsTaskRun(String namespace, String taskRun) throws IOException {
        ExecHelper.executeWithTerminal(project, Constants.TERMINAL_TITLE, false, command, "taskrun", "logs", taskRun, "-n", namespace);
    }

    @Override
    public void followLogsPipelineRun(String namespace, String pipelineRun) throws IOException {
        ExecHelper.executeWithTerminal(project, Constants.TERMINAL_TITLE,false, command, "pipelinerun", "logs", pipelineRun, "-f", "-n", namespace);
    }

    @Override
    public void followLogsTaskRun(String namespace, String taskRun) throws IOException {
        ExecHelper.executeWithTerminal(project, Constants.TERMINAL_TITLE, false, command, "taskrun", "logs", taskRun, "-f", "-n", namespace);
    }
}
