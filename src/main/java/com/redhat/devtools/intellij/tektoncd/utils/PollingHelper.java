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
import com.redhat.devtools.intellij.tektoncd.tkn.Tkn;
import com.redhat.devtools.intellij.tektoncd.utils.model.debug.DebugModel;
import com.redhat.devtools.intellij.tektoncd.utils.model.debug.DebugResourceState;
import io.fabric8.kubernetes.api.model.Pod;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PollingHelper {

    private static final Logger logger = LoggerFactory.getLogger(PollingHelper.class);
    private static PollingHelper instance;
    private Map<String, Timer> resourceInPolling;

    private PollingHelper() {
        resourceInPolling = new HashMap<>();
    }

    public static PollingHelper get() {
        if (instance == null) {
            instance = new PollingHelper();
        }
        return instance;
    }

    public void doPolling(Tkn tkn, DebugModel model, BiFunction<Tkn, DebugModel, CompletableFuture<Pair<Boolean, DebugModel>>> isReadyForExecution, BiConsumer<Tkn, DebugModel> doExecute) {
        Pod resource = model.getPod();
        if (isPollingActiveOnResource(resource)) {
            stopTimer(resource);
        }

        startPolling(tkn, model, isReadyForExecution, doExecute);
    }

    public void stopPolling(DebugModel model) {
        if (isPollingActiveOnResource(model.getPod())) {
            stopTimer(model.getPod());
        }
    }

    private boolean isPollingActiveOnResource(Pod resource) {
        String id = getId(resource);
        return resourceInPolling.containsKey(id);
    }

    private String getId(Pod resource) {
        return resource.getMetadata().getNamespace() + "-" + resource.getMetadata().getName();
    }

    private void startPolling(Tkn tkn, DebugModel model, BiFunction<Tkn, DebugModel, CompletableFuture<Pair<Boolean, DebugModel>>> isReadyForExecution, BiConsumer<Tkn, DebugModel> doExecute) {
        TimerTask pollTask = new TimerTask() {
            @Override
            public void run() {
                if (!canDoPolling(model)) {
                    return;
                }
                try {
                    Pod updatedPod = tkn.getPod(model.getPod().getMetadata().getNamespace(), model.getPod().getMetadata().getName());
                    model.setPod(updatedPod);
                    if (DebugHelper.isPodCompleted(updatedPod)) {
                        stopTimer(updatedPod);
                        execute(tkn,
                                model,
                                DebugHelper.isPodFailed(updatedPod)
                                        ? DebugResourceState.COMPLETE_FAILED
                                        : DebugResourceState.COMPLETE_SUCCESS,
                                doExecute);
                    } else {
                        isReadyForExecution.apply(tkn, model)
                                .whenComplete((isReadyForExecutionResult, t) -> {
                                    if (isReadyForExecutionResult.getFirst()) {
                                        execute(tkn, isReadyForExecutionResult.getSecond(), DebugResourceState.DEBUG, doExecute);
                                    }
                                });
                    }
                } catch (IOException | RuntimeException e) {
                    logger.warn(e.getLocalizedMessage(), e);
                }
            };
        };
        schedulePolling(model.getPod(), pollTask);
    }

    private boolean canDoPolling(DebugModel model) {
        if (model.getResourceStatus().equals(DebugResourceState.COMPLETE_SUCCESS)
            || model.getResourceStatus().equals(DebugResourceState.COMPLETE_FAILED)) {
            stopTimer(model.getPod());
            return false;
        }
        return true;
    }

    private void schedulePolling(Pod resource, TimerTask pollTask) {
        String resourceId = getId(resource);
        Timer pollTimer = new Timer();
        resourceInPolling.put(resourceId, pollTimer);
        pollTimer.schedule(pollTask, 1000, 5000);
    }

    private void stopTimer(Pod resource) {
        String resourceId = getId(resource);
        Timer timer = resourceInPolling.get(resourceId);
        timer.cancel();
        timer.purge();
    }

    private void execute(Tkn tkn, DebugModel model, DebugResourceState state, BiConsumer<Tkn, DebugModel> doExecute) {
        model.updateResourceStatus(state);
        doExecute.accept(tkn, model);
    }
}
