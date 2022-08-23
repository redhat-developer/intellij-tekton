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
        URL gifTreeLesson = getLessonGif("placeholder.gif");
        GettingStartedLesson supportPipelineAndTriggerLesson = new GettingStartedLesson(
                "Support for Tekton Configurations, Pipelines and Triggers",
                "<html><p>Based on the Tekton components installed within the active cluster the user is working on, " +
                        "the plugin shows the corresponding nodes and actions in the tree.</p>" +
                        "<p>The installed Pipelines component gives access to the Pipelines, Tasks and *Runs resources, the Triggers " +
                        "component to EventListeners, TriggerTemplates and TriggerBindings.</p>" +
                        "<p>The Configurations node is always visible and allows to check the current settings in read-only mode</p></html>",
                Collections.emptyList(),
                gifTreeLesson
        );

        URL gifNewFuncLesson = getLessonGif("placeholder.gif");
        GettingStartedLesson createPipelineLesson = new GettingStartedLesson(
                "Create new resource",
                "<html><p>The plugin provides a smart yaml-code completion support which make it easy to create " +
                        "any Tekton resource within the IDE's editor.</p>" +
                        "<p>By executing the `New <resourceName>` action a new editor tab opens up with a snippet " +
                        "for that resource. E.g. right-click the Pipelines node in the tree and select `New pipeline` in the " +
                        "context menu </p></html>",
                Collections.emptyList(),
                gifNewFuncLesson
        );

        URL gifBuildFuncLesson = getLessonGif("placeholder.gif");
        GettingStartedLesson startPipelineLesson = new GettingStartedLesson(
                "Start a pipeline/task",
                "<html>" +
                        "<p>After having created/edited your pipeline/task you can start it by executing the `Start Pipeline/Task` " +
                        "action.</p>" +
                        "<p>A wizard will pop up leading you through the process of setting all inputs necessary to " +
                        "get it started (params, workspaces, service accounts, ..). " +
                        "By default if the pipeline/task has no inputs, it will get started directly " +
                        "without the wizard step. You can change this behavior by updating the plugin's settings. </p>" +
                        "<p>If you already run a pipeline/task earlier and wants to run it again with the same inputs " +
                        "you did last time, you can achieve it by executing the `Start Last Run` action from the " +
                        "Pipelines/Tasks context menu nodes</p>" +
                        "<p>Opposite if you want to start a pipeline/task with the same inputs of an old run, you can do " +
                        "it by selecting the old pipelinerun/taskrun node and execute the `Mirror Start` action. " +
                        "This will open up the wizard pre-filled with the inputs used to start that pipeline/task in the past</p>" +
                        "</html>",
                Collections.emptyList(),
                gifBuildFuncLesson
        );

        URL gifDeployFuncLesson = getLessonGif("placeholder.gif");
        GettingStartedLesson logsLesson = new GettingStartedLesson(
                "Logs and diagnostic data",
                "<html><p>Logs are displayed automatically after a pipeline or a task gets started and/or can be retrieved " +
                        "by using the `Show Logs` and `Follow Logs` actions</p>" +
                        "<p>However, there are situations when logs are not printed out after starting a pipeline/task " +
                        "(e.g when creating a pvc with a size larger than the one supported by the cluster) and the only" +
                        " way to find infos about the failure is to fetch them from resources created by the run itself " +
                        "(pods, pvc, ..). To do this, you can use the `Show Diagnostic Data` action available when " +
                        "right clicking a pipelinerun or taskrun node.</p></html>",
                Collections.emptyList(),
                gifDeployFuncLesson
        );

        URL gifRunFuncLesson = getLessonGif("placeholder.gif");
        GettingStartedLesson debugLesson = new GettingStartedLesson(
                "Debug in Tekton",
                "<html><p>When a task fails its execution and looking at the logs it is not enough to identify " +
                        "the issue, you can think at starting it in debug mode. This way the container where the taskrun " +
                        "is executed will be kept running even when the run fails. A terminal connected to the container " +
                        "will be opened in the IDE so you can interact with it and identify any possible misbehavior/error.</p>" +
                        "<p>To use the debug feature, you must have access to a Kubernetes or OpenShift cluster with Tekton " +
                        "installed with a version greater than 0.26.0 and enable alpha mode -> " +
                        "`enable-api-fields: alpha` in the Features configuration file.</p>" +
                        "<p>N.B: Debugging is only available for tasks</p></html>",
                Collections.emptyList(),
                gifRunFuncLesson
        );

        URL gifTektonHubLesson = getLessonGif("placeholder.gif");
        GettingStartedLesson tektonHubLesson = new GettingStartedLesson(
                "Tekton Hub",
                "<html><p>The plugin provides a direct access to the Tekton Hub to search and import reusable pipelines and " +
                        "tasks from the IDE.</p>" +
                        "<p>The Recommended view suggests pipelines and tasks based on the active project" +
                        "opened in the IDE.</p>" +
                        "<p>Each resource has its own version and the newest one is always installed by default. " +
                        "It is also possible to import an older version by selecting it in the Hub wizard. " +
                        "</p></html>",
                Collections.emptyList(),
                gifTektonHubLesson
        );

        GettingStartedGroupLessons groupLessons = new GettingStartedGroupLessons(
                "Tekton Pipelines and Triggers",
                "Create, deploy and manage your Tekton resources without leaving your preferred IDE",
                supportPipelineAndTriggerLesson,
                createPipelineLesson,
                startPipelineLesson,
                logsLesson,
                debugLesson,
                tektonHubLesson);
        return groupLessons;
    }

    private GettingStartedGroupLessons buildTektonCodeCompletionLessons() {
        URL gifTreeLesson = getLessonGif("placeholder.gif");
        GettingStartedLesson taskRefLesson = new GettingStartedLesson(
                "List all tasks for taskRef field",
                "<html><p>When creating or editing a pipeline step, you can make the plugin import all params, workspaces and " +
                        "resources of a task without typing them by hand.</p>" +
                        "<p>In the pipeline yaml type the `name:` keyword below the `taskRef:` section. " +
                        "A list of all tasks will be shown and by selecting one of them, the pipeline will be filled accordingly. " +
                        "If a task input has a default value it will be copied in the pipeline." +
                        "</p></html>",
                Collections.emptyList(),
                gifTreeLesson
        );

        URL gifNewFuncLesson = getLessonGif("placeholder.gif");
        GettingStartedLesson runAfterLesson = new GettingStartedLesson(
                "List all tasks in current pipeline for runAfter field",
                "<html><p>When adding a runAfter clause to a step, the plugin provides the list of all tasks" +
                        "available in the pipeline to select the ones wanted easily.</p>" +
                        "</html>",
                Collections.emptyList(),
                gifNewFuncLesson
        );

        URL gifBuildFuncLesson = getLessonGif("placeholder.gif");
        GettingStartedLesson inputsCompletionLesson = new GettingStartedLesson(
                "Suggest inputs for step resources",
                "<html>" +
                        "<p>When filling the params, workspaces, resources needed by a task step, the plugin shows up the " +
                        "list of pipeline inputs that can be used within the step.</p>" +
                        "<p>Also the when clause and the old conditions CRD are supported</p>" +
                        "</html>",
                Collections.emptyList(),
                gifBuildFuncLesson
        );

        GettingStartedGroupLessons groupLessons = new GettingStartedGroupLessons(
                "Tekton Code Completion",
                "Creating or editing a Tekton resource is made easier",
                taskRefLesson,
                runAfterLesson,
                inputsCompletionLesson);
        return groupLessons;
    }

    private URL getLessonGif(String name) {
        return GettingStartedToolWindow.class.getResource("/gettingstarted/tekton-general/" + name);
    }
}
