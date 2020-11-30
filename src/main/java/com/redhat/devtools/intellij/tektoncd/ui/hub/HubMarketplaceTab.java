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
import com.intellij.openapi.project.Project;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.border.CustomLineBorder;
import com.intellij.ui.components.JBPanelWithEmptyText;
import com.intellij.util.ui.JBUI;
import com.redhat.devtools.intellij.tektoncd.hub.invoker.ApiCallback;
import com.redhat.devtools.intellij.tektoncd.hub.invoker.ApiException;
import com.redhat.devtools.intellij.tektoncd.hub.model.Resources;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import org.jetbrains.annotations.NotNull;

public class HubMarketplaceTab extends HubDialogTab {

    private JLabel lblResultsCount;
    private List<String> tasks;
    private Project project;
    private String namespace;

    public HubMarketplaceTab(Project project, String namespace, List<String> tasks) {
        super(project, namespace, tasks);
        this.project = project;
        this.tasks = tasks;
        this.namespace = namespace;
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
                ApiCallback<Resources> callback = new ApiCallback<Resources>() {
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
                HubModel.getInstance().search(self.mySearchTextField.getText(), null, null, callback);
            }
        });
    }

    @NotNull
    @Override
    protected void updateDetailsPanel(HubItem item) {
        myDetailsPage.show(item);
    }

    @NotNull
    @Override
    protected JComponent createContentPanel() {
        JPanel wrapperInnerContent = new JPanel(new BorderLayout());
        wrapperInnerContent.setMaximumSize(new Dimension(
                Integer.MAX_VALUE,
                80
        ));

        // small panel between search and list hub items
        lblResultsCount = new JLabel();
        lblResultsCount.setBorder(new EmptyBorder(5, 5, 5, 5));
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
            boolean sameNameOnCluster = tasks.contains(item.getResource().getName());
            JPanel itemAsPanel = item.createPanel(project, namespace, sameNameOnCluster, consumer);
            itemAsPanel.addMouseListener(new MouseListener() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    String old = HubModel.getInstance().getSelectedHubItem();
                    if (Strings.isNullOrEmpty(old) || old != item.getResource().getName()) {
                        if (!Strings.isNullOrEmpty(old)) {
                            Optional<HubItem> oldItem = items.stream().filter(item -> item.getResource().getName().equalsIgnoreCase(old)).findFirst();
                            if (oldItem.isPresent()) {
                                oldItem.get().parent.setBackground(MAIN_BG_COLOR);
                                oldItem.get().rightSide.setBackground(MAIN_BG_COLOR);
                                oldItem.get().bottomCenterPanel.setBackground(MAIN_BG_COLOR);
                            }
                        }
                        item.parent.setBackground(JBUI.CurrentTheme.StatusBar.hoverBackground());
                        item.rightSide.setBackground(JBUI.CurrentTheme.StatusBar.hoverBackground());
                        item.bottomCenterPanel.setBackground(JBUI.CurrentTheme.StatusBar.hoverBackground());
                        updateDetailsPanel(item);
                        HubModel.getInstance().setSelectedHubItem(item.getResource().getName());
                    }
                }

                @Override
                public void mousePressed(MouseEvent e) {  }

                @Override
                public void mouseReleased(MouseEvent e) { }

                @Override
                public void mouseEntered(MouseEvent e) { }

                @Override
                public void mouseExited(MouseEvent e) { }
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
        updateResultCounter(items.size());
        innerContentPanel.revalidate();
        innerContentPanel.repaint();
    }

    private void updateResultCounter(int count) {
        lblResultsCount.setText("Results (" + count + ")");
    }
}
