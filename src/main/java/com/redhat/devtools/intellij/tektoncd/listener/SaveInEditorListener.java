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
package com.redhat.devtools.intellij.tektoncd.listener;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileDocumentSynchronizationVetoer;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.treeStructure.Tree;
import com.redhat.devtools.intellij.tektoncd.Constants;
import com.redhat.devtools.intellij.tektoncd.tree.ParentableNode;
import com.redhat.devtools.intellij.tektoncd.tree.TektonTreeStructure;
import com.redhat.devtools.intellij.tektoncd.utils.DeployHelper;
import com.redhat.devtools.intellij.tektoncd.utils.TreeHelper;
import java.io.IOException;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import static com.redhat.devtools.intellij.common.CommonConstants.LAST_MODIFICATION_STAMP;
import static com.redhat.devtools.intellij.common.CommonConstants.PROJECT;
import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_PLURAL;
import static com.redhat.devtools.intellij.tektoncd.Constants.NAMESPACE;
import static com.redhat.devtools.intellij.tektoncd.Constants.NOTIFICATION_ID;
import static com.redhat.devtools.intellij.tektoncd.Constants.TARGET_NODE;

public class SaveInEditorListener extends FileDocumentSynchronizationVetoer {

    private static final Logger logger = LoggerFactory.getLogger(SaveInEditorListener.class);

    @Override
    public boolean maySaveDocument(@NotNull Document document, boolean isSaveExplicit) {
        VirtualFile vf = FileDocumentManager.getInstance().getFile(document);
        Project project = vf.getUserData(PROJECT);
        String namespace = vf.getUserData(NAMESPACE);
        Long lastModificationStamp = vf.getUserData(LAST_MODIFICATION_STAMP);
        Long currentModificationStamp = document.getModificationStamp();
        if (project == null ||
                namespace == null ||
                !isFileToPush(project, document, vf) ||
                currentModificationStamp.equals(lastModificationStamp)
        ) {
            return true;
        }

        vf.putUserData(LAST_MODIFICATION_STAMP, currentModificationStamp);
        if (save(document, project, namespace)) {
            try {
                Tree tree = TreeHelper.getTree(project);
                TektonTreeStructure treeStructure = (TektonTreeStructure) tree.getClientProperty(Constants.STRUCTURE_PROPERTY);
                ParentableNode selected = vf.getUserData(TARGET_NODE);
                if (selected != null) {
                    treeStructure.fireModified(selected);
                }
            } catch (Exception e) {
                logger.warn("Error: " + e.getLocalizedMessage(), e);
            }
            // notify user if saving was completed successfully
            Notification notification = new Notification(NOTIFICATION_ID, "Save Successful", StringUtils.capitalize(vf.getUserData(KIND_PLURAL)) + " has been saved!", NotificationType.INFORMATION);
            Notifications.Bus.notify(notification);
        }
        return false;
    }

    private boolean save(Document document, Project project, String namespace) {
        try {
            return DeployHelper.saveOnCluster(project, namespace, document.getText(), "Do you want to push the changes to the cluster?");
        } catch (IOException e) {
            Notification notification = new Notification(NOTIFICATION_ID, "Error", "An error occurred while saving \n" + e.getLocalizedMessage(), NotificationType.ERROR);
            Notifications.Bus.notify(notification);
            logger.warn("Error: " + e.getLocalizedMessage(), e);
            return false;
        }
    }

    private boolean isFileToPush(Project project, Document document, VirtualFile vf) {
        FileEditor selectedEditor = FileEditorManager.getInstance(project).getSelectedEditor();
        // if file is not the one selected, skip it
        if (selectedEditor == null || !selectedEditor.getFile().equals(vf)) return false;
        // if file is not related to tekton, skip it
        if (vf == null || vf.getUserData(KIND_PLURAL) == null || vf.getUserData(KIND_PLURAL).isEmpty()) {
            return false;
        }
        return true;
    }
}

