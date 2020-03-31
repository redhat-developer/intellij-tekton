/*******************************************************************************
 * Copyright (c) 2020 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc.
 ******************************************************************************/
package com.redhat.devtools.intellij.tektoncd;

import com.intellij.openapi.util.Key;

public class Constants {
    public static final String NOTIFICATION_ID = "Tekton Pipelines";
    public static final Key<String> KIND_PLURAL = Key.create("tekton.plural");

    public static final String KIND_CLUSTERTASKS = "clustertasks";
    public static final String KIND_PIPELINES = "pipelines";
    public static final String KIND_RESOURCES = "pipelineresources";
    public static final String KIND_TASKS = "tasks";

    public static final String KIND_PIPELINE = "pipeline";

    public static final String FLAG_PARAMETER = "-p";
    public static final String FLAG_INPUTRESOURCEPIPELINE = "-r";
    public static final String FLAG_INPUTRESOURCETASK = "-i";
    public static final String FLAG_OUTPUTRESOURCE = "-o";

    public static final String TERMINAL_TITLE = "Tekton";
}
