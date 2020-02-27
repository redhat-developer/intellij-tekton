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
package com.redhat.devtools.intellij.common.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.ui.treeStructure.Tree;

import javax.swing.tree.TreePath;
import java.awt.*;
import java.util.stream.Stream;

public abstract class TreeAction extends AnAction {


    private Class[] filters;

    public TreeAction(Class... filters) {
        this.filters = filters;
    }

    protected Tree getTree(AnActionEvent e) {
        return (Tree) e.getData(PlatformDataKeys.CONTEXT_COMPONENT);
    }

    @Override
    public void update(AnActionEvent e) {
        boolean visible = false;
        Component comp = getTree(e);

        if (comp instanceof Tree) {
            TreePath selectPath = ((Tree) comp).getSelectionModel().getSelectionPath();
            visible = isVisible(selectPath.getLastPathComponent());
        }
        e.getPresentation().setVisible(visible);
    }

    public boolean isVisible(Object selected) {
        return Stream.of(filters).anyMatch(cl -> cl.isAssignableFrom(selected.getClass()));
    }


    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        Tree tree = getTree(anActionEvent);
        TreePath selectedPath = tree.getSelectionModel().getSelectionPath();
        Object selected = selectedPath.getLastPathComponent();
        actionPerformed(anActionEvent, selectedPath, selected);
    }

    public abstract void actionPerformed(AnActionEvent anActionEvent, TreePath path, Object selected);
}
