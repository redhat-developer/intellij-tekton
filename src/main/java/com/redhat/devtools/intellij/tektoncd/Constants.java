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
import com.redhat.devtools.intellij.tektoncd.tree.ParentableNode;

public class Constants {
    public static final String NOTIFICATION_ID = "Tekton Pipelines";
    public static final Key<String> NAMESPACE = Key.create("com.redhat.devtools.intellij.tektoncd.tekton.namespace");
    public static final Key<String> KIND_PLURAL = Key.create("com.redhat.devtools.intellij.tektoncd.tekton.plural");
    public static final Key<ParentableNode> TARGET_NODE = Key.create("com.redhat.devtools.intellij.tektoncd.tekton.targetnode");

    public static final String KIND_CLUSTERTASKS = "clustertasks";
    public static final String KIND_PIPELINES = "pipelines";
    public static final String KIND_RESOURCES = "pipelineresources";
    public static final String KIND_TASKS = "tasks";
    public static final String KIND_CONDITIONS = "conditions";
    public static final String KIND_TRIGGERTEMPLATES = "triggertemplates";
    public static final String KIND_TRIGGERBINDINGS = "triggerbindings";
    public static final String KIND_CLUSTERTRIGGERBINDINGS = "clustertriggerbindings";
    public static final String KIND_EVENTLISTENERS = "eventlisteners";
    public static final String KIND_PIPELINERUNS = "pipelineruns";
    public static final String KIND_TASKRUNS = "taskruns";

    public static final String KIND_PIPELINE = "pipeline";
    public static final String KIND_PIPELINERUN = "pipelinerun";
    public static final String KIND_TASK = "task";
    public static final String KIND_CLUSTERTASK = "clustertask";
    public static final String KIND_TASKRUN = "taskrun";
    public static final String KIND_CONDITION = "condition";
    public static final String KIND_RESOURCE = "pipelineresource";
    public static final String KIND_TRIGGERTEMPLATE = "triggertemplate";
    public static final String KIND_TRIGGERBINDING = "triggerbinding";
    public static final String KIND_CLUSTERTRIGGERBINDING = "clustertriggerbinding";
    public static final String KIND_EVENTLISTENER = "eventlistener";

    public static final String KIND_PVC =  "PersistentVolumeClaim";
    public static final String KIND_CONFIGMAP =  "ConfigMap";
    public static final String KIND_SECRET =  "Secret";
    public static final String KIND_EMPTYDIR =  "EmptyDir";

    public static final String FLAG_SERVICEACCOUNT = "-s";
    public static final String FLAG_TASKSERVICEACCOUNT = "--task-serviceaccount";
    public static final String FLAG_PARAMETER = "-p";
    public static final String FLAG_INPUTRESOURCEPIPELINE = "-r";
    public static final String FLAG_INPUTRESOURCETASK = "-i";
    public static final String FLAG_OUTPUTRESOURCE = "-o";
    public static final String FLAG_WORKSPACE = "-w";
    public static final String FLAG_PREFIXNAME = "--prefix-name";

    public static final String TERMINAL_TITLE = "Tekton";

    public static final String STRUCTURE_PROPERTY = Constants.class.getPackage().getName() + ".structure";

    public enum InstallStatus {
        ERROR,
        INSTALLED,
        OVERWRITTEN
    }
}
