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
import com.intellij.ide.util.treeView.NodeRenderer;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.ui.CollectionListModel;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.tree.AsyncTreeModel;
import com.intellij.ui.tree.StructureTreeModel;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.ui.JBUI;
import com.redhat.devtools.intellij.common.tree.LabelAndIconDescriptor;
import com.redhat.devtools.intellij.common.utils.ExecHelper;
import com.redhat.devtools.intellij.common.utils.UIHelper;
import com.redhat.devtools.intellij.tektoncd.Constants;
import com.redhat.devtools.intellij.tektoncd.actions.bundle.toolbar.MoveToBundleAction;
import com.redhat.devtools.intellij.tektoncd.actions.bundle.toolbar.RemoveFromBundleAction;
import com.redhat.devtools.intellij.tektoncd.tkn.Bundle;
import com.redhat.devtools.intellij.tektoncd.tkn.Resource;
import com.redhat.devtools.intellij.tektoncd.tkn.Tkn;
import com.redhat.devtools.intellij.tektoncd.tree.ClusterTaskNode;
import com.redhat.devtools.intellij.tektoncd.tree.MutableTektonModelSynchronizer;
import com.redhat.devtools.intellij.tektoncd.tree.PipelineNode;
import com.redhat.devtools.intellij.tektoncd.tree.TaskNode;
import com.redhat.devtools.intellij.tektoncd.tree.TektonBundleResourceTreeStructure;
import com.redhat.devtools.intellij.tektoncd.utils.NotificationHelper;
import com.redhat.devtools.intellij.tektoncd.utils.TreeHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.List;

public class CreateBundleDialog extends BundleDialog {
    private static final Logger logger = LoggerFactory.getLogger(CreateBundleDialog.class);
    private Tree tree;
    private JBList<Resource> layers;
    private Bundle bundle;
    private JPanel bundleBodyPanel;
    private JButton moveToBundle, leaveFromBundle;
    private JTextField txtValueParam;
    private MoveToBundleAction moveToBundleAction;
    private RemoveFromBundleAction removeFromBundleAction;

    public CreateBundleDialog(@Nullable Project project, Tkn tkn) {
        super(project, "Create and deploy new bundle", "Deploy", tkn);
    }


    @Override
    protected void preInit() {
        bundle = new Bundle();;
    }

    @Override
    protected String getTopDescriptionText() {
        return "Publish a new Tekton Bundle to a registry by passing in a set (max 10) of Tekton objects";
    }

    @Override
    protected String getTopDescriptionTooltip() {
        return "Look for the Tekton resources you want to include in your bundle and move them from the tree to the bundle body. " +
                "Once done, add the image name and push it to the registry.";
    }

    @Override
    protected JComponent buildRightPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(buildBundleBodyPanel(), BorderLayout.CENTER);
        panel.add(buildSwitchButtonsPanel(), BorderLayout.WEST);
        return panel;
    }

    private JComponent buildSwitchButtonsPanel() {
        initActions();

        moveToBundle = createButton(moveToBundleAction,
                AllIcons.Actions.ArrowExpand);
        leaveFromBundle = createButton(removeFromBundleAction,
                AllIcons.Actions.ArrowCollapse);

        JPanel switchPanel = new JPanel();
        switchPanel.setLayout(new BoxLayout(switchPanel, BoxLayout.Y_AXIS));
        switchPanel.add(moveToBundle);
        switchPanel.add(leaveFromBundle);
        return switchPanel;
    }

    private void initActions() {
        moveToBundleAction = new MoveToBundleAction(bundle,
                () -> {
                    if (!bundle.hasSpace()) {
                        showWarning("The bundle already contain 10 Tekton objects. Please remove one layer before to add a new one.", null);
                    } else {
                        warning.setVisible(false);
                    }
                },
                updateBundlePanel(),
                () -> tree.getLastSelectedPathComponent());

        removeFromBundleAction = new RemoveFromBundleAction(bundle,
                () -> warning.setVisible(false),
                updateBundlePanel(),
                () -> layers.getSelectedValue());
    }

    private Runnable updateBundlePanel() {
        return () -> {
            layers.setModel(new CollectionListModel<>(bundle.getResources()));
            bundleBodyPanel.invalidate();
        };
    }

    private JButton createButton(Action action, Icon icon) {
        JButton button = new JButton();
        button.setPreferredSize(new Dimension(50, 35));
        button.setAction(action);
        button.setEnabled(false);
        button.setIcon(icon);
        return button;
    }

    private JComponent buildBundleBodyPanel() {
        bundleBodyPanel = new JPanel(new BorderLayout());

        layers = initJBList(new JBList<>(),
                Collections.emptyList(),
                "No resource in your Tekton Bundle",
                null,
                (list, value, index, isSelected, cellHasFocus) -> new JLabel(value.name(), value.getIcon(), SwingConstants.LEFT),
                e -> {
                    Object selected = layers.getSelectedValue();
                    leaveFromBundle.setEnabled(selected != null);
                }
        );
        layers.addMouseListener(createDoubleClickAdapter(() ->  removeFromBundleAction.actionPerformed(null)));

        JPanel innerPanel = new JPanel(new BorderLayout());
        innerPanel.setBorder(BorderFactory.createCompoundBorder(new JBScrollPane().getBorder(), JBUI.Borders.empty(10)));
        innerPanel.add(layers, BorderLayout.CENTER);
        bundleBodyPanel.add(innerPanel, BorderLayout.CENTER);

        JPanel registryPanel = new JPanel(new BorderLayout());
        JLabel lblBundleName = new JLabel("Image name:  ");
        lblBundleName.setToolTipText("The image must be in the form registry/repository/image:version (e.g. quay.io/myrepo/mybundle:latest");
        registryPanel.add(lblBundleName, BorderLayout.WEST);
        txtValueParam = new JTextField("");
        registryPanel.add(txtValueParam, BorderLayout.CENTER);
        registryPanel.setBorder(JBUI.Borders.empty(10, 5, 10, 0));
        bundleBodyPanel.add(registryPanel, BorderLayout.NORTH);
        return bundleBodyPanel;
    }

    private MouseAdapter createDoubleClickAdapter(Runnable runnable) {
        return new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    runnable.run();
                }
            }
        };
    }

    @Override
    protected JComponent buildLeftPanel() {
        tree = createTree();
        return new JBScrollPane(tree);
    }

    private Tree createTree() {
        TektonBundleResourceTreeStructure structure = new TektonBundleResourceTreeStructure(project);
        StructureTreeModel<TektonBundleResourceTreeStructure> model = null;
        try {
            model = TreeHelper.buildModel(structure, project);
        } catch (IllegalAccessException | InvocationTargetException | InstantiationException | NoSuchMethodException e) {
            logger.warn(e.getLocalizedMessage(), e);
        }
        new MutableTektonModelSynchronizer<>(model, structure, structure);

        Tree tree = new Tree(new AsyncTreeModel(model, project));
        tree.putClientProperty(Constants.STRUCTURE_PROPERTY, structure);
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.setCellRenderer(new NodeRenderer() {
            @Override
            public void customizeCellRenderer(@NotNull JTree tree, @NlsSafe Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
                super.customizeCellRenderer(tree, value, selected, expanded, leaf, row, hasFocus);
                if (selected) {
                    boolean isEnabled = false;
                    Object element = value;
                    if (element instanceof DefaultMutableTreeNode) {
                        Object userObject = ((DefaultMutableTreeNode) element).getUserObject();
                        LabelAndIconDescriptor descriptor = userObject == null ? null : (LabelAndIconDescriptor) userObject;
                        if (descriptor != null) {
                            element = descriptor.getElement();
                            isEnabled = element instanceof PipelineNode ||
                                    element instanceof TaskNode ||
                                    element instanceof ClusterTaskNode;
                        }
                    }
                    moveToBundle.setEnabled(isEnabled);
                }
            }
        });
        tree.addMouseListener(createDoubleClickAdapter(() -> moveToBundleAction.actionPerformed(null)));
        tree.setRootVisible(false);
        return tree;
    }

    @Override
    protected void doOKAction() {
        String imageName = txtValueParam.getText();
        if (imageName.isEmpty()) {
            showWarning("Please add a valid image name (e.g quay.io/myrepo/mybundle:latest)", txtValueParam);
            return;
        }

        List<Resource> resources = bundle.getResources();
        if (resources.isEmpty()) {
            showWarning("Please add atleast one Tekton resource to create a valid bundle (max 10 resources)", layers);
            return;
        }

        enableLoadingState();
        String finalImageName = BundleUtils.cleanImage(imageName);;
        ExecHelper.submit(() -> {
            try {
                List<String> yamlOfResources = tkn.getResourcesAsYaml(resources);
                tkn.deployBundle(finalImageName, yamlOfResources);
                NotificationHelper.notify(project, "Bundle deployed successful",
                        finalImageName + " has been successfully deployed!",
                        NotificationType.INFORMATION,
                        true);
                UIHelper.executeInUI(() -> {
                    disableLoadingState();
                    super.doOKAction();
                });
            } catch (IOException e) {
                // TODO ask for username/psw when issue in cli is fixed
                //  now show message to inform user needs to add configuration to docker config.json or podman auth.json
                String message = !e.getLocalizedMessage().toLowerCase().contains("unauthorized") ?
                        e.getLocalizedMessage() :
                        "The plugin does not support dynamic authentication. Please set up the docker.config " +
                                "and/or podman's auth.json in your home directory to deploy to your registry and try again";
                UIHelper.executeInUI(() -> {
                    disableLoadingState();
                    showTimedErrorMessage("<html>" + message + "</html>");
                });
            }
        });
    }

    protected void updateLoadingState(Cursor cursor) {
        tree.setCursor(cursor);
        layers.setCursor(cursor);
    }
}
