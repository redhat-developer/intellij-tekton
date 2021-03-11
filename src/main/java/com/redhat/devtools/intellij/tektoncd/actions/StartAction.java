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
package com.redhat.devtools.intellij.tektoncd.actions;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;
import com.redhat.devtools.intellij.common.utils.ExecHelper;
import com.redhat.devtools.intellij.common.utils.UIHelper;
import com.redhat.devtools.intellij.tektoncd.Constants;
import com.redhat.devtools.intellij.tektoncd.actions.logs.FollowLogsAction;
import com.redhat.devtools.intellij.tektoncd.settings.SettingsState;
import com.redhat.devtools.intellij.tektoncd.telemetry.TelemetryService;
import com.redhat.devtools.intellij.tektoncd.tkn.Resource;
import com.redhat.devtools.intellij.tektoncd.tkn.Run;
import com.redhat.devtools.intellij.tektoncd.tkn.Tkn;
import com.redhat.devtools.intellij.tektoncd.tkn.component.field.Workspace;
import com.redhat.devtools.intellij.tektoncd.tree.ClusterTaskNode;
import com.redhat.devtools.intellij.tektoncd.tree.ParentableNode;
import com.redhat.devtools.intellij.tektoncd.tree.PipelineNode;
import com.redhat.devtools.intellij.tektoncd.tree.PipelineRunNode;
import com.redhat.devtools.intellij.tektoncd.tree.TaskNode;
import com.redhat.devtools.intellij.tektoncd.tree.TaskRunNode;
import com.redhat.devtools.intellij.tektoncd.tree.TektonTreeStructure;
import com.redhat.devtools.intellij.tektoncd.ui.wizard.StartWizard;
import com.redhat.devtools.intellij.tektoncd.utils.model.actions.StartResourceModel;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit.http.HEAD;

import javax.swing.tree.TreePath;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_CLUSTERTASK;
import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_PIPELINE;
import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_TASK;
import static com.redhat.devtools.intellij.tektoncd.Constants.NOTIFICATION_ID;
import static com.redhat.devtools.intellij.telemetry.core.service.TelemetryMessageBuilder.ActionMessage;
import static com.redhat.devtools.intellij.telemetry.core.util.AnonymizeUtils.anonymizeResource;

public class StartAction extends TektonAction {

    private static final Logger logger = LoggerFactory.getLogger(StartAction.class);

    protected final ActionMessage telemetry = TelemetryService.instance().action("start");

    public StartAction(Class... filters) {
        super(filters);
    }

    public StartAction() {
        this(PipelineNode.class, TaskNode.class, ClusterTaskNode.class);
    }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent, TreePath path, Object selected, Tkn tkncli) {
        ParentableNode element = getElement(selected);
        String namespace = element.getNamespace();
        ExecHelper.submit(() -> {
            Notification notification;
            StartResourceModel model = createModel(tkncli, element, namespace);
            if (model == null) return;
            if (!model.isValid()) {
                UIHelper.executeInUI(() -> Messages.showErrorDialog(model.getErrorMessage(), "Error"));
                return;
            }

            boolean hasNoInputs = model.getParams().isEmpty()
                                    && model.getInputResources().isEmpty()
                                    && model.getOutputResources().isEmpty()
                                    && model.getWorkspaces().isEmpty();
            boolean showWizard = SettingsState.getInstance().showStartWizardWithNoInputs || !hasNoInputs;

            StartWizard startWizard = null;
            if (showWizard) {
                startWizard = UIHelper.executeInUI(() -> {
                    String titleDialog = ((element instanceof PipelineNode)? "Pipeline " : "Task ") + element.getName();
                    StartWizard wizard = new StartWizard(titleDialog, element, getEventProject(anActionEvent), model);
                    wizard.show();
                    return wizard;
                });
            }

            if (!showWizard || (startWizard != null && startWizard.isOK())) {
                try {
                    String serviceAccount = model.getServiceAccount();
                    Map<String, String> taskServiceAccount = model.getTaskServiceAccounts();
                    Map<String, String> params = model.getParams().stream().collect(Collectors.toMap(param -> param.name(), param -> param.value()));
                    Map<String, Workspace> workspaces = model.getWorkspaces();
                    Map<String, String> inputResources = model.getInputResources().stream().collect(Collectors.toMap(input -> input.name(), input -> input.value()));
                    Map<String, String> outputResources = model.getOutputResources().stream().collect(Collectors.toMap(output -> output.name(), output -> output.value()));
                    String runPrefixName = model.getRunPrefixName();
                    telemetry.property(TelemetryService.PROP_RESOURCE_KIND, model.getKind());
                    String runName = start(tkncli, namespace, model, serviceAccount, taskServiceAccount, params, workspaces, inputResources, outputResources, runPrefixName);
                    executeFollowLogsAction(tkncli, element, namespace, runName);
                    telemetry.success().send();
                    refreshTreeNode(anActionEvent, element);
                } catch (IOException e) {
                    String errorMessage = model.getName() + " in namespace " + namespace + " failed to start\n" + e.getLocalizedMessage();
                    telemetry.error(anonymizeResource(element.getName(), namespace, errorMessage)).send();
                    notification = new Notification(NOTIFICATION_ID,
                            "Error",
                            errorMessage,
                            NotificationType.ERROR);
                    Notifications.Bus.notify(notification);
                    logger.warn("Error: " + e.getLocalizedMessage());
                }
            }
        });
    }

    private StartResourceModel createModel(Tkn tkncli, ParentableNode element, String namespace) {
        StartResourceModel model = null;
        try {
            List<Resource> resources = tkncli.getResources(namespace);
            List<String> serviceAccounts = tkncli.getServiceAccounts(namespace);
            List<String> secrets = tkncli.getSecrets(namespace);
            List<String> configMaps = tkncli.getConfigMaps(namespace);
            List<String> persistentVolumeClaims = tkncli.getPersistentVolumeClaim(namespace);
            model = createModel(element, namespace, tkncli, resources, serviceAccounts, secrets, configMaps, persistentVolumeClaims);
        } catch (IOException e) {
            UIHelper.executeInUI(() ->
                    Messages.showErrorDialog(
                            element.getName() + " in namespace " + namespace + " failed to start. An error occurred while retrieving information.\n" + e.getLocalizedMessage(),
                            "Error"));
            logger.warn("Error: " + e.getLocalizedMessage());
        }
        return model;
    }

    protected StartResourceModel createModel(ParentableNode element, String namespace, Tkn tkncli, List<Resource> resources, List<String> serviceAccounts, List<String> secrets, List<String> configMaps, List<String> persistentVolumeClaims) throws IOException {
        String configuration = "";
        List<? extends Run> runs = new ArrayList<>();
        if (element instanceof PipelineNode) {
            telemetry.property(TelemetryService.PROP_RESOURCE_KIND, KIND_PIPELINE);
            configuration = tkncli.getPipelineYAML(namespace, element.getName());
            runs = tkncli.getPipelineRuns(namespace, element.getName());
        } else if (element instanceof TaskNode) {
            telemetry.property(TelemetryService.PROP_RESOURCE_KIND, KIND_TASK);
            configuration = tkncli.getTaskYAML(namespace, element.getName());
            runs = tkncli.getTaskRuns(namespace, element.getName());
        } else if (element instanceof ClusterTaskNode) {
            telemetry.property(TelemetryService.PROP_RESOURCE_KIND, KIND_CLUSTERTASK);
            configuration = tkncli.getClusterTaskYAML(element.getName());
        }
        return new StartResourceModel(configuration, resources, serviceAccounts, secrets, configMaps, persistentVolumeClaims, runs);
    }

    private void refreshTreeNode(AnActionEvent anActionEvent, ParentableNode element) {
        ParentableNode nodeToRefresh = element;
        if (element instanceof PipelineRunNode || element instanceof TaskRunNode) {
            nodeToRefresh = (ParentableNode) element.getParent();
        }
        ((TektonTreeStructure)getTree(anActionEvent).getClientProperty(Constants.STRUCTURE_PROPERTY)).fireModified(nodeToRefresh);
    }

    private void executeFollowLogsAction(Tkn tkncli, ParentableNode element, String namespace, String runName) {
        if(runName != null) {
            FollowLogsAction followLogsAction = (FollowLogsAction) ActionManager.getInstance().getAction("FollowLogsAction");
            followLogsAction.actionPerformed(namespace, runName, element.getClass(), tkncli);
        }
    }

    private String start(Tkn tkncli, String namespace, StartResourceModel model, String serviceAccount, Map<String, String> taskServiceAccount, Map<String, String> params, Map<String, Workspace> workspaces, Map<String, String> inputResources, Map<String, String> outputResources, String runPrefixName) throws IOException {
        String runName = null;
        if (model.getKind().equalsIgnoreCase(KIND_PIPELINE)) {
            runName = tkncli.startPipeline(namespace, model.getName(), params, inputResources, serviceAccount, taskServiceAccount, workspaces, runPrefixName);
        } else if (model.getKind().equalsIgnoreCase(KIND_TASK)) {
            runName = tkncli.startTask(namespace, model.getName(), params, inputResources, outputResources, serviceAccount, workspaces, runPrefixName);
        } else if (model.getKind().equalsIgnoreCase(KIND_CLUSTERTASK)) {
            runName = tkncli.startClusterTask(namespace, model.getName(), params, inputResources, outputResources, serviceAccount, workspaces, runPrefixName);
        }
        return runName;
    }
}
