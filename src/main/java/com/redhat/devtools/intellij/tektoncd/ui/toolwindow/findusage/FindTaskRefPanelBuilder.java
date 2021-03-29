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
package com.redhat.devtools.intellij.tektoncd.ui.toolwindow.findusage;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.fileEditor.impl.text.PsiAwareTextEditorImpl;
import com.intellij.openapi.fileEditor.impl.text.TextEditorProvider;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Divider;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.testFramework.LightVirtualFile;
import com.intellij.ui.OnePixelSplitter;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.ui.UIUtil;
import com.intellij.util.ui.tree.TreeUtil;
import com.redhat.devtools.intellij.common.tree.LabelAndIconDescriptor;
import com.redhat.devtools.intellij.tektoncd.utils.TektonVirtualFileManager;
import com.redhat.devtools.intellij.tektoncd.utils.VirtualFileHelper;
import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import static com.redhat.devtools.intellij.tektoncd.ui.UIConstants.SEARCH_FIELD_BORDER_COLOR;

public class FindTaskRefPanelBuilder {
    private static final Logger logger = LoggerFactory.getLogger(FindTaskRefPanelBuilder.class);
    private final static String FINDTASKREFTOOLWINDOW_ID = "FindTaskRef";
    private static FindTaskRefPanelBuilder instance;
    private JPanel editorPanel;
    private JTree tree;
    private JPopupMenu contextMenu;

    private FindTaskRefPanelBuilder() {}

    public static FindTaskRefPanelBuilder instance() {
        if (instance == null) {
            instance = new FindTaskRefPanelBuilder();
        }
        return instance;
    }

    public void build(Project project, String kind, String task, List<RefUsage> usages) {
        ToolWindow window = ToolWindowManager.getInstance(project).getToolWindow(FINDTASKREFTOOLWINDOW_ID);

        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content panel = contentFactory.createContent(buildTabPanel(project, usages), "Usages of " + kind + " " + task, true);
        panel.setCloseable(true);
        window.getContentManager().addContent(panel);
        window.getContentManager().setSelectedContent(panel);

        window.setToHideOnEmptyContent(true);
        window.setAvailable(true, null);
        window.activate(null);
        window.show(null);
    }

    public JComponent buildTabPanel(Project project, List<RefUsage> usages) {
        OnePixelSplitter tabPanel = new OnePixelSplitter(false, 0.37F) {
            protected Divider createDivider() {
                Divider divider = super.createDivider();
                divider.setBackground(SEARCH_FIELD_BORDER_COLOR);
                return divider;
            }
        };
        tabPanel.setFirstComponent(buildUsagesTree(project, usages));
        tabPanel.setSecondComponent(buildEditorPanel());
        return tabPanel;
    }

    private JComponent buildUsagesTree(Project project, List<RefUsage> usages) {
        DefaultTreeModel model = getModel(project, usages);
        tree = new Tree(model);
        tree.setCellRenderer(getTreeCellRenderer());
        tree.addTreeSelectionListener(getTreeSelectionListener(project));
        tree.addMouseListener(getMouseListener(project));
        tree.setVisible(true);
        return new JBScrollPane(tree);
    }

    private DefaultTreeModel getModel(Project project, List<RefUsage> usages) {
        int totalUsages = usages.stream().map(ref -> ref.getOccurrence()).reduce(0, Integer::sum);
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(new LabelAndIconDescriptor(project, null, "Find Usages", getUsageText(totalUsages, false), null, null));
        DefaultTreeModel model = new DefaultTreeModel(root);

        Map<String, List<RefUsage>> kindPerRefs = usages.stream().collect(Collectors.groupingBy(e -> e.getKind(), Collectors.toList()));
        kindPerRefs.entrySet().stream().forEach(entry -> {
            int totalUsagesByKind = entry.getValue().stream().map(ref -> ref.getOccurrence()).reduce(0, Integer::sum);
            DefaultMutableTreeNode kindNode = new DefaultMutableTreeNode(new LabelAndIconDescriptor(project, null, entry.getKey(), getUsageText(totalUsagesByKind, true), null, null));
            model.insertNodeInto(kindNode, root, 0);
            entry.getValue().stream().forEach(refUsage -> {
                DefaultMutableTreeNode refNode = new DefaultMutableTreeNode(new LabelAndIconDescriptor(project, refUsage, refUsage.getName(), getUsageText(refUsage.getOccurrence(), true), null, null));
                model.insertNodeInto(refNode, kindNode, 0);
            });
        });

        return model;
    }

    private TreeCellRenderer getTreeCellRenderer() {
        return (tree1, value, selected, expanded, leaf, row, hasFocus) -> {
            Object node = TreeUtil.getUserObject(value);
            if (node instanceof LabelAndIconDescriptor) {
                ((LabelAndIconDescriptor) node).update();
                return createLabel(((LabelAndIconDescriptor) node).getPresentation().getPresentableText(), ((LabelAndIconDescriptor) node).getPresentation().getLocationString());
            }
            return null;
        };
    }

    private TreeSelectionListener getTreeSelectionListener(Project project) {
        return e -> {
            LabelAndIconDescriptor node = getLastSelectedNode();
            if (node != null && node.getElement() instanceof RefUsage) {
                fillEditorPanel(project, (RefUsage) node.getElement());
            } else {
                fillEditorPanelWithMessage();
            }
        };
    }

    private MouseListener getMouseListener(Project project) {
        return new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    LabelAndIconDescriptor node = getLastSelectedNode();
                    if (node != null && node.getElement() instanceof RefUsage) {
                        getContextMenu(project).show(e.getComponent(), e.getX(), e.getY());
                    }
                }
            }
        };
    }

    private JPopupMenu getContextMenu(Project project) {
        if (contextMenu == null) {
            contextMenu = new JPopupMenu();
            JMenuItem item = new JMenuItem("Jump to Source", AllIcons.Actions.EditSource);
            item.addActionListener(getJumpSourceAction(project));
            contextMenu.add(item);
        }

        return contextMenu;
    }

    private ActionListener getJumpSourceAction(Project project) {
        return e -> {
            LabelAndIconDescriptor node = getLastSelectedNode();
            if (node != null && node.getElement() instanceof RefUsage) {
                RefUsage ref = (RefUsage) node.getElement();
                try {
                    VirtualFile vf = TektonVirtualFileManager.getInstance(project).findResource(ref.getNamespace(), ref.getKind(), ref.getName());
                    VirtualFileHelper.openVirtualFileInEditor(project, ref.getNamespace(), ref.getName(), ((LightVirtualFile) vf).getContent().toString(), ref.getKind(), false);
                } catch (IOException ex) {
                    logger.warn(ex.getLocalizedMessage());
                }

            }
        };
    }

    private LabelAndIconDescriptor getLastSelectedNode() {
        Object selection = tree.getLastSelectedPathComponent();
        if (selection == null || !(selection instanceof DefaultMutableTreeNode)) {
            return null;
        }
        Object userObject = ((DefaultMutableTreeNode)selection).getUserObject();
        return userObject == null ? null : (LabelAndIconDescriptor) userObject;
    }

    private JComponent buildEditorPanel() {
        editorPanel = new JPanel(new BorderLayout());
        fillEditorPanelWithMessage();
        return editorPanel;
    }

    private void fillEditorPanelWithMessage() {
        JLabel infoMessage = new JLabel("Select usage to preview");
        infoMessage.setEnabled(false);
        infoMessage.setHorizontalAlignment(JLabel.CENTER);
        updateEditorPanel(infoMessage);
    }

    private void fillEditorPanel(Project project, RefUsage usage) {
        if (project == null || usage == null) {
            fillEditorPanelWithMessage();
        }

        try {
            VirtualFile vf = TektonVirtualFileManager.getInstance(project).findResource(usage.getNamespace(), usage.getKind(), usage.getName());
            vf.setWritable(false);
            PsiAwareTextEditorImpl editor = new PsiAwareTextEditorImpl(project, vf, TextEditorProvider.getInstance());
            updateEditorPanel(new JBScrollPane(editor.getComponent()));
        } catch (IOException e) {
            logger.warn(e.getLocalizedMessage());
        }
    }

    private void updateEditorPanel(JComponent component) {
        editorPanel.removeAll();
        editorPanel.add(component, BorderLayout.CENTER);
        editorPanel.revalidate();
        editorPanel.repaint();
    }

    private String getUsageText(int occurrences, boolean emptyIfZero) {
        if (emptyIfZero && occurrences == 0) {
            return "";
        }
        return occurrences + " " + (occurrences == 1 ? "usage" : "usages");
    }

    private JComponent createLabel(String name, String location) {
        String label = "<html>" + name;
        if (!location.isEmpty()) {
            String rgb = "rgb(" + UIUtil.getLabelDisabledForeground().getRed() + ", " + UIUtil.getLabelDisabledForeground().getGreen() + ", " + UIUtil.getLabelDisabledForeground().getBlue() + ")";
            label += " <span style=\"color: " + rgb + "\">" + location + "</span>";
        }
        label += "</html>";
        return new JLabel(label);
    }
}
