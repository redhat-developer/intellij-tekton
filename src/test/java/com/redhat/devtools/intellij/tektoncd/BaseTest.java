/*******************************************************************************
 * Copyright (c) 2020 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc.
 ******************************************************************************/
package com.redhat.devtools.intellij.tektoncd;

import com.intellij.openapi.project.Project;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import com.intellij.ui.treeStructure.Tree;
import com.redhat.devtools.intellij.tektoncd.tkn.Tkn;
import com.redhat.devtools.intellij.tektoncd.tkn.TknCli;
import com.redhat.devtools.intellij.tektoncd.tree.ParentableNode;
import com.redhat.devtools.intellij.tektoncd.tree.PipelineNode;
import com.redhat.devtools.intellij.tektoncd.tree.TektonRootNode;
import com.redhat.devtools.intellij.tektoncd.tree.TektonTreeStructure;
import org.apache.commons.io.IOUtils;

import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public abstract class BaseTest extends BasePlatformTestCase {

    protected Project project;
    protected Tkn tkn;
    protected Tree tree;
    protected TektonTreeStructure tektonTreeStructure;
    protected TreeSelectionModel model;
    protected TreePath path;
    protected ParentableNode parentableNode;
    protected TektonRootNode tektonRootNode;
    protected PipelineNode pipelineNode;

    public void setUp() throws Exception {
        super.setUp();
        project = mock(Project.class);
        tkn = mock(TknCli.class);
        tree = mock(Tree.class);
        tektonTreeStructure = mock(TektonTreeStructure.class);
        model = mock(TreeSelectionModel.class);
        path = mock(TreePath.class);
        parentableNode = mock(ParentableNode.class);
        tektonRootNode = mock(TektonRootNode.class);
        pipelineNode = mock(PipelineNode.class);


        when(path.getLastPathComponent()).thenReturn(parentableNode);
        when(model.getSelectionPath()).thenReturn(path);
        when(model.getSelectionPaths()).thenReturn(new TreePath[] {path});
        when(tree.getSelectionModel()).thenReturn(model);
        when(tektonTreeStructure.getRootElement()).thenReturn(tektonRootNode);
        when(tektonRootNode.getTkn()).thenReturn(tkn);
        when(tree.getClientProperty(Constants.STRUCTURE_PROPERTY)).thenReturn(tektonTreeStructure);
    }

    protected String load(String name) throws IOException {
        return IOUtils.toString(BaseTest.class.getResource("/" + name), StandardCharsets.UTF_8);
    }
}
