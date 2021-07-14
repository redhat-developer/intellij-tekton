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

import com.google.common.base.Strings;
import com.intellij.icons.AllIcons;
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
import com.redhat.devtools.intellij.tektoncd.Constants;
import com.redhat.devtools.intellij.tektoncd.hub.invoker.ApiCallback;
import com.redhat.devtools.intellij.tektoncd.hub.invoker.ApiException;
import com.redhat.devtools.intellij.tektoncd.hub.model.ResourceData;
import com.redhat.devtools.intellij.tektoncd.hub.model.Resources;
import com.redhat.devtools.intellij.tektoncd.utils.NotificationHelper;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import org.jetbrains.annotations.NotNull;


import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_CLUSTERTASK;
import static com.redhat.devtools.intellij.tektoncd.ui.UIConstants.GRAY_COLOR;
import static com.redhat.devtools.intellij.tektoncd.ui.UIConstants.MAIN_BG_COLOR;
import static com.redhat.devtools.intellij.tektoncd.ui.UIConstants.SEARCH_BG_COLOR;
import static com.redhat.devtools.intellij.tektoncd.ui.UIConstants.SEARCH_FIELD_BORDER_COLOR;

public class HubItemsListPanelBuilder {

    private HubModel model;
    private List<JPanel> innerPanels = new ArrayList<>();
    private JBScrollPane scrollContentPanel;
    private JPanel contentPanel, wrapperInnerPanel;
    private SearchTextField mySearchTextField;
    private final Alarm mySearchUpdateAlarm = new Alarm();
    private HubDetailsPageComponent itemDetailsPage;
    private final static String RECOMMENDED = "Recommended";
    private final static String ALL = "All";
    private final static String INSTALLED = "Installed";

    public HubItemsListPanelBuilder(HubModel model, HubDetailsPageComponent itemDetailsPage) {
        this.model = model;
        this.itemDetailsPage = itemDetailsPage;

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

    public HubItemsListPanelBuilder withInstalled() {
        return with(INSTALLED);
    }

    public HubItemsListPanelBuilder withRecommended() {
        return with(RECOMMENDED);
    }

    public HubItemsListPanelBuilder withAll() {
        return with(ALL);
    }

    private HubItemsListPanelBuilder with(String panel) {
        JPanel hubItemsPanel = new JPanel();
        hubItemsPanel.setName(panel);
        hubItemsPanel.setLayout(new BoxLayout(hubItemsPanel, 1));
        hubItemsPanel.setBackground(MAIN_BG_COLOR);
        innerPanels.add(hubItemsPanel);
        return this;
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

        HubItemsListPanelBuilder self = this;

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
                    buildSearchPanel(Collections.emptyList());
                }
            }

            @Override
            public void onSuccess(Resources result, int statusCode, Map<String, List<String>> responseHeaders) {
                // draw(result.getData().stream().map(resource -> new HubItem(resource)).collect(Collectors.toList()), resource -> updateDetailsPanel(resource) );
                 buildSearchPanel(result.getData().stream().map(resource -> new HubItem(resource)).collect(Collectors.toList()));
            }

            @Override
            public void onUploadProgress(long bytesWritten, long contentLength, boolean done) {

            }

            @Override
            public void onDownloadProgress(long bytesRead, long contentLength, boolean done) {

            }
        };
    }

    private void updateDetailsPanel(HubItem item, BiConsumer<HubItem, String> installAsTaskCallback, BiConsumer<HubItem, String> installAsClusterTaskCallback) {
        if (itemDetailsPage != null) {
            itemDetailsPage.show(item, installAsTaskCallback, installAsClusterTaskCallback);
        }
    }
    private void updateDetailsPanel(HubItem item) {
        updateDetailsPanel(item, getInstallCallback(), getInstallAsClusterTaskCallback());
    }

    public void buildSearchPanel(List<HubItem> items) {
        wrapperInnerPanel.removeAll();


        JPanel hubItemsPanel = new JPanel();
        hubItemsPanel.setName("Search");
        hubItemsPanel.setLayout(new BoxLayout(hubItemsPanel, 1));
        hubItemsPanel.setBackground(MAIN_BG_COLOR);


        JPanel innerPanel = buildPanel();
        innerPanel.add(buildLabel("Results"), BorderLayout.NORTH);
        innerPanel.add(hubItemsPanel, BorderLayout.CENTER);
        wrapperInnerPanel.add(innerPanel, BorderLayout.CENTER);

        fillPanel(hubItemsPanel, items);
        wrapperInnerPanel.revalidate();
        wrapperInnerPanel.repaint();
    }

    public JPanel build() {
        wrapperInnerPanel.removeAll();
        buildInnerStructure();
        for (JPanel innerPanel: innerPanels) {
            if (innerPanel.getName().equals(ALL)) {
                fillPanel(innerPanel, model.getAllHubItems());
            } else if (innerPanel.getName().equals(INSTALLED)) {
                fillPanel(innerPanel, model.getInstalledHubItems());
            } else if (innerPanel.getName().equals(RECOMMENDED)) {
                fillPanel(innerPanel, model.getRecommendedHubItems());
            }
        }
        wrapperInnerPanel.revalidate();
        wrapperInnerPanel.repaint();
        return contentPanel;
    }

    private void fillPanel(JPanel panel, List<HubItem> items) {
        for (HubItem item: items) {
            Consumer<HubItem> consumer = resource -> updateDetailsPanel(resource);
            JPanel itemAsPanel = item.createPanel(model, consumer, getInstallCallback(), getInstallAsClusterTaskCallback());
            itemAsPanel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    String old = model.getSelectedHubItem();
                    if (Strings.isNullOrEmpty(old) || old != item.getResource().getName()) {
                        model.setSelectedHubItem(item.getResource().getName());
                        if (!Strings.isNullOrEmpty(old)) {
                            Optional<HubItem> oldItem = items.stream().filter(item -> item.getResource().getName().equalsIgnoreCase(old)).findFirst();
                            if (oldItem.isPresent()) {
                                oldItem.get().repaint(false);
                            }
                        }
                        item.repaint(true);
                        if (consumer != null) {
                            consumer.accept(item);
                        }
                    }
                }
            });
            panel.add(itemAsPanel);
        }
        if (items.isEmpty()) {
            panel.add(buildEmptyTextPanel());
        }
    }

    private void buildInnerStructure() {
        JComponent innerPanel = null;
        switch (innerPanels.size()) {
            case 0: {
                innerPanel = buildEmptyPanel();
            }
            case 1: {
                innerPanel = buildPanel();
                innerPanel.add(buildLabel(innerPanels.get(0).getName()), BorderLayout.NORTH);
                innerPanel.add(innerPanels.get(0), BorderLayout.CENTER);
            }
            case 2: {
                innerPanel = buildMultipleComponentsPanel();
            }
            case 3: {

            }
            default: break;
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



    /* public void draw(List<HubItem> items) {
        innerContentPanel.removeAll();
        for (HubItem item: items) {
            Consumer<HubItem> consumer = resource -> updateDetailsPanel(resource);
            JPanel itemAsPanel = item.createPanel(model, consumer, getInstallCallback(), getInstallAsClusterTaskCallback());
            itemAsPanel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    String old = model.getSelectedHubItem();
                    if (Strings.isNullOrEmpty(old) || old != item.getResource().getName()) {
                        model.setSelectedHubItem(item.getResource().getName());
                        if (!Strings.isNullOrEmpty(old)) {
                            Optional<HubItem> oldItem = items.stream().filter(item -> item.getResource().getName().equalsIgnoreCase(old)).findFirst();
                            if (oldItem.isPresent()) {
                                oldItem.get().repaint(false);
                            }
                        }
                        item.repaint(true);
                        if (consumer != null) {
                            consumer.accept(item);
                        }
                    }
                }
            });
            innerContentPanel.add(itemAsPanel);
        }
        if (items.isEmpty()) {
            JBPanelWithEmptyText myEmptyPanel = new JBPanelWithEmptyText();
            myEmptyPanel.setBorder(new CustomLineBorder(SEARCH_FIELD_BORDER_COLOR, JBUI.insets(1, 0, 0, 0)));
            myEmptyPanel.setOpaque(true);
            myEmptyPanel.setBackground(MAIN_BG_COLOR);
            myEmptyPanel.getEmptyText().setText("Nothing found.");
            innerContentPanel.add(myEmptyPanel);
        }
        lblResultsCount.setText("Results (" + items.size()+ ")");
        innerContentPanel.revalidate();
        innerContentPanel.repaint();
    }*/

    private BiConsumer<HubItem, String> getInstallCallback() {
        return (hubItem, version) -> {
            installHubItem(hubItem, hubItem.getResource().getKind(), version);
        };
    }

    private BiConsumer<HubItem, String> getInstallAsClusterTaskCallback() {
        return (hubItem, version) -> {
            installHubItem(hubItem, KIND_CLUSTERTASK, version);
        };
    }

    private void installHubItem(HubItem hubItem, String kindToBeSaved, String version) {
        ResourceData resource = hubItem.getResource();
        scrollContentPanel.setCursor(new Cursor(Cursor.WAIT_CURSOR));
        ExecHelper.submit(() -> {
            try {
                Constants.InstallStatus installed = model.installHubItem(resource.getName(), kindToBeSaved, version, resource.getCatalog().getName());
                UIHelper.executeInUI(() -> {
                    if (installed == Constants.InstallStatus.INSTALLED) {
                        JLabel warningNameAlreadyUsed = new JLabel("", AllIcons.General.Warning, SwingConstants.CENTER);
                        warningNameAlreadyUsed.setToolTipText("A " + kindToBeSaved + " with this name already exists on the cluster.");
                        hubItem.updateBottomPanel(warningNameAlreadyUsed);
                    }
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
