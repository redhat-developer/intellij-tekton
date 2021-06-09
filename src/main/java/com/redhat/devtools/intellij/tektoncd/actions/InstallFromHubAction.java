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
package com.redhat.devtools.intellij.tektoncd.actions;

import com.redhat.devtools.intellij.tektoncd.ui.hub.HubItem;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import javax.swing.Action;

public class InstallFromHubAction implements Action {
    private String text;
    private Supplier<HubItem> item;
    private Supplier<String> version;
    private Supplier<BiConsumer<HubItem, String>> doInstallAction;

    public InstallFromHubAction(String text, Supplier<HubItem> item, Supplier<String> version, Supplier<BiConsumer<HubItem, String>> doInstallAction) {
        this.text = text;
        this.item = item;
        this.version = version;
        this.doInstallAction = doInstallAction;
    }

    @Override
    public Object getValue(String key) {
        return this.text;
    }

    @Override
    public void putValue(String key, Object value) {

    }

    @Override
    public void setEnabled(boolean b) {

    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {

    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        doInstallAction.get().accept(item.get(), version.get());
    }
}
