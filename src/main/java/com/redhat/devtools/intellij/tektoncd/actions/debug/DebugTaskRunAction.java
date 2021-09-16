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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DebugTaskRunAction extends MirrorStartAction {

    private static final Logger logger = LoggerFactory.getLogger(DebugTaskRunAction.class);
    private Watcher<Pod> watcher;

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
        String runAsYAML = YAMLHelper.JSONToYAML(run, false);
        String runName = DeployHelper.saveResource(runAsYAML, namespace, tkncli);
        DebugModel debugModel = new DebugModel(runName);
        UIHelper.executeInUI(() -> DebugPanelBuilder.instance(tkncli).addContent(debugModel));
        tkncli.watchPodsWithLabel(namespace, "tekton.dev/taskRun", runName, createWatcher(tkncli, debugModel));

        return null;
    }

    private Watcher<Pod> createWatcher(Tkn tkn, DebugModel debugModel) {
        watcher = new Watcher<Pod>() {
            @Override
            public void eventReceived(Action action, Pod resource) {
                debugModel.setPod(resource);
                PollingHelper.get().pollResource(tkn,
                        debugModel,
                        DebugTaskRunAction.this::isReadyForDebugging,
                        DebugTaskRunAction.this::updateDebugPanel);
                //this.onClose();
                //String t  ="";
            }

            @Override
            public void onClose(WatcherException cause) {

            }

        };
        return watcher;
    }

    private Pair<Boolean, DebugModel> isReadyForDebugging(Tkn tkn, DebugModel model) {
        Pod pod = model.getPod();
        List<ContainerStatus> containerStatuses = pod.getStatus().getContainerStatuses();
        for (ContainerStatus containerStatus: containerStatuses) {
            try {
                if (containerStatus.getStarted()
                        && containerStatus.getState().getRunning() != null
                        && tkn.isContainerStuckOnDebug(pod.getMetadata().getNamespace(), pod.getMetadata().getName(), containerStatus.getName())) {
                    model.setContainerId(containerStatus.getName());
                    model.setStep(containerStatus.getName());
                    model.setImage(containerStatus.getImage());
                    return Pair.create(true, model);
                }
            } catch (IOException e) {
                logger.warn(e.getLocalizedMessage(), e);
            }
        }

        return Pair.create(false, model);
    }

    private void updateDebugPanel(Tkn tkn, DebugModel model) {
        UIHelper.executeInUI(() -> DebugPanelBuilder.instance(tkn).addContent(model));
    }
}
