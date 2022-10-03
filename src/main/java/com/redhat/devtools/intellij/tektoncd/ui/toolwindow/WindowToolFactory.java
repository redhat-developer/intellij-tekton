/*******************************************************************************
 * Copyright (c) 2019 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package com.redhat.devtools.intellij.tektoncd.ui.toolwindow;

import com.intellij.ide.util.treeView.NodeRenderer;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.project.ProjectManagerListener;
import com.intellij.openapi.ui.Divider;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.OnePixelSplitter;
import com.intellij.ui.PopupHandler;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.tree.AsyncTreeModel;
import com.intellij.ui.tree.StructureTreeModel;
import com.intellij.ui.treeStructure.Tree;
import com.redhat.devtools.intellij.common.utils.function.TriConsumer;
import com.redhat.devtools.intellij.tektoncd.Constants;
import com.redhat.devtools.intellij.tektoncd.listener.TektonTreeDoubleClickListener;
import com.redhat.devtools.intellij.tektoncd.listener.TektonTreePopupMenuListener;
import com.redhat.devtools.intellij.tektoncd.tkn.TknCliFactory;
import com.redhat.devtools.intellij.tektoncd.tree.MutableTektonModelSynchronizer;
import com.redhat.devtools.intellij.tektoncd.tree.TektonTreeStructure;
import com.redhat.devtools.intellij.tektoncd.ui.hub.HubDetailsDialog;
import com.redhat.devtools.intellij.tektoncd.ui.hub.HubItem;
import com.redhat.devtools.intellij.tektoncd.ui.hub.HubItemPanelsBoard;
import com.redhat.devtools.intellij.tektoncd.ui.hub.HubModel;
import com.redhat.devtools.intellij.tektoncd.utils.TreeHelper;
import org.jetbrains.annotations.NotNull;

import javax.swing.JComponent;
import javax.swing.JPanel;
import java.lang.reflect.InvocationTargetException;
import java.util.Optional;
import java.util.function.BiConsumer;

import static com.redhat.devtools.intellij.tektoncd.Constants.TEKTON_TOOLBAR_ACTION_GROUP_ID;
import static com.redhat.devtools.intellij.tektoncd.Constants.TOOLBAR_PLACE;
import static com.redhat.devtools.intellij.tektoncd.ui.UIConstants.SEARCH_FIELD_BORDER_COLOR;

public class WindowToolFactory implements ToolWindowFactory {
    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        try {
            ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();

            TektonTreeStructure structure = new TektonTreeStructure(project);
            StructureTreeModel<TektonTreeStructure> model = TreeHelper.buildModel(structure, project);
            new MutableTektonModelSynchronizer<>(model, structure, structure);
            Tree tree = new Tree(new AsyncTreeModel(model, project));
            tree.putClientProperty(Constants.STRUCTURE_PROPERTY, structure);
            tree.setCellRenderer(new NodeRenderer());
            ActionManager actionManager = ActionManager.getInstance();
            ActionGroup group = (ActionGroup)actionManager.getAction("com.redhat.devtools.intellij.tektoncd.tree");
            PopupHandler.installPopupHandler(tree, group, ActionPlaces.POPUP, actionManager, new TektonTreePopupMenuListener());

            new TektonTreeDoubleClickListener(tree);

            TknCliFactory.getInstance().getTkn(project).whenComplete((tkn, err) -> {
                HubModel hubModel = new HubModel(project, tkn, null);

                JPanel hubItemsListPanel = new HubItemPanelsBoard(hubModel, getDoSelectAction(project, hubModel))
                        .withRecommended()
                        .withInstalled()
                        .build(Optional.empty());

                SimpleToolWindowPanel panel = createPanelWithTree(tree);
                addToolbarMenuToPanel(panel);
                OnePixelSplitter tabPanel = createTabPanel(panel, hubItemsListPanel);

                toolWindow.getContentManager().addContent(contentFactory.createContent(tabPanel, "", false));
                executeOnProjectClosing(project, () -> {
                    structure.dispose();
                    hubModel.dispose();
                });
            });
        } catch (IllegalAccessException | InvocationTargetException | InstantiationException | NoSuchMethodException e) {
            throw new RuntimeException((e));
        }
    }

    private SimpleToolWindowPanel createPanelWithTree(Tree tree) {
        SimpleToolWindowPanel panel = new SimpleToolWindowPanel(true, true);
        panel.setContent(new JBScrollPane(tree));
        return panel;
    }

    private void addToolbarMenuToPanel(SimpleToolWindowPanel panel) {
        ActionManager actionManager = ActionManager.getInstance();
        ActionToolbar actionToolbar = actionManager.createActionToolbar(TOOLBAR_PLACE, (ActionGroup) actionManager.getAction(TEKTON_TOOLBAR_ACTION_GROUP_ID), true);
        panel.setToolbar(actionToolbar.getComponent());
    }

    private OnePixelSplitter createTabPanel(JComponent firstComp, JComponent secondComp) {
        OnePixelSplitter tabPanel = new OnePixelSplitter(true, 0.37F) {
            protected Divider createDivider() {
                Divider divider = super.createDivider();
                divider.setBackground(SEARCH_FIELD_BORDER_COLOR);
                return divider;
            }
        };
        tabPanel.setFirstComponent(firstComp);
        tabPanel.setSecondComponent(secondComp);
        return tabPanel;
    }

    private void executeOnProjectClosing(Project project, Runnable runnable) {
        ProjectManager.getInstance().addProjectManagerListener(project, new ProjectManagerListener() {
            @Override
            public void projectClosing(@NotNull Project project) {
                runnable.run();
            }
        });
    }

    private BiConsumer<HubItem, TriConsumer<HubItem, String, String>> getDoSelectAction(Project project, HubModel model) {
        return (item, callback) -> {
            HubDetailsDialog dialog = new HubDetailsDialog(item.getResource().getName() + " details", project, model);
            dialog.setModal(false);
            dialog.show(item, callback);
        };
    }
}
