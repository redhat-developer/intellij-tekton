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
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileDocumentSynchronizationVetoer;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.redhat.devtools.intellij.common.utils.JSONHelper;
import com.redhat.devtools.intellij.common.utils.UIHelper;
import com.redhat.devtools.intellij.common.utils.YAMLHelper;
import com.redhat.devtools.intellij.tektoncd.utils.CRDHelper;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_PLURAL;
import static com.redhat.devtools.intellij.tektoncd.Constants.NOTIFICATION_ID;

public class SaveInEditorListener extends FileDocumentSynchronizationVetoer {
    Logger logger = LoggerFactory.getLogger(SaveInEditorListener.class);

    @Override
    public boolean maySaveDocument(@NotNull Document document, boolean isSaveExplicit) {
        VirtualFile vf = FileDocumentManager.getInstance().getFile(document);
        // if file is not related to tekton we can skip it
        if (vf == null || vf.getUserData(KIND_PLURAL).isEmpty()) {
            return true;
        }

        String namespace, name, apiVersion;
        JsonNode spec;
        CustomResourceDefinitionContext crdContext;
        try {
            namespace = YAMLHelper.getStringValueFromYAML(document.getText(), new String[] {"metadata", "namespace"});
            name = YAMLHelper.getStringValueFromYAML(document.getText(), new String[] {"metadata", "name"});
            if (Strings.isNullOrEmpty(namespace) || Strings.isNullOrEmpty(name)) {
                throw new IOException("Tekton file has not a valid format. Namespace and/or name fields are invalid.");
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
            UIHelper.executeInUI(() -> Messages.showErrorDialog("Error: " + e.getLocalizedMessage(), "Error"));
            return false;
        }

        Notification notification;
        try (final KubernetesClient client = new DefaultKubernetesClient()) {
            JsonNode customResource = JSONHelper.MapToJSON(client.customResource(crdContext).get(namespace, name));
            ((ObjectNode) customResource).set("spec", spec);
            client.customResource(crdContext).edit(namespace, name, customResource.toString());
        } catch (IOException e) {
            // give a visual notification to user if an error occurs during saving
            notification = new Notification(NOTIFICATION_ID, "Error", "An error occurred while saving " + vf.getUserData(KIND_PLURAL) + " " + name + "\n" + e.getLocalizedMessage(), NotificationType.ERROR);
            Notifications.Bus.notify(notification);
            logger.error("Error: " + e.getLocalizedMessage());
            return false;
        }

        // notify user if saving was completed successfully
        notification = new Notification(NOTIFICATION_ID, "Save Successful", StringUtils.capitalize(vf.getUserData(KIND_PLURAL)) + " " + name + " has been saved!", NotificationType.INFORMATION);
        Notifications.Bus.notify(notification);
        return true;
    }
}
