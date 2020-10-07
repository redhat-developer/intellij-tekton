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
package com.redhat.devtools.intellij.tektoncd.ui.wizard.addtrigger;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.ex.QuickList;
import com.intellij.openapi.actionSystem.ex.QuickListsManager;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.keymap.impl.ui.QuickListsPanel;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.util.IconLoader;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.Icons;
import com.intellij.util.ui.ColorIcon;
import com.redhat.devtools.intellij.tektoncd.ui.wizard.BaseStep;
import com.redhat.devtools.intellij.tektoncd.utils.model.actions.AddTriggerModel;
import com.redhat.devtools.intellij.tektoncd.utils.model.resources.TriggerBindingConfigurationModel;
import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.Image;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.font.TextAttribute;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import javax.swing.Box;
import javax.swing.DefaultListCellRenderer;
import javax.swing.GrayFilter;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListModel;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;


import static com.intellij.util.PlatformIcons.ADD_ICON;
import static com.redhat.devtools.intellij.tektoncd.ui.UIConstants.BLUE;
import static com.redhat.devtools.intellij.tektoncd.ui.UIConstants.BORDER_COMPONENT_VALUE;
import static com.redhat.devtools.intellij.tektoncd.ui.UIConstants.BORDER_LABEL_NAME;
import static com.redhat.devtools.intellij.tektoncd.ui.UIConstants.MARGIN_10;
import static com.redhat.devtools.intellij.tektoncd.ui.UIConstants.MARGIN_BOTTOM_10;
import static com.redhat.devtools.intellij.tektoncd.ui.UIConstants.NO_BORDER;
import static com.redhat.devtools.intellij.tektoncd.ui.UIConstants.ROMAN_PLAIN_13;
import static com.redhat.devtools.intellij.tektoncd.ui.UIConstants.TIMES_PLAIN_14;

public class TriggerStep extends BaseStep {

    private JComboBox cmbPreMadeTriggerBindingTemplates;
    private JTextArea textAreaNewTriggerBinding;
    private JScrollPane scrollTriggerBindingAreaPane;
    private JList listBindingsAvailableOnCluster;
    private JLabel lblErrorNewBinding;

    public TriggerStep(AddTriggerModel model, Map<String, String> triggerBindingTemplates) {
        super("Trigger", model);
        setContent(triggerBindingTemplates);
    }

    @Override
    public void setContent() {}

    public void setContent(Map<String, String> triggerBindingTemplates) {
        final int[] row = {0};

        String infoText = "<html>The following list allows you to associate one or more bindings to the event-listener ";
               infoText += "which will be created eventually. You are allowed to select none, one or many bindings. ";
               infoText += "Only newly-created bindings that are selected will be actually pushed on cluster.</html>";
        JLabel lblInfoText = new JLabel(infoText);
        addComponent(lblInfoText, defaultLabelFont, new EmptyBorder(10, 0, 10, 0), new Dimension(594, 91), buildGridBagConstraints(0, row[0], 2, GridBagConstraints.NORTHWEST, null));
        row[0] += 1;

        JLabel lblExistingBindings = new JLabel("<html><span style=\\\"font-family:serif;font-size:10px;font-weight:bold;\\\">Select one or more TriggerBindings</span></html>");
        addComponent(lblExistingBindings, null, MARGIN_BOTTOM_10, null, 0, row[0], GridBagConstraints.NORTHWEST);
        row[0] += 1;

        listBindingsAvailableOnCluster = new JBList();
        listBindingsAvailableOnCluster.setLayoutOrientation(JList.VERTICAL);
        listBindingsAvailableOnCluster.setBorder(new EmptyBorder(5,2,2,2));
        // show name as tooltip
        listBindingsAvailableOnCluster.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                JList sourceList = (JList)e.getSource();
                int index = sourceList.locationToIndex(e.getPoint());
                if(index>-1) {
                    sourceList.setToolTipText(sourceList.getModel().getElementAt(index).toString());
                }
            }
        });
        listBindingsAvailableOnCluster.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (label.getText().endsWith(" NEW")) {
                    String beautifierText = label.getText().replace(" NEW", "");
                    label.setText(beautifierText);
                    label.setIcon(AllIcons.Actions.New);
                }
                return label;
            }
        });
        fillAvailableBindingsList();

        JScrollPane outerListScrollPane = new JBScrollPane();
        outerListScrollPane.setViewportView(listBindingsAvailableOnCluster);
        outerListScrollPane.setPreferredSize(new Dimension(200, 350));

        // actions column
        JButton btnAdd = createActionButton(AllIcons.General.Add, AllIcons.General.InlineAdd);
        JButton btnRemove = createActionButton(AllIcons.General.Remove, IconLoader.getDisabledIcon(AllIcons.General.Remove));
        btnRemove.setEnabled(false);
        JButton btnFind = createActionButton(AllIcons.Actions.Find, IconLoader.getDisabledIcon(AllIcons.Actions.Find));
        btnFind.setEnabled(false);

        Box box = Box.createVerticalBox();
        box.add(btnAdd);
        box.add(btnRemove);
        box.add(btnFind);

        // right panel
        JPanel rightPanel = new JPanel();
        rightPanel.setBackground(backgroundTheme);

        // description panel
        JLabel lblDescriptionNewBinding = createDescriptionLabel("Add a new binding", AllIcons.General.Add, SwingConstants.LEFT, BORDER_LABEL_NAME);
        JLabel lblDescriptionRemoveBinding = createDescriptionLabel("Remove a newly-created selected binding", AllIcons.General.Remove, SwingConstants.LEFT, BORDER_LABEL_NAME);
        JLabel lblDescriptionFindBinding = createDescriptionLabel("Show the content of the selected binding", AllIcons.Actions.Find, SwingConstants.LEFT, BORDER_LABEL_NAME);
        JLabel lblDescriptionGeneral1 = createDescriptionLabel("Select none or one or many bindings", null, -1, BORDER_LABEL_NAME);
        JLabel lblDescriptionGeneral2 = createDescriptionLabel("Go Next", null, -1, BORDER_LABEL_NAME);

        JPanel descriptionRightPanel = new JPanel(gridBagLayout);
        descriptionRightPanel.setBorder(new EmptyBorder(60, 30, 0, 0));
        descriptionRightPanel.setBackground(backgroundTheme);

        descriptionRightPanel.add(lblDescriptionNewBinding, buildGridBagConstraints(0, 0, 1, GridBagConstraints.WEST, null));
        descriptionRightPanel.add(lblDescriptionRemoveBinding, buildGridBagConstraints(0, 1, 1, GridBagConstraints.WEST, null));
        descriptionRightPanel.add(lblDescriptionFindBinding, buildGridBagConstraints(0, 2, 1, GridBagConstraints.WEST, null));
        descriptionRightPanel.add(lblDescriptionGeneral1, buildGridBagConstraints(0, 3, 1, GridBagConstraints.WEST, null));
        descriptionRightPanel.add(lblDescriptionGeneral2, buildGridBagConstraints(0, 4, 1, GridBagConstraints.WEST, null));

        // edit panel
        JPanel editRightPanel = new JPanel(gridBagLayout);
        editRightPanel.setBackground(backgroundTheme);
        editRightPanel.setBorder(new EmptyBorder(0, 15, 0, 0));
        editRightPanel.setVisible(false);

        JLabel btnSave = new JLabel("Save");
        Font font = btnSave.getFont();
        btnSave.setFont(font.deriveFont(font.getStyle() | Font.BOLD));
        btnSave.setForeground(BLUE);
        btnSave.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel btnClose = new JLabel("Close");
        Font fontClose = btnClose.getFont();
        btnClose.setFont(fontClose.deriveFont(fontClose.getStyle() | Font.BOLD));
        btnClose.setForeground(BLUE);
        btnClose.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // top right panel
        JPanel topButtonsPanel = new JPanel(new FlowLayout());
        topButtonsPanel.setBackground(backgroundTheme);
        topButtonsPanel.add(btnSave);
        topButtonsPanel.add(btnClose);

        int internalRow = 0;
        editRightPanel.add(topButtonsPanel, buildGridBagConstraints(1, internalRow, -1, GridBagConstraints.EAST, null));
        internalRow++;

        JLabel lblSelectTemplate = new JLabel("Select a template");

        cmbPreMadeTriggerBindingTemplates = new ComboBox();
        cmbPreMadeTriggerBindingTemplates.addItem("");
        triggerBindingTemplates.keySet().stream().sorted().forEach(template -> cmbPreMadeTriggerBindingTemplates.addItem(template));
        cmbPreMadeTriggerBindingTemplates.addItemListener(itemEvent -> {
            if (itemEvent.getStateChange() == 1) {
                // when cmbPreMadeTriggerBindingTemplates combo box value changes, the new value is saved and preview is updated
                String templateSelected = (String) itemEvent.getItem();
                if (!templateSelected.isEmpty()) {
                    String content = triggerBindingTemplates.get(templateSelected);
                    textAreaNewTriggerBinding.setText(content);
                } else {
                    textAreaNewTriggerBinding.setText("");
                }
                fireStateChanged();
            }
        });

        editRightPanel.add(lblSelectTemplate, buildGridBagConstraints(0, internalRow, 1, GridBagConstraints.WEST, null));
        editRightPanel.add(cmbPreMadeTriggerBindingTemplates, buildGridBagConstraints(1, internalRow, 1, GridBagConstraints.EAST, null));
        internalRow++;

        lblErrorNewBinding = new JLabel();
        lblErrorNewBinding.setVisible(false);
        lblErrorNewBinding.setForeground(Color.RED);
        editRightPanel.add(lblErrorNewBinding, buildGridBagConstraints(0, internalRow, 2, GridBagConstraints.WEST, null));
        internalRow++;

        textAreaNewTriggerBinding = new JTextArea(15, 35);
        textAreaNewTriggerBinding.setEditable(true);
        textAreaNewTriggerBinding.setText("");
        textAreaNewTriggerBinding.setFont(defaultLabelFont);

        scrollTriggerBindingAreaPane = new JBScrollPane(textAreaNewTriggerBinding);
        scrollTriggerBindingAreaPane.setBorder(BORDER_LABEL_NAME);

        editRightPanel.add(scrollTriggerBindingAreaPane, buildGridBagConstraints(0, internalRow, 2, GridBagConstraints.WEST, new Insets(10, 0, 0, 0)));
        internalRow++;

        rightPanel.add(descriptionRightPanel);
        rightPanel.add(editRightPanel);

        // listeners
        editRightPanel.addComponentListener ( new ComponentAdapter()
        {
            public void componentShown(ComponentEvent e)
            {
                descriptionRightPanel.setVisible(false);
            }

            public void componentHidden(ComponentEvent e)
            {
                descriptionRightPanel.setVisible(true);
            }
        });

        btnAdd.addActionListener(e -> {
            if (!editRightPanel.isVisible()) {
                btnAdd.setEnabled(false);
                editRightPanel.setVisible(true);
            }
            lblSelectTemplate.setVisible(true);
            cmbPreMadeTriggerBindingTemplates.setVisible(true);
            btnSave.setVisible(true);
        });

        btnClose.addMouseListener(new MouseAdapter() {
                Font original;

                @Override
                public void mouseClicked(MouseEvent e) {
                    if (editRightPanel.isVisible()) {
                        btnAdd.setEnabled(true);
                        textAreaNewTriggerBinding.setText("");
                        lblErrorNewBinding.setVisible(false);
                        cmbPreMadeTriggerBindingTemplates.setSelectedIndex(0);
                        editRightPanel.setVisible(false);

                    }
                }

                @Override
                public void mouseEntered(MouseEvent e) {

                    original = e.getComponent().getFont();
                    Map attributes = original.getAttributes();
                    attributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
                    e.getComponent().setFont(original.deriveFont(attributes));
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    e.getComponent().setFont(original);
                }
        });

        listBindingsAvailableOnCluster.addListSelectionListener(listSelectionEvent -> {
            if (!listBindingsAvailableOnCluster.getSelectedValuesList().isEmpty()) {
                if (listBindingsAvailableOnCluster.getSelectedValuesList().get(0).toString().endsWith(" NEW")) {
                    btnRemove.setEnabled(true);
                } else {
                    btnRemove.setEnabled(false);
                }
                btnFind.setEnabled(true);
            } else {
                btnRemove.setEnabled(false);
                btnFind.setEnabled(false);
            }
        });

        btnFind.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                String bindingSelected = listBindingsAvailableOnCluster.getSelectedValuesList().get(0).toString();
                String bindingContent = ((AddTriggerModel) model).getBindingsAvailableOnCluster().get(bindingSelected);

                btnSave.setVisible(false);
                lblSelectTemplate.setVisible(false);
                cmbPreMadeTriggerBindingTemplates.setVisible(false);

                textAreaNewTriggerBinding.setText(bindingContent);
                btnAdd.setEnabled(true);

                if (!editRightPanel.isVisible()) {
                    editRightPanel.setVisible(true);
                }
            }
        });

        btnRemove.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                String bindingSelected = listBindingsAvailableOnCluster.getSelectedValuesList().get(0).toString();
                if (bindingSelected.endsWith(" NEW")) {
                    ((AddTriggerModel) model).getBindingsAvailableOnCluster().remove(bindingSelected);
                    fillAvailableBindingsList();
                }
            }
        });

        btnSave.addMouseListener(
                new MouseAdapter() {
                    Font original;

                    @Override
                    public void mouseClicked(MouseEvent e) {
                        String configuration = textAreaNewTriggerBinding.getText();
                        String error = "";
                        if (!configuration.isEmpty()) {
                            TriggerBindingConfigurationModel bindingModel = new TriggerBindingConfigurationModel(configuration);
                            if (bindingModel.isValid()) {
                                if (((AddTriggerModel) model).getBindingsAvailableOnCluster().keySet().stream().map(binding -> binding.replace(" NEW", "")).anyMatch(binding -> binding.equalsIgnoreCase(bindingModel.getName()))) {
                                    error = "<html>The name has already been used for another TriggerBinding. <br> Please change it and save again!!</html>";
                                } else {
                                    ((AddTriggerModel) model).getBindingsAvailableOnCluster().put(bindingModel.getName() + " NEW", textAreaNewTriggerBinding.getText());
                                    fillAvailableBindingsList();
                                }
                            } else {
                                error = bindingModel.getErrorMessage();
                            }
                        } else {
                            error = "You cannot save an empty TriggerBinding.";
                        }

                        if (error.isEmpty()) {
                            editRightPanel.setVisible(false);
                            btnAdd.setEnabled(true);
                            return;
                        }

                        // if the new binding written by the user is not valid, we should show an error message with some info
                        lblErrorNewBinding.setText(error);
                        lblErrorNewBinding.setVisible(true);
                    }

                    @Override
                    public void mouseEntered(MouseEvent e) {
                        original = e.getComponent().getFont();
                        Map attributes = original.getAttributes();
                        attributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
                        e.getComponent().setFont(original.deriveFont(attributes));
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        e.getComponent().setFont(original);
                    }
        });

        JPanel wrapperContentPanel = new JPanel(new BorderLayout());
        wrapperContentPanel.add(outerListScrollPane, BorderLayout.WEST);
        wrapperContentPanel.add(box, BorderLayout.CENTER);
        wrapperContentPanel.add(rightPanel, BorderLayout.EAST);
        addComponent(wrapperContentPanel, editorFont, null, null, 0, row[0], GridBagConstraints.NORTHWEST);
        row[0] += 1;

        adjustContentPanel();
    }

    private void fillAvailableBindingsList() {
        listBindingsAvailableOnCluster.removeAll();
        listBindingsAvailableOnCluster.setListData(((AddTriggerModel) model).getBindingsAvailableOnCluster().keySet().stream().sorted().toArray());
    }

    private JLabel createDescriptionLabel(String text, Icon icon, int horizontalPosition, Border border) {
        JLabel lblDescription = new JLabel(text);
        if (icon != null) {
            lblDescription.setIcon(icon);
        }
        if (horizontalPosition != -1) {
            lblDescription.setHorizontalTextPosition(horizontalPosition);
        }
        if (border != null) {
            lblDescription.setBorder(border);
        }
        return lblDescription;
    }

    private JButton createActionButton(Icon activeIcon, Icon disabledIcon) {
        JButton actionButton = new JButton();
        actionButton.setIcon(activeIcon);
        actionButton.setDisabledIcon(disabledIcon);
        actionButton.setContentAreaFilled(false);
        actionButton.setBorderPainted(false);
        actionButton.setPreferredSize(new Dimension(AllIcons.General.Add.getIconWidth() + 10, AllIcons.General.Add.getIconHeight() + 10));
        actionButton.setBackground(backgroundTheme);
        actionButton.setBorder(NO_BORDER);
        return actionButton;
    }

    private GridBagConstraints buildGridBagConstraints(int col, int row, int gridWidth, int anchor, Insets insets) {
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = col;
        gridBagConstraints.gridy = row;
        if (anchor != -1) {
            gridBagConstraints.anchor = anchor;
        }
        if (gridWidth != -1) {
            gridBagConstraints.gridwidth = gridWidth;
        }
        if (insets != null) {
            gridBagConstraints.insets = insets;
        }
        return gridBagConstraints;
    }

    @Override
    public boolean isComplete() {
        // trigger bindings are optional in an event listener so users can select none or everything - the result is always valid
        lblErrorNewBinding.setVisible(false);
        ((AddTriggerModel) model).getBindingsSelectedByUser().clear();
        // get selected bindings
        listBindingsAvailableOnCluster.getSelectedValuesList().forEach(binding -> {
            String bindingAsYAML = ((AddTriggerModel) model).getBindingsAvailableOnCluster().get(binding.toString());
            ((AddTriggerModel) model).getBindingsSelectedByUser().put((String) binding, bindingAsYAML);
        });
        return true;
    }

    @Override
    public String getHelpId() {
        return "https://github.com/tektoncd/triggers/blob/master/docs/triggerbindings.md";
    }
}