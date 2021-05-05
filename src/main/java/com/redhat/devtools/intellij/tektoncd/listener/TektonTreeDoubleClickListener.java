/*******************************************************************************
 * Copyright (c) 2021 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc.
 ******************************************************************************/
package com.redhat.devtools.intellij.tektoncd.listener;

import com.redhat.devtools.intellij.common.listener.TreeDoubleClickListener;
import com.redhat.devtools.intellij.tektoncd.utils.TreeHelper;
import javax.swing.JTree;
import javax.swing.tree.TreePath;
import org.jetbrains.annotations.NotNull;

public class TektonTreeDoubleClickListener extends TreeDoubleClickListener {

    public TektonTreeDoubleClickListener(JTree tree) {
        super(tree);
    }

    @Override
    protected void processDoubleClick(@NotNull TreePath treePath) {
        TreeHelper.openTektonResourceInEditor(treePath);
    }
}
