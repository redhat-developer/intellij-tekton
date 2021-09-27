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

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Divider;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.util.IconLoader;
import com.intellij.terminal.TerminalExecutionConsole;
import com.intellij.ui.AnimatedIcon;
import com.intellij.ui.OnePixelSplitter;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.ui.UIUtil;
import com.intellij.util.ui.tree.TreeUtil;
import com.pty4j.PtyProcess;
import com.pty4j.WinSize;
import com.redhat.devtools.intellij.common.tree.LabelAndIconDescriptor;
import com.redhat.devtools.intellij.common.utils.ExecProcessHandler;
import com.redhat.devtools.intellij.tektoncd.actions.debug.toolbar.DebugToolbarAction;
import com.redhat.devtools.intellij.tektoncd.actions.debug.toolbar.DebugToolbarContinueAction;
import com.redhat.devtools.intellij.tektoncd.actions.debug.toolbar.DebugToolbarContinueWithFailureAction;
import com.redhat.devtools.intellij.tektoncd.actions.debug.toolbar.DebugToolbarTerminateAction;
import com.redhat.devtools.intellij.tektoncd.tkn.Tkn;
import com.redhat.devtools.intellij.tektoncd.utils.model.debug.DebugModel;
import com.redhat.devtools.intellij.tektoncd.utils.model.debug.DebugResourceState;
import io.fabric8.kubernetes.client.dsl.ExecWatch;
import java.awt.BorderLayout;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;


import static com.intellij.ui.AnimatedIcon.ANIMATION_IN_RENDERER_ALLOWED;
import static com.redhat.devtools.intellij.tektoncd.ui.UIConstants.SEARCH_FIELD_BORDER_COLOR;

public class DebugTabPanel {

    private DebugModel model;
    private String displayName, rootLocation;
    private Icon rootIcon;
    private Tree tree;
    private DefaultTreeModel treeModel;
    private JPanel terminalPanel;
    private Tkn tkn;
    private ExecWatch activeContainerExecWatch;
    private Process activeDebugProcess;
    private TerminalExecutionConsole terminalExecutionConsole;

    private DebugToolbarAction debugContinueAction, debugContinueWithFailureAction, debugTerminateAction;

    public DebugTabPanel(String displayName, DebugModel model, Tkn tkn) {
        this.displayName = displayName;
        this.model = model;
        this.tkn = tkn;
    }

    public String getDisplayName() {
        return displayName;
    }

    public DebugModel getModel() {
        return model;
    }

    public void updateModel(DebugModel model) {
        this.model = model;
        update();
    }

    public JComponent getComponent() {
        JComponent mainPanel = createMainPanel();
        ActionToolbar actionToolbar = createActionsColumn(tkn);
        actionToolbar.setTargetComponent(mainPanel);

        SimpleToolWindowPanel wrapper = new SimpleToolWindowPanel(false, true);
        wrapper.setContent(mainPanel);
        wrapper.setToolbar(actionToolbar.getComponent());
        wrapper.revalidate();
        return wrapper;
    }

    private ActionToolbar createActionsColumn(Tkn tkn) {
        ensureInitActions(tkn);

        DefaultActionGroup toolbarGroup = new DefaultActionGroup();
        toolbarGroup.add(debugContinueAction);
        toolbarGroup.add(debugContinueWithFailureAction);
        toolbarGroup.add(debugTerminateAction);

        return ActionManager.getInstance().createActionToolbar(ActionPlaces.TODO_VIEW_TOOLBAR, toolbarGroup, false);
    }

    private void ensureInitActions(Tkn tkn) {
        debugContinueAction = new DebugToolbarContinueAction("Continue",
                "Mark the step as completed with success so that the task continues executing",
                AllIcons.Actions.Execute,
                tkn,
                model);

        debugContinueWithFailureAction = new DebugToolbarContinueWithFailureAction("Continue with Failure",
                "Mark the step as completed with failure and terminate the task",
                AllIcons.RunConfigurations.RerunFailedTests,
                tkn,
                model);

        debugTerminateAction = new DebugToolbarTerminateAction("Terminate",
                "Stop the task execution",
                AllIcons.Actions.Suspend,
                tkn,
                model);
    }

    private JComponent createMainPanel() {
        OnePixelSplitter tabPanel = new OnePixelSplitter(false, 0.37F) {
            protected Divider createDivider() {
                Divider divider = super.createDivider();
                divider.setBackground(SEARCH_FIELD_BORDER_COLOR);
                return divider;
            }
        };
        tabPanel.setFirstComponent(buildDebugTree(tkn, model));
        tabPanel.setSecondComponent(buildTerminalPanel());
        return tabPanel;
    }

    private JComponent buildTerminalPanel() {
        terminalPanel = new JPanel(new BorderLayout());
        fillTerminalPanelWithMessage();
        return terminalPanel;
    }

    private void fillTerminalPanelWithMessage() {
        JLabel infoMessage = new JLabel("Nothing to show");
        infoMessage.setEnabled(false);
        infoMessage.setHorizontalAlignment(JLabel.CENTER);
        updateTerminalPanel(infoMessage);
    }

    private void updateTerminalPanel(JComponent component) {
        terminalPanel.removeAll();
        terminalPanel.add(component, BorderLayout.CENTER);
        terminalPanel.revalidate();
        terminalPanel.repaint();
    }

    private JComponent buildDebugTree(Tkn tkn, DebugModel model) {
        updateRoot();
        DefaultTreeModel treeModel = getTreeModel(tkn.getProject(), model);
        tree = new Tree(treeModel);
        UIUtil.putClientProperty(tree, ANIMATION_IN_RENDERER_ALLOWED, true);
        tree.setCellRenderer(getTreeCellRenderer());
        tree.setVisible(true);
        return new JBScrollPane(tree);
    }

    private DefaultTreeModel getTreeModel(Project project, DebugModel model) {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(
                new LabelAndIconDescriptor(project,
                        null,
                        model::getResource,
                        () -> rootLocation,
                        () -> rootIcon,
                        null
                ));
        treeModel = new DefaultTreeModel(root);
        return treeModel;
    }

    private TreeCellRenderer getTreeCellRenderer() {
        return (tree1, value, selected, expanded, leaf, row, hasFocus) -> {
            Object node = TreeUtil.getUserObject(value);
            if (node instanceof LabelAndIconDescriptor) {
                ((LabelAndIconDescriptor) node).update();
                return createLabel(((LabelAndIconDescriptor) node).getPresentation().getPresentableText(),
                        ((LabelAndIconDescriptor) node).getPresentation().getLocationString(),
                        ((LabelAndIconDescriptor) node).getIcon()
                );
            }
            return null;
        };
    }

    private JComponent createLabel(String name, String location, Icon icon) {
        String label = "<html><span style=\"font-weight: bold;\">" + name + "</span> ";
        if (location != null && !location.isEmpty() ) {
            label += location;
        }
        label += "</html>";
        return new JLabel(label, icon, SwingConstants.LEFT);
    }

    private void update() {
        ((DefaultMutableTreeNode)treeModel.getRoot()).removeAllChildren();
        treeModel.reload();

        if (model.getResourceStatus().equals(DebugResourceState.DEBUG)) {
            DefaultMutableTreeNode stepNode = new DefaultMutableTreeNode(
                    new LabelAndIconDescriptor(tkn.getProject(),
                            null,
                            model.getStep(),
                            "(" + model.getImage() + ")",
                            null,
                            null));
            treeModel.insertNodeInto(stepNode, (MutableTreeNode) treeModel.getRoot(), 0);

            updateTerminalPanel(createTerminalComponent());
        } else {
            closeDebugProcess();
            ((DefaultMutableTreeNode)treeModel.getRoot()).removeAllChildren();
            fillTerminalPanelWithMessage();
        }
        updateRoot();
        tree.invalidate();
        tree.expandPath(new TreePath(((DefaultMutableTreeNode)treeModel.getRoot()).getPath()));
    }

    private void closeDebugProcess() {
        if (activeDebugProcess != null) {
            activeDebugProcess.destroy();
        }
        if (activeContainerExecWatch != null) {
            activeContainerExecWatch.close();
        }
        if (terminalExecutionConsole != null) {
            terminalExecutionConsole.dispose();
        }
    }

    private JComponent createTerminalComponent() {
        activeContainerExecWatch = tkn.execCommandInContainer(model.getPod(), model.getContainerId(), "sh");
        activeDebugProcess = createDebugProcess(activeContainerExecWatch);

        ExecProcessHandler processHandler = new ExecProcessHandler(activeDebugProcess, "debug", Charset.defaultCharset());
        terminalExecutionConsole = new TerminalExecutionConsole(tkn.getProject(), processHandler);
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(terminalExecutionConsole.getComponent(), BorderLayout.CENTER);
        processHandler.startNotify();

        return panel;
    }

    private PtyProcess createDebugProcess(ExecWatch execWatch) {
        return new PtyProcess() {
            @Override
            public boolean isRunning() {
                return true;
            }

            @Override
            public void setWinSize(WinSize winSize) {

            }

            @Override
            public WinSize getWinSize() throws IOException {
                return null;
            }

            @Override
            public int getPid() {
                return 0;
            }

            @Override
            public OutputStream getOutputStream() {
                return execWatch.getInput();
            }

            @Override
            public InputStream getInputStream() {
                return execWatch.getOutput();
            }

            @Override
            public InputStream getErrorStream() {
                return execWatch.getError();
            }

            @Override
            public int waitFor() throws InterruptedException {
                return 0;
            }

            @Override
            public int exitValue() {
                return 0;
            }

            @Override
            public void destroy() {

            }

        };
    }

    private void updateRoot() {
        switch(model.getResourceStatus()) {
            case RUNNING: {
                setRoot("Running task...", new AnimatedIcon.FS());
                break;
            }
            case DEBUG: {
                setRoot("Stopped. Ready for Debugging...", AllIcons.Actions.Pause);
                break;
            }
            case COMPLETE_SUCCESS: {
                setRoot("Task Completed", getIcon("/images/success.png"));
                break;
            }
            case COMPLETE_FAILED: {
                setRoot("Task Failed", getIcon("/images/failed.png"));
                break;
            }
        }
    }

    private void setRoot(String location, Icon icon) {
        rootLocation = location;
        rootIcon = icon;
    }

    private Icon getIcon(String path) {
        return IconLoader.findIcon(path, DebugTabPanel.class);
    }
}
