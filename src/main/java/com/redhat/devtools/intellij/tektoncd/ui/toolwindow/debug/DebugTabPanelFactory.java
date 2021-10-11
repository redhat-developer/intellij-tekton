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

public class DebugTabPanelFactory {

    private static final String TEKTONDEBUGTOOLWINDOW_ID = "TektonDebug";
    private static DebugTabPanelFactory instance;
    private Map<String, DebugTabPanel> resourceXTab;

    private DebugTabPanelFactory() {
        this.resourceXTab = new HashMap<>();
    }

    public static DebugTabPanelFactory instance() {
        if (instance == null) {
            instance = new DebugTabPanelFactory();
        }
        return instance;
    }

    public Content getResourceDebugPanel(String resource) {
        DebugTabPanel debugTabPanel = resourceXTab.getOrDefault(resource, null);
        if (debugTabPanel != null) {
            return getContentManager(debugTabPanel.getProject()).findContent(debugTabPanel.getDisplayName());
        }
        return null;
    }

    public void addContent(Project project, Tkn tkn, DebugModel model) {
        DebugTabPanel debugTabPanel = resourceXTab.getOrDefault(model.getResource(), null);
        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content panel = getResourceDebugPanel(model.getResource());

        if (debugTabPanel != null) {
            if (panel == null) {
                panel = contentFactory.createContent(debugTabPanel.getComponent(), debugTabPanel.getDisplayName(), true);
            }
            debugTabPanel.updateModel(model);
        } else {
            debugTabPanel = new DebugTabPanel(model.getResource(), model, project, tkn);
            panel = contentFactory.createContent(debugTabPanel.getComponent(), debugTabPanel.getDisplayName(), true);
            resourceXTab.put(model.getResource(), debugTabPanel);
        }

        panel.setCloseable(true);
        getContentManager(project).addContent(panel);
        ensureTabIsSelected(project, debugTabPanel.getDisplayName());
        ensureToolWindowOpened(project);
    }

    private ToolWindow getDebugToolWindow(Project project) {
        return ToolWindowManager.getInstance(project).getToolWindow(TEKTONDEBUGTOOLWINDOW_ID);
    }

    private ContentManager getContentManager(Project project) {
        return getDebugToolWindow(project).getContentManager();
    }

    private void ensureTabIsSelected(Project project, String tabName) {
        ContentManager contentManager = getContentManager(project);
        Content content = contentManager.findContent(tabName);
        contentManager.setSelectedContent(content);
    }

    private void ensureToolWindowOpened(Project project) {
        ToolWindow toolWindow = getDebugToolWindow(project);
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
