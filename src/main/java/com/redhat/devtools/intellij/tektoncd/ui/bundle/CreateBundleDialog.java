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
import com.intellij.ide.util.treeView.AbstractTreeStructure;
import com.intellij.ide.util.treeView.NodeRenderer;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Divider;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.ui.CollectionListModel;
import com.intellij.ui.OnePixelSplitter;
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
import com.redhat.devtools.intellij.tektoncd.tkn.Bundle;
import com.redhat.devtools.intellij.tektoncd.tkn.Resource;
import com.redhat.devtools.intellij.tektoncd.tkn.Tkn;
import com.redhat.devtools.intellij.tektoncd.tree.ClusterTaskNode;
import com.redhat.devtools.intellij.tektoncd.tree.ConditionNode;
import com.redhat.devtools.intellij.tektoncd.tree.EventListenerNode;
import com.redhat.devtools.intellij.tektoncd.tree.MutableTektonModelSynchronizer;
import com.redhat.devtools.intellij.tektoncd.tree.ParentableNode;
import com.redhat.devtools.intellij.tektoncd.tree.PipelineNode;
import com.redhat.devtools.intellij.tektoncd.tree.PipelineRunNode;
import com.redhat.devtools.intellij.tektoncd.tree.ResourceNode;
import com.redhat.devtools.intellij.tektoncd.tree.TaskNode;
import com.redhat.devtools.intellij.tektoncd.tree.TaskRunNode;
import com.redhat.devtools.intellij.tektoncd.tree.TektonBundleResourceTreeStructure;
import com.redhat.devtools.intellij.tektoncd.tree.TriggerBindingNode;
import com.redhat.devtools.intellij.tektoncd.tree.TriggerTemplateNode;
import com.redhat.devtools.intellij.tektoncd.utils.NotificationHelper;
import com.redhat.devtools.intellij.tektoncd.utils.TreeHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.AbstractAction;
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
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static com.redhat.devtools.intellij.tektoncd.ui.UIConstants.RED;
import static com.redhat.devtools.intellij.tektoncd.ui.UIConstants.RED_BORDER_SHOW_ERROR;
import static com.redhat.devtools.intellij.tektoncd.ui.UIConstants.SEARCH_FIELD_BORDER_COLOR;

public class CreateBundleDialog extends DialogWrapper {

    private Tree tree;
    private JBList<Resource> layers;
    private Project project;
    private Bundle bundle;
    private JPanel bundleBodyPanel, switchPanel;
    private JButton moveToBundle, leaveFromBundle;
    private JLabel warning, lblGeneralError;
    private JTextField txtValueParam;
    private Tkn tkn;

    public CreateBundleDialog(@Nullable Project project, Tkn tkn) {
        super(project, true);
        this.project = project;
        this.tkn = tkn;
        this.bundle = new Bundle();
        setOKButtonText("Deploy");
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
        JLabel lbl = new JLabel("Publish a new Tekton Bundle to a registry by passing in a set (max 10) of Tekton objects",
                AllIcons.General.ShowInfos,
                SwingConstants.LEFT
        );
        lbl.setToolTipText("Look for the Tekton resources you want to include in your bundle and move them from the tree to the bundle body. " +
                "Once done, add the image name and push it to the registry.");
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
        tabPanel.setFirstComponent(buildResourcesTree());
        tabPanel.setSecondComponent(buildRightPanel());
        return tabPanel;
    }

    private JComponent buildRightPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(buildBundleBodyPanel(), BorderLayout.CENTER);
        panel.add(buildSwitchButton(), BorderLayout.WEST);
        return panel;
    }

    private JComponent buildSwitchButton() {
        switchPanel = new JPanel();
        switchPanel.setLayout(new BoxLayout(switchPanel, BoxLayout.Y_AXIS));


        moveToBundle = new JButton();
        moveToBundle.setPreferredSize(new Dimension(50, 35));
        moveToBundle.setAction(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                LabelAndIconDescriptor descriptor = getLastSelectedNode();
                if (descriptor != null) {
                    String resourceName = descriptor.getName();
                    String kind = TreeHelper.getKindFromNode((ParentableNode<?>) descriptor.getElement());
                    Icon icon = descriptor.getIcon();
                    Resource resource = new Resource(resourceName, kind, icon);
                    if(!bundle.hasSpace()) {
                        showWarning("The bundle already contain 10 Tekton objects. Please remove one layer before to add a new one.", null);
                        return;
                    } else {
                        warning.setVisible(false);
                    }
                    if(bundle.hasResource(resource)) {
                        return;
                    }
                    bundle.addResource(resource);
                    layers.setModel(new CollectionListModel<>(bundle.getResources()));
                    bundleBodyPanel.invalidate();
                }
            }

            private LabelAndIconDescriptor getLastSelectedNode() {
                Object selection = tree.getLastSelectedPathComponent();
                if (!(selection instanceof DefaultMutableTreeNode)) {
                    return null;
                }
                Object userObject = ((DefaultMutableTreeNode)selection).getUserObject();
                return userObject == null ? null : (LabelAndIconDescriptor) userObject;
            }
        });
        moveToBundle.setEnabled(false);
        moveToBundle.setIcon(AllIcons.Actions.ArrowExpand);
        switchPanel.add(moveToBundle);

        leaveFromBundle = new JButton();
        leaveFromBundle.setPreferredSize(new Dimension(50, 35));
        leaveFromBundle.setAction(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                warning.setVisible(false);
                Resource selected = layers.getSelectedValue();
                bundle.removeResource(selected);
                layers.setModel(new CollectionListModel<>(bundle.getResources()));
                bundleBodyPanel.invalidate();
            }
        });
        leaveFromBundle.setEnabled(false);
        leaveFromBundle.setIcon(AllIcons.Actions.ArrowCollapse);
        switchPanel.add(leaveFromBundle);

        return switchPanel;
    }

    private JComponent buildBundleBodyPanel() {
        bundleBodyPanel = new JPanel(new BorderLayout());
        JPanel innerPanel = new JPanel(new BorderLayout());

        CompoundBorder border = BorderFactory.createCompoundBorder(new JBScrollPane().getBorder(), JBUI.Borders.empty(10));
        innerPanel.setBorder(border);
        layers = new JBList<>();
        layers.setEmptyText("No resource in your Tekton Bundle");
        layers.setCellRenderer((list, value, index, isSelected, cellHasFocus) -> new JLabel(value.name(), value.getIcon(), SwingConstants.LEFT));
        layers.addListSelectionListener(e -> {
            Object selected = layers.getSelectedValue();
            leaveFromBundle.setEnabled(selected != null);
        });
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

    private JComponent buildResourcesTree() {
        TektonBundleResourceTreeStructure structure = new TektonBundleResourceTreeStructure(project);
        StructureTreeModel<TektonBundleResourceTreeStructure> model = null;
        try {
            model = buildModel(structure, project);
        } catch (IllegalAccessException | InvocationTargetException | InstantiationException | NoSuchMethodException e) {
            e.printStackTrace();
        }
        new MutableTektonModelSynchronizer<>(model, structure, structure);
        tree = new Tree(new AsyncTreeModel(model, project));
        tree.putClientProperty(Constants.STRUCTURE_PROPERTY, structure);
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
                                    element instanceof ConditionNode ||
                                    element instanceof EventListenerNode ||
                                    element instanceof PipelineRunNode ||
                                    element instanceof ResourceNode ||
                                    element instanceof TaskRunNode ||
                                    element instanceof ClusterTaskNode ||
                                    element instanceof TriggerBindingNode ||
                                    element instanceof TriggerTemplateNode;

                        }

                    }
                    moveToBundle.setEnabled(isEnabled);

                }

            }
        });
        tree.setRootVisible(false);
        return new JBScrollPane(tree);
    }

    private StructureTreeModel buildModel(TektonBundleResourceTreeStructure structure, Project project) throws IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException {
        try {
            Constructor<StructureTreeModel> constructor = StructureTreeModel.class.getConstructor(new Class[] {AbstractTreeStructure.class});
            return constructor.newInstance(structure);
        } catch (NoSuchMethodException e) {
            Constructor<StructureTreeModel> constructor = StructureTreeModel.class.getConstructor(new Class[] {AbstractTreeStructure.class, Disposable.class});
            return constructor.newInstance(structure, project);
        }
    }

    @Override
    protected void doOKAction() {
        String imageName = txtValueParam.getText();
        if (imageName.isEmpty()) {
            showWarning("Please add a valid image name (e.g quay.io/myrepo/mybundle:latest)", txtValueParam);
        }
        List<Resource> resources = bundle.getResources();
        if (resources.isEmpty()) {
            showWarning("Please add atleast one Tekton resource to create a valid bundle (max 10 resources)", layers);
        }

        enableLoadingState();
        ExecHelper.submit(() -> {
            try {
                List<String> yamlOfResources = tkn.getResourcesAsYaml(resources);
                deployResources(imageName, yamlOfResources);
                NotificationHelper.notify(project, "Bundle deployed successful",
                        imageName + " has been successfully deployed!",
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
                    lblGeneralError.setText("<html>" + message + "</html>");
                    lblGeneralError.setVisible(true);
                    ExecHelper.submit(() -> {
                        new Timer().schedule(new TimerTask() {
                            @Override
                            public void run() {
                                lblGeneralError.setVisible(false);
                            }
                        }, 30000);
                    });
                });
            }
        });
    }

    private void deployResources(String imageName, List<String> yamlOfResources) throws IOException {
        tkn.deployBundle(imageName, yamlOfResources);
    }

    private void showWarning(String text, JComponent failingComponent) {
        Border curBorder = null;
        if (failingComponent != null) {
            curBorder = failingComponent.getBorder();
            failingComponent.setBorder(RED_BORDER_SHOW_ERROR);
        }
        warning.setText(text);
        warning.setForeground(RED);
        warning.setVisible(true);
        warning.invalidate();

        Border finalCurBorder = curBorder;
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                warning.setVisible(false);
                if (failingComponent != null) {
                    failingComponent.setBorder(finalCurBorder);
                }
            }
        }, 5000);
    }

    private void enableLoadingState() {
        updateLoadingState(new Cursor(Cursor.WAIT_CURSOR));
    }

    private void disableLoadingState() {
        updateLoadingState(Cursor.getDefaultCursor());
    }

    private void updateLoadingState(Cursor cursor) {
        tree.setCursor(cursor);
        layers.setCursor(cursor);
    }
}
