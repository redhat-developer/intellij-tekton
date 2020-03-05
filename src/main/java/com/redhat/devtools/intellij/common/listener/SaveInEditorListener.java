package com.redhat.devtools.intellij.common.listener;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileDocumentSynchronizationVetoer;
import com.intellij.openapi.vfs.VirtualFile;
import com.redhat.devtools.intellij.common.utils.ParserHelper;
import com.redhat.devtools.intellij.tektoncd.utils.CRDHelper;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static com.redhat.devtools.intellij.common.CommonConstants.*;

public class SaveInEditorListener extends FileDocumentSynchronizationVetoer {
    Logger logger = LoggerFactory.getLogger(SaveInEditorListener.class);

    @Override
    public boolean maySaveDocument(@NotNull Document document, boolean isSaveExplicit) {
        VirtualFile vf = FileDocumentManager.getInstance().getFile(document);
        // if file is not related to tekton we can skip it
        if (vf == null || vf.getUserData(TEKTON_RS).isEmpty()) {
            return true;
        }

        try (final KubernetesClient client = new DefaultKubernetesClient()) {
            JsonNode specToUpload = ParserHelper.getSpecJSON(document.getText());
            CustomResourceDefinitionContext crdContext = CRDHelper.getCRDContext(vf.getUserData((TEKTON_PLURAL)));
            JsonNode customResource = ParserHelper.MapToJSON(client.customResource(crdContext).get(vf.getUserData(TEKTON_NS), vf.getUserData(TEKTON_RS)));
            ((ObjectNode) customResource).set("spec", specToUpload);
            client.customResource(crdContext).edit(vf.getUserData(TEKTON_NS), vf.getUserData(TEKTON_RS), customResource.toString());
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
