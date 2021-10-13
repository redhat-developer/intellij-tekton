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

import java.util.concurrent.ExecutionException;
import com.intellij.openapi.project.Project;
import com.redhat.devtools.intellij.common.utils.DownloadHelper;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TknCliFactory {
    private static final Logger logger = LoggerFactory.getLogger(TknCliFactory.class);
    private static TknCliFactory INSTANCE;
    private Project lastProject;

    public static TknCliFactory getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new TknCliFactory();
        }
        return INSTANCE;
    }

    private CompletableFuture<Tkn> future;

    private TknCliFactory() {
    }

    public CompletableFuture<Tkn> getTkn(Project project) {
        if (future == null
                || !lastProject.equals(project)) {
            lastProject = project;
            future = DownloadHelper.getInstance().downloadIfRequiredAsync("tkn", TknCliFactory.class.getResource("/tkn.json")).thenApply(command -> new TknCli(project, command));
        }
        return future;
    }

    public void resetTkn() {
        disposeTkn();
        future = null;
    }

    private void disposeTkn() {
        try {
            if (future != null) {
                Tkn tkn = future.get();
                tkn.dispose();
            }
        } catch (InterruptedException | ExecutionException e) {
            logger.warn(e.getLocalizedMessage(), e);
        }
    }
}
