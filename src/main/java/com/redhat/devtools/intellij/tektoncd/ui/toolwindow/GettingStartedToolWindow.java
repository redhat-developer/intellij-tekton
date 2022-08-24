/*******************************************************************************
 *  Copyright (c) 2022 Red Hat, Inc.
 *  Distributed under license by Red Hat, Inc. All rights reserved.
 *  This program is made available under the terms of the
 *  Eclipse Public License v2.0 which accompanies this distribution,
 *  and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 *  Contributors:
 *  Red Hat, Inc.
 ******************************************************************************/
package com.redhat.devtools.intellij.tektoncd.ui.toolwindow;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.openapi.wm.ex.ToolWindowManagerListener;
import com.intellij.openapi.wm.impl.ToolWindowManagerImpl;
import com.redhat.devtools.intellij.common.gettingstarted.GettingStartedContent;
import com.redhat.devtools.intellij.common.gettingstarted.GettingStartedCourse;
import com.redhat.devtools.intellij.common.gettingstarted.GettingStartedCourseBuilder;
import com.redhat.devtools.intellij.common.gettingstarted.GettingStartedGroupLessons;
import com.redhat.devtools.intellij.common.gettingstarted.GettingStartedLesson;
import com.redhat.devtools.intellij.tektoncd.settings.SettingsState;
import org.jetbrains.annotations.NotNull;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;

public class GettingStartedToolWindow implements ToolWindowFactory {
    private GettingStartedCourse course;
    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        toolWindow.setIcon(AllIcons.Toolwindows.Documentation);
        toolWindow.setStripeTitle("Getting Started");
        ((ToolWindowManagerImpl) ToolWindowManager.getInstance(project)).addToolWindowManagerListener(new ToolWindowManagerListener() {
            @Override
            public void stateChanged(@NotNull ToolWindowManager toolWindowManager) {
                if (hasToShowToolWindow()) {
                    toolWindow.show();
                }
            }
        });
    }

    private boolean hasToShowToolWindow() {
        String version = course.getVersion();
        if (SettingsState.getInstance().courseVersion.equals(version)) {
            return false;
        }
        SettingsState.getInstance().courseVersion = version;
        return true;
    }

    @Override
    public void init(@NotNull ToolWindow toolWindow) {
        final String version = "1.0";
        course = new GettingStartedCourseBuilder()
                .createGettingStartedCourse(
                        version,
                        "Learn IDE Features for Tekton",
                        "Start creating, running, and managing your Tekton pipelines on OpenShift and Kubernetes",
                        getFeedbackURL())
                .withGroupLessons(buildMainTektonLessons())
                .withGroupLessons(buildTektonCodeCompletionLessons())
                .build();
        GettingStartedContent content = new GettingStartedContent(toolWindow, "", course);
        toolWindow.getContentManager().addContent(content);
    }

    private URL getFeedbackURL() {
        URL feedbackUrl = null;
        try {
            feedbackUrl = new URL("https://github.com/redhat-developer/intellij-tekton");
        } catch (MalformedURLException ignored) { }
        return feedbackUrl;
    }

    @Override
    public boolean shouldBeAvailable(@NotNull Project project) {
        return true;
    }

    private GettingStartedGroupLessons buildMainTektonLessons() {
        URL gifTreeLesson = getGeneralLessonGif("supportPipelinesAndTriggers.gif");
        GettingStartedLesson supportPipelineAndTriggerLesson = new GettingStartedLesson(
                "Support for Tekton Configurations, Pipelines and Triggers",
                "<html><p>Based on the Tekton components installed and the configuration of the active cluster " +
                        "which the plugin is connected to, the tree of the Tekton tool window shows the corresponding " +
                        "nodes and all allowed actions by using the context menu (right-click on a tree node).</p>" +
                        "<p>The plugin is able to detect remotely two components: the Pipelines component which enables " +
                        "the Pipelines, Tasks and *Runs resources tree nodes and the Triggers component give access to " +
                        "EventListeners, TriggerTemplates and TriggerBindings nodes.</p>" +
                        "<p>The Configurations node is always visible and allows to check the current Tekton settings " +
                        "in read-only mode</p></html>",
                Collections.emptyList(),
                gifTreeLesson
        );

        URL gifNewFuncLesson = getGeneralLessonGif("createPipeline.gif");
        GettingStartedLesson createPipelineLesson = new GettingStartedLesson(
                "Create new resource",
                "<html><p>The plugin provides a smart yaml-code completion support which make it easy to create " +
                        "any Tekton resource by using the IDE editor.</p>" +
                        "<p>By executing the `New <resource>` action a new editor tab opens up with a snippet " +
                        "for that specific resource. Right-click on a Kind node (e.g Pipelines) in the tree and select " +
                        "the New action (e.g `New pipeline`) in the context menu. " +
                        "Once you have done editing it, click the Push action in the top toolbar or in the " +
                        "notification bar.</p></html>",
                Collections.emptyList(),
                gifNewFuncLesson
        );

        URL gifBuildFuncLesson = getGeneralLessonGif("startPipeline.gif");
        GettingStartedLesson startPipelineLesson = new GettingStartedLesson(
                "Start a pipeline/task",
                "<html>" +
                        "<p>After having created/edited your pipeline/task and successfully pushed it on cluster, you can " +
                        "run it by executing the `Start Pipeline/Task` action. " +
                        "A wizard will pop up leading you through the process of setting all inputs necessary to " +
                        "get it started (params, workspaces, service accounts, ..). " +
                        "<p>N.B: By default if the pipeline/task has no inputs, it will get started directly " +
                        "without the wizard popping up. You can change this behavior by updating the plugin's settings " +
                        "(File -> Settings -> Tools -> Tekton Pipelines By Red Hat). </p>" +
                        "<p>If you already run a pipeline/task earlier and wants to run it again with the same inputs " +
                        "you did last time, you can achieve it by executing the `Start Last Run` action from the " +
                        "Pipelines/Tasks context menu nodes</p>" +
                        "<p>Opposite, if you want to start a pipeline/task with the same inputs of a specific old run " +
                        "(not the latest), you can do it by selecting the old pipelinerun/taskrun node and execute the " +
                        "`Mirror Start` action. This will open up the wizard pre-filled with the inputs used to start " +
                        "that pipeline/task in the past</p>" +
                        "</html>",
                Collections.emptyList(),
                gifBuildFuncLesson
        );

        URL gifDeployFuncLesson = getGeneralLessonGif("logsDiagnostic.gif");
        GettingStartedLesson logsLesson = new GettingStartedLesson(
                "Logs and diagnostic data",
                "<html><p>Logs are displayed automatically after a pipeline or a task starts and/or can be " +
                        "retrieved by using the `Show Logs` and `Follow Logs` actions.</p>" +
                        "<p>In case logs are not enough to identify the issue or they are not printed out at all " +
                        "(e.g a pvc with a size larger than the one supported by the cluster is needed and the pipelinerun/" +
                        "taskrun fails at starting) you can fetch additional data from the resources created by the operator " +
                        "(pods, pvc, ..). " +
                        "To do this, just execute the `Show Diagnostic Data` action available when " +
                        "right clicking a pipelinerun or taskrun node.</p></html>",
                Collections.emptyList(),
                gifDeployFuncLesson
        );

        URL gifRunFuncLesson = getGeneralLessonGif("debugTekton.gif");
        GettingStartedLesson debugLesson = new GettingStartedLesson(
                "Debug in Tekton",
                "<html><p>When a task fails its execution and looking at the logs it is not enough to identify " +
                        "the issue, you can think at starting it in debug mode. This way the container where the taskrun " +
                        "is executed will be kept running even when the run fails. A terminal connected to the container " +
                        "will be opened in the IDE allowing you to interact with it and identify any possible misbehavior/error " +
                        "in the environment (e.g some dependencies not downloaded correctly in the previous steps).</p>" +
                        "<p>`Start in Debug mode` action is enabled and visible for Task and TaskRun nodes if and only if the " +
                        "version of the Tekton operator installed on cluster is greater than 0.26.0 and its alpha mode is enabled. " +
                        "To verify it, check that the `enable-api-fields` property is set to `alpha` in the Features configuration " +
                        "file.</p></html>",
                Collections.emptyList(),
                gifRunFuncLesson
        );

        URL gifTektonHubLesson = getGeneralLessonGif("tektonHub.gif");
        GettingStartedLesson tektonHubLesson = new GettingStartedLesson(
                "Tekton Hub",
                "<html><p>The plugin provides a direct access to the Tekton Hub to search and import reusable " +
                        "pipelines and tasks from the IDE.</p>" +
                        "<p>The Recommended view suggests pipelines and tasks based on the active project" +
                        "opened in JetBrains.</p>" +
                        "<p>Each resource has its own version and the newest one is always installed by default. " +
                        "It is also possible to import an older version by selecting it in the Hub wizard when " +
                        "clicking on the hub resource name you want to install. " +
                        "</p></html>",
                Collections.emptyList(),
                gifTektonHubLesson
        );

        return new GettingStartedGroupLessons(
                "Tekton Pipelines and Triggers",
                "Create, deploy and manage your Tekton resources without leaving your preferred IDE",
                supportPipelineAndTriggerLesson,
                createPipelineLesson,
                startPipelineLesson,
                logsLesson,
                debugLesson,
                tektonHubLesson);
    }

    private GettingStartedGroupLessons buildTektonCodeCompletionLessons() {
        URL gifTreeLesson = getCodeCompletionLessonGif("taskRef.gif");
        GettingStartedLesson taskRefLesson = new GettingStartedLesson(
                "List all tasks for taskRef field",
                "<html><p>When creating or editing a pipeline step, you can make the plugin import all params, " +
                        "workspaces and resources of a task without typing them by hand.</p>" +
                        "<p>In the pipeline yaml type the `name:` keyword below the `taskRef:` section. " +
                        "A list of all tasks will be shown and by selecting one of them, the pipeline will be filled accordingly. " +
                        "If a task input has a default value it will also be imported in the pipeline." +
                        "</p></html>",
                Collections.emptyList(),
                gifTreeLesson
        );

        URL gifNewFuncLesson = getCodeCompletionLessonGif("runAfter.gif");
        GettingStartedLesson runAfterLesson = new GettingStartedLesson(
                "List all tasks in current pipeline for runAfter field",
                "<html><p>When adding a runAfter clause to a step, the plugin provides the list of all tasks " +
                        "available in the pipeline to select the ones needed.</p>" +
                        "</html>",
                Collections.emptyList(),
                gifNewFuncLesson
        );

        URL gifBuildFuncLesson = getCodeCompletionLessonGif("inputCompletion.gif");
        GettingStartedLesson inputsCompletionLesson = new GettingStartedLesson(
                "Suggest inputs for step resources",
                "<html>" +
                        "<p>When filling the params, workspaces, resources field in a task step, the plugin shows up the " +
                        "list of pipeline inputs that can be used within the step.</p>" +
                        "<p>Also the when clause and the old conditions CRD are supported</p>" +
                        "</html>",
                Collections.emptyList(),
                gifBuildFuncLesson
        );

        return new GettingStartedGroupLessons(
                "Tekton Code Completion",
                "Creating or editing a Tekton resource is made easier",
                taskRefLesson,
                runAfterLesson,
                inputsCompletionLesson);
    }

    private URL getGeneralLessonGif(String name) {
        return getLessonGif("tekton-general/" + name);
    }

    private URL getCodeCompletionLessonGif(String name) {
        return getLessonGif("tekton-codecompletion/" + name);
    }

    private URL getLessonGif(String name) {
        return GettingStartedToolWindow.class.getResource("/gettingstarted/" + name);
    }
}
