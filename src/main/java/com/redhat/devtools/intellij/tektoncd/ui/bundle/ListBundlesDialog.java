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
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Divider;
import com.intellij.openapi.ui.InputValidator;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.ui.AnActionButton;
import com.intellij.ui.AnActionButtonRunnable;
import com.intellij.ui.AnActionButtonUpdater;
import com.intellij.ui.CollectionListModel;
import com.intellij.ui.OnePixelSplitter;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.ui.JBUI;
import com.redhat.devtools.intellij.common.utils.ExecHelper;
import com.redhat.devtools.intellij.common.utils.UIHelper;
import com.redhat.devtools.intellij.tektoncd.actions.bundle.toolbar.ImportBundleResourceAction;
import com.redhat.devtools.intellij.tektoncd.settings.SettingsState;
import com.redhat.devtools.intellij.tektoncd.tkn.Bundle;
import com.redhat.devtools.intellij.tektoncd.tkn.Resource;
import com.redhat.devtools.intellij.tektoncd.tkn.Tkn;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.IOException;
import java.util.List;

import static com.redhat.devtools.intellij.tektoncd.ui.UIConstants.RED;
import static com.redhat.devtools.intellij.tektoncd.ui.UIConstants.SEARCH_FIELD_BORDER_COLOR;

public class ListBundlesDialog extends DialogWrapper {

    private Tree tree;
    private JBList<Resource> layers;
    private JBList<String> bundles;
    private JTextArea txtBundleResourceYAML;
    private List<String> bundleList;
    private Project project;
    private Bundle bundle;
    private JPanel buildListBundlesPanel;
    private JLabel warning, lblGeneralError;
    private Tkn tkn;

    public ListBundlesDialog(@Nullable Project project, Tkn tkn) {
        super(project, true);
        this.project = project;
        this.tkn = tkn;
        this.bundle = new Bundle();
        this.bundleList = SettingsState.getInstance().bundleList;
        setOKButtonText("Close");
        super.init();
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        final JPanel panel = new JPanel(new BorderLayout());
        panel.add(createDescriptionPanel(), BorderLayout.NORTH);
        panel.add(createSplitter(), BorderLayout.CENTER);
        panel.add(createErrorSouthPanel(), BorderLayout.SOUTH);
        panel.setPreferredSize(new Dimension(630, 400));
        return panel;
    }

    private JPanel createErrorSouthPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(JBUI.Borders.emptyTop(10));
        lblGeneralError = new JLabel("");
        lblGeneralError.setForeground(RED);
        lblGeneralError.setVisible(false);
        panel.add(lblGeneralError, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createDescriptionPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        JLabel lbl = new JLabel("Add a new Tekton Bundle and import its resource to your cluster",
                AllIcons.General.ShowInfos,
                SwingConstants.LEFT
        );
        lbl.setToolTipText("Add a Tekton bundle deployed in a registry (e.g quay.io/myrepo/mybundle:latest) and pick the " +
                "resources you want to import in your cluster.");
        panel.add(lbl);
        warning = new JLabel("");
        warning.setBorder(JBUI.Borders.emptyTop(5));
        warning.setVisible(false);
        panel.add(warning);
        panel.setBorder(JBUI.Borders.emptyBottom(10));
        return panel;
    }

    private JComponent createSplitter() {
        OnePixelSplitter tabPanel = new OnePixelSplitter(false, 0.37F) {
            protected Divider createDivider() {
                Divider divider = super.createDivider();
                divider.setBackground(SEARCH_FIELD_BORDER_COLOR);
                return divider;
            }
        };
        tabPanel.setFirstComponent(buildListBundlesPanel());
        tabPanel.setSecondComponent(buildRightPanel());
        return tabPanel;
    }

    private JComponent buildRightPanel() {
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
       // txtBundleResourceYAML.setFont(new JLabel().getFont());
        txtBundleResourceYAML.setLineWrap(true);
        //txtBundleResourceYAML.setBackground(JBColor.BLACK);

        JBScrollPane scrollBundleResourceAreaPane = new JBScrollPane(txtBundleResourceYAML);
       // Dimension sizeTextAreaNewTriggerBinding = txtBundleResourceYAML.getPreferredScrollableViewportSize();
        //txtBundleResourceYAML.setPreferredSize(new Dimension(sizeTextAreaNewTriggerBinding.width, sizeTextAreaNewTriggerBinding.height + 10));
        return scrollBundleResourceAreaPane;
    }

    private JComponent buildBundleLayersPanel() {
        JPanel bundleLayer = new JPanel(new BorderLayout());
        layers = new JBList<>();
        layers.setEmptyText("No bundle selected");
        layers.setCellRenderer((list, value, index, isSelected, cellHasFocus) -> new JLabel(value.name(), value.getIcon(), SwingConstants.LEFT));
        final ToolbarDecorator decorator = ToolbarDecorator.createDecorator(layers)
                .disableUpAction()
                .disableDownAction()
                .disableAddAction()
                .disableRemoveAction()
                .setActionGroup(new ActionGroup() {
                    @Override
                    public AnAction[] getChildren(@Nullable AnActionEvent e) {
                        return new AnAction[] { new ImportBundleResourceAction("import bundle resource", "", AllIcons.ToolbarDecorator.Import, layers, tkn)};
                    }
                });
        layers.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                Resource resource = layers.getSelectedValue();
                String bundleName = bundles.getSelectedValue();
                try {
                    String yaml = tkn.getBundleResourceYAML(bundleName, resource);
                    txtBundleResourceYAML.setText(yaml);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });
        JPanel innerPanel = decorator.createPanel();
        bundleLayer.add(innerPanel, BorderLayout.CENTER);
        return bundleLayer;
    }

    private JComponent buildListBundlesPanel() {

        buildListBundlesPanel = new JPanel(new BorderLayout());


        bundles = new JBList<>();
        bundles.setModel(new CollectionListModel<>(bundleList));
        bundles.setEmptyText("No bundle added");
        bundles.setCellRenderer((list, value, index, isSelected, cellHasFocus) ->  new JLabel(value));
        bundles.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting()) {
                    loadResourcesFromBundle();
                }
               // loadResourcesFromBundle();
            }
        });
        final ToolbarDecorator decorator = ToolbarDecorator.createDecorator(bundles)
                .disableUpAction()
                .disableDownAction()
                .setAddAction(button -> {
                    String newBundle = Messages.showInputDialog(project, "Add the name of the bundle to import", "Add Bundle", AllIcons.General.Add, null, new InputValidator() {
                        @Override
                        public boolean checkInput(@NlsSafe String inputString) {
                            return true;
                        }

                        @Override
                        public boolean canClose(@NlsSafe String inputString) {
                            return true;
                        }
                    });
                    if (newBundle != null && !newBundle.isEmpty()) {
                        bundleList.add(newBundle);
                        bundles.setModel(new CollectionListModel<>(bundleList));
                        bundles.invalidate();
                        bundles.setSelectedIndex(bundleList.size()-1);
                        SettingsState.getInstance().bundleList = bundleList;
                        loadResourcesFromBundle();
                    }
                })
                .setRemoveAction(new AnActionButtonRunnable() {
                    @Override
                    public void run(AnActionButton anActionButton) {

                    }


                })
                .setRemoveActionUpdater(new AnActionButtonUpdater() {
                    @Override
                    public boolean isEnabled(@NotNull AnActionEvent e) {
                        return true;
                    }
                });
        JPanel innerPanel = decorator.createPanel();
        buildListBundlesPanel.add(innerPanel, BorderLayout.CENTER);
        return buildListBundlesPanel;
    }

    private void loadResourcesFromBundle() {
        String bundleName = bundles.getSelectedValue();
        ExecHelper.submit(() -> {
            try {
                List<Resource> resources = tkn.listResourceFromBundle(bundleName);
                UIHelper.executeInUI(() -> {
                    layers.removeAll();
                    layers.setModel(new CollectionListModel<>(resources));
                    layers.invalidate();
                });
            } catch (IOException e) {
                // print error
                e.printStackTrace();
            }

        });

    }
}
