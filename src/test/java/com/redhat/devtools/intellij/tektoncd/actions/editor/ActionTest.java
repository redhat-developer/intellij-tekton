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
package com.redhat.devtools.intellij.tektoncd.actions.editor;

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.redhat.devtools.intellij.tektoncd.FixtureBaseTest;


import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ActionTest extends FixtureBaseTest {

    protected Editor editor;
    protected Document document;
    protected DataContext dataContext;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        editor = mock(Editor.class);
        document = mock(Document.class);
        dataContext = mock(DataContext.class);

        when(editor.getDocument()).thenReturn(document);
        when(document.getText()).thenReturn("content");

    }
}
