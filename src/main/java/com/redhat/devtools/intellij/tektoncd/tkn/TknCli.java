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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.intellij.openapi.project.Project;
import com.redhat.devtools.intellij.common.utils.DownloadHelper;
import com.redhat.devtools.intellij.common.utils.ExecHelper;
import com.redhat.devtools.intellij.tektoncd.Constants;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext;
import io.fabric8.openshift.client.OpenShiftClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.redhat.devtools.intellij.tektoncd.Constants.FLAG_INPUTRESOURCEPIPELINE;
import static com.redhat.devtools.intellij.tektoncd.Constants.FLAG_INPUTRESOURCETASK;
import static com.redhat.devtools.intellij.tektoncd.Constants.FLAG_OUTPUTRESOURCE;
import static com.redhat.devtools.intellij.tektoncd.Constants.FLAG_PARAMETER;

public class TknCli implements Tkn {
    private static final ObjectMapper TASKRUN_JSON_MAPPER = new ObjectMapper(new JsonFactory());
    private static final ObjectMapper PIPERUN_JSON_MAPPER = new ObjectMapper(new JsonFactory());
    private static final ObjectMapper RESOURCE_JSON_MAPPER = new ObjectMapper(new JsonFactory());

    static {
        SimpleModule tr_module = new SimpleModule();
        tr_module.addDeserializer(List.class, new TaskRunDeserializer());
        TASKRUN_JSON_MAPPER.registerModule(tr_module);

        SimpleModule pr_module = new SimpleModule();
        pr_module.addDeserializer(List.class, new PipelineRunDeserializer());
        PIPERUN_JSON_MAPPER.registerModule(pr_module);

        SimpleModule rs_module = new SimpleModule();
        rs_module.addDeserializer(List.class, new ResourceDeserializer());
        RESOURCE_JSON_MAPPER.registerModule(rs_module);
    }
    /**
     * Home sub folder for the plugin
     */
    public static final String PLUGIN_FOLDER = ".tkn";

    private String command;
    private final Project project;

    private TknCli(Project project) throws IOException {
        command = getCommand();
        this.project = project;
    }

    private static Tkn INSTANCE;

    public static final Tkn get(Project project) throws IOException {
        if (INSTANCE == null) {
            INSTANCE = new TknCli(project);
        }
        return INSTANCE;
    }

    public String getCommand() throws IOException {
        if (command == null) {
            command = getTknCommand();
        }
        return command;
    }

    private String getTknCommand() throws IOException {
        return DownloadHelper.getInstance().downloadIfRequired("tkn", TknCli.class.getResource("/tkn.json"));
    }

    @Override
    public boolean isTektonAware(KubernetesClient client) {
        return client.rootPaths().getPaths().stream().filter(path -> path.endsWith("tekton.dev")).findFirst().isPresent();
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
        return PIPERUN_JSON_MAPPER.readValue(json, new TypeReference<List<PipelineRun>>() {});
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
        return TASKRUN_JSON_MAPPER.readValue(json, new TypeReference<List<TaskRun>>() {});
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
    public Map<String, Object> getCustomResource(KubernetesClient client, String namespace, String name, CustomResourceDefinitionContext crdContext) {
        try {
            return client.customResource(crdContext).get(namespace, name);
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

    public void startTask(String namespace, String task, Map<String, String> parameters, Map<String, String> inputResources, Map<String, String> outputResources) throws IOException {
        List<String> args = new ArrayList<>(Arrays.asList("task", "start", task, "-n", namespace));
        args.addAll(argsToList(parameters, FLAG_PARAMETER));
        args.addAll(argsToList(inputResources, FLAG_INPUTRESOURCETASK));
        args.addAll(argsToList(outputResources, FLAG_OUTPUTRESOURCE));
        ExecHelper.execute(command, args.toArray(new String[0]));
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

    public void startPipeline(String namespace, String pipeline) throws IOException {
        ExecHelper.execute(command, "pipeline", "start", pipeline, "-n", namespace);
    }
}
