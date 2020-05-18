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
package com.redhat.devtools.intellij.tektoncd.actions.condition;

import com.google.common.base.Strings;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.redhat.devtools.intellij.tektoncd.actions.TektonAction;
import com.redhat.devtools.intellij.tektoncd.tkn.Tkn;
import com.redhat.devtools.intellij.tektoncd.tree.ConditionsNode;

import javax.swing.tree.TreePath;

import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_CONDITIONS;

public class CreateConditionAction extends TektonAction {
    public CreateConditionAction() { super(ConditionsNode.class); }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent, TreePath path, Object selected, Tkn tkncli) {
        String namespace = ((ConditionsNode)getElement(selected)).getParent().getName();
        String content = getSnippet(namespace, "Tekton: Condition");

        if (!Strings.isNullOrEmpty(content)) {
            createAndOpenVirtualFile(anActionEvent.getProject(), namespace, namespace + "-newcondition.yaml", content, KIND_CONDITIONS);
        }
    }
}
