/*******************************************************************************
 *  Copyright (c) 2022 Red Hat, Inc.
 *  Distributed under license by Red Hat, Inc. All rights reserved.
 *  This program is made available under the terms of the
 *  Eclipse Public License v2.0 which accompanies this distribution,
 *  and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 *  Contributors:
 *  Red Hat, Inc.
 ******************************************************************************/
package com.redhat.devtools.intellij.tektoncd.actions.bundle.createwizard;

import com.intellij.ui.treeStructure.Tree;
import com.redhat.devtools.intellij.tektoncd.tkn.Bundle;
import com.redhat.devtools.intellij.tektoncd.tkn.Resource;
import com.redhat.devtools.intellij.tektoncd.tree.ParentableNode;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

public class AddToBundleAction extends AbstractAction {

    private final Bundle bundle;
    private final Tree from;

    public AddToBundleAction(Tree from, Bundle bundle) {
        this.from = from;
        this.bundle = bundle;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object selected = this.from.getLastSelectedPathComponent();
        if (selected instanceof ParentableNode) {
            String resourceName = ((ParentableNode)selected).getName();
            String kind = ((ParentableNode)((ParentableNode)selected).getParent()).getName();
            bundle.addResource(new Resource(resourceName, kind));
        }
    }
}
