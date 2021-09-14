/*******************************************************************************
 * Copyright (c) 2021 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc.
 ******************************************************************************/
package com.redhat.devtools.intellij.tektoncd.actions.debug;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.redhat.devtools.intellij.common.utils.UIHelper;
import com.redhat.devtools.intellij.common.utils.YAMLHelper;
import com.redhat.devtools.intellij.tektoncd.actions.MirrorStartAction;
import com.redhat.devtools.intellij.tektoncd.actions.task.DebugModel;
import com.redhat.devtools.intellij.tektoncd.tkn.Tkn;
import com.redhat.devtools.intellij.tektoncd.tree.ParentableNode;
import com.redhat.devtools.intellij.tektoncd.tree.TaskRunNode;
import com.redhat.devtools.intellij.tektoncd.ui.toolwindow.debug.DebugPanelBuilder;
import com.redhat.devtools.intellij.tektoncd.utils.DeployHelper;
import com.redhat.devtools.intellij.tektoncd.utils.PollingHelper;
import com.redhat.devtools.intellij.tektoncd.utils.YAMLBuilder;
import com.redhat.devtools.intellij.tektoncd.utils.model.actions.StartResourceModel;
import io.fabric8.kubernetes.api.model.ContainerStatus;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.WatcherException;
import java.io.IOException;
import java.util.List;

public class DebugTaskRunAction extends MirrorStartAction {

    public DebugTaskRunAction() {
        super(TaskRunNode.class);
    }

    @Override
    protected boolean canBeStarted(Project project, ParentableNode element, StartResourceModel model) {
        return true;
    }

    /*@Override
    public void actionPerformed(AnActionEvent anActionEvent, TreePath path, Object selected, Tkn tkncli) {
        DebugModel debugModel = new DebugModel(null, "", "taskrun-98r");
        DebugPanelBuilder.instance().build(tkncli, debugModel);

        ExecHelper.submit(() -> {
            try {
                Thread.sleep(4000);
                DebugPanelBuilder.instance().update(tkncli.getProject());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }*/

    @Override
    protected String doStart(Tkn tkncli, String namespace, StartResourceModel model) throws IOException {

        ObjectNode run = YAMLBuilder.createRun(model, true);
        if (run == null) {
            throw new IOException("Unable to debug task" + model.getName());
        }
        String runAsYAML = YAMLHelper.JSONToYAML(run);
        String runName = DeployHelper.saveResource(runAsYAML, namespace, tkncli);
        DebugModel debugModel = new DebugModel(runName);
        UIHelper.executeInUI(() -> DebugPanelBuilder.instance().build(tkncli, debugModel));
        tkncli.watchPodsWithLabel(namespace, "tekton.dev/taskRun", runName, new Watcher<Pod>() {
            @Override
            public void eventReceived(Action action, Pod resource) {
                debugModel.setPod(resource);
                PollingHelper.get().pollResource(tkncli,
                        debugModel,
                        DebugTaskRunAction.this::isReadyForDebugging,
                        DebugTaskRunAction.this::openTerminalForDebugging);
                this.onClose();
                String t  ="";
            }

            @Override
            public void onClose(WatcherException cause) {

            }
        });

        return null;
    }

    private Pair<Boolean, String> isReadyForDebugging(Tkn tkn, Pod pod) {
        List<ContainerStatus> containerStatuses = pod.getStatus().getContainerStatuses();
        for (ContainerStatus containerStatus: containerStatuses) {
            try {
                if (containerStatus.getStarted()
                        && containerStatus.getState().getRunning() != null
                        && tkn.isContainerStuckOnDebug(pod.getMetadata().getNamespace(), pod.getMetadata().getName(), containerStatus.getName())) {
                    return Pair.create(true, containerStatus.getName());
                }
            } catch (IOException e) {
                //e.printStackTrace();
            }
        }

        return Pair.create(false, "");
    }

    private void openTerminalForDebugging(Tkn tkn, DebugModel model) {
        UIHelper.executeInUI(() -> DebugPanelBuilder.instance().update(tkn, model));
    }
}
