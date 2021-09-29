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
package com.redhat.devtools.intellij.tektoncd.utils;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.util.Pair;
import com.redhat.devtools.intellij.common.utils.ExecHelper;
import com.redhat.devtools.intellij.common.utils.UIHelper;
import com.redhat.devtools.intellij.tektoncd.tkn.Tkn;
import com.redhat.devtools.intellij.tektoncd.ui.toolwindow.debug.DebugPanelBuilder;
import com.redhat.devtools.intellij.tektoncd.utils.model.debug.DebugModel;
import com.redhat.devtools.intellij.tektoncd.utils.model.debug.DebugResourceState;
import io.fabric8.kubernetes.api.model.ContainerState;
import io.fabric8.kubernetes.api.model.ContainerStatus;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.WatcherException;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_POD;

public class DebugHelper {
    private static final Logger logger = LoggerFactory.getLogger(DebugHelper.class);

    public static void doDebugTaskRun(Tkn tkncli, String namespace, String nameTaskRun) {
        ExecHelper.submit(() -> {
            DebugModel debugModel = new DebugModel(nameTaskRun);
            UIHelper.executeInUI(() -> DebugPanelBuilder.instance(tkncli).addContent(debugModel));
            String keyLabel = "tekton.dev/taskRun";
            WatchHandler.get().setWatchByLabel(tkncli,
                    namespace,
                    KIND_POD,
                    keyLabel,
                    nameTaskRun,
                    createWatcher(tkncli, debugModel, keyLabel, nameTaskRun),
                    true);
        });
    }

    private static Watcher<Pod> createWatcher(Tkn tkn, DebugModel debugModel, String key, String value) {
        final boolean[] isFirst = {true};
        return new Watcher<Pod>() {
            @Override
            public void eventReceived(Action action, Pod resource) {
                if (isFirst[0]) {
                    isFirst[0] = false;
                    debugModel.setPod(resource);
                    PollingHelper.get().doPolling(tkn,
                            debugModel,
                            DebugHelper::isReadyForDebuggingAsync,
                            DebugHelper::updateDebugPanel);
                } else if (isPodCompleted(resource)) {
                    closeWatch(resource.getMetadata().getNamespace(), key, value);
                    debugModel.updateResourceStatus(isPodFailed(resource)
                            ? DebugResourceState.COMPLETE_FAILED
                            : DebugResourceState.COMPLETE_SUCCESS);
                    updateDebugPanel(tkn, debugModel);
                }
            }

            @Override
            public void onClose(WatcherException cause) {
                String e = cause.getLocalizedMessage();
            }

        };
    }

    public static boolean isPodCompleted(Pod pod) {
        return pod == null
                || isPodInPhase(pod, "Succeeded")
                || isPodInPhase(pod, "Failed")
                || isPodContainerFailed(pod);
    }

    public static boolean isPodContainerFailed(Pod pod) {
        if (pod != null
                && pod.getStatus() != null
                && pod.getStatus().getContainerStatuses() != null) {
            for (ContainerStatus containerStatus: pod.getStatus().getContainerStatuses()) {
                ContainerState state = containerStatus.getState();
                if (state.getWaiting() != null
                        && state.getWaiting().getReason().equalsIgnoreCase("CreateContainerConfigError")) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isPodInPhase(Pod pod, String phase) {
        if (pod != null
                && pod.getStatus() != null
                && pod.getStatus().getPhase() != null) {
            return pod.getStatus().getPhase().equalsIgnoreCase(phase);
        }
        return false;
    }

    public static boolean isPodFailed(Pod pod) {
        return isPodInPhase(pod, "Failed")
                || isPodContainerFailed(pod);
    }

    private static void closeWatch(String namespace, String key, String value) {
        WatchHandler.get().removeWatchByLabel(namespace, KIND_POD, key, value);
    }

    private static CompletableFuture<Pair<Boolean, DebugModel>> isReadyForDebuggingAsync(Tkn tkn, DebugModel model) {
        CompletableFuture<Pair<Boolean, DebugModel>> result = new CompletableFuture();
        ApplicationManager.getApplication().invokeLater(() -> result.complete(isReadyForDebugging(tkn, model)));
        return result;
    }

    private static Pair<Boolean, DebugModel> isReadyForDebugging(Tkn tkn, DebugModel model) {
        Pod pod = model.getPod();
        if (pod.getStatus().getPhase().equalsIgnoreCase("Failed")
                || pod.getStatus().getPhase().equalsIgnoreCase("Succeeded")) {
            return Pair.create(false, model);
        }

        List<ContainerStatus> containerStatuses = pod.getStatus().getContainerStatuses();
        for (int i=containerStatuses.size() - 1; i>=0; i--) {
            ContainerStatus containerStatus = containerStatuses.get(i);
            try {
                if (containerStatus.getStarted()
                        && containerStatus.getState().getRunning() != null
                        && !containerStatus.getName().equalsIgnoreCase(model.getStep())
                        && tkn.isContainerStoppedOnDebug(pod.getMetadata().getNamespace(), pod.getMetadata().getName(), containerStatus.getName(), pod)) {
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

    private static void updateDebugPanel(Tkn tkn, DebugModel model) {
        UIHelper.executeInUI(() -> DebugPanelBuilder.instance(tkn).addContent(model));
    }
}
