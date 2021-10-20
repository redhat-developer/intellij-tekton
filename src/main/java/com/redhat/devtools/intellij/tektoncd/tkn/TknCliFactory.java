/*******************************************************************************
 * Copyright (c) 2020 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package com.redhat.devtools.intellij.tektoncd.tkn;

import com.intellij.openapi.project.Project;
import com.redhat.devtools.intellij.common.utils.DownloadHelper;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class TknCliFactory {
    private static TknCliFactory INSTANCE;
    private Map<Project, CompletableFuture<Tkn>> projectFutureMap;

    public static TknCliFactory getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new TknCliFactory();
        }
        return INSTANCE;
    }

    private TknCliFactory() {
        projectFutureMap = new HashMap<>();
    }

    public CompletableFuture<Tkn> getTkn(Project project) {
        if (projectFutureMap.containsKey(project)) {
            return projectFutureMap.get(project);
        } {
            CompletableFuture<Tkn> tknCompletableFuture = DownloadHelper.getInstance()
                    .downloadIfRequiredAsync("tkn", TknCliFactory.class.getResource("/tkn.json"))
                    .thenApply(command -> new TknCli(project, command));
            projectFutureMap.put(project, tknCompletableFuture);
            return tknCompletableFuture;
        }
    }

    public void resetTkn(Project project) {
        CompletableFuture<Tkn> tknCompletableFuture = projectFutureMap.remove(project);
        if (tknCompletableFuture != null) {
            tknCompletableFuture.whenComplete((tkn, throwable) -> tkn.dispose());
        }
    }
}
