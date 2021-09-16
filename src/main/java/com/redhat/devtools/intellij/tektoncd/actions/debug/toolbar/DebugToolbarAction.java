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
package com.redhat.devtools.intellij.tektoncd.actions.debug.toolbar;

import com.intellij.openapi.project.DumbAwareAction;
import com.redhat.devtools.intellij.tektoncd.utils.model.debug.DebugModel;
import com.redhat.devtools.intellij.tektoncd.tkn.Tkn;
import java.util.function.Supplier;
import javax.swing.Icon;

public abstract class DebugToolbarAction extends DumbAwareAction {

    protected Tkn tkn;
    protected Supplier<DebugModel> model;

    public DebugToolbarAction(String text, String description, Icon icon, Tkn tkn, Supplier<DebugModel> model) {
        super(text, description, icon);
        this.tkn = tkn;
        this.model = model;
    }
}
