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
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.redhat.devtools.intellij.common.utils.ExecHelper;
import com.redhat.devtools.intellij.common.utils.NetworkUtils;
import com.redhat.devtools.intellij.tektoncd.Constants;
import com.redhat.devtools.intellij.tektoncd.tkn.component.field.Workspace;
import com.redhat.devtools.intellij.tektoncd.utils.VirtualFileHelper;
import com.twelvemonkeys.lang.Platform;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodCondition;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.api.model.PodStatus;
import io.fabric8.kubernetes.api.model.apps.StatefulSet;
import io.fabric8.kubernetes.api.model.apps.StatefulSetCondition;
import io.fabric8.kubernetes.api.model.apps.StatefulSetList;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.Watch;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext;
import io.fabric8.tekton.client.TektonClient;
import io.fabric8.tekton.pipeline.v1alpha1.Condition;
import io.fabric8.tekton.pipeline.v1beta1.ClusterTask;
import io.fabric8.tekton.pipeline.v1beta1.ClusterTaskList;
import io.fabric8.tekton.pipeline.v1beta1.Pipeline;
import io.fabric8.tekton.pipeline.v1beta1.PipelineList;
import io.fabric8.tekton.pipeline.v1beta1.Task;
import io.fabric8.tekton.pipeline.v1beta1.TaskList;
import io.fabric8.tekton.resource.v1alpha1.PipelineResource;
import io.fabric8.tekton.triggers.v1alpha1.ClusterTriggerBinding;
import io.fabric8.tekton.triggers.v1alpha1.EventListener;
import io.fabric8.tekton.triggers.v1alpha1.TriggerBinding;
import io.fabric8.tekton.triggers.v1alpha1.TriggerTemplate;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.apache.commons.io.FileUtils;


import static com.redhat.devtools.intellij.tektoncd.Constants.FLAG_INPUTRESOURCEPIPELINE;
import static com.redhat.devtools.intellij.tektoncd.Constants.FLAG_INPUTRESOURCETASK;
import static com.redhat.devtools.intellij.tektoncd.Constants.FLAG_OUTPUTRESOURCE;
import static com.redhat.devtools.intellij.tektoncd.Constants.FLAG_PARAMETER;
import static com.redhat.devtools.intellij.tektoncd.Constants.FLAG_PREFIXNAME;
import static com.redhat.devtools.intellij.tektoncd.Constants.FLAG_SERVICEACCOUNT;
import static com.redhat.devtools.intellij.tektoncd.Constants.FLAG_TASKSERVICEACCOUNT;
import static com.redhat.devtools.intellij.tektoncd.Constants.FLAG_WORKSPACE;
import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_PIPELINERUN;
import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_TASKRUN;

public class TknCli implements Tkn {
    private static final ObjectMapper JSON_MAPPER = new ObjectMapper(new JsonFactory());

    private String command;
    private final Project project;
    private final KubernetesClient client;

    private Map<String, String> envVars;


    TknCli(Project project, String command) {
        this.command = command;
        this.project = project;
        this.client = new DefaultKubernetesClient(new ConfigBuilder().build());
        try {
            this.envVars = NetworkUtils.buildEnvironmentVariables(client.getMasterUrl().toString());
        } catch (URISyntaxException e) {
            this.envVars = Collections.emptyMap();
        }
    }

    @Override
    public boolean isTektonAware() throws IOException {
        try {
            return client.rootPaths().getPaths().stream().filter(path -> path.endsWith("tekton.dev")).findFirst().isPresent();
        } catch (KubernetesClientException e) {
            throw new IOException(e);
        }
    }

    @Override
    public boolean isTektonTriggersAware() {
        try {
            return client.rootPaths().getPaths().stream().filter(path -> path.endsWith("triggers.tekton.dev")).findFirst().isPresent();
        } catch (KubernetesClientException e) {
            return false;
        }
    }

    @Override
    public String getNamespace() throws IOException {
        String namespace = client.getNamespace();
        if (Strings.isNullOrEmpty(namespace)) {
            namespace = "default";
        }
        return namespace;
    }

    @Override
    public List<String> getServiceAccounts(String namespace) {
        return client.serviceAccounts().inNamespace(namespace).list().getItems().stream().map(serviceAccount -> serviceAccount.getMetadata().getName()).collect(Collectors.toList());
    }

    @Override
    public List<String> getSecrets(String namespace) {
        return client.secrets().inNamespace(namespace).list().getItems().stream().map(secret -> secret.getMetadata().getName()).collect(Collectors.toList());
    }

    @Override
    public List<String> getConfigMaps(String namespace) {
        return client.configMaps().inNamespace(namespace).list().getItems().stream().map(configMap -> configMap.getMetadata().getName()).collect(Collectors.toList());
    }

    @Override
    public List<String> getPersistentVolumeClaim(String namespace) {
        return client.persistentVolumeClaims().inNamespace(namespace).list().getItems().stream().map(volume -> volume.getMetadata().getName()).collect(Collectors.toList());
    }

    @Override
    public List<String> getPipelines(String namespace) throws IOException {
        try {
            PipelineList pipelines = client.adapt(TektonClient.class).v1beta1().pipelines().inNamespace(namespace).list();
            return pipelines.getItems().stream().map(pipeline -> pipeline.getMetadata().getName()).collect(Collectors.toList());
        } catch (KubernetesClientException e) {
            throw new IOException(e);
        }
    }

    @Override
    public List<PipelineRun> getPipelineRuns(String namespace, String pipeline) throws IOException {
        String json = ExecHelper.execute(command, envVars, "pipelinerun", "ls", pipeline, "-n", namespace, "-o", "json");
        return getCustomCollection(json, PipelineRun.class);
    }

    @Override
    public List<Resource> getResources(String namespace) throws IOException {
        String json = ExecHelper.execute(command, envVars, "resource", "ls", "-n", namespace, "-o", "json");
        return getCustomCollection(json, Resource.class);
    }

    @Override
    public List<Task> getTasks(String namespace) throws IOException {
        try {
            TaskList tasks = client.adapt(TektonClient.class).v1beta1().tasks().inNamespace(namespace).list();
            return tasks.getItems();
        } catch (KubernetesClientException e) {
            throw new IOException(e);
        }
    }

    @Override
    public List<ClusterTask> getClusterTasks() throws IOException {
        try {
            ClusterTaskList tasks = client.adapt(TektonClient.class).v1beta1().clusterTasks().list();
            return tasks.getItems();
        } catch (KubernetesClientException e) {
            throw new IOException(e);
        }
    }

    @Override
    public List<TaskRun> getTaskRuns(String namespace, String task) throws IOException {
        String json = ExecHelper.execute(command, envVars, "taskrun", "ls", task, "-n", namespace, "-o", "json");
        return getCustomCollection(json, TaskRun.class);
    }

    @Override
    public List<Condition> getConditions(String namespace) throws IOException, NullPointerException {
        String conditionListJson = ExecHelper.execute(command, envVars, "conditions", "ls", "-n", namespace, "-o", "json");
        return getCustomCollection(conditionListJson, Condition.class);
    }

    private <T> List<T> getCustomCollection(String json, Class<T> customClass) throws IOException {
        if (!JSON_MAPPER.readTree(json).has("items")) return Collections.emptyList();
        if (JSON_MAPPER.readTree(json).get("items").isNull()) return Collections.emptyList();

        JavaType customClassCollection = JSON_MAPPER.getTypeFactory().constructCollectionType(List.class, customClass);
        return JSON_MAPPER.readValue(JSON_MAPPER.readTree(json).get("items").toString(), customClassCollection);
    }

    @Override
    public List<String> getTriggerTemplates(String namespace) throws IOException {
        String output = ExecHelper.execute(command, envVars, "triggertemplates", "ls", "-n", namespace, "-o", "jsonpath={.items[*].metadata.name}");
        return Arrays.stream(output.split("\\s+")).filter(item -> !item.isEmpty()).collect(Collectors.toList());
    }

    @Override
    public List<String> getTriggerBindings(String namespace) throws IOException {

        String output = ExecHelper.execute(command, envVars, "triggerbindings", "ls", "-n", namespace, "-o", "jsonpath={.items[*].metadata.name}");
        return Arrays.stream(output.split("\\s+")).filter(item -> !item.isEmpty()).collect(Collectors.toList());
    }

    @Override
    public List<String> getClusterTriggerBindings() throws IOException {
        String output = ExecHelper.execute(command, envVars, "ctb", "ls", "-o", "jsonpath={.items[*].metadata.name}");
        return Arrays.stream(output.split("\\s+")).filter(item -> !item.isEmpty()).collect(Collectors.toList());
    }

    @Override
    public List<String> getEventListeners(String namespace) throws IOException {
        String output = ExecHelper.execute(command, envVars, "eventlisteners", "ls", "-n", namespace, "-o", "jsonpath={.items[*].metadata.name}");
        return Arrays.stream(output.split("\\s+")).filter(item -> !item.isEmpty()).collect(Collectors.toList());
    }

    @Override
    public String getPipelineYAML(String namespace, String pipeline) throws IOException {
        return ExecHelper.execute(command, envVars, "pipeline", "describe", pipeline, "-n", namespace, "-o", "yaml");
    }

    @Override
    public String getResourceYAML(String namespace, String resource) throws IOException {
        return ExecHelper.execute(command, envVars, "resource", "describe", resource, "-n", namespace, "-o", "yaml");
    }

    @Override
    public String getTaskYAML(String namespace, String task) throws IOException {
        return ExecHelper.execute(command, envVars, "task", "describe", task, "-n", namespace, "-o", "yaml");
    }

    @Override
    public String getClusterTaskYAML(String task) throws IOException {
        return ExecHelper.execute(command, envVars, "clustertask", "describe", task, "-o", "yaml");
    }

    @Override
    public String getConditionYAML(String namespace, String condition) throws IOException {
        return ExecHelper.execute(command, envVars, "condition", "describe", condition, "-n", namespace, "-o", "yaml");
    }

    @Override
    public String getTriggerTemplateYAML(String namespace, String triggerTemplate) throws IOException {
        return ExecHelper.execute(command, envVars, "triggertemplate", "describe", triggerTemplate, "-n", namespace, "-o", "yaml");
    }

    @Override
    public String getTriggerBindingYAML(String namespace, String triggerBinding) throws IOException {
        return ExecHelper.execute(command, envVars, "triggerbinding", "describe", triggerBinding, "-n", namespace, "-o", "yaml");
    }

    @Override
    public String getClusterTriggerBindingYAML(String ctb) throws IOException {
        return ExecHelper.execute(command, envVars, "ctb", "describe", ctb, "-o", "yaml");
    }

    @Override
    public String getEventListenerYAML(String namespace, String eventListener) throws IOException {
        return ExecHelper.execute(command, envVars, "eventlistener", "describe", eventListener, "-n", namespace, "-o", "yaml");
    }

    @Override
    public void deletePipelines(String namespace, List<String> pipelines, boolean deleteRelatedResources) throws IOException {
        if (deleteRelatedResources) {
            ExecHelper.execute(command, envVars, getDeleteArgs(namespace, "pipeline", pipelines, "--prs=true"));
        } else {
            ExecHelper.execute(command, envVars, getDeleteArgs(namespace, "pipeline", pipelines));
        }
    }

    @Override
    public void deletePipelineRuns(String namespace, List<String> prs) throws IOException {
        ExecHelper.execute(command, envVars, getDeleteArgs(namespace, "pr", prs));
    }

    @Override
    public void deleteTasks(String namespace, List<String> tasks, boolean deleteRelatedResources) throws IOException {
        if (deleteRelatedResources) {
            ExecHelper.execute(command, envVars, getDeleteArgs(namespace, "task", tasks, "--trs=true"));
        } else {
            ExecHelper.execute(command, envVars, getDeleteArgs(namespace, "task", tasks));
        }
    }

    @Override
    public void deleteClusterTasks(List<String> tasks, boolean deleteRelatedResources) throws IOException {
        if (deleteRelatedResources) {
            ExecHelper.execute(command, envVars, getDeleteArgs("", "clustertask", tasks, "--trs=true"));
        } else {
            ExecHelper.execute(command, envVars, getDeleteArgs("", "clustertask", tasks));
        }
    }

    @Override
    public void deleteTaskRuns(String namespace, List<String> trs) throws IOException {
        ExecHelper.execute(command, envVars, getDeleteArgs(namespace, "tr", trs));
    }

    @Override
    public void deleteResources(String namespace, List<String> resources) throws IOException {
        ExecHelper.execute(command, envVars, getDeleteArgs(namespace, "resource", resources));
    }

    @Override
    public void deleteConditions(String namespace, List<String> conditions) throws IOException {
        ExecHelper.execute(command, envVars, getDeleteArgs(namespace, "conditions", conditions));
    }

    @Override
    public void deleteTriggerTemplates(String namespace, List<String> triggerTemplates) throws IOException {
        ExecHelper.execute(command, envVars, getDeleteArgs(namespace, "triggertemplate", triggerTemplates));
    }

    @Override
    public void deleteTriggerBindings(String namespace, List<String> triggerBindings) throws IOException {
        ExecHelper.execute(command, envVars, getDeleteArgs(namespace, "triggerbinding", triggerBindings));
    }

    @Override
    public void deleteClusterTriggerBindings(List<String> ctbs) throws IOException {
        ExecHelper.execute(command, envVars, getDeleteArgs("", "ctb", ctbs));
    }

    @Override
    public void deleteEventListeners(String namespace, List<String> eventListeners) throws IOException {
        ExecHelper.execute(command, envVars, getDeleteArgs(namespace, "eventlistener", eventListeners));
    }

    private String[] getDeleteArgs(String namespace, String kind, List<String> resourcesToDelete, String... flags) {
        List<String> args = new ArrayList<>(Arrays.asList(kind, "delete", "-f"));
        args.addAll(resourcesToDelete);
        if (flags.length > 0) args.addAll(Arrays.asList(flags));
        if (!namespace.isEmpty()) {
            args.addAll(Arrays.asList("-n", namespace));
        }
        return args.toArray(new String[0]);
    }

    public Map<String, Object> getCustomResources(String namespace, CustomResourceDefinitionContext crdContext) {
        try {
            if (namespace.isEmpty()) {
                return client.customResource(crdContext).list();
            }
            return client.customResource(crdContext).list(namespace);
        } catch(KubernetesClientException e) {
            // call failed bc resource doesn't exist - 404
            return null;
        }
    }

    @Override
    public Map<String, Object> getCustomResource(String namespace, String name, CustomResourceDefinitionContext crdContext) {
        try {
            if (namespace.isEmpty()) {
                return new TreeMap<>(client.customResource(crdContext).get(name));
            }
            return new TreeMap<>(client.customResource(crdContext).get(namespace, name));
        } catch(KubernetesClientException e) {
            // call failed bc resource doesn't exist - 404
            return null;
        }
    }

    @Override
    public void editCustomResource(String namespace, String name, CustomResourceDefinitionContext crdContext, String objectAsString) throws IOException {
        if (namespace.isEmpty()) {
            client.customResource(crdContext).edit(name, objectAsString);
        } else {
            client.customResource(crdContext).edit(namespace, name, objectAsString);
        }
    }

    @Override
    public void createCustomResource(String namespace, CustomResourceDefinitionContext crdContext, String objectAsString) throws IOException {
        if (namespace.isEmpty()) {
            client.customResource(crdContext).create(objectAsString);
        } else {
            client.customResource(crdContext).create(namespace, objectAsString);
        }
    }

    @Override
    public String startPipeline(String namespace, String pipeline, Map<String, String> parameters, Map<String, String> resources, String serviceAccount, Map<String, String> taskServiceAccount, Map<String, Workspace> workspaces, String runPrefixName) throws IOException {
        List<String> args = new ArrayList<>(Arrays.asList("pipeline", "start", pipeline, "-n", namespace));
        if (!serviceAccount.isEmpty()) {
            args.add(FLAG_SERVICEACCOUNT + "=" + serviceAccount);
        }
        args.addAll(argsToList(taskServiceAccount, FLAG_TASKSERVICEACCOUNT));
        args.addAll(workspaceArgsToList(workspaces));
        args.addAll(argsToList(parameters, FLAG_PARAMETER));
        args.addAll(argsToList(resources, FLAG_INPUTRESOURCEPIPELINE));
        if (!runPrefixName.isEmpty()) {
            args.add(FLAG_PREFIXNAME + "=" + runPrefixName);
        }
        String output = ExecHelper.execute(command, envVars, args.toArray(new String[0]));
        return this.getTektonRunName(output);
    }

    @Override
    public String startLastPipeline(String namespace, String pipeline) throws IOException {
        String output = ExecHelper.execute(command, envVars, "pipeline", "start", pipeline, "--last", "-n", namespace);
        return this.getTektonRunName(output);
    }

    public String startTask(String namespace, String task, Map<String, String> parameters, Map<String, String> inputResources, Map<String, String> outputResources, String serviceAccount, Map<String, Workspace> workspaces, String runPrefixName) throws IOException {
        List<String> args = new ArrayList<>(Arrays.asList("task", "start", task, "-n", namespace));
        return startTaskInternal(args, parameters, inputResources, outputResources, serviceAccount, workspaces, runPrefixName);
    }

    public String startClusterTask(String namespace, String clusterTask, Map<String, String> parameters, Map<String, String> inputResources, Map<String, String> outputResources, String serviceAccount, Map<String, Workspace> workspaces, String runPrefixName) throws IOException {
        List<String> args = new ArrayList<>(Arrays.asList("clustertask", "start", clusterTask, "-n", namespace)); // -n is used to retreive input/output resources
        return startTaskInternal(args, parameters, inputResources, outputResources, serviceAccount, workspaces, runPrefixName);
    }

    private String startTaskInternal(List<String> args, Map<String, String> parameters, Map<String, String> inputResources, Map<String, String> outputResources, String serviceAccount, Map<String, Workspace> workspaces, String runPrefixName) throws IOException {
        if (!serviceAccount.isEmpty()) {
            args.add(FLAG_SERVICEACCOUNT + "=" + serviceAccount);
        }
        args.addAll(workspaceArgsToList(workspaces));
        args.addAll(argsToList(parameters, FLAG_PARAMETER));
        args.addAll(argsToList(inputResources, FLAG_INPUTRESOURCETASK));
        args.addAll(argsToList(outputResources, FLAG_OUTPUTRESOURCE));
        if (!runPrefixName.isEmpty()) {
            args.add(FLAG_PREFIXNAME + "=" + runPrefixName);
        }
        String output = ExecHelper.execute(command, envVars, args.toArray(new String[0]));
        return getTektonRunName(output);
    }

    @Override
    public String startLastTask(String namespace, String task) throws IOException {
        String output = ExecHelper.execute(command, envVars, "task", "start", task, "--last", "-n", namespace);
        return getTektonRunName(output);
    }

    private List<String> argsToList(Map<String, String> argMap, String flag) {
        List<String> args = new ArrayList<>();
        if (argMap != null) {
            argMap.entrySet().stream().forEach(param -> {
                if (!param.getKey().isEmpty() && !param.getValue().isEmpty()) {
                    args.add(flag);
                    args.add(param.getKey() + "=" + param.getValue());
                }
            });
        }
        return args;
    }

    private List<String> workspaceArgsToList(Map<String, Workspace> argMap) {
        List<String> args = new ArrayList<>();
        if (argMap != null) {
            argMap.values().stream().forEach(item -> {
                args.add(FLAG_WORKSPACE);
                if (item.getKind() == Workspace.Kind.PVC) {
                    args.add("name=" + item.getName() + ",claimName=" + item.getResource());
                } else if (item.getKind() == Workspace.Kind.CONFIGMAP) {
                    args.add("name=" + item.getName() + ",config=" + item.getResource());
                } else if (item.getKind() == Workspace.Kind.SECRET) {
                    args.add("name=" + item.getName() + ",secret=" + item.getResource());
                } else if (item.getKind() == Workspace.Kind.EMPTYDIR) {
                    args.add("name=" + item.getName() + ",emptyDir=");
                }
            });
        }
        return args;
    }

    @Override
    public void showLogsPipelineRun(String namespace, String pipelineRun, boolean toEditor) throws IOException {
        if (!toEditor) {
            ExecHelper.executeWithTerminal(project, Constants.TERMINAL_TITLE,false, envVars, command, "pipelinerun", "logs", pipelineRun, "-n", namespace);
        } else {
            String fileName = namespace + "-" + KIND_PIPELINERUN + "-" + pipelineRun + ".log";
            ExecHelper.executeWithUI(envVars, outputToEditor(fileName),command, "pipelinerun", "logs", pipelineRun, "-f", "-n", namespace);
        }
    }

    @Override
    public void showLogsTaskRun(String namespace, String taskRun, boolean toEditor) throws IOException {
        if (!toEditor) {
            ExecHelper.executeWithTerminal(project, Constants.TERMINAL_TITLE, false, envVars, command, "taskrun", "logs", taskRun, "-n", namespace);
        } else {
            String fileName = namespace + "-" + KIND_TASKRUN + "-" + taskRun + ".log";
            ExecHelper.executeWithUI(envVars, outputToEditor(fileName),command, "taskrun", "logs", taskRun, "-f", "-n", namespace);
        }
    }

    @Override
    public void showLogsEventListener(String namespace, String el) throws IOException {
        ExecHelper.executeWithTerminal(project, Constants.TERMINAL_TITLE, false, envVars, command, "el", "logs", el, "-n", namespace);
    }

    @Override
    public void followLogsPipelineRun(String namespace, String pipelineRun, boolean toEditor) throws IOException {
        if (!toEditor) {
            ExecHelper.executeWithTerminal(project, Constants.TERMINAL_TITLE,false, envVars, command, "pipelinerun", "logs", pipelineRun, "-f", "-n", namespace);
        } else {
            String fileName = namespace + "-" + KIND_PIPELINERUN + "-" + pipelineRun + "-follow.log";
            ExecHelper.executeWithUI(envVars, openEmptyEditor(fileName), outputToEditor(fileName),command, "pipelinerun", "logs", pipelineRun, "-f", "-n", namespace);
        }
    }

    @Override
    public void followLogsTaskRun(String namespace, String taskRun, boolean toEditor) throws IOException {
        if (!toEditor) {
            ExecHelper.executeWithTerminal(project, Constants.TERMINAL_TITLE, false, envVars, command, "taskrun", "logs", taskRun, "-f", "-n", namespace);
        } else {
            String fileName = namespace + "-" + KIND_TASKRUN + "-" + taskRun + "-follow.log";
            ExecHelper.executeWithUI(envVars, openEmptyEditor(fileName), outputToEditor(fileName),command, "taskrun", "logs", taskRun, "-f", "-n", namespace);
        }
    }

    private Runnable openEmptyEditor(String fileName) {
        return () -> VirtualFileHelper.openVirtualFileInEditor(project, fileName, "", true);
    }

    private Consumer<String> outputToEditor(String fileName) {
        return (sb) -> ApplicationManager.getApplication().runWriteAction(
                            () -> VirtualFileHelper.openVirtualFileInEditor(project, fileName, sb, true)
        );
    }

    @Override
    public String getTaskRunYAML(String namespace, String taskRun) throws IOException {
        return ExecHelper.execute(command, envVars, "taskrun", "describe", taskRun, "-n", namespace, "-o", "yaml");
    }

    @Override
    public String getPipelineRunYAML(String namespace, String pipelineRun) throws IOException {
        return ExecHelper.execute(command, envVars, "pipelinerun", "describe", pipelineRun, "-n", namespace, "-o", "yaml");
    }

    @Override
    public void cancelPipelineRun(String namespace, String pipelineRun) throws IOException {
        ExecHelper.execute(command, "pipelinerun", "cancel", pipelineRun, "-n", namespace);
    }

    @Override
    public void cancelTaskRun(String namespace, String taskRun) throws IOException {
        ExecHelper.execute(command, "taskrun", "cancel", taskRun, "-n", namespace);
    }

    @Override
    public Watch watchPipelines(String namespace, Watcher<Pipeline> watcher) throws IOException {
        try {
            return client.adapt(TektonClient.class).v1beta1().pipelines().inNamespace(namespace).watch(watcher);
        } catch (KubernetesClientException e) {
            throw new IOException(e);
        }
    }

    @Override
    public Watch watchPipelineRuns(String namespace, Watcher<io.fabric8.tekton.pipeline.v1beta1.PipelineRun> watcher) throws IOException {
        try {
            return client.adapt(TektonClient.class).v1beta1().pipelineRuns().inNamespace(namespace).watch(watcher);
        } catch (KubernetesClientException e) {
            throw new IOException(e);
        }
    }

    @Override
    public Watch watchTask(String namespace, String task, Watcher<Task> watcher) throws IOException {
        try {
            return client.adapt(TektonClient.class).v1beta1().tasks().inNamespace(namespace).withName(task).watch(watcher);
        } catch (KubernetesClientException e) {
            throw new IOException(e);
        }
    }

    @Override
    public Watch watchTasks(String namespace, Watcher<Task> watcher) throws IOException {
        try {
            return client.adapt(TektonClient.class).v1beta1().tasks().inNamespace(namespace).watch(watcher);
        } catch (KubernetesClientException e) {
            throw new IOException(e);
        }
    }

    @Override
    public Watch watchTaskRuns(String namespace, Watcher<io.fabric8.tekton.pipeline.v1beta1.TaskRun> watcher) throws IOException {
        try {
            return client.adapt(TektonClient.class).v1beta1().taskRuns().inNamespace(namespace).watch(watcher);
        } catch (KubernetesClientException e) {
            throw new IOException(e);
        }
    }

    @Override
    public Watch watchPipelineResources(String namespace, Watcher<PipelineResource> watcher) throws IOException {
        try {
            return client.adapt(TektonClient.class).v1alpha1().pipelineResources().inNamespace(namespace).watch(watcher);
        } catch (KubernetesClientException e) {
            throw new IOException(e);
        }
    }

    @Override
    public Watch watchClusterTasks(Watcher<ClusterTask> watcher) throws IOException {
        try {
            return client.adapt(TektonClient.class).v1beta1().clusterTasks().watch(watcher);
        } catch (KubernetesClientException e) {
            throw new IOException(e);
        }
    }

    @Override
    public Watch watchConditions(String namespace, Watcher<io.fabric8.tekton.pipeline.v1alpha1.Condition> watcher) throws IOException {
        try {
            return client.adapt(TektonClient.class).v1alpha1().conditions().inNamespace(namespace).watch(watcher);
        } catch (KubernetesClientException e) {
            throw new IOException(e);
        }
    }

    @Override
    public Watch watchTriggerTemplates(String namespace, Watcher<TriggerTemplate> watcher) throws IOException {
        try {
            return client.adapt(TektonClient.class).v1alpha1().triggerTemplates().inNamespace(namespace).watch(watcher);
        } catch (KubernetesClientException e) {
            throw new IOException(e);
        }
    }

    @Override
    public Watch watchTriggerBindings(String namespace, Watcher<TriggerBinding> watcher) throws IOException {
        try {
            return client.adapt(TektonClient.class).v1alpha1().triggerBindings().inNamespace(namespace).watch(watcher);
        } catch (KubernetesClientException e) {
            throw new IOException(e);
        }
    }

    @Override
    public Watch watchClusterTriggerBindings(Watcher<ClusterTriggerBinding> watcher) throws IOException {
        try {
            return client.adapt(TektonClient.class).v1alpha1().clusterTriggerBindings().watch(watcher);
        } catch (KubernetesClientException e) {
            throw new IOException(e);
        }
    }

    @Override
    public Watch watchEventListeners(String namespace, Watcher<EventListener> watcher) throws IOException {
        try {
            return client.adapt(TektonClient.class).v1alpha1().eventListeners().inNamespace(namespace).watch(watcher);
        } catch (KubernetesClientException e) {
            throw new IOException(e);
        }
    }

    @Override
    public boolean getDiagnosticData(String namespace, String keyLabel, String valueLabel) throws IOException {
        String data = "";
        PodList pods = client.pods().inNamespace(namespace).withLabel(keyLabel, valueLabel).list();
        for (Pod pod: pods.getItems()) {
            PodStatus podStatus = pod.getStatus();
            data += getFormattedWarningMessage(podStatus.getReason(), podStatus.getMessage());
            for (PodCondition podCondition : podStatus.getConditions()) {
                data += getFormattedWarningMessage(podCondition.getReason(), podCondition.getMessage());
            }
        }
        StatefulSetList states = client.apps().statefulSets().inNamespace(namespace).withLabel(keyLabel, valueLabel).list();
        for (StatefulSet state: states.getItems()) {
            for (StatefulSetCondition stateCondition : state.getStatus().getConditions()) {
                data += getFormattedWarningMessage(stateCondition.getReason(), stateCondition.getMessage());
            }
        }

        if (data.isEmpty()) {
            return false;
        }

        if (Platform.os() == Platform.OperatingSystem.Windows) {
            //Windows echo does not support line feeds, save the file and use more to dump the file instead
            String tempFile = getTempFile(data);
            ExecHelper.executeWithTerminal(project, Constants.TERMINAL_TITLE, false, envVars, "more.com", tempFile);
        } else {
            ExecHelper.executeWithTerminal(project, Constants.TERMINAL_TITLE, false, envVars, "echo", "-e", data);
        }
        return true;
    }

    @Override
    public void installTaskFromHub(String task, String version, boolean overwrite) throws IOException {
        String installType = overwrite ? "reinstall" : "install";
        ExecHelper.execute(command, envVars, "hub", installType, "task", task, "--version", version);
    }

    @Override
    public String getTaskYAMLFromHub(String task, String version) throws IOException {
        return ExecHelper.execute(command, envVars, "hub", "get", "task", task, "--version", version);
    }

    private String getTempFile(String data) throws IOException {
        File f = File.createTempFile("log", "txt");
        f.deleteOnExit();
        FileUtils.write(f, data, StandardCharsets.UTF_8);
        return f.getAbsolutePath();
    }

    private String getFormattedWarningMessage(String reason, String message) {
        String data = "";
        if (!Strings.isNullOrEmpty(reason)) {
            data = "reason: " + reason;
        }
        if (!Strings.isNullOrEmpty(message)) {
            data = "message:" + message;
        }
        return data + "\n";
    }

    @Override
    public URL getMasterUrl() {
        return client.getMasterUrl();
    }

    @Override
    public <T> T getClient(Class<T> clazz) {
        return client.adapt(clazz);
    }

    private String getTektonRunName(String output) {
        String[] strings = output.split("\n");
        if(strings.length > 0){
            String firstString = strings[0];
            String[] pipelineNameArr = firstString.split(":");
            if(pipelineNameArr.length >= 2){
                return pipelineNameArr[1].trim();
            }
        }
        return null;
    }
}
