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

import com.redhat.devtools.intellij.tektoncd.tkn.Bundle;
import com.redhat.devtools.intellij.tektoncd.tkn.Resource;

import javax.swing.AbstractAction;
import javax.swing.JList;
import java.awt.event.ActionEvent;

public class RemoveFromBundleAction extends AbstractAction {

    private final Bundle bundle;
    private final JList<Resource> from;

    public RemoveFromBundleAction(JList<Resource> from, Bundle bundle) {
        this.from = from;
        this.bundle = bundle;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Resource selected = this.from.getSelectedValue();
        bundle.addResource(selected);
    }
}
