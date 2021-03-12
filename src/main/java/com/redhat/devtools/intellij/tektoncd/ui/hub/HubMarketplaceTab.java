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
package com.redhat.devtools.intellij.tektoncd.ui.hub;

import com.google.common.base.Strings;
import com.intellij.icons.AllIcons;
import com.intellij.notification.NotificationType;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.border.CustomLineBorder;
import com.intellij.ui.components.JBPanelWithEmptyText;
import com.intellij.util.ui.JBUI;
import com.redhat.devtools.intellij.tektoncd.Constants;
import com.redhat.devtools.intellij.tektoncd.hub.invoker.ApiCallback;
import com.redhat.devtools.intellij.tektoncd.hub.invoker.ApiException;
import com.redhat.devtools.intellij.tektoncd.hub.model.ResourceData;
import com.redhat.devtools.intellij.tektoncd.hub.model.Resources;
import com.redhat.devtools.intellij.tektoncd.utils.NotificationHelper;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
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
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import org.jetbrains.annotations.NotNull;


import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_CLUSTERTASK;
import static com.redhat.devtools.intellij.tektoncd.ui.UIConstants.MAIN_BG_COLOR;
import static com.redhat.devtools.intellij.tektoncd.ui.UIConstants.SEARCH_FIELD_BORDER_COLOR;

public class HubMarketplaceTab extends HubDialogTab {

    private JLabel lblResultsCount;

    public HubMarketplaceTab(HubModel model) {
        super(model);
    }

    @Override
    protected void createSearchTextField(final int flyDelay) {
        super.createSearchTextField(flyDelay);

        HubMarketplaceTab self = this;
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

    @NotNull
    @Override
    protected void updateDetailsPanel(HubItem item) {
        myDetailsPage.show(item, getInstallCallback(), getInstallAsClusterTaskCallback());
    }

    @NotNull
    @Override
    protected JComponent createContentPanel() {
        JPanel wrapperInnerContent = new JPanel(new BorderLayout());
        wrapperInnerContent.setMaximumSize(new Dimension(Integer.MAX_VALUE,80));

        // small panel between search and list hub items
        lblResultsCount = new JLabel();
        lblResultsCount.setBorder(JBUI.Borders.empty(5));
        wrapperInnerContent.add(lblResultsCount, BorderLayout.PAGE_START);

        // list of hub items
        innerContentPanel = new JPanel();
        innerContentPanel.setLayout(new BoxLayout(innerContentPanel, 1));
        innerContentPanel.setBackground(MAIN_BG_COLOR);
        wrapperInnerContent.add(innerContentPanel, BorderLayout.CENTER);

        return createScrollPane(wrapperInnerContent);
    }

    public void draw(List<HubItem> items) {
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
                        updateDetailsPanel(item);
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
    }

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
        try {
            Constants.InstallStatus installed = model.installHubItem(resource.getName(), kindToBeSaved, version, resource.getCatalog().getName());
            if (installed == Constants.InstallStatus.INSTALLED) {
                JLabel warningNameAlreadyUsed = new JLabel("", AllIcons.General.Warning, SwingConstants.CENTER);
                warningNameAlreadyUsed.setToolTipText("A " + kindToBeSaved + " with this name already exists on the cluster.");
                hubItem.updateBottomPanel(warningNameAlreadyUsed);
            }
            if (installed != Constants.InstallStatus.ERROR) {
                NotificationHelper.notify(model.getProject(), "Save Successful", resource.getKind() + " " + resource.getName() + " has been saved!", NotificationType.INFORMATION, true);
            }
        } catch (IOException ex) {
            NotificationHelper.notify(model.getProject(), "Error", "An error occurred while saving " + resource.getKind() + " " + resource.getName() + "\n" + ex.getLocalizedMessage(), NotificationType.ERROR, false);
        }
    }

    private ApiCallback<Resources> getSearchCallback() {
         return new ApiCallback<Resources>() {
            @Override
            public void onFailure(ApiException e, int statusCode, Map<String, List<String>> responseHeaders) {
                if(statusCode == 404) {
                    draw(Collections.emptyList());
                }
            }

            @Override
            public void onSuccess(Resources result, int statusCode, Map<String, List<String>> responseHeaders) {
                draw(result.getData().stream().map(resource -> new HubItem(resource)).collect(Collectors.toList()));
            }

            @Override
            public void onUploadProgress(long bytesWritten, long contentLength, boolean done) {

            }

            @Override
            public void onDownloadProgress(long bytesRead, long contentLength, boolean done) {

            }
        };
    }

}
