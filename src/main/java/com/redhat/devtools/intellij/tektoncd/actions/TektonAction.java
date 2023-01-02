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
package com.redhat.devtools.intellij.tektoncd.actions;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Strings;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.treeStructure.Tree;
import com.redhat.devtools.intellij.common.actions.StructureTreeAction;
import com.redhat.devtools.intellij.common.utils.YAMLHelper;
import com.redhat.devtools.intellij.tektoncd.Constants;
import com.redhat.devtools.intellij.tektoncd.tkn.Tkn;
import com.redhat.devtools.intellij.tektoncd.tkn.component.field.Workspace;
import com.redhat.devtools.intellij.tektoncd.tree.TektonRootNode;
import com.redhat.devtools.intellij.tektoncd.tree.TektonTreeStructure;
import com.redhat.devtools.intellij.tektoncd.utils.SnippetHelper;
import com.redhat.devtools.intellij.tektoncd.utils.VirtualFileHelper;
import com.redhat.devtools.intellij.tektoncd.utils.YAMLBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.tree.TreePath;
import java.io.IOException;
import java.util.Map;

import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_VCT;
import static com.redhat.devtools.intellij.tektoncd.Constants.PIPELINES_BETA1_API_VERSION;

public class TektonAction extends StructureTreeAction {
    Logger logger = LoggerFactory.getLogger(TektonAction.class);

    public TektonAction(Class... filters) {
        super(filters);
    }

    public TektonAction(boolean acceptMultipleItems, Class... filters) {
        super(acceptMultipleItems, filters);
    }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent, TreePath path, Object selected) {
        try {
            this.actionPerformed(anActionEvent, path, selected, getTkn(anActionEvent));
        } catch (IOException e) {
            Messages.showErrorDialog("Error: " + e.getLocalizedMessage(), "Error");
        }
    }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent, TreePath[] path, Object[] selected) {
        if (selected.length == 0) {
            return;
        }
        try {
            this.actionPerformed(anActionEvent, path, selected, getTkn(anActionEvent));
        } catch (IOException e) {
            Messages.showErrorDialog("Error: " + e.getLocalizedMessage(), "Error");
        }
    }

    private Tkn getTkn(AnActionEvent anActionEvent) throws IOException {
        Tree tree = getTree(anActionEvent);
        return ((TektonRootNode)((TektonTreeStructure)tree.getClientProperty(Constants.STRUCTURE_PROPERTY)).getRootElement()).getTkn();
    }

    public void actionPerformed(AnActionEvent anActionEvent, TreePath path, Object selected, Tkn tkn) {}

    public void actionPerformed(AnActionEvent anActionEvent, TreePath[] path, Object[] selected, Tkn tkn) {
        actionPerformed(anActionEvent, path[0], selected[0], tkn);
    }

    public String getSnippet(String snippet, String version) {
        String content = null;
        try {
            if (version.isEmpty()) {
                content = SnippetHelper.getBody(snippet);
            } else {
                content = SnippetHelper.getBody(snippet, version);
            }
        } catch (IOException e) {
            logger.warn("Error: " + e.getLocalizedMessage(), e);
        }
        return content;
    }

    public String getSnippet(String snippet) {
        return getSnippet(snippet, "");
    }

    public String getSnippet(String snippet, Map<String, String> replacements) {
        String content = getSnippet(snippet);
        if (!Strings.isNullOrEmpty(content)) {
            content = findAndReplaceInSnippet(content, replacements);
        }
        return content;
    }

    private String findAndReplaceInSnippet(String content, Map<String, String> replacements) {
        for (Map.Entry<String, String> entry: replacements.entrySet()) {
            if (content.contains(entry.getKey())) {
                content = content.replaceAll(entry.getKey(), entry.getValue());
            }
        }
        return content;
    }

    protected void createNewVolumes(Map<String, Workspace> workspaces, Tkn tkn) throws IOException{
        int counter = 0;
        for(Map.Entry<String, Workspace> entry: workspaces.entrySet()) {
            Workspace workspace = entry.getValue();
            if (workspace != null
                    && workspace.getKind() == Workspace.Kind.PVC
                    && workspace.getResource().isEmpty()
                    && workspace.getItems().size() > 0) {
                if (KIND_VCT.equals(workspace.getItems().get("type"))) {
                    VirtualFile vf = createVCT(workspace.getItems(), counter);
                    workspace.getItems().put("file", vf.getPath());
                    counter++;
                } else {
                    String name = createNewPVC(workspace.getItems(), tkn);
                    workspace.setResource(name);
                }
            }
        }
    }

    private String createNewPVC(Map<String, String> items, Tkn tkn) throws IOException {
        String name = items.get("name");
        tkn.createPVC(name, items.get("accessMode"), items.get("size"), items.get("unit"));
        return name;
    }

    private VirtualFile createVCT(Map<String, String> items, int counter) throws IOException {
        ObjectNode newVCT = YAMLBuilder.createVCT(items.get("name"), items.get("accessMode"), items.get("size"), items.get("unit"));
        return VirtualFileHelper.createVirtualFile("vct-" + counter + ".yaml", YAMLHelper.JSONToYAML(newVCT), false);
    }
}
