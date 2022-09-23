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

import com.redhat.devtools.intellij.tektoncd.tkn.Bundle;
import com.redhat.devtools.intellij.tektoncd.tkn.Resource;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;
import java.util.function.Supplier;

public class RemoveFromBundleAction extends AbstractAction {

    private Bundle bundle;
    private Runnable updateErrorPanel, updateBundlePanel;
    private Supplier<Resource> getSelectedLayer;

    public RemoveFromBundleAction(Bundle bundle, Runnable updateErrorPanel, Runnable updateBundlePanel, Supplier<Resource> getSelectedLayer) {
        this.bundle = bundle;
        this.updateErrorPanel = updateErrorPanel;
        this.updateBundlePanel = updateBundlePanel;
        this.getSelectedLayer = getSelectedLayer;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        updateErrorPanel.run();
        Resource selected = getSelectedLayer.get();
        bundle.removeResource(selected);
        updateBundlePanel.run();
    }
}
