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
package com.redhat.devtools.intellij.tektoncd.ui.toolwindow.debug;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.content.ContentManager;
import com.redhat.devtools.intellij.tektoncd.utils.model.debug.DebugModel;
import com.redhat.devtools.intellij.tektoncd.tkn.Tkn;
import java.util.HashMap;
import java.util.Map;

public class DebugPanelBuilder {

    private static final String TEKTONDEBUGTOOLWINDOW_ID = "TektonDebug";
    private static DebugPanelBuilder instance;
    private Map<String, DebugTabPanel> resourceXTab;
    private Tkn tkn;

    private DebugPanelBuilder(Tkn tkn) {
        this.resourceXTab = new HashMap<>();
        this.tkn = tkn;
    }

    public static DebugPanelBuilder instance(Tkn tkn) {
        if (instance == null) {
            instance = new DebugPanelBuilder(tkn);
        }
        return instance;
    }

    public void addContent(DebugModel model) {
        DebugTabPanel debugTabPanel = resourceXTab.getOrDefault(model.getResource(), null);
        if (debugTabPanel != null) {
            debugTabPanel.updateModel(model);
        } else {
            ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
            debugTabPanel = new DebugTabPanel(model.getResource(), model, tkn);
            Content panel = contentFactory.createContent(debugTabPanel.getComponent(), debugTabPanel.getDisplayName(), true);
            resourceXTab.put(model.getResource(), debugTabPanel);
            panel.setCloseable(true);
            getContentManager().addContent(panel);
        }

        ensureTabIsSelected(debugTabPanel.getDisplayName());
        ensureToolWindowOpened();
    }

    private ToolWindow getDebugToolWindow() {
        Project project = tkn.getProject();
        return ToolWindowManager.getInstance(project).getToolWindow(TEKTONDEBUGTOOLWINDOW_ID);
    }

    private ContentManager getContentManager() {
        return getDebugToolWindow().getContentManager();
    }

    private void ensureTabIsSelected(String tabName) {
        ContentManager contentManager = getContentManager();
        Content content = contentManager.findContent(tabName);
        contentManager.setSelectedContent(content);
    }

    private void ensureToolWindowOpened() {
        ToolWindow toolWindow = getDebugToolWindow();
        if (toolWindow.isVisible()
                && toolWindow.isActive()
                && toolWindow.isAvailable()) {
            return;
        }
        toolWindow.setToHideOnEmptyContent(true);
        toolWindow.setAvailable(true, null);
        toolWindow.activate(null);
        toolWindow.show(null);
    }

}
