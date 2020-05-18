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
package com.redhat.devtools.intellij.tektoncd.actions.triggers;

import com.google.common.base.Strings;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.redhat.devtools.intellij.tektoncd.actions.TektonAction;
import com.redhat.devtools.intellij.tektoncd.tkn.Tkn;
import com.redhat.devtools.intellij.tektoncd.tree.TriggerTemplatesNode;

import javax.swing.tree.TreePath;

import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_TRIGGERTEMPLATES;

public class CreateTriggerTemplateAction extends TektonAction {
    public CreateTriggerTemplateAction() { super(TriggerTemplatesNode.class); }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent, TreePath path, Object selected, Tkn tkncli) {
        String namespace = ((TriggerTemplatesNode)getElement(selected)).getParent().getName();
        String content = getSnippet(namespace, "Tekton: TriggerTemplate");

        if (!Strings.isNullOrEmpty(content)) {
            createAndOpenVirtualFile(anActionEvent.getProject(), namespace, namespace + "-newtriggertemplate.yaml", content, KIND_TRIGGERTEMPLATES);
        }
    }
}
