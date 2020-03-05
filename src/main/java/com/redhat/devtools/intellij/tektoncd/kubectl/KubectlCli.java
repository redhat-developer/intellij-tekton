/*******************************************************************************
 * Copyright (c) 2019 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package com.redhat.devtools.intellij.tektoncd.kubectl;

import com.redhat.devtools.intellij.common.utils.DownloadHelper;
import com.redhat.devtools.intellij.common.utils.ExecHelper;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;

public class KubectlCli implements Kubectl {
    /**
     * Home sub folder for the plugin
     */
    public static final String PLUGIN_FOLDER = ".kube";

    private String command;

    private KubectlCli() throws IOException {
        command = getCommand();
    }

    private static Kubectl INSTANCE;

    public static final Kubectl get() throws IOException {
        if (INSTANCE == null) {
            INSTANCE = new KubectlCli();
        }
        return INSTANCE;
    }

    public String getCommand() throws IOException {
        if (command == null) {
            command = getKubectlCommand();
        }
        return command;
    }

    private String getKubectlCommand() throws IOException {
        return DownloadHelper.getInstance().downloadIfRequired("kubectl", KubectlCli.class.getResource("/kubectl.json"));
    }

    @Override
    public void create(String namespace, String path) throws IOException {
        if (StringUtils.isBlank(namespace)) {
            ExecHelper.execute(command, "crete", "-f", path);
        } else {
            ExecHelper.execute(command, "create", "-f", path, "-n", namespace);
        }
    }

    @Override
    public void apply(String namespace, String path) throws IOException {
        if (StringUtils.isBlank(namespace)) {
            ExecHelper.execute(command, "patch", "pipeline", "petclinic-deploy-pipeline", "--type", "merge", "-p", path);
            //ExecHelper.executeWithTerminal(command, "patch", "pipeline", "petclinic-deploy-pipeline", "--type", "merge", "-p", "'{\"spec\":{\"resources\":[{\"name\":\"app-git\",\"type\":\"git\"},{\"name\":\"app-image\",\"type\":\"image\"}],\"tasks\":[{\"name\":\"tekton\",\"taskRef\":{\"name\":\"tkn\",\"kind\":\"Task\"},\"resources\":{\"inputs\":[{\"name\":\"source\",\"resource\":\"app-git\"}],\"outputs\":[{\"name\":\"image\",\"resource\":\"app-image\"}]},\"params\":[{\"name\":\"ARGS\",\"value\":[\"version -o v\"]}]}]}}'");
            //ExecHelper.execute(command, "apply", "-f", path);
        } else {
            ExecHelper.execute(command, "patch", "-f", path, "-n", namespace);
        }
    }
}
