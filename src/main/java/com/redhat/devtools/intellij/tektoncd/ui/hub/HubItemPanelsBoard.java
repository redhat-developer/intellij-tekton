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
package com.redhat.devtools.intellij.tektoncd.ui.hub;

import com.intellij.notification.NotificationType;
import com.intellij.openapi.ui.Divider;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.OnePixelSplitter;
import com.intellij.ui.SearchTextField;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.ui.border.CustomLineBorder;
import com.intellij.ui.components.JBPanelWithEmptyText;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextField;
import com.intellij.ui.scale.JBUIScale;
import com.intellij.util.Alarm;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.StatusText;
import com.redhat.devtools.intellij.common.utils.ExecHelper;
import com.redhat.devtools.intellij.common.utils.UIHelper;
import com.redhat.devtools.intellij.common.utils.function.TriConsumer;
import com.redhat.devtools.intellij.tektoncd.Constants;
import com.redhat.devtools.intellij.tektoncd.hub.invoker.ApiCallback;
import com.redhat.devtools.intellij.tektoncd.hub.invoker.ApiException;
import com.redhat.devtools.intellij.tektoncd.hub.model.ResourceData;
import com.redhat.devtools.intellij.tektoncd.hub.model.Resources;
import com.redhat.devtools.intellij.tektoncd.utils.NotificationHelper;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.DocumentEvent;
import org.jetbrains.annotations.NotNull;


import static com.redhat.devtools.intellij.tektoncd.ui.UIConstants.GRAY_COLOR;
import static com.redhat.devtools.intellij.tektoncd.ui.UIConstants.MAIN_BG_COLOR;
import static com.redhat.devtools.intellij.tektoncd.ui.UIConstants.SEARCH_BG_COLOR;
import static com.redhat.devtools.intellij.tektoncd.ui.UIConstants.SEARCH_FIELD_BORDER_COLOR;

public class HubItemPanelsBoard {

    private HubModel model;
    private List<JPanel> innerPanels = new ArrayList<>();
    private JBScrollPane scrollContentPanel;
    private JPanel contentPanel, wrapperInnerPanel, unclassifiedPanel;
    private SearchTextField mySearchTextField;
    private final Alarm mySearchUpdateAlarm = new Alarm();
    private BiConsumer<HubItem, TriConsumer<HubItem, String, String>> doSelectAction;
    private final static String RECOMMENDED = "Recommended";
    private final static String ALL = "All";
    private final static String ALL_TASKS = "All Tasks";
    private final static String ALL_PIPELINES = "All Pipelines";
    private final static String INSTALLED = "Installed";
    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
    private ScheduledFuture scheduler;
    private HubItemPanel selected;

    public HubItemPanelsBoard(HubModel model, BiConsumer<HubItem, TriConsumer<HubItem, String, String>> doSelectAction) {
        this.model = model;
        this.doSelectAction = doSelectAction;
        model.registerHubPanelCallback(getHubPanelCallback());

        wrapperInnerPanel = new JPanel(new BorderLayout());
        wrapperInnerPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE,80));

        scrollContentPanel = new JBScrollPane(wrapperInnerPanel, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollContentPanel.setBackground(MAIN_BG_COLOR);
        scrollContentPanel.setBorder(JBUI.Borders.empty());

        createSearchTextField(250);
        contentPanel = new JPanel(new BorderLayout());
        contentPanel.add(mySearchTextField, "North");
        contentPanel.add(scrollContentPanel);
    }

    public HubModel getModel() {
        return model;
    }

    public HubItemPanelsBoard withInstalled() {
        return with(INSTALLED);
    }

    public HubItemPanelsBoard withRecommended() {
        return with(RECOMMENDED);
    }

    public HubItemPanelsBoard withAllTasks() {
        return with(ALL_TASKS);
    }

    public HubItemPanelsBoard withAllPipelines() {
        return with(ALL_PIPELINES);
    }

    public HubItemPanelsBoard withAll() {
        return with(ALL);
    }

    private HubItemPanelsBoard with(String panel) {
        JPanel hubItemsPanel = buildBoxLayoutPanel(panel);
        innerPanels.add(hubItemsPanel);
        return this;
    }

    private JPanel buildBoxLayoutPanel(String panel) {
        JPanel hubItemsPanel = new JPanel();
        hubItemsPanel.setName(panel);
        hubItemsPanel.setLayout(new BoxLayout(hubItemsPanel, 1));
        hubItemsPanel.setBackground(MAIN_BG_COLOR);
        return hubItemsPanel;
    }

    private void createSearchTextField(final int flyDelay) {
        mySearchTextField = new SearchTextField() {
            @Override
            protected boolean preprocessEventForTextField(KeyEvent event) {
                int keyCode = event.getKeyCode();
                int id = event.getID();

                if (keyCode == KeyEvent.VK_ENTER || event.getKeyChar() == '\n') {
                    return true;
                }
                if ((keyCode == KeyEvent.VK_DOWN || keyCode == KeyEvent.VK_UP) && id == KeyEvent.KEY_PRESSED) {
                    return true;
                }
                return super.preprocessEventForTextField(event);
            }
        };
        mySearchTextField.setBorder(JBUI.Borders.customLine(SEARCH_FIELD_BORDER_COLOR));
        JBTextField editor = mySearchTextField.getTextEditor();
        editor.putClientProperty("JTextField.Search.Gap", JBUIScale.scale(6));
        editor.putClientProperty("JTextField.Search.GapEmptyText", JBUIScale.scale(-1));
        editor.setBorder(JBUI.Borders.empty(6, 6));
        editor.setOpaque(true);
        editor.setBackground(SEARCH_BG_COLOR);
        String text = "Search for task or pipeline"; //"Type / to see options";
        StatusText emptyText = mySearchTextField.getTextEditor().getEmptyText();
        emptyText.appendText(text, new SimpleTextAttributes(0, GRAY_COLOR));

        HubItemPanelsBoard self = this;

        mySearchTextField.getTextEditor().getDocument().addDocumentListener(new DocumentAdapter() {
            protected void textChanged(@NotNull DocumentEvent e) {
                if (e == null) {
                    return;
                }
                self.mySearchUpdateAlarm.cancelAllRequests();
                self.mySearchUpdateAlarm.addRequest(this::searchOnTheFly, flyDelay);
            }

            private void searchOnTheFly() {
                model.search(self.mySearchTextField.getText(), null, null, getSearchCallback());
            }
        });
    }

    private ApiCallback<Resources> getSearchCallback() {
        return new ApiCallback<Resources>() {
            @Override
            public void onFailure(ApiException e, int statusCode, Map<String, List<String>> responseHeaders) {
                if(statusCode == 404) {
                    build(Optional.empty());
                }
            }

            @Override
            public void onSuccess(Resources result, int statusCode, Map<String, List<String>> responseHeaders) {
                 build(Optional.of(result.getData().stream().map(resource -> new HubItem(resource)).collect(Collectors.toList())));
            }

            @Override
            public void onUploadProgress(long bytesWritten, long contentLength, boolean done) {

            }

            @Override
            public void onDownloadProgress(long bytesRead, long contentLength, boolean done) {

            }
        };
    }

    private HubPanelCallback getHubPanelCallback() {
        return () -> {
            if (scheduler != null && !scheduler.isCancelled() && !scheduler.isDone()) {
                scheduler.cancel(true);
            }

            scheduler = executor.schedule(() -> {
                build(Optional.empty());
            }, 500, TimeUnit.MILLISECONDS);
        };
    }

    public void openItemDetailsPanel(HubItem item) {
        doSelectAction.accept(item, getInstallCallback());
    }

    public JPanel build(Optional<List<HubItem>> unclassifiedItems) {
        wrapperInnerPanel.removeAll();
        boolean showUnclassifiedItems = !mySearchTextField.getTextEditor().getText().isEmpty();
        buildInnerStructure(showUnclassifiedItems);
        if (showUnclassifiedItems) {
            fillPanel(unclassifiedPanel, unclassifiedItems.isPresent() ? unclassifiedItems.get() : Collections.emptyList());
        } else {
            for (JPanel innerPanel : innerPanels) {
                if (innerPanel.getName().equals(ALL)) {
                    fillPanel(innerPanel, model.getAllHubItems());
                } else if (innerPanel.getName().equals(ALL_PIPELINES)) {
                    fillPanel(innerPanel, model.getAllPipelineHubItems());
                } else if (innerPanel.getName().equalsIgnoreCase(ALL_TASKS)) {
                    fillPanel(innerPanel, model.getAllTasksHubItems());
                } else if (innerPanel.getName().equals(INSTALLED)) {
                    fillPanel(innerPanel, model.getInstalledHubItems(), null);
                } else if (innerPanel.getName().equals(RECOMMENDED)) {
                    fillPanel(innerPanel, model.getRecommendedHubItems());
                }
            }
        }
        wrapperInnerPanel.revalidate();
        wrapperInnerPanel.repaint();
        return contentPanel;
    }

    private void fillPanel(JPanel panel, List<HubItem> items) {
        fillPanel(panel, items, getInstallCallback());
    }

    private void fillPanel(JPanel panel, List<HubItem> items, TriConsumer<HubItem, String, String> installCallback) {
        panel.removeAll();
        if (items.isEmpty()) {
            panel.add(buildEmptyTextPanel());
        } else {
            for (HubItem item : items) {
                HubItemPanel hubItemPanel = new HubItemPanel(item, this, installCallback);
                panel.add(hubItemPanel.build());
            }
        }

        Optional<Component> label = Arrays.stream(panel.getParent().getComponents()).filter(comp -> comp instanceof JLabel).findFirst();
        label.ifPresent(component -> component.setName(panel.getName() + " (" + items.size() + ")"));
    }

    public void updateActiveHubItemPanel(HubItemPanel panel) {
        if (selected != null && !selected.getHubItem().getResource().getName().equalsIgnoreCase(panel.getHubItem().getResource().getName())) {
            selected.setActive(false);
        }
        selected = panel;
    }

    private void buildInnerStructure(boolean useUnclassifiedPanel) {
        JComponent innerPanel;
        if (useUnclassifiedPanel) {
            unclassifiedPanel = buildBoxLayoutPanel("Results");
            innerPanel = buildPanel();
            innerPanel.add(buildLabel("Results"), BorderLayout.NORTH);
            innerPanel.add(unclassifiedPanel, BorderLayout.CENTER);
        } else {
            switch (innerPanels.size()) {
                case 0: {
                    innerPanel = buildEmptyPanel();
                    break;
                }
                case 1: {
                    innerPanel = buildPanel();
                    innerPanel.add(buildLabel(innerPanels.get(0).getName()), BorderLayout.NORTH);
                    innerPanel.add(innerPanels.get(0), BorderLayout.CENTER);
                    break;
                }
                default: {
                    innerPanel = buildMultipleComponentsPanel();
                    break;
                }
            }
        }
        if (innerPanel != null) {
            wrapperInnerPanel.add(innerPanel, BorderLayout.CENTER);
        }
    }

    private JComponent buildMultipleComponentsPanel() {
        JComponent firstComponent = null;
        for (JPanel innerPanel: innerPanels) {
            JComponent secondComponent  = buildPanel();
            secondComponent.add(buildLabel(innerPanel.getName()), BorderLayout.NORTH);
            secondComponent.add(innerPanel, BorderLayout.CENTER);
            if (firstComponent != null) {
                firstComponent = buildSplitter(firstComponent, secondComponent);
            } else {
                firstComponent = secondComponent;
            }
        }
        return firstComponent;
    }

    private JComponent buildSplitter(JComponent comp1, JComponent comp2) {
        OnePixelSplitter tabPanel = new OnePixelSplitter(true, 0.37F) {
            protected Divider createDivider() {
                Divider divider = super.createDivider();
                divider.setBackground(SEARCH_FIELD_BORDER_COLOR);
                return divider;
            }
        };
        tabPanel.setFirstComponent(comp1);
        tabPanel.setSecondComponent(comp2);
        return tabPanel;
    }

    private JLabel buildLabel(String name) {
        JLabel label = new JLabel(name);
        label.setBorder(JBUI.Borders.empty(5));
        return label;
    }

    private JPanel buildPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE,80));
        return panel;
    }

    private JPanel buildEmptyPanel() {
        JPanel emptyPanel = buildPanel();
        emptyPanel.add(buildEmptyTextPanel());
        return emptyPanel;
    }

    private JBPanelWithEmptyText buildEmptyTextPanel() {
        JBPanelWithEmptyText myEmptyTextPanel = new JBPanelWithEmptyText();
        myEmptyTextPanel.setBorder(new CustomLineBorder(SEARCH_FIELD_BORDER_COLOR, JBUI.insets(1, 0, 0, 0)));
        myEmptyTextPanel.setOpaque(true);
        myEmptyTextPanel.setBackground(MAIN_BG_COLOR);
        myEmptyTextPanel.getEmptyText().setText("Nothing found.");
        return myEmptyTextPanel;
    }

    private TriConsumer<HubItem, String, String> getInstallCallback() {
        return this::installHubItem;
    }

    private void installHubItem(HubItem hubItem, String kindToBeSaved, String version) {
        ResourceData resource = hubItem.getResource();
        scrollContentPanel.setCursor(new Cursor(Cursor.WAIT_CURSOR));
        ExecHelper.submit(() -> {
            try {
                Constants.InstallStatus installed = model.installHubItem(resource.getName(), kindToBeSaved, version, resource.getCatalog().getName());
                UIHelper.executeInUI(() -> {
                    if (installed != Constants.InstallStatus.ERROR) {
                        NotificationHelper.notify(model.getProject(), "Save Successful", resource.getKind() + " " + resource.getName() + " has been saved!", NotificationType.INFORMATION, true);
                    }
                });
            } catch (IOException ex) {
                UIHelper.executeInUI(() -> NotificationHelper.notify(model.getProject(), "Error", "An error occurred while saving " + resource.getKind() + " " + resource.getName() + "\n" + ex.getLocalizedMessage(), NotificationType.ERROR, false));
            }
            UIHelper.executeInUI(() -> {
                scrollContentPanel.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            });
        });
    }
}
