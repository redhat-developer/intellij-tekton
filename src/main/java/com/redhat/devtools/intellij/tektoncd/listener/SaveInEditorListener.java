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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Strings;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileDocumentSynchronizationVetoer;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.treeStructure.Tree;
import com.redhat.devtools.intellij.common.utils.JSONHelper;
import com.redhat.devtools.intellij.common.utils.UIHelper;
import com.redhat.devtools.intellij.common.utils.YAMLHelper;
import com.redhat.devtools.intellij.tektoncd.Constants;
import com.redhat.devtools.intellij.tektoncd.tkn.Tkn;
import com.redhat.devtools.intellij.tektoncd.tree.ParentableNode;
import com.redhat.devtools.intellij.tektoncd.tree.TektonRootNode;
import com.redhat.devtools.intellij.tektoncd.tree.TektonTreeStructure;
import com.redhat.devtools.intellij.tektoncd.utils.CRDHelper;
import com.redhat.devtools.intellij.tektoncd.utils.TreeHelper;
import io.fabric8.kubernetes.api.model.Status;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

import static com.redhat.devtools.intellij.common.CommonConstants.LAST_MODIFICATION_STAMP;
import static com.redhat.devtools.intellij.common.CommonConstants.PROJECT;
import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_PLURAL;
import static com.redhat.devtools.intellij.tektoncd.Constants.NAMESPACE;
import static com.redhat.devtools.intellij.tektoncd.Constants.NOTIFICATION_ID;
import static com.redhat.devtools.intellij.tektoncd.Constants.TARGET_NODE;

public class SaveInEditorListener extends FileDocumentSynchronizationVetoer {
    Logger logger = LoggerFactory.getLogger(SaveInEditorListener.class);

    @Override
    public boolean maySaveDocument(@NotNull Document document, boolean isSaveExplicit) {
        VirtualFile vf = FileDocumentManager.getInstance().getFile(document);
        Project project = vf.getUserData(PROJECT);
        String namespace = vf.getUserData(NAMESPACE);
        Long lastModificationStamp = vf.getUserData(LAST_MODIFICATION_STAMP);
        Long currentModificationStamp = document.getModificationStamp();
        if(project == null ||
                namespace == null ||
                !isFileToPush(project, document, vf) ||
                currentModificationStamp.equals(lastModificationStamp)
        ) {
            return true;
        }
        vf.putUserData(LAST_MODIFICATION_STAMP, currentModificationStamp);

        String name, apiVersion;
        JsonNode spec;
        CustomResourceDefinitionContext crdContext;
        Notification notification;
        try {
            name = YAMLHelper.getStringValueFromYAML(document.getText(), new String[] {"metadata", "name"});
            if (Strings.isNullOrEmpty(name)) {
                throw new IOException("Tekton file has not a valid format. Name field is not valid or found.");
            }
            apiVersion = YAMLHelper.getStringValueFromYAML(document.getText(), new String[] {"apiVersion"});
            if (Strings.isNullOrEmpty(apiVersion)) {
                throw new IOException("Tekton file has not a valid format. ApiVersion field is not found.");
            }
            crdContext = CRDHelper.getCRDContext(apiVersion, vf.getUserData(KIND_PLURAL));
            if (crdContext == null) {
                throw new IOException("Tekton file has not a valid format. ApiVersion field contains an invalid value.");
            }
            spec = YAMLHelper.getValueFromYAML(document.getText(), new String[] {"spec"});
            if (spec == null) {
                throw new IOException("Tekton file has not a valid format. Spec field is not found.");
            }
        } catch (IOException e) {
            notification = new Notification(NOTIFICATION_ID, "Error", "An error occurred while saving \n" + e.getLocalizedMessage(), NotificationType.ERROR);
            Notifications.Bus.notify(notification);
            logger.warn("Error: " + e.getLocalizedMessage(), e);
            return false;
        }

        int resultDialog = UIHelper.executeInUI(() ->
                Messages.showYesNoDialog(
                        "Do you want to push the changes to the cluster?",
                        "Save to cluster",
                        null
                ));

        if (resultDialog != Messages.OK) return false;

        String errorMsg = "An error occurred while saving " + StringUtils.capitalize(vf.getUserData(KIND_PLURAL)) + " " + name + "\n";

        Tree tree;
        TektonTreeStructure treeStructure;
        Tkn tknCli;
        try {
            tree = TreeHelper.getTree(project);
            treeStructure = (TektonTreeStructure)tree.getClientProperty(Constants.STRUCTURE_PROPERTY);
            TektonRootNode root = (TektonRootNode) treeStructure.getRootElement();
            tknCli = root.getTkn();
        } catch (Exception e) {
            notification = new Notification(NOTIFICATION_ID, "Error", errorMsg + e.getLocalizedMessage(), NotificationType.ERROR);
            Notifications.Bus.notify(notification);
            logger.warn("Error: " + e.getLocalizedMessage(), e);
            return false;
        }

        try {
            String resourceNamespace = CRDHelper.isClusterScopedResource(vf.getUserData(KIND_PLURAL)) ? "" : namespace;
            Map<String, Object> resource = tknCli.getCustomResource(resourceNamespace, name, crdContext);
            if (resource == null) {
                tknCli.createCustomResource(resourceNamespace, crdContext, document.getText());
                ParentableNode selected = vf.getUserData(TARGET_NODE);
                if (selected != null) {
                    treeStructure.fireModified(selected);
                }
            } else {
                JsonNode customResource = JSONHelper.MapToJSON(resource);
                ((ObjectNode) customResource).set("spec", spec);
                tknCli.editCustomResource(resourceNamespace, name, crdContext, customResource.toString());
            }
        } catch (KubernetesClientException e) {
            Status errorStatus = e.getStatus();
            if (errorStatus != null && !Strings.isNullOrEmpty(errorStatus.getMessage())) {
                errorMsg += errorStatus.getMessage() + "\n";
            }
            // give a visual notification to user if an error occurs during saving
            notification = new Notification(NOTIFICATION_ID,"Error", errorMsg + e.getLocalizedMessage(), NotificationType.ERROR);
            Notifications.Bus.notify(notification);
            logger.warn("Error: " + e.getLocalizedMessage(), e);
            return false;
        } catch (IOException e) {
            // give a visual notification to user if an error occurs during saving
            notification = new Notification(NOTIFICATION_ID, "Error", errorMsg + e.getLocalizedMessage(), NotificationType.ERROR);
            Notifications.Bus.notify(notification);
            logger.warn("Error: " + e.getLocalizedMessage(), e);
            return false;
        }

        // notify user if saving was completed successfully
        notification = new Notification(NOTIFICATION_ID, "Save Successful", StringUtils.capitalize(vf.getUserData(KIND_PLURAL)) + " " + name + " has been saved!", NotificationType.INFORMATION);
        Notifications.Bus.notify(notification);
        return false;
    }

    private boolean isFileToPush(Project project, Document document, VirtualFile vf) {
        Editor selectedEditor = FileEditorManager.getInstance(project).getSelectedTextEditor();
        // if file is not the one selected, skip it
        if (selectedEditor == null || selectedEditor.getDocument() != document) return false;
        // if file is not related to tekton, skip it
        if (vf == null || vf.getUserData(KIND_PLURAL) == null || vf.getUserData(KIND_PLURAL).isEmpty()) {
            return false;
        }
        return true;
    }
}

