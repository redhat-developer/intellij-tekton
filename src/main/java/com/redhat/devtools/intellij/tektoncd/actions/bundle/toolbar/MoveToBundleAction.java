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
package com.redhat.devtools.intellij.tektoncd.actions.bundle.toolbar;

import com.redhat.devtools.intellij.common.tree.LabelAndIconDescriptor;
import com.redhat.devtools.intellij.tektoncd.tkn.Bundle;
import com.redhat.devtools.intellij.tektoncd.tkn.Resource;
import com.redhat.devtools.intellij.tektoncd.tree.ClusterTaskNode;
import com.redhat.devtools.intellij.tektoncd.tree.ParentableNode;
import com.redhat.devtools.intellij.tektoncd.tree.PipelineNode;
import com.redhat.devtools.intellij.tektoncd.tree.TaskNode;
import com.redhat.devtools.intellij.tektoncd.utils.TreeHelper;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.event.ActionEvent;
import java.util.function.Supplier;

public class MoveToBundleAction extends AbstractAction {

    private Bundle bundle;
    private Runnable updateErrorPanel, updateBundlePanel;
    private Supplier<Object> getLastSelectedPathComponent;

    public MoveToBundleAction(Bundle bundle, Runnable updateErrorPanel, Runnable updateBundlePanel, Supplier<Object> getLastSelectedPathComponent) {
        this.bundle = bundle;
        this.updateErrorPanel = updateErrorPanel;
        this.updateBundlePanel = updateBundlePanel;
        this.getLastSelectedPathComponent = getLastSelectedPathComponent;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        LabelAndIconDescriptor descriptor = getLastSelectedNode();
        if (descriptor != null &&
            isActionEnabled((ParentableNode<?>) descriptor.getElement())) {
            updateErrorPanel.run();
            if(!bundle.hasSpace()) {
                return;
            }
            String resourceName = descriptor.getName();
            String kind = TreeHelper.getKindFromNode((ParentableNode<?>) descriptor.getElement());
            Icon icon = descriptor.getIcon();
            Resource resource = new Resource(resourceName, kind, icon);
            if(bundle.hasResource(resource)) {
                return;
            }
            bundle.addResource(resource);
            updateBundlePanel.run();
        }
    }

    private boolean isActionEnabled(ParentableNode element) {
        return element instanceof PipelineNode ||
                element instanceof TaskNode ||
                element instanceof ClusterTaskNode;
    }

    private LabelAndIconDescriptor getLastSelectedNode() {
        Object selection = getLastSelectedPathComponent.get();
        if (!(selection instanceof DefaultMutableTreeNode)) {
            return null;
        }
        Object userObject = ((DefaultMutableTreeNode)selection).getUserObject();
        return userObject == null ? null : (LabelAndIconDescriptor) userObject;
    }
}
