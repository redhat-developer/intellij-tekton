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
import java.util.concurrent.CompletableFuture;

public class TknCliFactory {
    private static TknCliFactory INSTANCE;

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
        if (future == null) {
            setTkn(project);
        }
        return future;
    }

    public void setTkn(Project project) {
        future = DownloadHelper.getInstance().downloadIfRequiredAsync("tkn", TknCliFactory.class.getResource("/tkn.json")).thenApply(command -> new TknCli(project, command));
    }
}
