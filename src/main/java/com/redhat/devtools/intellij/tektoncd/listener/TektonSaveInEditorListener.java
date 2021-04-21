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
package com.redhat.devtools.intellij.tektoncd.listener;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.redhat.devtools.intellij.common.editor.SaveInEditorListener;
import com.redhat.devtools.intellij.common.utils.YAMLHelper;
import com.redhat.devtools.intellij.tektoncd.tree.ParentableNode;
import com.redhat.devtools.intellij.tektoncd.utils.DeployHelper;
import com.redhat.devtools.intellij.tektoncd.utils.TreeHelper;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_PLURAL;
import static com.redhat.devtools.intellij.tektoncd.Constants.NOTIFICATION_ID;

public class TektonSaveInEditorListener extends SaveInEditorListener {
    private static final Logger logger = LoggerFactory.getLogger(TektonSaveInEditorListener.class);

    @Override
    protected void notify(Document document) {
        try {
            String kind = YAMLHelper.getStringValueFromYAML(document.getText(), new String[] { "kind" });
            String name = YAMLHelper.getStringValueFromYAML(document.getText(), new String[] { "metadata", "name" });
            Notification notification = new Notification(NOTIFICATION_ID, "Save Successful", kind + " " + name + " has been saved!", NotificationType.INFORMATION);
            Notifications.Bus.notify(notification);
        } catch (IOException e) {
            logger.warn(e.getLocalizedMessage());
        }
    }

    @Override
    protected void refresh(Project project, Object node) {
        if (node != null && node instanceof ParentableNode) {
            TreeHelper.refresh(project, (ParentableNode) node);
        }
    }

    @Override
    protected boolean save(Document document, Project project) {
        try {
            return DeployHelper.saveOnCluster(project, document.getText());
        } catch (IOException e) {
            Notification notification = new Notification(NOTIFICATION_ID, "Error", "An error occurred while saving \n" + e.getLocalizedMessage(), NotificationType.ERROR);
            Notifications.Bus.notify(notification);
            logger.warn("Error: " + e.getLocalizedMessage(), e);
            return false;
        }
    }

    protected boolean isFileToPush(Project project, VirtualFile vf) {
        if (vf == null || vf.getUserData(KIND_PLURAL) == null || !vf.getUserData(KIND_PLURAL).isEmpty()) {
            return false;
        }
        return super.isFileToPush(project, vf);
    }
}
