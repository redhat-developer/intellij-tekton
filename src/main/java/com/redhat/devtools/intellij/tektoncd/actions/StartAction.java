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

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.redhat.devtools.intellij.common.utils.ExecHelper;
import com.redhat.devtools.intellij.common.utils.UIHelper;
import com.redhat.devtools.intellij.common.utils.YAMLHelper;
import com.redhat.devtools.intellij.tektoncd.Constants;
import com.redhat.devtools.intellij.tektoncd.actions.logs.FollowLogsAction;
import com.redhat.devtools.intellij.tektoncd.settings.SettingsState;
import com.redhat.devtools.intellij.tektoncd.telemetry.TelemetryService;
import com.redhat.devtools.intellij.tektoncd.tkn.Resource;
import com.redhat.devtools.intellij.tektoncd.tkn.Tkn;
import com.redhat.devtools.intellij.tektoncd.tkn.component.field.Input;
import com.redhat.devtools.intellij.tektoncd.tkn.component.field.Output;
import com.redhat.devtools.intellij.tektoncd.tkn.component.field.Workspace;
import com.redhat.devtools.intellij.tektoncd.tree.ClusterTaskNode;
import com.redhat.devtools.intellij.tektoncd.tree.ParentableNode;
import com.redhat.devtools.intellij.tektoncd.tree.PipelineNode;
import com.redhat.devtools.intellij.tektoncd.tree.PipelineRunNode;
import com.redhat.devtools.intellij.tektoncd.tree.TaskNode;
import com.redhat.devtools.intellij.tektoncd.tree.TaskRunNode;
import com.redhat.devtools.intellij.tektoncd.tree.TektonTreeStructure;
import com.redhat.devtools.intellij.tektoncd.ui.wizard.StartWizard;
import com.redhat.devtools.intellij.tektoncd.utils.VirtualFileHelper;
import com.redhat.devtools.intellij.tektoncd.utils.YAMLBuilder;
import com.redhat.devtools.intellij.tektoncd.utils.model.actions.StartResourceModel;
import io.fabric8.kubernetes.api.model.HasMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import static com.redhat.devtools.intellij.tektoncd.telemetry.TelemetryService.NAME_PREFIX_START_STOP;
import static com.redhat.devtools.intellij.tektoncd.telemetry.TelemetryService.PROP_RESOURCE_KIND;
import static com.redhat.devtools.intellij.tektoncd.telemetry.TelemetryService.VALUE_ABORTED;
import static com.redhat.devtools.intellij.telemetry.core.service.TelemetryMessageBuilder.ActionMessage;
import static com.redhat.devtools.intellij.telemetry.core.util.AnonymizeUtils.anonymizeResource;

public class StartAction extends TektonAction {
    private static final Logger logger = LoggerFactory.getLogger(StartAction.class);

    protected ActionMessage telemetry;

    public StartAction(Class... filters) { super(filters); }

    public StartAction() { super(PipelineNode.class, TaskNode.class, ClusterTaskNode.class); }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent, TreePath path, Object selected, Tkn tkncli) {
        this.telemetry = createTelemetry();
        ParentableNode element = getElement(selected);
        String namespace = element.getNamespace();
        ExecHelper.submit(() -> {
            StartResourceModel model = createModel(tkncli, element, namespace);
            if (model == null) return;
            telemetry.property(PROP_RESOURCE_KIND, model.getKind());
            if (!model.isValid()) {
                telemetry
                        .error(model.getErrorMessage())
                        .send();
                UIHelper.executeInUI(() -> Messages.showErrorDialog(model.getErrorMessage(), "Error"));
                return;
            }

            Project project = getEventProject(anActionEvent);
            if (!canBeStarted(project, element, model)) {
                return;
            }

            try {
                createNewVolumes(model.getWorkspaces(), tkncli);
                String runName = doStart(tkncli, namespace, model);
                FollowLogsAction.run(namespace, runName, element.getClass(), tkncli);
                refreshTreeNode(anActionEvent, element);
                telemetry.send();
            } catch (IOException e) {
                String errorMessage = model.getName() + " in namespace " + namespace + " failed to start\n" + e.getLocalizedMessage();
                telemetry
                        .error(anonymizeResource(element.getName(), namespace, errorMessage))
                        .send();
                Notification notification = new Notification(NOTIFICATION_ID,
                        "Error",
                        errorMessage,
                        NotificationType.ERROR);
                Notifications.Bus.notify(notification);
                logger.warn("Error: " + e.getLocalizedMessage(), e);
            }
        });
    }

    protected boolean canBeStarted(Project project, ParentableNode element, StartResourceModel model) {
        boolean hasNoInputs = model.getParams().isEmpty()
                && model.getInputResources().isEmpty()
                && model.getOutputResources().isEmpty()
                && model.getWorkspaces().isEmpty();
        boolean showWizard = SettingsState.getInstance().showStartWizardWithNoInputs || !hasNoInputs;

        if (showWizard) {
            StartWizard startWizard = UIHelper.executeInUI(() -> {
                String titleDialog = ((element instanceof PipelineNode) ? "Pipeline " : "Task ") + element.getName();
                StartWizard wizard = new StartWizard(titleDialog, element, project, model);
                wizard.show();
                return wizard;
            });
            if (startWizard != null && !startWizard.isOK()) {
                telemetry
                        .result(VALUE_ABORTED)
                        .send();
                return false;
            }
        }

        return true;
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
            String errorMessage = element.getName() + " in namespace " + namespace + " failed to start. An error occurred while retrieving information.\n" + e.getLocalizedMessage();
            UIHelper.executeInUI(() -> {
                telemetry
                        .error(anonymizeResource(element.getName(), namespace, errorMessage))
                        .send();
                Messages.showErrorDialog(
                        errorMessage,
                        "Error");
            });
            logger.warn("Error: " + errorMessage, e);
        }
        return model;
    }

    protected StartResourceModel createModel(ParentableNode element, String namespace, Tkn tkncli, List<Resource> resources, List<String> serviceAccounts, List<String> secrets, List<String> configMaps, List<String> persistentVolumeClaims) throws IOException {
        String configuration = "";
        List<? extends HasMetadata> runs = new ArrayList<>();
        if (element instanceof PipelineNode) {
            telemetry.property(PROP_RESOURCE_KIND, KIND_PIPELINE);
            configuration = tkncli.getPipelineYAML(namespace, element.getName());
            runs = tkncli.getPipelineRuns(namespace, element.getName());
        } else if (element instanceof TaskNode) {
            telemetry.property(PROP_RESOURCE_KIND, KIND_TASK);
            configuration = tkncli.getTaskYAML(namespace, element.getName());
            runs = tkncli.getTaskRuns(namespace, element.getName());
        } else if (element instanceof ClusterTaskNode) {
            telemetry.property(PROP_RESOURCE_KIND, KIND_CLUSTERTASK);
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

    protected String doStart(Tkn tkncli, String namespace, StartResourceModel model) throws IOException {
        String serviceAccount = model.getServiceAccount();
        Map<String, String> taskServiceAccount = model.getTaskServiceAccounts();
        Map<String, Input> params = model.getParams().stream().collect(Collectors.toMap(Input::name, param -> param));
        Map<String, Workspace> workspaces = model.getWorkspaces();
        Map<String, String> inputResources = model.getInputResources().stream().collect(Collectors.toMap(Input::name, Input::value));
        Map<String, String> outputResources = model.getOutputResources().stream().collect(Collectors.toMap(Output::name, Output::value));
        String runPrefixName = model.getRunPrefixName();
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

    private void createNewVolumes(Map<String, Workspace> workspaces, Tkn tkn) throws IOException{
        int counter = 0;
        for(Map.Entry<String, Workspace> entry: workspaces.entrySet()) {
            Workspace workspace = entry.getValue();
            if (workspace != null
                    && workspace.getKind() == Workspace.Kind.PVC
                    && workspace.getItems().size() > 0) {
                if (!workspace.getResource().isEmpty()) {
                    String name = createNewPVC(workspace.getItems(), tkn);
                    workspace.setResource(name);
                } else {
                    VirtualFile vf = createVCT(workspace.getItems(), counter);
                    workspace.getItems().put("file", vf.getPath());
                    counter++;
                }
            }
        }
    }

    private String createNewPVC(Map<String, String> items, Tkn tkn) throws IOException {
        String name = items.get("name");
        tkn.createPVC(name, items.get("accessMode"), items.get("size"), items.get("unit"));
        return name;
    }

    private VirtualFile createVCT(Map<String, String> items, int counter) throws IOException {
        ObjectNode newVCT = YAMLBuilder.createVCT(items.get("name"), items.get("accessMode"), items.get("size"), items.get("unit"));
        return VirtualFileHelper.createVirtualFile("vct-" + counter + ".yaml", YAMLHelper.JSONToYAML(newVCT), false);
    }


    protected ActionMessage createTelemetry() {
         return TelemetryService.instance().action(NAME_PREFIX_START_STOP + "start");
    }
}
