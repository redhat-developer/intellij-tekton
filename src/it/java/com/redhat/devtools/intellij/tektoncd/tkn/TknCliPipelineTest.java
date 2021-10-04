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

import com.redhat.devtools.intellij.tektoncd.TestUtils;
import com.redhat.devtools.intellij.tektoncd.tkn.component.field.Input;
import io.fabric8.tekton.client.TektonClient;
import io.fabric8.tekton.pipeline.v1beta1.TaskRun;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import org.junit.Test;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TknCliPipelineTest extends TknCliTest {

    @Test
    public void verifyCreatePipelineAndDelete() throws IOException {
        final String PIPELINE_NAME = "first";
        String pipelineConfig = TestUtils.load("pipeline1.yaml").replace("pipelinefoo", PIPELINE_NAME);
        TestUtils.saveResource(tkn, pipelineConfig, NAMESPACE, "pipelines");
        // verify pipeline has been created
        List<String> pipelines = tkn.getPipelines(NAMESPACE).stream().map(pp -> pp.getMetadata().getName()).collect(Collectors.toList());
        assertTrue(pipelines.contains(PIPELINE_NAME));
        // clean up and verify cleaning succeed
        tkn.deletePipelines(NAMESPACE, pipelines, false);
        pipelines = tkn.getPipelines(NAMESPACE).stream().map(pp -> pp.getMetadata().getName()).collect(Collectors.toList());;
        assertFalse(pipelines.contains(PIPELINE_NAME));
    }

    @Test
    public void verifyPipelineYAMLIsReturnedCorrectly() throws IOException {
        final String PIPELINE_NAME = "second";
        String pipelineConfig = TestUtils.load("pipeline1.yaml").replace("pipelinefoo", PIPELINE_NAME);
        TestUtils.saveResource(tkn, pipelineConfig, NAMESPACE, "pipelines");
        // verify pipeline has been created
        List<String> pipelines = tkn.getPipelines(NAMESPACE).stream().map(pp -> pp.getMetadata().getName()).collect(Collectors.toList());;
        assertTrue(pipelines.contains(PIPELINE_NAME));
        // get YAML from cluster and verify is the same uploaded
        String resourceBodyFromCluster = tkn.getPipelineYAML(NAMESPACE, PIPELINE_NAME);
        assertEquals(TestUtils.getSpecFromResource(pipelineConfig), TestUtils.getSpecFromResource(resourceBodyFromCluster));
        // clean up and verify cleaning succeed
        tkn.deletePipelines(NAMESPACE, pipelines, false);
        pipelines = tkn.getPipelines(NAMESPACE).stream().map(pp -> pp.getMetadata().getName()).collect(Collectors.toList());;
        assertFalse(pipelines.contains(PIPELINE_NAME));
    }

    @Test
    public void verifyCreatePipelineAndPipelineRunAndDeleteAll() throws IOException, InterruptedException {
        final String TASK_NAME = "forpptest3";
        final String PIPELINE_NAME = "third";
        final String PIPELINE_RUN_NAME = "run-third";
        String taskConfig = TestUtils.load("task1.yaml").replace("taskfoo", TASK_NAME);
        String pipelineConfig = TestUtils.load("pipeline1.yaml").replace("pipelinefoo", PIPELINE_NAME).replace("taskfoo", TASK_NAME);
        String pipelineRunConfig = TestUtils.load("pipelinerun1.yaml").replace("pipelinerunfoo", PIPELINE_RUN_NAME).replace("pipelinefoo", PIPELINE_NAME);
        TestUtils.saveResource(tkn, taskConfig, NAMESPACE, "tasks");
        TestUtils.saveResource(tkn, pipelineConfig, NAMESPACE, "pipelines");
        TestUtils.saveResource(tkn, pipelineRunConfig, NAMESPACE, "pipelineruns");

        // verify task has been created
        List<String> tasks = tkn.getTasks(NAMESPACE).stream().map(task -> task.getMetadata().getName()).collect(Collectors.toList());
        assertTrue(tasks.contains(TASK_NAME));
        // verify pipeline has been created
        List<String> pipelines = tkn.getPipelines(NAMESPACE).stream().map(pp -> pp.getMetadata().getName()).collect(Collectors.toList());;
        assertTrue(pipelines.contains(PIPELINE_NAME));
        // verify pipelinerun has been created
        CompletableFuture<List<io.fabric8.tekton.pipeline.v1beta1.PipelineRun>> completableFuture = tkn.getClient(TektonClient.class).v1beta1().pipelineRuns().inNamespace(NAMESPACE).
                informOnCondition(pipelineRuns -> pipelineRuns.stream().anyMatch(pr -> !pr.getMetadata().getName().equals(PIPELINE_RUN_NAME)));
        completableFuture.join();
        try {
            tkn.cancelPipelineRun(NAMESPACE, PIPELINE_RUN_NAME); //pipelinerun may have already finished its execution
        } catch (IOException ignored) {}
        tkn.cancelPipelineRun(NAMESPACE, PIPELINE_RUN_NAME);
        // clean up and verify cleaning succeed
        tkn.deletePipelines(NAMESPACE, Arrays.asList(PIPELINE_NAME), true);
        pipelines = tkn.getPipelines(NAMESPACE).stream().map(pp -> pp.getMetadata().getName()).collect(Collectors.toList());;
        assertFalse(pipelines.contains(PIPELINE_NAME));
        List<PipelineRun> pipelineRuns = tkn.getPipelineRuns(NAMESPACE, PIPELINE_NAME);
        assertTrue(pipelineRuns.stream().noneMatch(run -> run.getName().equals(PIPELINE_RUN_NAME)));
    }

    @Test
    public void verifyStartPipelineCreateRuns() throws IOException, InterruptedException {
        String TASK_NAME = "add-task";
        String PIPELINE_NAME = "sum-three-pipeline";
        String taskConfig = TestUtils.load("start/add-task.yaml");
        String pipelineConfig = TestUtils.load("start/sum-pipeline.yaml");
        TestUtils.saveResource(tkn, taskConfig, NAMESPACE, "tasks");
        TestUtils.saveResource(tkn, pipelineConfig, NAMESPACE, "pipelines");

        // verify task has been created
        List<String> tasks = tkn.getTasks(NAMESPACE).stream().map(task -> task.getMetadata().getName()).collect(Collectors.toList());
        assertTrue(tasks.contains(TASK_NAME));
        // verify pipeline has been created
        List<String> pipelines = tkn.getPipelines(NAMESPACE).stream().map(pp -> pp.getMetadata().getName()).collect(Collectors.toList());;
        assertTrue(pipelines.contains(PIPELINE_NAME));
        // start pipeline and verify taskrun and pipelinerun has been created
        Map<String, Input> params = new HashMap<>();
        params.put("first", new Input("name", "string", Input.Kind.PARAMETER, "value", Optional.empty(), Optional.empty()));
        params.put("second", new Input("name2", "string", Input.Kind.PARAMETER, "value2", Optional.empty(), Optional.empty()));
        params.put("third", new Input("name3", "string", Input.Kind.PARAMETER, "value3", Optional.empty(), Optional.empty()));
        tkn.startPipeline(NAMESPACE, PIPELINE_NAME, params, Collections.emptyMap(), "", Collections.emptyMap(), Collections.emptyMap(), "");
        CompletableFuture<List<io.fabric8.tekton.pipeline.v1beta1.PipelineRun>> prCompletableFuture = tkn.getClient(TektonClient.class).v1beta1().pipelineRuns().inNamespace(NAMESPACE).
                informOnCondition(pipelineRuns -> pipelineRuns.stream().anyMatch(pr -> pr.getMetadata().getLabels().get("tekton.dev/pipeline").equals(PIPELINE_NAME)));
        CompletableFuture<List<TaskRun>> trCompletableFuture = tkn.getClient(TektonClient.class).v1beta1().taskRuns().inNamespace(NAMESPACE).
                informOnCondition(taskruns -> taskruns.stream().anyMatch(tr -> tr.getMetadata().getLabels().get("tekton.dev/pipeline").equals(PIPELINE_NAME)));
        List<io.fabric8.tekton.pipeline.v1beta1.PipelineRun> pipelineRunList = prCompletableFuture.join();
        trCompletableFuture.join();
        Optional<String> namePipelineRun = pipelineRunList.stream().filter(pr -> pr.getMetadata().getLabels().get("tekton.dev/pipeline").equals(PIPELINE_NAME))
                .map(pr -> pr.getMetadata().getName()).findFirst();
        assertTrue(namePipelineRun.isPresent());
        try {
            tkn.cancelPipelineRun(NAMESPACE, namePipelineRun.get()); //pipelinerun may have already finished its execution
        } catch(IOException ignored){}
        // clean up
        tkn.deletePipelines(NAMESPACE, Arrays.asList(PIPELINE_NAME), true);
        tkn.deleteTasks(NAMESPACE, Arrays.asList(TASK_NAME), false);
    }
}
