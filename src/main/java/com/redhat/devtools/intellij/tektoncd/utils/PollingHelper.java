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
import com.redhat.devtools.intellij.tektoncd.actions.task.DebugModel;
import com.redhat.devtools.intellij.tektoncd.tkn.Tkn;
import io.fabric8.kubernetes.api.model.Pod;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

public class PollingHelper {

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

    public void pollResource(Tkn tkn, DebugModel model, BiFunction<Tkn, Pod, Pair<Boolean, String>> isReadyForExecution, BiConsumer<Tkn, DebugModel> doExecute) {
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
                    Pod updatedPod = tkn.getPod(resource.getMetadata().getNamespace(), resource.getMetadata().getName());
                    Pair<Boolean, String> isReadyForExecutionResult = isReadyForExecution.apply(tkn, updatedPod);
                    if (isReadyForExecutionResult.getFirst()) {
                        DebugModel debugModel = new DebugModel(updatedPod, isReadyForExecutionResult.getSecond(), model.getResource());
                        doExecute.accept(tkn, debugModel);
                        pollTimer.cancel();
                        pollTimer.purge();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            };
        };
        pollTimer.schedule(pollTask, 1000, 5000);
    }
}
