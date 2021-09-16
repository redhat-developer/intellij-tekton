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

import com.intellij.openapi.util.Pair;
import com.redhat.devtools.intellij.tektoncd.utils.model.debug.DebugModel;
import com.redhat.devtools.intellij.tektoncd.utils.model.debug.DebugResourceState;
import com.redhat.devtools.intellij.tektoncd.tkn.Tkn;
import io.fabric8.kubernetes.api.model.Pod;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PollingHelper {

    private static final Logger logger = LoggerFactory.getLogger(PollingHelper.class);
    private static PollingHelper instance;
    private List<String> resourceInPolling;

    private PollingHelper() {
        resourceInPolling = new ArrayList<>();
    }

    public static PollingHelper get() {
        if (instance == null) {
            instance = new PollingHelper();
        }
        return instance;
    }

    public void pollResource(Tkn tkn, DebugModel model, BiFunction<Tkn, DebugModel, Pair<Boolean, DebugModel>> isReadyForExecution, BiConsumer<Tkn, DebugModel> doExecute) {
        Pod resource = model.getPod();
        String name = resource.getMetadata().getNamespace() + "-" + resource.getMetadata().getName();
        if (resourceInPolling.contains(name)) {
            return;
        } else {
            resourceInPolling.add(name);
        }


        Timer pollTimer = new Timer();
        TimerTask pollTask = new TimerTask() {
            @Override
            public void run() {
                try {
                    if (model.getResourceStatus().equals(DebugResourceState.DEBUG)) {
                        return;
                    }
                    Pod updatedPod = tkn.getPod(resource.getMetadata().getNamespace(), resource.getMetadata().getName());
                    if (isPodCompleted(updatedPod)) {
                        model.setResourceStatus(isPodInPhase(updatedPod, "Failed") ? DebugResourceState.COMPLETE_FAILED : DebugResourceState.COMPLETE_SUCCESS);
                        doExecute.accept(tkn, model);
                        pollTimer.cancel();
                        pollTimer.purge();
                    }
                    model.setPod(updatedPod);
                    Pair<Boolean, DebugModel> isReadyForExecutionResult = isReadyForExecution.apply(tkn, model);
                    if (isReadyForExecutionResult.getFirst()) {
                        model.setResourceStatus(DebugResourceState.DEBUG);
                        doExecute.accept(tkn, isReadyForExecutionResult.getSecond());
                    }
                } catch (IOException e) {
                    logger.warn(e.getLocalizedMessage(), e);
                }

            };
        };
        pollTimer.schedule(pollTask, 1000, 5000);
    }

    private boolean isPodCompleted(Pod pod) {
        return isPodInPhase(pod, "Succeeded") ||
                isPodInPhase(pod, "Failed");
    }

    private boolean isPodInPhase(Pod pod, String phase) {
        if (pod.getStatus() != null &&
                pod.getStatus().getPhase() != null) {
            return pod.getStatus().getPhase().equalsIgnoreCase(phase);
        }
        return false;
    }
}
