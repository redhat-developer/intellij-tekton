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

import com.redhat.devtools.intellij.tektoncd.utils.TreeHelper;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;

import javax.swing.JTree;
import javax.swing.tree.TreePath;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;

public class TektonTreeDoubleClickListenerTest {

    private TektonTreeDoubleClickListener tektonTreeDoubleClickListener;
    private TreePath treePath;

    @Before
    public void setUp() {
        JTree jTree = mock(JTree.class);
        treePath = mock(TreePath.class);
        tektonTreeDoubleClickListener = new TektonTreeDoubleClickListener(jTree);
    }

    @Test
    public void ProcessDoubleClick_OpenTektonResourceInEditor() {
        try (MockedStatic<TreeHelper> treeHelperMockedStatic = mockStatic(TreeHelper.class)) {
            tektonTreeDoubleClickListener.processDoubleClick(treePath);
            treeHelperMockedStatic.verify(() -> TreeHelper.openTektonResourceInEditor(any()), times(1));
        }
    }

}
