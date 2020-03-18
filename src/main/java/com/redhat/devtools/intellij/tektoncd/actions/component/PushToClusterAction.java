package com.redhat.devtools.intellij.tektoncd.actions.component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Strings;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.keymap.KeymapManager;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.treeStructure.Tree;
import com.redhat.devtools.intellij.common.tree.LazyMutableTreeNode;
import com.redhat.devtools.intellij.common.utils.UIHelper;
import com.redhat.devtools.intellij.tektoncd.tree.PipelinesNode;
import com.redhat.devtools.intellij.tektoncd.utils.CRDHelper;
import com.redhat.devtools.intellij.tektoncd.utils.JSONHelper;
import com.redhat.devtools.intellij.tektoncd.utils.YAMLHelper;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.redhat.devtools.intellij.common.CommonConstants.KIND_PLURAL;
import static com.redhat.devtools.intellij.common.CommonConstants.NAMESPACE;
import static java.awt.event.InputEvent.CTRL_DOWN_MASK;
import static java.awt.event.KeyEvent.VK_S;

public class PushToClusterAction extends AnAction {
    Logger logger = LoggerFactory.getLogger(PushToClusterAction.class);
    PushToClusterAction() {
        super();
        KeyStroke keyStroke = KeyStroke.getKeyStroke(VK_S, CTRL_DOWN_MASK, true);
        Shortcut s = new KeyboardShortcut(keyStroke, null);
        KeymapManager.getInstance().getActiveKeymap().addShortcut("tektoncd.saveAll", s);
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        final Editor editor = e.getRequiredData(CommonDataKeys.EDITOR);
        if (editor == null) return;
        final Document document = editor.getDocument();
        if (document == null) return;
        VirtualFile vf = FileDocumentManager.getInstance().getFile(document);

        // if file is not related to tekton we can skip it
        if (vf == null || vf.getUserData(KIND_PLURAL).isEmpty() || vf.getUserData(NAMESPACE).isEmpty()) {
            return;
        }

        int resultDialog = UIHelper.executeInUI(() ->
                Messages.showYesNoDialog(
                        "The file has been saved. Do you want to push the changes to the cluster?",
                        "Save to cluster",
                        null
                ));

        if (resultDialog != Messages.OK) return;
        String name, namespace = vf.getUserData(NAMESPACE);
        JsonNode spec;
        try {
            name = YAMLHelper.getStringValueFromYAML(document.getText(), new String[] {"metadata", "name"});
            if (Strings.isNullOrEmpty(name)) {
                throw new IOException("Tekton file has not a valid format. Name field is not found.");
            }
            spec = YAMLHelper.getValueFromYAML(document.getText(), new String[] {"spec"});
            if (spec == null) {
                throw new IOException("Tekton file has not a valid format. Spec field is not found.");
            }
        } catch (IOException ex) {
            UIHelper.executeInUI(() -> Messages.showErrorDialog("Error: " + ex.getLocalizedMessage(), "Error"));
            return;
        }

        Notification notification;
        try (final KubernetesClient client = new DefaultKubernetesClient()) {
            CustomResourceDefinitionContext crdContext = CRDHelper.getCRDContext(vf.getUserData(KIND_PLURAL));
            Map listResources = client.customResource(crdContext).list(namespace);
            boolean resourceExists = ((ArrayList) listResources.get("items")).stream().filter(o -> ((HashMap)((HashMap)o).get("metadata")).get("name").equals(name)).findFirst().isPresent();
            if (!resourceExists) {
                client.customResource(crdContext).create(namespace, document.getText());
            } else {
                JsonNode customResource = JSONHelper.MapToJSON(client.customResource(crdContext).get(namespace, name));
                ((ObjectNode) customResource).set("spec", spec);
                client.customResource(crdContext).edit(namespace, name, customResource.toString());
            }
        } catch (IOException ex) {
            // give a visual notification to user if an error occurs during saving
            notification = new Notification("SaveNotification", "Error", "An error occurred while saving " + vf.getUserData(KIND_PLURAL) + " " + name, NotificationType.ERROR);
            notification.notify(ProjectManager.getInstance().getDefaultProject());
            logger.error("Error: " + ex.getLocalizedMessage());
            return;
        }

        // notify user if saving was completed successfully
        notification = new Notification("SaveNotification", "Save Successful", StringUtils.capitalize(vf.getUserData(KIND_PLURAL)) + " " + name + " has been saved!", NotificationType.INFORMATION);
        notification.notify(ProjectManager.getInstance().getDefaultProject());

        // refresh tree
        try {
            ToolWindow window = ToolWindowManager.getInstance(e.getProject()).getToolWindow("Tekton");
            if (window == null) return;
            JBScrollPane pane = (JBScrollPane) window.getContentManager().findContent("").getComponent();
            if (pane == null) return;
            Tree tree = (Tree) pane.getViewport().getView();
            if (tree == null) return;
            LazyMutableTreeNode[] nodes = tree.getSelectedNodes(PipelinesNode.class, null);
            if (nodes != null && nodes.length > 0) nodes[0].reload();
        } catch (Exception ex) {
            logger.error("Error: " + ex.getLocalizedMessage());
        }

    }
}
