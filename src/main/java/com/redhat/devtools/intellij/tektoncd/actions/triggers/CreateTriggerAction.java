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
package com.redhat.devtools.intellij.tektoncd.actions.triggers;

import com.google.common.base.Strings;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.redhat.devtools.intellij.tektoncd.actions.TektonAction;
import com.redhat.devtools.intellij.tektoncd.telemetry.TelemetryService;
import com.redhat.devtools.intellij.tektoncd.tkn.Tkn;
import com.redhat.devtools.intellij.tektoncd.tree.NamespaceNode;
import com.redhat.devtools.intellij.tektoncd.tree.ParentableNode;
import com.redhat.devtools.intellij.tektoncd.utils.VirtualFileHelper;
import com.redhat.devtools.intellij.telemetry.core.service.TelemetryMessageBuilder;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.swing.tree.TreePath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import static com.redhat.devtools.intellij.tektoncd.Constants.API_VERSION_PLACEHOLDER;
import static com.redhat.devtools.intellij.tektoncd.telemetry.TelemetryService.PROP_RESOURCE_KIND;
import static com.redhat.devtools.intellij.telemetry.core.util.AnonymizeUtils.anonymizeResource;

public abstract class CreateTriggerAction extends TektonAction {

    private static final Logger logger = LoggerFactory.getLogger(CreateTriggerAction.class);

    public CreateTriggerAction(Class... filters) {
        super(filters);
    }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent, TreePath path, Object selected, Tkn tkncli) {
        TelemetryMessageBuilder.ActionMessage telemetry = createActionMessage(getActionName(), getKind());
        ParentableNode<NamespaceNode> item = getElement(selected);
        String namespace = item.getParent().getName();
        String name = namespace + getNewFilename();
        try {
            String content = getSnippet(tkncli, getSnippetName());
            createAndOpenTriggerVirtualFile(content, item, anActionEvent.getProject(), name, namespace, getKind(), telemetry);
        } catch (IOException e) {
            telemetry
                    .error(anonymizeResource(name, namespace, e.getMessage()))
                    .send();
            logger.warn(getErrorMessage() + e.getLocalizedMessage(), e);
        }
    }

    protected TelemetryMessageBuilder.ActionMessage createActionMessage(String actionName, String kind) {
        return TelemetryService.instance()
                .action(actionName)
                .property(PROP_RESOURCE_KIND, kind);
    }

    protected void createAndOpenTriggerVirtualFile(String content, ParentableNode<NamespaceNode> item, Project project, String name, String namespace, String kind, TelemetryMessageBuilder.ActionMessage telemetry) throws IOException {
        if (Strings.isNullOrEmpty(content)) {
            telemetry
                    .error("snippet content empty")
                    .send();
        } else {
            VirtualFileHelper.createAndOpenVirtualFile(project, namespace, name, content, kind, item);
            telemetry.send();
        }
    }

    protected String getSnippet(Tkn tkn, String snippet) throws IOException {
        Map<String, String> replacements = new HashMap<>();
        replacements.put(API_VERSION_PLACEHOLDER, tkn.getTektonTriggersApiVersion());
        return getSnippet(snippet, replacements);
    }

    public abstract String getKind();

    public abstract String getActionName();

    public abstract String getNewFilename();

    public abstract String getSnippetName();

    public abstract String getErrorMessage();
}
