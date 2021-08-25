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
package com.redhat.devtools.intellij.tektoncd.actions.setting;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;
import com.redhat.devtools.intellij.common.utils.ExecHelper;
import com.redhat.devtools.intellij.common.utils.UIHelper;
import com.redhat.devtools.intellij.tektoncd.actions.TektonAction;
import com.redhat.devtools.intellij.tektoncd.tkn.Tkn;
import com.redhat.devtools.intellij.tektoncd.tree.TektonRootNode;
import com.redhat.devtools.intellij.tektoncd.utils.VirtualFileHelper;
import com.redhat.devtools.intellij.tektoncd.utils.YAMLBuilder;
import io.fabric8.kubernetes.api.model.ConfigMap;
import java.io.IOException;
import javax.swing.tree.TreePath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import static com.redhat.devtools.intellij.tektoncd.Constants.NOTIFICATION_ID;

public abstract class ShowConfigurationAction extends TektonAction {
    private static final Logger logger = LoggerFactory.getLogger(ShowConfigurationAction.class);

    public ShowConfigurationAction() { super(TektonRootNode.class); }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent, TreePath path, Object selected, Tkn tkncli) {
        String namespace = getNamespace();
        String name = getName();
        ExecHelper.submit(() -> {
            ConfigMap configMap = tkncli.getConfigMap(namespace, name);
            UIHelper.executeInUI(() -> {
                if (configMap == null) {
                    Messages.showErrorDialog("No configuration file " + name + " found in namespace " + namespace, "Configuration not Found");
                    return;
                }
                try {
                    String yaml = YAMLBuilder.writeValueAsString(configMap);
                    VirtualFileHelper.openVirtualFileInEditor(anActionEvent.getProject(), name + ".yaml", yaml, true,true);
                } catch (IOException e) {
                    Notification notification = new Notification(NOTIFICATION_ID,
                            "Error",
                            name + " file in namespace " + namespace + " was not found\n" + e.getLocalizedMessage(),
                            NotificationType.ERROR);
                    Notifications.Bus.notify(notification);
                    logger.warn("Error: " + e.getLocalizedMessage(), e);
                }
            });
        });
    }

    public abstract String getNamespace();

    public abstract String getName();
}
