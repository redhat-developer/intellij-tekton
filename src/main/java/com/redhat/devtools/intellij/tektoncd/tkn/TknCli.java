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
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Strings;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.redhat.devtools.intellij.common.kubernetes.ClusterHelper;
import com.redhat.devtools.intellij.common.kubernetes.ClusterInfo;
import com.redhat.devtools.intellij.common.utils.ExecHelper;
import com.redhat.devtools.intellij.common.utils.MetadataClutter;
import com.redhat.devtools.intellij.common.utils.NetworkUtils;
import com.redhat.devtools.intellij.common.utils.YAMLHelper;
import com.redhat.devtools.intellij.tektoncd.Constants;
import com.redhat.devtools.intellij.tektoncd.telemetry.TelemetryService;
import com.redhat.devtools.intellij.tektoncd.tkn.component.field.Input;
import com.redhat.devtools.intellij.tektoncd.tkn.component.field.Workspace;
import com.redhat.devtools.intellij.tektoncd.ui.toolwindow.debug.DebugTabPanelFactory;
import com.redhat.devtools.intellij.tektoncd.ui.toolwindow.findusage.RefUsage;
import com.redhat.devtools.intellij.tektoncd.utils.PollingHelper;
import com.redhat.devtools.intellij.tektoncd.utils.VirtualFileHelper;
import com.redhat.devtools.intellij.tektoncd.utils.WatchHandler;
import com.redhat.devtools.intellij.telemetry.core.service.TelemetryMessageBuilder;
import com.twelvemonkeys.lang.Platform;
import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.GenericKubernetesResource;
import io.fabric8.kubernetes.api.model.GenericKubernetesResourceList;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaim;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaimSpec;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodCondition;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.api.model.PodStatus;
import io.fabric8.kubernetes.api.model.Quantity;
import io.fabric8.kubernetes.api.model.ResourceRequirements;
import io.fabric8.kubernetes.api.model.apps.StatefulSet;
import io.fabric8.kubernetes.api.model.apps.StatefulSetCondition;
import io.fabric8.kubernetes.api.model.apps.StatefulSetList;
import io.fabric8.kubernetes.client.Client;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.PodResource;
import io.fabric8.kubernetes.client.impl.KubernetesClientImpl;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.Watch;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.WatcherException;
import io.fabric8.kubernetes.client.dsl.ExecWatch;
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext;
import io.fabric8.kubernetes.client.dsl.internal.ExecWebSocketListener;
import io.fabric8.kubernetes.client.utils.Serialization;
import io.fabric8.openshift.client.OpenShiftClient;
import io.fabric8.tekton.client.TektonClient;
import io.fabric8.tekton.pipeline.v1beta1.ClusterTask;
import io.fabric8.tekton.pipeline.v1beta1.ClusterTaskList;
import io.fabric8.tekton.pipeline.v1beta1.Pipeline;
import io.fabric8.tekton.pipeline.v1beta1.PipelineList;
import io.fabric8.tekton.pipeline.v1beta1.PipelineRun;
import io.fabric8.tekton.pipeline.v1beta1.PipelineRunList;
import io.fabric8.tekton.pipeline.v1beta1.Task;
import io.fabric8.tekton.pipeline.v1beta1.TaskList;
import io.fabric8.tekton.pipeline.v1beta1.TaskRun;
import io.fabric8.tekton.pipeline.v1beta1.TaskRunList;
import io.fabric8.tekton.triggers.v1alpha1.ClusterTriggerBinding;
import io.fabric8.tekton.triggers.v1alpha1.EventListener;
import io.fabric8.tekton.triggers.v1alpha1.TriggerBinding;
import io.fabric8.tekton.triggers.v1alpha1.TriggerTemplate;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.redhat.devtools.intellij.tektoncd.Constants.FLAG_PARAMETER;
import static com.redhat.devtools.intellij.tektoncd.Constants.FLAG_PREFIXNAME;
import static com.redhat.devtools.intellij.tektoncd.Constants.FLAG_SERVICEACCOUNT;
import static com.redhat.devtools.intellij.tektoncd.Constants.FLAG_SKIP_OPTIONAL_WORKSPACES;
import static com.redhat.devtools.intellij.tektoncd.Constants.FLAG_TASKSERVICEACCOUNT;
import static com.redhat.devtools.intellij.tektoncd.Constants.FLAG_WORKSPACE;
import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_CLUSTERTASK;
import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_CONFIGMAP;
import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_PIPELINE;
import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_PIPELINERUN;
import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_TASK;
import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_TASKRUN;
import static com.redhat.devtools.intellij.tektoncd.Constants.PIPELINES_ALPHA1_API_VERSION;
import static com.redhat.devtools.intellij.tektoncd.Constants.PIPELINES_BETA1_API_VERSION;
import static com.redhat.devtools.intellij.tektoncd.Constants.PIPELINES_V1_API_VERSION;
import static com.redhat.devtools.intellij.tektoncd.Constants.TRIGGER_ALPHA1_API_VERSION;
import static com.redhat.devtools.intellij.tektoncd.Constants.TRIGGER_BETA1_API_VERSION;
import static com.redhat.devtools.intellij.tektoncd.telemetry.TelemetryService.IS_OPENSHIFT;
import static com.redhat.devtools.intellij.tektoncd.telemetry.TelemetryService.KUBERNETES_VERSION;
import static com.redhat.devtools.intellij.tektoncd.telemetry.TelemetryService.NAME_PREFIX_DIAG;
import static com.redhat.devtools.intellij.tektoncd.telemetry.TelemetryService.OPENSHIFT_VERSION;
import static com.redhat.devtools.intellij.tektoncd.telemetry.TelemetryService.PROP_RESOURCE_KIND;

public class TknCli implements Tkn {

    private static final Logger LOGGER = LoggerFactory.getLogger(TknCli.class);
    private static final ObjectMapper JSON_MAPPER = new ObjectMapper(new JsonFactory());

    private String command;
    private final Project project;
    private KubernetesClient client;
    private DebugTabPanelFactory debugTabPanelFactory;
    private final WatchHandler watchHandler;
    private final PollingHelper pollingHelper;

    private Map<String, String> envVars;

    private volatile String tektonVersion = "0.0.0";
    private volatile boolean hasAlphaFeaturesEnabled = false;

    private volatile String pipelinesApiVersion = "";
    private volatile String triggersApiVersion = "";

    TknCli(Project project, String command) {
        this.command = command;
        this.project = project;
        createConfig();
        this.debugTabPanelFactory = new DebugTabPanelFactory(project, this);
        try {
            this.envVars = NetworkUtils.buildEnvironmentVariables(client.getMasterUrl().toString());
        } catch (URISyntaxException e) {
            this.envVars = Collections.emptyMap();
        }
        this.watchHandler = new WatchHandler(this);
        this.pollingHelper = new PollingHelper(this);
        initTektonPipelinesApiVersion();
        initTektonTriggersApiVersion();
        reportTelemetry();
        updateTektonInfos();
    }

    private void createConfig() {
        try {
            this.client = new DefaultKubernetesClient(new ConfigBuilder().build());
            if (this.client.isAdaptable(OpenShiftClient.class)) {
                this.client = this.client.adapt(OpenShiftClient.class);
            }
        } catch(Exception e) {
            LOGGER.warn(e.getLocalizedMessage(), e);
        }
    }

    private void reportTelemetry() {
        TelemetryMessageBuilder.ActionMessage telemetry = TelemetryService.instance().action(TelemetryService.NAME_PREFIX_MISC + "login");
        try {
            ClusterInfo info = ClusterHelper.getClusterInfo(client);
            telemetry.property(KUBERNETES_VERSION, info.getKubernetesVersion());
            telemetry.property(IS_OPENSHIFT, Boolean.toString(info.isOpenshift()));
            telemetry.property(OPENSHIFT_VERSION, info.getOpenshiftVersion());
            telemetry.send();
        } catch (RuntimeException e) {
            // do not send telemetry when there is no context ( ie default kube URL as master URL )
            try {
                //workaround to not send null values
                if (e.getMessage() != null) {
                    telemetry.error(e).send();
                } else {
                    telemetry.error(e.toString()).send();
                }
            } catch (RuntimeException ex) {
                LOGGER.warn(ex.getLocalizedMessage(), ex);
            }
        }
    }

    @Override
    public boolean isTektonAware() throws IOException {
        try {
            return client.rootPaths().getPaths().stream().anyMatch(path -> path.endsWith("tekton.dev"));
        } catch (KubernetesClientException e) {
            throw new IOException(e);
        }
    }

    @Override
    public boolean isTektonTriggersAware() {
        try {
            return client.rootPaths().getPaths().stream().anyMatch(path -> path.endsWith("triggers.tekton.dev"));
        } catch (KubernetesClientException e) {
            return false;
        }
    }

    private void initTektonPipelinesApiVersion() {
        try {
            this.pipelinesApiVersion = getTektonPipelinesApiVersion();
        } catch (IOException ignored) {}
    }

    @Override
    public String getTektonPipelinesApiVersionOrDefault(String defaultValue) {
        try {
            return getTektonPipelinesApiVersion();
        } catch (IOException e) {
            return defaultValue;
        }
    }

    @Override
    public String getTektonPipelinesApiVersion() throws IOException {
        if (!this.pipelinesApiVersion.isEmpty()) {
            return this.pipelinesApiVersion;
        }
        try {
            List<String> paths = client.rootPaths().getPaths();
            if (paths.stream().anyMatch(path -> path.endsWith("tekton.dev/v1"))) {
                this.pipelinesApiVersion = PIPELINES_V1_API_VERSION;
            } else if (paths.stream().anyMatch(path -> path.endsWith("tekton.dev/v1beta1"))) {
                this.pipelinesApiVersion = PIPELINES_BETA1_API_VERSION;
            } else {
                this.pipelinesApiVersion = PIPELINES_ALPHA1_API_VERSION;
            }
        } catch (KubernetesClientException e) {
            throw new IOException(e);
        }
        return this.pipelinesApiVersion;
    }

    private void initTektonTriggersApiVersion() {
        try {
            this.triggersApiVersion = getTektonTriggersApiVersion();
        } catch (IOException ignored) {}
    }

    @Override
    public String getTektonTriggersApiVersion() throws IOException {
        if (!this.triggersApiVersion.isEmpty()) {
            return this.triggersApiVersion;
        }
        try {
            if (client.rootPaths().getPaths().stream().anyMatch(path -> path.endsWith("triggers.tekton.dev/v1beta1"))) {
                this.triggersApiVersion = TRIGGER_BETA1_API_VERSION;
            } else {
                this.triggersApiVersion = TRIGGER_ALPHA1_API_VERSION;
            }
        } catch (KubernetesClientException e) {
            throw new IOException(e);
        }
        return this.triggersApiVersion;
    }

    @Override
    public String getTektonVersion() {
        return tektonVersion.indexOf("v") == 0
                ? tektonVersion.substring(1)
                : tektonVersion;
    }

    @Override
    public boolean isTektonAlphaFeatureEnabled() {
        return hasAlphaFeaturesEnabled;
    }

    private void updateTektonInfos() {
        try {
            String coreNs = getTektonCoreNamespace();
            ConfigMap pipelineInfoMap = getConfigMap(coreNs, "pipelines-info");
            tektonVersion = pipelineInfoMap.getData().get("version");
            ConfigMap alphaMap = getConfigMap(coreNs, "feature-flags");
            hasAlphaFeaturesEnabled = alphaMap.getData().get("enable-api-fields").equalsIgnoreCase("alpha");
            initConfigMapWatchers(coreNs);
        } catch (Exception e) {
            LOGGER.warn(e.getLocalizedMessage(), e);
        }
    }

    private void initConfigMapWatchers(String coreNs) {
        initConfigMapWatcher(coreNs, "pipelines-info", (configMap -> tektonVersion = configMap.getData().get("version")));
        initConfigMapWatcher(coreNs, "feature-flags", (configMap -> hasAlphaFeaturesEnabled = configMap.getData().get("enable-api-fields").equalsIgnoreCase("alpha")));
    }

    private void initConfigMapWatcher(String namespace, String resource, Consumer<ConfigMap> doUpdate) {
        this.watchHandler.setWatchByResourceName(namespace, KIND_CONFIGMAP, resource,
                new Watcher<ConfigMap>() {
                    @Override
                    public void eventReceived(Action action, ConfigMap resource) {
                        doUpdate.accept(resource);
                    }

                    @Override
                    public void onClose(WatcherException cause) {

                    }
                });
    }

    @Override
    public String getNamespace() {
        String namespace = client.getNamespace();
        if (Strings.isNullOrEmpty(namespace)) {
            namespace = "default";
        }
        return namespace;
    }

    @Override
    public String getTektonCoreNamespace() {
        if (this.client.isAdaptable(OpenShiftClient.class)) {
            return "openshift-pipelines";
        }
        return "tekton-pipelines";
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
    public List<Pipeline> getPipelines(String namespace) throws IOException {
        try {
            PipelineList pipelines = client.adapt(TektonClient.class).v1beta1().pipelines().inNamespace(namespace).list();
            return pipelines.getItems();
        } catch (KubernetesClientException e) {
            throw new IOException(e);
        }
    }

    @Override
    public List<PipelineRun> getPipelineRuns(String namespace, String pipeline) throws IOException {
        try {
            PipelineRunList pipelineRunList;
            if (!pipeline.isEmpty()) {
                pipelineRunList = client.adapt(TektonClient.class).v1beta1().pipelineRuns()
                        .inNamespace(namespace).withLabel("tekton.dev/pipeline", pipeline).list();
            } else {
                pipelineRunList = client.adapt(TektonClient.class).v1beta1().pipelineRuns()
                        .inNamespace(namespace).list();
            }
            return pipelineRunList.getItems();
        } catch (KubernetesClientException e) {
            throw new IOException(e);
        }
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

    public List<TaskRun> getTaskRuns(String namespace, String task) throws IOException {
        try {
            TaskRunList taskRunList;
            if (!task.isEmpty()) {
                taskRunList = client.adapt(TektonClient.class).v1beta1().taskRuns().inNamespace(namespace)
                        .withLabel("tekton.dev/task", task).list();
            } else {
                taskRunList = client.adapt(TektonClient.class).v1beta1().taskRuns().inNamespace(namespace).list();
            }
            return taskRunList.getItems();
        } catch (KubernetesClientException e) {
            throw new IOException(e);
        }
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
    public ConfigMap getConfigMap(String namespace, String name) {
        return client.configMaps().inNamespace(namespace).withName(name).get();
    }

    @Override
    public String getPipelineYAML(String namespace, String pipeline) throws IOException {
        return ExecHelper.execute(command, envVars, "pipeline", "describe", pipeline, "-n", namespace, "-o", "yaml");
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
    public  List<RefUsage> findTaskUsages(String kind, String resource) throws IOException {
        String jsonPathExpr = "jsonpath=\"{range .items[*]}{@.metadata.name}|{range .spec.tasks[*]}{.taskRef.kind},{.taskRef.name}|{end}{end}\"";
        String result = ExecHelper.execute(command, envVars, "pipeline", "ls", "-n", getNamespace(), "-o", jsonPathExpr);
        String[] resultSplitted = result.replace("\"", "").split("\\|");
        List<RefUsage> usages = new ArrayList<>();
        String pipeline = "";
        for (String item: resultSplitted) {
            if (!item.contains(",")) {
                pipeline = item;
                continue;
            }

            String[] kindName = item.split(",");
            if (kindName.length == 2 && kindName[0].equalsIgnoreCase(kind) && kindName[1].equalsIgnoreCase(resource) && !pipeline.isEmpty()) {
                String finalPipeline = pipeline;
                Optional<RefUsage> refUsage = usages.stream().filter(ref -> ref.getKind().equals(KIND_PIPELINE) && ref.getName().equals(finalPipeline)).findFirst();
                if (refUsage.isPresent()) {
                    refUsage.get().incremetOccurrence();
                } else {
                    usages.add(new RefUsage(getNamespace(), pipeline, KIND_PIPELINE));
                }
            }
        }
        return usages;
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

    public GenericKubernetesResourceList getCustomResources(String namespace, CustomResourceDefinitionContext crdContext) {
        try {
            if (namespace.isEmpty()) {
                return client.genericKubernetesResources(crdContext).list();
            }
            return client.genericKubernetesResources(crdContext).inNamespace(namespace).list();
        } catch(KubernetesClientException e) {
            // call failed bc resource doesn't exist - 404
            return null;
        }
    }

    @Override
    public GenericKubernetesResource getCustomResource(String namespace, String name, CustomResourceDefinitionContext crdContext) {
        try {
            if (namespace.isEmpty()) {
                return client.genericKubernetesResources(crdContext).withName(name).get();
            }
            return client.genericKubernetesResources(crdContext).inNamespace(namespace).withName(name).get();
        } catch(KubernetesClientException e) {
            // call failed bc resource doesn't exist - 404
            return null;
        }
    }

    @Override
    public void editCustomResource(String namespace, String name, CustomResourceDefinitionContext crdContext, String objectAsString) throws IOException {
        try {
            GenericKubernetesResource genericKubernetesResource = Serialization.unmarshal(objectAsString , GenericKubernetesResource.class);
            if (namespace.isEmpty()) {
                client.genericKubernetesResources(crdContext).replace(genericKubernetesResource);
            } else {
                client.genericKubernetesResources(crdContext).inNamespace(namespace).replace(genericKubernetesResource);
            }
        } catch(KubernetesClientException e) {
            throw new IOException(e.getLocalizedMessage(), e);
        }
    }

    @Override
    public String createCustomResource(String namespace, CustomResourceDefinitionContext crdContext, String objectAsString) throws IOException {
        try {
            GenericKubernetesResource genericKubernetesResource = Serialization.unmarshal(objectAsString , GenericKubernetesResource.class);
            if (namespace.isEmpty()) {
                genericKubernetesResource = client.genericKubernetesResources(crdContext).create(genericKubernetesResource);
            } else {
                genericKubernetesResource = client.genericKubernetesResources(crdContext).inNamespace(namespace).create(genericKubernetesResource);
            }
            return getResourceNameFromMap(genericKubernetesResource);
        } catch(KubernetesClientException e) {
            throw new IOException(e.getLocalizedMessage(), e);
        }
    }

    private String getResourceNameFromMap(GenericKubernetesResource resource) {
        if (resource == null
            || resource.getMetadata() == null) {
            return "";
        }
        return resource.getMetadata().getName();
    }

    @Override
    public void createPVC(String name, String accessMode, String size, String unit) throws IOException {
        PersistentVolumeClaim claim = new PersistentVolumeClaim();

        ObjectMeta metadata = new ObjectMeta();
        metadata.setName(name);

        ResourceRequirements resourceRequirements = new ResourceRequirements();
        Map<String, Quantity> requests = new HashMap<>();
        requests.put("storage", new Quantity(size, unit));
        resourceRequirements.setRequests(requests);

        PersistentVolumeClaimSpec spec = new PersistentVolumeClaimSpec();
        spec.setAccessModes(Arrays.asList(accessMode));
        spec.setVolumeMode("Filesystem");
        spec.setResources(resourceRequirements);

        claim.setMetadata(metadata);
        claim.setSpec(spec);

        createPVC(claim);
    }

    private void createPVC(PersistentVolumeClaim persistentVolumeClaim) throws IOException {
        try {
            client.persistentVolumeClaims().create(persistentVolumeClaim);
        } catch (KubernetesClientException e) {
            throw new IOException(e.getLocalizedMessage(), e);
        }
    }

    @Override
    public String startPipeline(String namespace, String pipeline, Map<String, Input> parameters, String serviceAccount, Map<String, String> taskServiceAccount, Map<String, Workspace> workspaces, String runPrefixName) throws IOException {
        List<String> args = new ArrayList<>(Arrays.asList("pipeline", "start", pipeline, "-n", namespace));
        if (!serviceAccount.isEmpty()) {
            args.add(FLAG_SERVICEACCOUNT + "=" + serviceAccount);
        }
        args.addAll(argsToList(taskServiceAccount, FLAG_TASKSERVICEACCOUNT));
        args.addAll(workspaceArgsToList(workspaces));
        args.addAll(paramsToArgsList(parameters, FLAG_PARAMETER));
        if (!runPrefixName.isEmpty()) {
            args.add(FLAG_PREFIXNAME + "=" + runPrefixName);
        }
        args.add(FLAG_SKIP_OPTIONAL_WORKSPACES);
        String output = ExecHelper.execute(command, envVars, args.toArray(new String[0]));
        return this.getTektonRunName(output);
    }

    @Override
    public String startLastPipeline(String namespace, String pipeline) throws IOException {
        String output = ExecHelper.execute(command, envVars, "pipeline", "start", pipeline, "--last", "-n", namespace, FLAG_SKIP_OPTIONAL_WORKSPACES);
        return this.getTektonRunName(output);
    }

    public String startTask(String namespace, String task, Map<String, Input> parameters, String serviceAccount, Map<String, Workspace> workspaces, String runPrefixName) throws IOException {
        List<String> args = new ArrayList<>(Arrays.asList("task", "start", task, "-n", namespace));
        return startTaskAndGetRunName(args, parameters, serviceAccount, workspaces, runPrefixName);
    }

    @Override
    public String createRunFromTask(String namespace, String task, Map<String, Input> parameters, String serviceAccount, Map<String, Workspace> workspaces, String runPrefixName) throws IOException {
        List<String> args = new ArrayList<>(Arrays.asList("task", "start", task, "-n", namespace, "--dry-run"));
        return startTask(args, parameters, serviceAccount, workspaces, runPrefixName);
    }

    public String startClusterTask(String namespace, String clusterTask, Map<String, Input> parameters, String serviceAccount, Map<String, Workspace> workspaces, String runPrefixName) throws IOException {
        List<String> args = new ArrayList<>(Arrays.asList("clustertask", "start", clusterTask, "-n", namespace)); // -n is used to retreive input/output resources
        return startTaskAndGetRunName(args, parameters, serviceAccount, workspaces, runPrefixName);
    }

    @Override
    public String createRunFromClusterTask(String namespace, String clusterTask, Map<String, Input> parameters, String serviceAccount, Map<String, Workspace> workspaces, String runPrefixName) throws IOException {
        List<String> args = new ArrayList<>(Arrays.asList("clustertask", "start", clusterTask, "-n", namespace, "--dry-run")); // -n is used to retreive input/output resources
        return startTask(args, parameters, serviceAccount, workspaces, runPrefixName);
    }

    private String startTask(List<String> args, Map<String, Input> parameters, String serviceAccount, Map<String, Workspace> workspaces, String runPrefixName) throws IOException {
        if (!serviceAccount.isEmpty()) {
            args.add(FLAG_SERVICEACCOUNT + "=" + serviceAccount);
        }
        args.addAll(workspaceArgsToList(workspaces));
        args.addAll(paramsToArgsList(parameters, FLAG_PARAMETER));
        if (!runPrefixName.isEmpty()) {
            args.add(FLAG_PREFIXNAME + "=" + runPrefixName);
        }
        args.add(FLAG_SKIP_OPTIONAL_WORKSPACES);
        return ExecHelper.execute(command, envVars, args.toArray(new String[0]));
    }

    private String startTaskAndGetRunName(List<String> args, Map<String, Input> parameters, String serviceAccount, Map<String, Workspace> workspaces, String runPrefixName) throws IOException {
        String output = startTask(args, parameters, serviceAccount, workspaces, runPrefixName);
        return getTektonRunName(output);
    }

    @Override
    public String startLastTask(String namespace, String task) throws IOException {
        String output = ExecHelper.execute(command, envVars, "task", "start", task, "--last", "-n", namespace, FLAG_SKIP_OPTIONAL_WORKSPACES);
        return getTektonRunName(output);
    }

    private List<String> paramsToArgsList(Map<String, Input> argMap, String flag) {
        List<String> args = new ArrayList<>();
        if (argMap != null) {
            argMap.entrySet().forEach(param -> {
                if (!param.getKey().isEmpty() && !(param.getValue().type().equalsIgnoreCase("string") && param.getValue().value().isEmpty())) {
                    args.add(flag);
                    args.add(param.getKey() + "=" + param.getValue().value());
                }
            });
        }
        return args;
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
            argMap.entrySet().forEach(item -> {
                String name = item.getKey();
                Workspace workspace = item.getValue();
                if (workspace.isOptional() && workspace.getKind() == null) {
                    return;
                }
                args.add(FLAG_WORKSPACE);
                if (workspace.getKind() == Workspace.Kind.EMPTYDIR) {
                    args.add("name=" + name + ",emptyDir=");
                } else if (workspace.getKind() == Workspace.Kind.PVC) {
                    if (workspace.getItems() != null && workspace.getItems().containsKey("file")) {
                        args.add("name=" + workspace.getName() + ",volumeClaimTemplateFile=" + workspace.getItems().get("file"));
                    } else {
                        args.add("name=" + workspace.getName() + ",claimName=" + workspace.getResource());
                    }
                } else if (workspace.getKind() == Workspace.Kind.CONFIGMAP) {
                    args.add("name=" + workspace.getName() + ",config=" + workspace.getResource());
                } else if (workspace.getKind() == Workspace.Kind.SECRET) {
                    args.add("name=" + workspace.getName() + ",secret=" + workspace.getResource());
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
            ExecHelper.executeWithUI(envVars,
                    outputToEditor(fileName, KIND_PIPELINERUN),
                    command, KIND_PIPELINERUN, "logs", pipelineRun, "-f", "-n", namespace);
        }
    }

    @Override
    public void showLogsTaskRun(String namespace, String taskRun, boolean toEditor) throws IOException {
        if (!toEditor) {
            ExecHelper.executeWithTerminal(project, Constants.TERMINAL_TITLE, false, envVars, command, "taskrun", "logs", taskRun, "-n", namespace);
        } else {
            String fileName = namespace + "-" + KIND_TASKRUN + "-" + taskRun + ".log";
            ExecHelper.executeWithUI(envVars,
                    outputToEditor(fileName, KIND_TASKRUN),
                    command, KIND_TASKRUN, "logs", taskRun, "-f", "-n", namespace);
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
            ExecHelper.executeWithUI(envVars,
                    openEmptyEditor(fileName, KIND_PIPELINERUN),
                    outputToEditor(fileName, KIND_PIPELINERUN),
                    command, KIND_PIPELINERUN, "logs", pipelineRun, "-f", "-n", namespace);
        }
    }

    @Override
    public void followLogsTaskRun(String namespace, String taskRun, boolean toEditor) throws IOException {
        if (!toEditor) {
            ExecHelper.executeWithTerminal(project, Constants.TERMINAL_TITLE, false, envVars, command, "taskrun", "logs", taskRun, "-f", "-n", namespace);
        } else {
            String fileName = namespace + "-" + KIND_TASKRUN + "-" + taskRun + "-follow.log";
            ExecHelper.executeWithUI(envVars,
                    openEmptyEditor(fileName, KIND_TASKRUN),
                    outputToEditor(fileName, KIND_TASKRUN),
                    command, KIND_TASKRUN, "logs", taskRun, "-f", "-n", namespace);
        }
    }

    private Runnable openEmptyEditor(String fileName, String kind) {
        return () -> {
            try {
                VirtualFileHelper.openVirtualFileInEditor(project, fileName, "");
            } catch (IOException e) {
                String errorMessage = "Could open empty editor for logs: " + e.getLocalizedMessage();
                TelemetryService.instance().action(NAME_PREFIX_DIAG + "follow logs in editor")
                        .property(TelemetryService.PROP_RESOURCE_KIND, kind)
                        .error(errorMessage)
                        .send();
                LOGGER.warn(errorMessage, e);
            }
        };
    }

    private Consumer<String> outputToEditor(String fileName, String kind) {
        return (sb) -> ApplicationManager.getApplication().runWriteAction(() -> {
                    try {
                        // called each time a line is added to log: dont send telemetry
                        VirtualFileHelper.openVirtualFileInEditor(project, fileName, sb);
                    } catch (IOException e) {
                        String errorMessage = "Could not output logs to editor: " + e.getLocalizedMessage();
                        TelemetryService.instance().action(NAME_PREFIX_DIAG + "output logs in editor")
                                .property(PROP_RESOURCE_KIND, kind)
                                .error(errorMessage)
                                .send();
                        LOGGER.warn(errorMessage, e);
                    }
                }
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
    public Watch watchPipeline(String namespace, String pipeline, Watcher<Pipeline> watcher) throws IOException {
        try {
            return client.adapt(TektonClient.class).v1beta1().pipelines().inNamespace(namespace).withName(pipeline).watch(watcher);
        } catch (KubernetesClientException e) {
            throw new IOException(e);
        }
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
    public Watch watchTaskRuns(String namespace, Watcher<TaskRun> watcher) throws IOException {
        try {
            return client.adapt(TektonClient.class).v1beta1().taskRuns().inNamespace(namespace).watch(watcher);
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

    public Watch watchPodsWithLabel(String namespace, String key, String value, Watcher<Pod> watcher) throws IOException {
        try {
            return client.pods().inNamespace(namespace).withLabel(key, value).watch(watcher);
        } catch (KubernetesClientException e) {
            throw new IOException(e);
        }
    }

    public Pod getPod(String namespace, String name) throws IOException {
        try {
            return client.pods().inNamespace(namespace).withName(name).get();
        } catch (KubernetesClientException e) {
            throw new IOException(e);
        }
    }

    public boolean isContainerStoppedOnDebug(String namespace, String name, String container, Pod resource) throws IOException {
        // This is a hack to prevent from displaying error messages in the IDE Fatal Errors panel
        // regarding a container already closed during command execution
        LogManager.getLogger(ExecWebSocketListener.class).setLevel(Level.FATAL);
        try(ExecWatch watch = execCommandInContainer(resource, container, "sh", "-c", "cat tekton/termination")) {
            return watch.getOutput().read() != -1;
        } catch(Throwable e) {
            throw new IOException(e);
        }
    }

    public ExecWatch execCommandInContainer(Pod pod, String containerId, String... command) {
        return client.pods().inNamespace(pod.getMetadata().getNamespace())
                .withName(pod.getMetadata().getName())
                .inContainer(containerId)
                .redirectingInput()
                .redirectingOutput()
                .redirectingError()
                .withTTY()
                .exec(command);

    }

    public ExecWatch customExecCommandInContainer(Pod pod, String containerId, String... command) {
        String namespace = pod.getMetadata().getNamespace();
        MixedOperation<Pod, PodList, PodResource> podOperations = new KubernetesClientImpl().pods();
        return podOperations.inNamespace(namespace)
                .withName(pod.getMetadata().getName())
                .inContainer(containerId)
                .redirectingInput()
                .redirectingOutput()
                .redirectingError()
                .withTTY()
                .exec(command);

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

    @Override
    public String getPipelineYAMLFromHub(String pipeline, String version) throws IOException {
        return ExecHelper.execute(command, envVars, "hub", "get", "pipeline", pipeline, "--version", version);
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

    public List<String> getResourcesAsYaml(List<Resource> resources) throws IOException {
        String ns = getNamespace();
        CompletableFuture[] futures = resources.stream().map(resource -> CompletableFuture.supplyAsync(() -> {
            try {
                switch (resource.type()) {
                    case KIND_PIPELINE: {
                        return cleanYaml(getPipelineYAML(ns, resource.name()));
                    }
                    case KIND_TASK: {
                        return cleanYaml(getTaskYAML(ns, resource.name()));
                    }
                    case KIND_CLUSTERTASK: {
                        return cleanYaml(getClusterTaskYAML(resource.name()));
                    }
                }
            } catch (IOException ignored) {}
            return "";
        })).toArray(CompletableFuture[]::new);

        try {
            CompletableFuture.allOf(futures).join();
        } catch (Exception e) {
            throw new IOException(e.getLocalizedMessage(), e);
        }

        List<String> resourcesAsYaml = new ArrayList<>();
        for (CompletableFuture future: futures) {
            try {
                resourcesAsYaml.add(future.get().toString());
            } catch (InterruptedException | ExecutionException e) {
                throw new IOException(e.getLocalizedMessage(), e);
            }
        }
        return resourcesAsYaml;
    }

    private String cleanYaml(String yaml) throws IOException {
        if (yaml.isEmpty()) {
            return yaml;
        }
        ObjectNode contentNode = (ObjectNode) YAMLHelper.YAMLToJsonNode(yaml);
        ObjectNode metadata = contentNode.has("metadata") ? (ObjectNode) contentNode.get("metadata") : null;
        if (metadata != null) {
            metadata.remove("namespace");
            contentNode.set("metadata", metadata);
            yaml = YAMLHelper.JSONToYAML(contentNode, false);
        }
        return MetadataClutter.remove(yaml, false);
    }

    private List<String> getBundleRegistryAuthenticationCommand(Authenticator authenticator) {
        List<String> command = new ArrayList<>();
        if (!authenticator.getUsername().isEmpty() && !authenticator.getPassword().isEmpty()) {
            command.addAll(Arrays.asList(
                    "--remote-username",
                    authenticator.getUsername(),
                    "--remote-password",
                    authenticator.getPassword()
            ));
        } else if (!authenticator.getToken().isEmpty()) {
            command.addAll(Arrays.asList(
                    "--remote-bearer",
                    authenticator.getToken()
            ));
        }

        if (authenticator.isSkipTls()) {
            command.add("--remote-skip-tls");
        }
        return command;
    }

    public void deployBundle(String image, List<String> resources, Authenticator authenticator) throws IOException {
        String res = String.join("---\n", resources);
        VirtualFile file = VirtualFileHelper.createVirtualFile("bundle-" + Instant.now().toEpochMilli() + ".yaml", res, false);
        List<String> args = new ArrayList<>(Arrays.asList(
                "bundle", "push", image, "-f", file.getPath()
        ));
        if (authenticator != null) {
            args.addAll(getBundleRegistryAuthenticationCommand(authenticator));
        }
        ExecHelper.execute(command, envVars, args.toArray(new String[0]));
    }

    @Override
    public List<Resource> listResourceFromBundle(String bundle, Authenticator authenticator) throws IOException {
        List<String> args = new ArrayList<>(Arrays.asList(
                "bundle", "list", bundle
        ));
        if (authenticator != null) {
            args.addAll(getBundleRegistryAuthenticationCommand(authenticator));
        }
        String output = ExecHelper.execute(command, envVars, args.toArray(new String[0]));
        return Arrays.stream(output.split("\n"))
                .filter(item -> !item.isEmpty())
                .map(item -> {
                    String[] kindName = item.split("/");
                    String kind = kindName[0].contains("pipeline") ? KIND_PIPELINE :
                                  kindName[0].contains("task") ? KIND_TASK :
                                  KIND_CLUSTERTASK;
                    return new Resource(kindName[1], kind);
                }).collect(Collectors.toList());
    }

    @Override
    public String getBundleResourceYAML(String bundle, Resource resource, Authenticator authenticator) throws IOException {
        List<String> args = new ArrayList<>(Arrays.asList(
                "bundle", "list", bundle, resource.type(), resource.name(), "-o", "yaml"
        ));
        if (authenticator != null) {
            args.addAll(getBundleRegistryAuthenticationCommand(authenticator));
        }
        return ExecHelper.execute(command, envVars, args.toArray(new String[0]));
    }

    @Override
    public URL getMasterUrl() {
        return client.getMasterUrl();
    }

    @Override
    public <T extends Client> T getClient(Class<T> clazz) {
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

    public DebugTabPanelFactory getDebugTabPanelFactory() {
        return debugTabPanelFactory;
    }

    public WatchHandler getWatchHandler() {
        return watchHandler;
    }

    public PollingHelper getPollingHelper() {
        return pollingHelper;
    }

    public void dispose() {
        watchHandler.removeAll();
        debugTabPanelFactory.closeTabPanels();
    }
}
