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
package com.redhat.devtools.intellij.tektoncd;

import com.intellij.ide.util.treeView.NodeRenderer;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.PopupHandler;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.tree.AsyncTreeModel;
import com.intellij.ui.tree.StructureTreeModel;
import com.intellij.ui.treeStructure.Tree;
import com.redhat.devtools.intellij.common.tree.MutableModelSynchronizer;
import com.redhat.devtools.intellij.tektoncd.tree.TektonTreeStructure;
import org.jetbrains.annotations.NotNull;

public class WindowToolFactory implements ToolWindowFactory {
    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();

        TektonTreeStructure structure = new TektonTreeStructure(project);
        StructureTreeModel<TektonTreeStructure> model = new StructureTreeModel<>(structure);
        new MutableModelSynchronizer<Object>(model, structure, structure);
        Tree tree = new Tree(new AsyncTreeModel(model));
        tree.putClientProperty(Constants.STRUCTURE_PROPERTY, structure);
        tree.setCellRenderer(new NodeRenderer());
        PopupHandler.installPopupHandler(tree, "com.redhat.devtools.intellij.tektoncd.tree", ActionPlaces.UNKNOWN);
        toolWindow.getContentManager().addContent(contentFactory.createContent(new JBScrollPane(tree), "", false));
    }
}
