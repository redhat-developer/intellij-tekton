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
import javax.swing.tree.TreePath;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class MoveToBundleAction extends AbstractAction {

    private Bundle bundle;
    private Runnable updateErrorPanel, updateBundlePanel;
    private Supplier<TreePath[]> getSelectionPaths;

    public MoveToBundleAction(Bundle bundle, Runnable updateErrorPanel, Runnable updateBundlePanel, Supplier<TreePath[]> getSelectionPaths) {
        this.bundle = bundle;
        this.updateErrorPanel = updateErrorPanel;
        this.updateBundlePanel = updateBundlePanel;
        this.getSelectionPaths = getSelectionPaths;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        List<LabelAndIconDescriptor> labelAndIconDescriptors = getSelectedNodes();
        if (bundle.getEmptyLayers() < labelAndIconDescriptors.size()) {
            updateErrorPanel.run();
            return;
        }

        for (LabelAndIconDescriptor descriptor: labelAndIconDescriptors) {
            String resourceName = descriptor.getName();
            String kind = TreeHelper.getKindFromNode((ParentableNode<?>) descriptor.getElement());
            Icon icon = descriptor.getIcon();
            Resource resource = new Resource(resourceName, kind, icon);
            if(bundle.hasResource(resource)) {
                continue;
            }
            bundle.addResource(resource);
        }
        updateBundlePanel.run();
    }

    private boolean isActionEnabled(ParentableNode element) {
        return element instanceof PipelineNode ||
                element instanceof TaskNode ||
                element instanceof ClusterTaskNode;
    }

    private List<LabelAndIconDescriptor> getSelectedNodes() {
        List<LabelAndIconDescriptor> labelAndIconDescriptors = new ArrayList<>();
        TreePath[] treePaths = getSelectionPaths.get();
        for (TreePath treePath : treePaths) {
            Object selection = treePath.getLastPathComponent();
            if (!(selection instanceof DefaultMutableTreeNode)) {
                continue;
            }
            Object userObject = ((DefaultMutableTreeNode)selection).getUserObject();
            if (userObject != null &&
                    isActionEnabled((ParentableNode<?>) ((LabelAndIconDescriptor)userObject).getElement())) {
                labelAndIconDescriptors.add((LabelAndIconDescriptor) userObject);
            }
        }
        return labelAndIconDescriptors;
    }
}
