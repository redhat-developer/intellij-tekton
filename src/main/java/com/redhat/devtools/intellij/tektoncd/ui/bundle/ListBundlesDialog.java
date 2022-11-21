/*******************************************************************************
 *  Copyright (c) 2022 Red Hat, Inc.
 *  Distributed under license by Red Hat, Inc. All rights reserved.
 *  This program is made available under the terms of the
 *  Eclipse Public License v2.0 which accompanies this distribution,
 *  and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 *  Contributors:
 *  Red Hat, Inc.
 ******************************************************************************/
package com.redhat.devtools.intellij.tektoncd.ui.bundle;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Divider;
import com.intellij.openapi.ui.InputValidator;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.ui.CollectionListModel;
import com.intellij.ui.OnePixelSplitter;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.JBUI;
import com.redhat.devtools.intellij.common.utils.ExecHelper;
import com.redhat.devtools.intellij.common.utils.UIHelper;
import com.redhat.devtools.intellij.tektoncd.actions.bundle.toolbar.ImportBundleResourceAction;
import com.redhat.devtools.intellij.tektoncd.settings.SettingsState;
import com.redhat.devtools.intellij.tektoncd.tkn.Authenticator;
import com.redhat.devtools.intellij.tektoncd.tkn.Resource;
import com.redhat.devtools.intellij.tektoncd.tkn.Tkn;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionListener;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Font;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import static com.redhat.devtools.intellij.tektoncd.ui.UIConstants.SEARCH_FIELD_BORDER_COLOR;

public class ListBundlesDialog extends BundleDialog {
    private static final Logger logger = LoggerFactory.getLogger(ListBundlesDialog.class);

    private JBList<Resource> layersListPanel;
    private JBList<String> bundlesListPanel;
    private JTextArea txtBundleResourceYAML;
    private List<String> bundleList;
    private Map<String, BundleCacheItem> bundleCache;
    private Map<String, String> bundleResourceCache;

    public ListBundlesDialog(@Nullable Project project, Tkn tkn) {
        super(project, "Import Bundle Resources", tkn);
    }

    @Override
    protected void preInit() {
        bundleCache = new HashMap<>();
        bundleResourceCache = new HashMap<>();
        bundleList = SettingsState.getInstance().bundleList;
    }

    @Override
    protected String getTopDescriptionText() {
        return "Add a new Tekton Bundle and import its resource to your cluster";
    }

    @Override
    protected String getTopDescriptionTooltip() {
        return "Add a Tekton bundle deployed in a registry (e.g quay.io/myrepo/mybundle:latest) and pick the " +
                "resources you want to import in your cluster.";
    }

    @Override
    protected JComponent buildRightPanel() {
        OnePixelSplitter tabPanel = new OnePixelSplitter(true, 0.5F) {
            protected Divider createDivider() {
                Divider divider = super.createDivider();
                divider.setBackground(SEARCH_FIELD_BORDER_COLOR);
                return divider;
            }
        };
        tabPanel.setFirstComponent(buildBundleLayersPanel());
        tabPanel.setSecondComponent(buildResourceYamlPanel());
        return tabPanel;
    }

    private JComponent buildResourceYamlPanel() {
        txtBundleResourceYAML = new JTextArea(15, 35);
        txtBundleResourceYAML.setEditable(false);
        txtBundleResourceYAML.setText("");
        txtBundleResourceYAML.setLineWrap(true);
        Font curFont = txtBundleResourceYAML.getFont();
        txtBundleResourceYAML.setFont(curFont.deriveFont(12f));
        return new JBScrollPane(txtBundleResourceYAML);
    }

    private JComponent buildBundleLayersPanel() {
        JPanel bundleLayer = new JPanel(new BorderLayout());
        bundleLayer.add(createLayersListPanel(), BorderLayout.CENTER);
        return bundleLayer;
    }

    private JPanel createLayersListPanel() {
        layersListPanel = initJBList(new JBList<>(),
                Collections.emptyList(),
                "No bundle selected",
                JBUI.Borders.empty(5, 10),
                (list, value, index, isSelected, cellHasFocus) -> new JLabel(value.name(), value.getIcon(), SwingConstants.LEFT),
                createLayersListSelectionListener()
        );

        final ToolbarDecorator decorator = ToolbarDecorator.createDecorator(layersListPanel)
                .disableUpAction()
                .disableDownAction()
                .disableAddAction()
                .disableRemoveAction()
                .setActionGroup(new ActionGroup() {
                    @Override
                    public AnAction[] getChildren(@Nullable AnActionEvent e) {
                        return new AnAction[] { new ImportBundleResourceAction(
                                "Import bundle resource",
                                "",
                                AllIcons.ToolbarDecorator.Import,
                                bundlesListPanel,
                                layersListPanel,
                                bundleCache,
                                bundleResourceCache,
                                tkn)};
                    }
                });
        return decorator.createPanel();
    }

    private ListSelectionListener createLayersListSelectionListener() {
        return e -> {
            Resource resource = layersListPanel.getSelectedValue();
            String bundleName = bundlesListPanel.getSelectedValue();
            if (bundleName == null || resource == null) {
                txtBundleResourceYAML.setText("");
                return;
            }
            String keyCache = BundleUtils.createCacheKey(bundleName, resource);
            String yaml = bundleResourceCache.getOrDefault(keyCache, "");
            if (!yaml.isEmpty()) {
                txtBundleResourceYAML.setText(yaml);
                return;
            }
            enableLoadingState();
            ExecHelper.submit(() -> {
                try {
                    String updated = fetchResourceFromBundle(bundleName, resource);
                    bundleResourceCache.put(keyCache, updated);
                    UIHelper.executeInUI(() -> {
                        disableLoadingState();
                        txtBundleResourceYAML.setText(updated);
                        txtBundleResourceYAML.setSelectionStart(0);
                        txtBundleResourceYAML.setSelectionEnd(0);
                    });
                } catch (IOException ex) {
                    logger.warn(ex.getLocalizedMessage(), ex);
                    UIHelper.executeInUI(() -> {
                        disableLoadingState();
                        txtBundleResourceYAML.setText("");
                        lblGeneralError.setText(ex.getLocalizedMessage());
                        lblGeneralError.setVisible(true);
                        ExecHelper.submit(() -> {
                            new Timer().schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    UIHelper.executeInUI(() -> lblGeneralError.setVisible(false));
                                }
                            }, 10000);
                        });
                    });
                }
            });
        };
    }

    private String fetchResourceFromBundle(String bundleName, Resource resource) throws IOException {
        BundleCacheItem bundleCacheItem = bundleCache.getOrDefault(bundleName, null);
        Authenticator authenticator = null;
        if (bundleCacheItem != null) {
            authenticator = bundleCacheItem.getAuthenticator();
        }
        try {
            return tkn.getBundleResourceYAML(bundleName, resource, authenticator);
        } catch(IOException e) {
            if (!e.getLocalizedMessage().toLowerCase().contains("unauthorized")) {
                throw e;
            }
        }

        // it failed because of an unauthorized error, retry by asking authentication data
        authenticator = getRegistryAuthenticationDataFromUser(bundleName);
        return tkn.getBundleResourceYAML(bundleName, resource, authenticator);
    }

    @Override
    protected JComponent buildLeftPanel() {
        JPanel buildBundlesPanel = new JPanel(new BorderLayout());

        bundlesListPanel = initJBList(new JBList<>(),
                bundleList,
                "No bundle added",
                JBUI.Borders.empty(10, 5),
                (list, value, index, isSelected, cellHasFocus) ->  new JLabel(value),
                e -> {
                    if (e.getValueIsAdjusting()) {
                        loadResourcesFromBundle();
                    }
                });
        final ToolbarDecorator decorator = ToolbarDecorator.createDecorator(bundlesListPanel)
                .disableUpAction()
                .disableDownAction()
                .setAddAction(button -> {
                    String newBundle = Messages.showInputDialog(project, "Add the name of the bundle to pull (e.g quay.io/myrepo/mybundle:latest)", "Add Bundle", null, null, new InputValidator() {
                        @Override
                        public boolean checkInput(@NlsSafe String inputString) {
                            return !inputString.isEmpty();
                        }

                        @Override
                        public boolean canClose(@NlsSafe String inputString) {
                            return true;
                        }
                    });
                    if (newBundle != null && !newBundle.isEmpty()) {
                        bundleList.add(newBundle);
                        updateBundlesModel(bundleList);
                        bundlesListPanel.setSelectedIndex(bundleList.size()-1);
                        SettingsState.getInstance().bundleList = bundleList;
                        loadResourcesFromBundle();
                    }
                })
                .setRemoveAction(anActionButton -> {
                    String selected = bundlesListPanel.getSelectedValue();
                    int result = Messages.showOkCancelDialog("Remove " + selected + "?", "Remove Bundle", "Ok", "Cancel", null);
                    if (result == OK_EXIT_CODE) {
                        bundleList.remove(selected);
                        bundleCache.remove(selected);
                        updateBundlesModel(bundleList);
                        loadResourcesFromBundle();
                    }
                })
                .setRemoveActionUpdater(e -> bundlesListPanel.getSelectedValue() != null);
        buildBundlesPanel.add(decorator.createPanel(), BorderLayout.CENTER);
        return buildBundlesPanel;
    }

    private void loadResourcesFromBundle() {
        String bundleName = bundlesListPanel.getSelectedValue();
        if (bundleName == null) {
            updateLayersModel(Collections.emptyList());
            return;
        }
        enableLoadingState();
        ExecHelper.submit(() -> {
            try {
                BundleCacheItem bundleCacheItem = bundleCache.getOrDefault(bundleName, null);
                if(bundleCacheItem == null) {
                    bundleCacheItem = fetchResourcesFromBundle(bundleName);
                    bundleCache.put(bundleName, bundleCacheItem);
                }
                BundleCacheItem finalBundleCacheItem = bundleCacheItem;
                UIHelper.executeInUI(() -> {
                    disableLoadingState();
                    updateLayersModel(finalBundleCacheItem.getResources());
                });
            } catch (IOException e) {
                logger.warn(e.getLocalizedMessage(), e);
                UIHelper.executeInUI(() -> {
                    disableLoadingState();
                    lblGeneralError.setText(e.getLocalizedMessage());
                    lblGeneralError.setVisible(true);
                });
            }
        });
    }

    private BundleCacheItem fetchResourcesFromBundle(String bundleName) throws IOException {
        try {
            List<Resource> resources = tkn.listResourceFromBundle(bundleName, null);
            return new BundleCacheItem(null, resources);
        } catch(IOException e) {
            if (!e.getLocalizedMessage().toLowerCase().contains("unauthorized")) {
                throw e;
            }
        }

        // it failed because of an unauthorized error, retry by asking authentication data
        Authenticator authenticator = getRegistryAuthenticationDataFromUser(bundleName);
        List<Resource> resources = tkn.listResourceFromBundle(bundleName, authenticator);
        return new BundleCacheItem(authenticator, resources);
    }

    private void updateBundlesModel(List<String> bundles) {
        bundlesListPanel.setModel(new CollectionListModel<>(bundles));
        bundlesListPanel.invalidate();
    }

    private void updateLayersModel(List<Resource> resources) {
        layersListPanel.removeAll();
        layersListPanel.setModel(new CollectionListModel<>(resources));
        layersListPanel.invalidate();
    }

    protected void updateLoadingState(Cursor cursor) {
        bundlesListPanel.setCursor(cursor);
        layersListPanel.setCursor(cursor);
        txtBundleResourceYAML.setCursor(cursor);
    }
}
