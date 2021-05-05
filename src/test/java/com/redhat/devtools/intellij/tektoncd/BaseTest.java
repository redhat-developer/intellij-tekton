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
import com.redhat.devtools.intellij.tektoncd.tkn.Tkn;
import com.redhat.devtools.intellij.tektoncd.tkn.TknCli;
import com.redhat.devtools.intellij.tektoncd.tree.ParentableNode;
import com.redhat.devtools.intellij.tektoncd.tree.TektonRootNode;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.IOUtils;
import org.junit.Before;


import static org.mockito.Mockito.mock;

public class BaseTest {

    protected Project project;
    protected Tkn tkn;
    protected ParentableNode parentableNode;
    protected TektonRootNode tektonRootNode;

    @Before
    public void setUp() throws Exception {
        project = mock(Project.class);
        tkn = mock(TknCli.class);
        parentableNode = mock(ParentableNode.class);
        tektonRootNode = mock(TektonRootNode.class);
    }

    protected String load(String name) throws IOException {
        return IOUtils.toString(BaseTest.class.getResource("/" + name), StandardCharsets.UTF_8);
    }
}
