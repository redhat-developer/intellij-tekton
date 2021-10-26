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

import com.redhat.devtools.intellij.tektoncd.tkn.Tkn;
import com.redhat.devtools.intellij.tektoncd.utils.model.debug.DebugModel;
import javax.swing.Icon;

public class DebugToolbarContinueWithFailureAction extends DebugToolbarContinueAction {

    public DebugToolbarContinueWithFailureAction(String text, String description, Icon icon, Tkn tkn, DebugModel model) {
        super(text, description, icon, tkn, model);
    }

    @Override
    protected String getScript() {
        return "/tekton/debug/scripts/debug-fail-continue";
    }
}
