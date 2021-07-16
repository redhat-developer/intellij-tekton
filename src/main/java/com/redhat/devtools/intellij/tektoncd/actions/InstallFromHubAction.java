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

import com.redhat.devtools.intellij.common.utils.function.TriConsumer;
import com.redhat.devtools.intellij.tektoncd.ui.hub.HubItem;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.util.function.Supplier;
import javax.swing.Action;

public class InstallFromHubAction implements Action {
    private String text;
    private Supplier<HubItem> item;
    private Supplier<String> kind, version;
    private Supplier<TriConsumer<HubItem, String, String>> doInstallAction;

    public InstallFromHubAction(String text, Supplier<HubItem> item, Supplier<String> kind, Supplier<String> version, Supplier<TriConsumer<HubItem, String, String>> doInstallAction) {
        this.text = text;
        this.item = item;
        this.kind = kind;
        this.version = version;
        this.doInstallAction = doInstallAction;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public Object getValue(String key) {
        if (key.equals("Name")) {
            return this.text;
        }
        return null;
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
        doInstallAction.get().accept(item.get(), kind.get(), version.get());
    }
}
