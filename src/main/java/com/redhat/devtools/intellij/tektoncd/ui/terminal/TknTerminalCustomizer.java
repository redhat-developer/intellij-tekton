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
package com.redhat.devtools.intellij.tektoncd.ui.terminal;

import com.intellij.openapi.project.Project;
import com.redhat.devtools.intellij.tektoncd.tkn.Tkn;
import com.redhat.devtools.intellij.tektoncd.tkn.TknCliFactory;
import org.jetbrains.plugins.terminal.LocalTerminalCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ExecutionException;

public class TknTerminalCustomizer extends LocalTerminalCustomizer {

    private static final Logger logger = LoggerFactory.getLogger(TknTerminalCustomizer.class);

    private Project project;
    private Tkn tkn;

    public String[] customizeCommandAndEnvironment(Project project, String[] command, Map<String, String> envs) {
        Tkn tkn = getTkn(project);
        if (tkn != null) {
            envs.putAll(tkn.getEnvVariables());
        }
        return command;
    }

    private Tkn getTkn(Project project) {
        if (project == null) {
            return null;
        }
        if (tkn != null) {
            return tkn;
        }
        try {
            return this.tkn = TknCliFactory.getInstance().getTkn(project).get();
        } catch (InterruptedException | ExecutionException e) {
            logger.warn("Could not get tkn", e);
            return null;
        }
    }
}