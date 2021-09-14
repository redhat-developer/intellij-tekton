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
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.terminal.JBTerminalSystemSettingsProviderBase;
import com.intellij.terminal.JBTerminalWidget;
import com.intellij.ui.AnimatedIcon;
import com.intellij.ui.OnePixelSplitter;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.content.ContentManager;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import com.intellij.util.ui.tree.TreeUtil;
import com.jediterm.terminal.ProcessTtyConnector;
import com.jediterm.terminal.ui.TerminalPanel;
import com.redhat.devtools.intellij.common.tree.LabelAndIconDescriptor;
import com.redhat.devtools.intellij.tektoncd.actions.debug.toolbar.DebugToolbarAction;
import com.redhat.devtools.intellij.tektoncd.actions.debug.toolbar.DebugToolbarContinueAction;
import com.redhat.devtools.intellij.tektoncd.actions.debug.toolbar.DebugToolbarContinueWithFailureAction;
import com.redhat.devtools.intellij.tektoncd.actions.debug.toolbar.DebugToolbarTerminateAction;
import com.redhat.devtools.intellij.tektoncd.actions.task.DebugModel;
import com.redhat.devtools.intellij.tektoncd.tkn.Tkn;
import com.redhat.devtools.intellij.tektoncd.ui.toolwindow.findusage.FindTaskRefPanelBuilder;
import io.fabric8.kubernetes.client.dsl.ExecWatch;
import java.awt.BorderLayout;
import java.awt.Color;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Map;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import static com.intellij.ui.AnimatedIcon.ANIMATION_IN_RENDERER_ALLOWED;
import static com.redhat.devtools.intellij.tektoncd.ui.UIConstants.SEARCH_FIELD_BORDER_COLOR;

public class DebugPanelBuilder {
    private static final Logger logger = LoggerFactory.getLogger(FindTaskRefPanelBuilder.class);
    private final static String TEKTONDEBUGTOOLWINDOW_ID = "TektonDebug";
    private static DebugPanelBuilder instance;
    private JPanel terminalPanel;
    private Tree tree;
    private DefaultTreeModel treeModel;
    private String rootLocation;
    private Icon rootIcon;
    private DebugModel debugModel;
    private Map<String, DebugModel> tabs;

    private DebugToolbarAction debugContinueAction, debugContinueWithFailureAction, debugTerminateAction;

    private DebugPanelBuilder() {
        rootLocation = "Running task...";
        rootIcon = new AnimatedIcon.FS();
    }

    public static DebugPanelBuilder instance() {
        if (instance == null) {
            instance = new DebugPanelBuilder();
        }
        return instance;
    }

    public void build(Tkn tkn, DebugModel model) {
        Project project = tkn.getProject();
        ToolWindow window = ToolWindowManager.getInstance(project).getToolWindow(TEKTONDEBUGTOOLWINDOW_ID);

        ContentManager contentManager = window.getContentManager();
        debugModel = model;

        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content panel = contentFactory.createContent(buildDebugPanel(tkn, debugModel), debugModel.getResource(), true);
        panel.setCloseable(true);
        contentManager.addContent(panel);
        contentManager.setSelectedContent(panel);

        window.setToHideOnEmptyContent(true);
        window.setAvailable(true, null);
        window.activate(null);
        window.show(null);
    }

    private JComponent buildDebugPanel(Tkn tkn, DebugModel model) {
        JComponent tabPanel = buildTabPanel(tkn, model);
        ActionToolbar actionToolbar = buildActionColumn(tkn, model);
        actionToolbar.setTargetComponent(tabPanel);

        SimpleToolWindowPanel wrapper = new SimpleToolWindowPanel(false, true);
        wrapper.setContent(tabPanel);
        wrapper.setToolbar(actionToolbar.getComponent());
        wrapper.revalidate();
        return wrapper;
    }

    private JComponent buildTabPanel(Tkn tkn, DebugModel model) {
        OnePixelSplitter tabPanel = new OnePixelSplitter(false, 0.37F) {
            protected Divider createDivider() {
                Divider divider = super.createDivider();
                divider.setBackground(SEARCH_FIELD_BORDER_COLOR);
                return divider;
            }
        };
        tabPanel.setFirstComponent(buildDebugTree(tkn, model));
        tabPanel.setSecondComponent(buildTerminalPanel());
        //panel.add(buildTerminalPanel(tkn, model), BorderLayout.CENTER);
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
        DefaultTreeModel treeModel = getTreeModel(tkn.getProject(), model);
        tree = new Tree(treeModel);
        UIUtil.putClientProperty(tree, ANIMATION_IN_RENDERER_ALLOWED, true);
        tree.setCellRenderer(getTreeCellRenderer());
        // tree.addTreeSelectionListener(getTreeSelectionListener(project));
        //tree.addMouseListener(getMouseListener(project));
        tree.setVisible(true);
        return new JBScrollPane(tree);
    }

    private DefaultTreeModel getTreeModel(Project project, DebugModel model) {

        DefaultMutableTreeNode root = new DefaultMutableTreeNode(new LabelAndIconDescriptor(project, null, () -> model.getResource(), () -> rootLocation, () -> rootIcon, null));
        treeModel = new DefaultTreeModel(root);

       /* Map<String, List<RefUsage>> kindPerRefs = usages.stream().collect(Collectors.groupingBy(e -> e.getKind(), Collectors.toList()));
        kindPerRefs.entrySet().stream().forEach(entry -> {
            int totalUsagesByKind = entry.getValue().stream().map(ref -> ref.getOccurrence()).reduce(0, Integer::sum);
            DefaultMutableTreeNode kindNode = new DefaultMutableTreeNode(new LabelAndIconDescriptor(project, null, entry.getKey(), getUsageText(totalUsagesByKind, true), null, null));
            model.insertNodeInto(kindNode, root, 0);
            entry.getValue().stream().forEach(refUsage -> {
                DefaultMutableTreeNode refNode = new DefaultMutableTreeNode(new LabelAndIconDescriptor(project, refUsage, refUsage.getName(), getUsageText(refUsage.getOccurrence(), true), null, null));
                model.insertNodeInto(refNode, kindNode, 0);
            });
        });*/

        return treeModel;
    }

    public void update(Tkn tkn, DebugModel model) {
        debugModel = model;
        Project project = tkn.getProject();
        debugContinueAction.getTemplatePresentation().setEnabled(true);
        debugContinueWithFailureAction.getTemplatePresentation().setEnabled(true);

        DefaultMutableTreeNode kindNode = new DefaultMutableTreeNode(new LabelAndIconDescriptor(project, null, "step 1", null, null));
        treeModel.insertNodeInto(kindNode, (MutableTreeNode) treeModel.getRoot(), 0);
        tree.revalidate();
        rootLocation = "Stopped. Ready for Debugging...";
        rootIcon = AllIcons.Actions.Pause;
        tree.expandPath(new TreePath(((DefaultMutableTreeNode)treeModel.getRoot()).getPath()));

        JComponent terminalPanel = buildTerminalPanel(tkn, model);
        updateTerminalPanel(terminalPanel);

    }

    private TreeCellRenderer getTreeCellRenderer() {
        return (tree1, value, selected, expanded, leaf, row, hasFocus) -> {
            Object node = TreeUtil.getUserObject(value);
            if (node instanceof LabelAndIconDescriptor) {
                ((LabelAndIconDescriptor) node).update();
                return createLabel(((LabelAndIconDescriptor) node).getPresentation().getPresentableText(),
                        ((LabelAndIconDescriptor) node).getPresentation().getLocationString(),
                        ((LabelAndIconDescriptor) node).getIcon(),
                        SwingConstants.LEFT);
            }
            return null;
        };
    }

    private JComponent createLabel(String name, String location, Icon icon, int horizontalAlignment) {
        String label = "<html><span style=\"font-weight: bold;\">" + name + ":</span> ";
        if (location != null && !location.isEmpty() ) {
            label += location;
        }
        label += "</html>";
        return new JLabel(label, icon, horizontalAlignment);
    }

    private ActionToolbar buildActionColumn(Tkn tkn, DebugModel model) {
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
                () -> debugModel);

        debugContinueWithFailureAction = new DebugToolbarContinueWithFailureAction("Continue with Failure",
                "Mark the step as completed with failure and terminate the task",
                AllIcons.RunConfigurations.RerunFailedTests,
                tkn,
                () -> debugModel);

        debugTerminateAction = new DebugToolbarTerminateAction("Terminate",
                "Stop the task execution",
                AllIcons.Actions.Suspend,
                tkn,
                () -> debugModel);
    }

    private JButton createActionButton(Icon icon, Color background) {
        JButton button = new JButton(icon);

        button.setBackground(background);
        button.setBorder(JBUI.Borders.empty());
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        return button;
    }

    private JComponent buildTerminalPanel(Tkn tkn, DebugModel model) {
        ExecWatch execWatch = tkn.openContainerWatch(model.getPod(), model.getContainerId());
        Process process = createDebugProcess(execWatch);
        final JBTerminalWidget terminal = new JBTerminalWidget(tkn.getProject(),
                new JBTerminalSystemSettingsProviderBase(),
                Disposer.newDisposable());
        terminal.setTtyConnector(createDebugProcessConnector(process));
        TerminalPanel panel = terminal.getTerminalPanel();
        terminal.start();
        return panel;
    }

    private Process createDebugProcess(ExecWatch execWatch) {
        return new Process() {
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

    private ProcessTtyConnector createDebugProcessConnector(Process process) {
        return new ProcessTtyConnector(process, Charset.defaultCharset()) {

            @Override
            protected void resizeImmediately() {

            }

            @Override
            public String getName() {
                return "test";
            }

            @Override
            public boolean isConnected() {
                return true;
            }
        };
    }
    /*
    it works
    private JComponent buildTerminalPanel(Project project, DebugModel model) {
        TerminalPanel panel = null;
        try {
            String[] command = new String[] {"kubectl", "exec", "-it", "-c", "step-build-sources-2", "foo-run-cd28p-pod-bqwqs", "sh"};
            //kubectl exec -it -c $name-of-container $name-of-pod (sh | bash | ash)
            PtyProcessBuilder builder = new PtyProcessBuilder(command)
                    .setDirectory(".");
            PtyProcess process = builder.start();


            final JBTerminalWidget terminal = new JBTerminalWidget(project, new JBTerminalSystemSettingsProviderBase(), Disposer.newDisposable());

            terminal.setTtyConnector(new PtyProcessTtyConnector(process, Charset.defaultCharset()));

            panel = terminal.getTerminalPanel();

            terminal.start();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return panel;
    } */


}
