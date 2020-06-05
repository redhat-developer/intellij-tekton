/*******************************************************************************
 * Copyright (c) 2020 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package com.redhat.devtools.intellij.tektoncd.tree;

import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.project.Project;
import com.intellij.ui.DoubleClickListener;
import com.intellij.util.ui.tree.WideSelectionTreeUI;
import org.jetbrains.annotations.NotNull;

import javax.swing.JTree;
import javax.swing.tree.TreePath;
import java.awt.event.MouseEvent;


public class DoubleClickHandler {
    public interface Handler {
        void onDoubleClick(TreePath path);
    }

    protected DoubleClickHandler() {
    }

    public static void install(final JTree tree, @NotNull final Handler handler) {
        new TreeDoubleClickListener(handler, tree).installOn(tree);
    }

    public static class TreeDoubleClickListener extends DoubleClickListener {

        private final Handler handler;
        private final JTree tree;

        public TreeDoubleClickListener(Handler handler, JTree tree) {
            this.handler = handler;
            this.tree = tree;
        }

        @Override
        protected boolean onDoubleClick(MouseEvent event) {
            final TreePath clickPath = tree.getUI() instanceof WideSelectionTreeUI ? tree.getClosestPathForLocation(event.getX(), event.getY())
                    : tree.getPathForLocation(event.getX(), event.getY());
            if (clickPath == null) {
                return false;
            }

            final DataContext dataContext = DataManager.getInstance().getDataContext(tree);
            final Project project = CommonDataKeys.PROJECT.getData(dataContext);
            if (project == null) {
                return false;
            }

            TreePath selectionPath = tree.getSelectionPath();
            if (!clickPath.equals(selectionPath)) {
                return false;
            }

            if (event.getClickCount() == 2) {
                processDoubleClick(selectionPath);
                return true;
            }
            return false;
        }

        protected void processDoubleClick(@NotNull TreePath treePath) {
            if (handler != null) {
                handler.onDoubleClick(treePath);
            }
        }
    }
}
