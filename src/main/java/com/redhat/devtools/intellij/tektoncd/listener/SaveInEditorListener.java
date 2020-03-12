package com.redhat.devtools.intellij.tektoncd.listener;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Strings;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileDocumentSynchronizationVetoer;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.redhat.devtools.intellij.common.utils.UIHelper;
import com.redhat.devtools.intellij.tektoncd.utils.CRDHelper;
import com.redhat.devtools.intellij.tektoncd.utils.JSONHelper;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static com.redhat.devtools.intellij.common.CommonConstants.KIND_PLURAL;

public class SaveInEditorListener extends FileDocumentSynchronizationVetoer {
    Logger logger = LoggerFactory.getLogger(SaveInEditorListener.class);

    @Override
    public boolean maySaveDocument(@NotNull Document document, boolean isSaveExplicit) {
        VirtualFile vf = FileDocumentManager.getInstance().getFile(document);
        // if file is not related to tekton we can skip it
        if (vf == null || vf.getUserData(KIND_PLURAL).isEmpty()) {
            return true;
        }

        String namespace = null;
        String name = null;
        try {
           namespace = JSONHelper.getNamespace(document.getText());
           name = JSONHelper.getName(document.getText());
            if (Strings.isNullOrEmpty(namespace) || Strings.isNullOrEmpty(name)) {
                throw new IOException("Tekton file has not a valid format. Namespace and/or name properties are invalid.");
            }
        } catch (IOException e) {
            UIHelper.executeInUI(() -> Messages.showErrorDialog("Error: " + e.getLocalizedMessage(), "Error"));
            return false;
        }

        try (final KubernetesClient client = new DefaultKubernetesClient()) {
            JsonNode specToUpload = JSONHelper.getSpecJSON(document.getText());
            CustomResourceDefinitionContext crdContext = CRDHelper.getCRDContext(vf.getUserData((KIND_PLURAL)));
            JsonNode customResource = JSONHelper.MapToJSON(client.customResource(crdContext).get(namespace, name));
            ((ObjectNode) customResource).set("spec", specToUpload);
            client.customResource(crdContext).edit(namespace, name, customResource.toString());
        } catch (IOException e) {
            logger.error("Error: " + e.getLocalizedMessage());
            return false;
        } catch (Exception e) {
            logger.error("Error: " + e.getLocalizedMessage());
            return false;
        }
        return true;
    }
}
