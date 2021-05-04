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
package com.redhat.devtools.intellij.tektoncd.ui.wizard;

import com.intellij.openapi.editor.colors.ColorKey;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.redhat.devtools.intellij.tektoncd.tkn.component.field.Workspace;
import com.redhat.devtools.intellij.tektoncd.utils.model.actions.ActionToRunModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.text.NumberFormatter;
import org.jetbrains.annotations.NotNull;


import static com.redhat.devtools.intellij.tektoncd.tkn.component.field.Workspace.Kind.CONFIGMAP;
import static com.redhat.devtools.intellij.tektoncd.tkn.component.field.Workspace.Kind.EMPTYDIR;
import static com.redhat.devtools.intellij.tektoncd.tkn.component.field.Workspace.Kind.PVC;
import static com.redhat.devtools.intellij.tektoncd.tkn.component.field.Workspace.Kind.SECRET;
import static com.redhat.devtools.intellij.tektoncd.ui.UIConstants.BORDER_LABEL_NAME;
import static com.redhat.devtools.intellij.tektoncd.ui.UIConstants.MARGIN_TOP_35;
import static com.redhat.devtools.intellij.tektoncd.ui.UIConstants.NO_BORDER;
import static com.redhat.devtools.intellij.tektoncd.ui.UIConstants.RED_BORDER_SHOW_ERROR;
import static com.redhat.devtools.intellij.tektoncd.ui.UIConstants.ROW_DIMENSION;
import static com.redhat.devtools.intellij.tektoncd.ui.UIConstants.ROW_DIMENSION_ERROR;
import static com.redhat.devtools.intellij.tektoncd.ui.UIConstants.TIMES_PLAIN_10;
import static com.redhat.devtools.intellij.tektoncd.ui.UIConstants.TIMES_PLAIN_14;

public class WorkspacesStep extends BaseStep {
    Map<String, JPanel> workspacePanelMapper;


    public WorkspacesStep(ActionToRunModel model) {
        super("Workspaces", model);
    }

    @Override
    public boolean isComplete() {
        boolean isComplete = model.getWorkspaces().values().stream().allMatch(workspace -> workspace != null);
        if (!isComplete) {
            final int[] row = {1};
            workspacePanelMapper.entrySet().forEach(entry -> {
                String workspace = entry.getKey();
                JPanel panel = entry.getValue();
                JComboBox cmbWorkspaceTypes = (JComboBox) Arrays.stream(panel.getComponents()).filter(component -> component.getName() != null && component.getName().equals("cmbWorkspaceTypes")).findFirst().get();
                if (!isValid(cmbWorkspaceTypes)) {
                    //add error message
                    cmbWorkspaceTypes.setBorder(RED_BORDER_SHOW_ERROR);
                }
                Workspace.Kind kind = (Workspace.Kind) cmbWorkspaceTypes.getSelectedItem();
                if (kind == PVC) {
                    JComboBox cmbWorkspaceTypeValues = (JComboBox) Arrays.stream(panel.getComponents()).filter(component -> component.getName() != null && component.getName().equals("cmbWorkspaceTypeValues")).findFirst().get();
                    String valueSelected = cmbWorkspaceTypeValues.getSelectedItem().toString();
                    if (valueSelected.equals("Create new PVC")) {
                        JPanel newPVCNamePanel = (JPanel) Arrays.stream(panel.getComponents())
                                .filter(component -> component.getName() != null && component.getName().equals("newPVCNamePanel")).findFirst().get();
                        JTextField namePVC = (JTextField) Arrays.stream(newPVCNamePanel.getComponents())
                                .filter(component -> component.getName() != null && component.getName().equals("txtNameNewPVC")).findFirst().get();
                        JPanel accessModePanel = (JPanel) Arrays.stream(panel.getComponents())
                                .filter(component -> component.getName() != null && component.getName().equals("accessModePanel")).findFirst().get();
                        JComboBox accessModePVC = (JComboBox) Arrays.stream(accessModePanel.getComponents())
                                .filter(component -> component.getName() != null && component.getName().equals("cmbAccessMode")).findFirst().get();
                        JPanel sizePanel = (JPanel) Arrays.stream(panel.getComponents())
                                .filter(component -> component.getName() != null && component.getName().equals("sizePanel")).findFirst().get();
                        JSpinner sizeSpinner = (JSpinner) Arrays.stream(sizePanel.getComponents()).filter(component -> component.getName() != null && component.getName().equals("txtSize")).findFirst().get();
                        JComboBox sizeUnit = (JComboBox) Arrays.stream(sizePanel.getComponents()).filter(component -> component.getName() != null && component.getName().equals("cmbSizeMeasureUnit")).findFirst().get();

                        String name = namePVC.getText();
                        if (name.isEmpty()) {
                            namePVC.setBorder(RED_BORDER_SHOW_ERROR);
                        }
                        String accessMode = accessModePVC.getSelectedItem().toString();
                        if (accessMode.isEmpty()) {
                            accessModePVC.setBorder(RED_BORDER_SHOW_ERROR);
                        }
                        String size = ((JSpinner.NumberEditor)sizeSpinner.getEditor()).getTextField().getText();
                        if (size.isEmpty() || size.equals("0")) {
                            sizeSpinner.setBorder(RED_BORDER_SHOW_ERROR);
                        }
                        String unit = sizeUnit.getSelectedItem().toString();
                        if (unit.isEmpty()) {
                            sizeUnit.setBorder(RED_BORDER_SHOW_ERROR);
                        }
                        if (!name.isEmpty() && !accessMode.isEmpty() && !size.isEmpty() && !unit.isEmpty()) {
                            Map<String, String> values = new HashMap<>();
                            values.put("accessMode", accessMode);
                            values.put("size", size);
                            values.put("unit", unit);
                            Workspace workspace1 = new Workspace(name, PVC, "", values);
                            model.getWorkspaces().put(workspace, workspace1);
                        }
                    }
                }

            });
            /*cmbsWorkspaceTypes.stream().forEach(cmb -> {
                if (!isValid(cmb)) {
                    cmb.setBorder(RED_BORDER_SHOW_ERROR);
                    JLabel lblErrorText = new JLabel("Please select a value.");
                    lblErrorText.setForeground(Color.red);
                    addComponent(lblErrorText, TIMES_PLAIN_10, MARGIN_TOP_35, ROW_DIMENSION_ERROR, 0, row[0], GridBagConstraints.PAGE_END);
                    errorFieldsByRow.put(row[0], lblErrorText);
                    lblErrorText.setEnabled(true);
                }
                row[0] += 3;
            });*/
        }
        return isComplete;
    }

    @Override
    public String getHelpId() {
        return "https://github.com/tektoncd/pipeline/blob/master/docs/workspaces.md";
    }

    public void setContent() {
        workspacePanelMapper = new HashMap<>();
        final int[] row = {0};

        model.getWorkspaces().keySet().forEach(workspaceName -> {
            int innerPanelRow = 0;
            GridBagConstraints workspacePanelConstraint = new GridBagConstraints();
            JPanel workspacePanel = new JPanel(new GridBagLayout());
            workspacePanel.setBackground(backgroundTheme);

            JLabel lblWorkspaceName = new JLabel("<html><span style=\\\"font-family:serif;font-size:11px;font-weight:bold;\\\">" + workspaceName + "</span></html");
            addComponent(lblWorkspaceName, workspacePanel, workspacePanelConstraint,  null, row[0] == 0 ? BORDER_LABEL_NAME : getBottomBorder(), ROW_DIMENSION, 0, innerPanelRow, GridBagConstraints.NORTH);
            innerPanelRow += 1;

            JComboBox cmbWorkspaceTypes = new JComboBox();
            cmbWorkspaceTypes.setName("cmbWorkspaceTypes");
            cmbWorkspaceTypes = (JComboBox) addComponent(cmbWorkspaceTypes, workspacePanel, workspacePanelConstraint, TIMES_PLAIN_14, null, ROW_DIMENSION, 0, innerPanelRow, GridBagConstraints.NORTH);
            cmbWorkspaceTypes.addItem("");
            cmbWorkspaceTypes.addItem(EMPTYDIR);
            cmbWorkspaceTypes.addItem(CONFIGMAP);
            cmbWorkspaceTypes.addItem(SECRET);
            cmbWorkspaceTypes.addItem(PVC);
            Workspace.Kind typeToBeSelected = workspaceName == null ? null : model.getWorkspaces().get(workspaceName) == null ? null : model.getWorkspaces().get(workspaceName).getKind();
            if (typeToBeSelected != null) {
                cmbWorkspaceTypes.setSelectedItem(typeToBeSelected);
            } else {
                cmbWorkspaceTypes.setSelectedItem("");
            }
            innerPanelRow += 1;

            JComboBox cmbWorkspaceTypeValues = new JComboBox();
            cmbWorkspaceTypeValues.setName("cmbWorkspaceTypeValues");
            cmbWorkspaceTypeValues = (JComboBox) addComponent(cmbWorkspaceTypeValues, workspacePanel, workspacePanelConstraint, TIMES_PLAIN_14, null, ROW_DIMENSION, 0, innerPanelRow, GridBagConstraints.NORTH);
            setCmbWorkspaceTypeValues(workspaceName, typeToBeSelected, cmbWorkspaceTypeValues, innerPanelRow - 1);
            addListeners(workspaceName, workspacePanel, innerPanelRow - 1);
            innerPanelRow += 1;

            JLabel lblNewPVCName = new JLabel("<html><span style=\\\"font-family:serif;font-size:9px;font-weight:bold;\\\">Name:</span>   </html");
            lblNewPVCName.setName("lblNameNewPVC");
            JTextField txtNewPVCName = new JTextField("");
            txtNewPVCName.setName("txtNameNewPVC");
            JPanel newPVCNamePanel = createCompoundComponentAsPanel("newPVCNamePanel", lblNewPVCName, txtNewPVCName, null);
            addComponent(newPVCNamePanel, workspacePanel, workspacePanelConstraint,null, NO_BORDER, ROW_DIMENSION, 0, innerPanelRow, GridBagConstraints.NORTH);
            innerPanelRow += 1;

            JLabel lblAccessMode = new JLabel("<html><span style=\\\"font-family:serif;font-size:9px;font-weight:bold;\\\">Mode:</span>   </html");
            lblAccessMode.setName("lblAccessMode");
            JComboBox cmbAccessMode = new JComboBox();
            cmbAccessMode.setName("cmbAccessMode");
            cmbAccessMode.addItem("Single User (RWO)");
            cmbAccessMode.addItem("Shared Access (RWX)");
            cmbAccessMode.addItem("Read Only (ROX)");
            JPanel accessModePanel = createCompoundComponentAsPanel("accessModePanel", lblAccessMode, cmbAccessMode, null);
            addComponent(accessModePanel, workspacePanel, workspacePanelConstraint,null, NO_BORDER, ROW_DIMENSION, 0, innerPanelRow, GridBagConstraints.NORTH);
            innerPanelRow += 1;

            JLabel lblSize = new JLabel("<html><span style=\\\"font-family:serif;font-size:9px;font-weight:bold;\\\">Size:</span>   </html");
            lblSize.setName("lblSize");
            lblSize.setPreferredSize(lblNewPVCName.getPreferredSize());
            JSpinner txtSize = new JSpinner(new SpinnerNumberModel(0,0, Integer.MAX_VALUE,1));
            txtSize.setName("txtSize");
            txtSize.setEditor(new JSpinner.NumberEditor(txtSize, "#"));
            JTextField textField = ((JSpinner.NumberEditor)txtSize.getEditor()).getTextField();
            ((NumberFormatter)((JFormattedTextField) textField).getFormatter()).setAllowsInvalid(false);
            JComboBox cmbSizeMeasureUnit = new JComboBox();
            cmbSizeMeasureUnit.setName("cmbSizeMeasureUnit");
            cmbSizeMeasureUnit.addItem("MB");
            cmbSizeMeasureUnit.addItem("GB");
            cmbSizeMeasureUnit.addItem("TB");
            JPanel sizePanel = createCompoundComponentAsPanel("sizePanel", lblSize, txtSize, cmbSizeMeasureUnit);
            addComponent(sizePanel, workspacePanel, workspacePanelConstraint,null, NO_BORDER, ROW_DIMENSION, 0, innerPanelRow, GridBagConstraints.NORTH);


            addComponent(workspacePanel, null, NO_BORDER, null, 0, row[0], GridBagConstraints.NORTH);
            hidePVCAndVCTComponents(workspacePanel);
            workspacePanelMapper.put(workspaceName, workspacePanel);
            row[0] += 1;
        });

    }

    private JPanel createCompoundComponentAsPanel(String id, JComponent leftComponent, JComponent centerComponent, JComponent rightComponent) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setName(id);
        panel.setBackground(backgroundTheme);
        panel.setBorder(new EmptyBorder(7, 0, 0, 0));
        if (leftComponent != null) {
            panel.add(leftComponent, BorderLayout.LINE_START);
        }
        if (centerComponent != null) {
            panel.add(centerComponent, BorderLayout.CENTER);
        }
        if (rightComponent != null) {
            panel.add(rightComponent, BorderLayout.LINE_END);
        }
        return panel;
    }

    private Border getBottomBorder() {
        Border lineSeparatorBelow = new MatteBorder(1, 0, 0, 0, EditorColorsManager.getInstance().getGlobalScheme().getColor(ColorKey.find("SEPARATOR_BELOW_COLOR")));
        Border outside = BorderFactory.createCompoundBorder(new EmptyBorder(10, 0, 0, 0), lineSeparatorBelow);
        Border margin_5 = new EmptyBorder(10, 0, 5, 0);
        return BorderFactory.createCompoundBorder(outside, margin_5);
    }

    private JComponent addComponent(@NotNull JComponent component, JPanel wrapper, GridBagConstraints gridBagConstraints, Font font, Border border, Dimension preferredSize, @NotNull int col, @NotNull int row, @NotNull int anchor) {
        gridBagConstraints.gridx = col;
        gridBagConstraints.gridy = row;
        gridBagConstraints.anchor = anchor;
        component = setComponentStyle(component, font, border, preferredSize);
        wrapper.add(component, gridBagConstraints);
        return component;
    }

    private void changeNewPVCComponentsVisibility(JPanel parent, boolean visible) {
        List<String> newPVCComponentsName = Arrays.asList("newPVCNamePanel");
        Arrays.stream(parent.getComponents())
                .filter(component -> newPVCComponentsName.contains(component.getName()))
                .forEach(component -> {
                    component.setVisible(visible);
                });
        this.changeNewVCTComponentsVisibility(parent, visible);
    }

    private void changeNewVCTComponentsVisibility(JPanel parent, boolean visible) {
        List<String> newPVCComponentsName = Arrays.asList("accessModePanel", "sizePanel");
        Arrays.stream(parent.getComponents())
                .filter(component -> newPVCComponentsName.contains(component.getName()))
                .forEach(component -> {
                    component.setVisible(visible);
                });
    }

    private void hidePVCAndVCTComponents(JPanel parent) {
        changeNewPVCComponentsVisibility(parent, false);
    }

    private void addListeners(String workspace, JPanel parent, int row) {
        JComboBox cmbWorkspaceTypes = (JComboBox) Arrays.stream(parent.getComponents()).filter(component -> component.getName() != null && component.getName().equals("cmbWorkspaceTypes")).findFirst().get();
        JComboBox cmbWorkspaceTypeValues = (JComboBox) Arrays.stream(parent.getComponents()).filter(component -> component.getName() != null && component.getName().equals("cmbWorkspaceTypeValues")).findFirst().get();
        cmbWorkspaceTypes.addItemListener(itemEvent -> {
            if (itemEvent.getStateChange() == 1) {
                // when cmbWorkspaceTypes combo box value changes, a type (secret, emptyDir, pvcs ..) is chosen and cmbWorkspaceTypeValues combo box is filled with all existing resources of that kind
                Workspace.Kind kindSelected = cmbWorkspaceTypes.getSelectedItem().equals("") ? null : (Workspace.Kind) cmbWorkspaceTypes.getSelectedItem();
                setCmbWorkspaceTypeValues(workspace, kindSelected, cmbWorkspaceTypeValues, row);
                String resource = cmbWorkspaceTypeValues.isVisible() && cmbWorkspaceTypeValues.getItemCount() > 0 ? cmbWorkspaceTypeValues.getSelectedItem().toString() : null;
                updateWorkspaceModel(workspace, kindSelected, resource);
                // reset error graphics if error occurred earlier
                if (isValid(cmbWorkspaceTypes)) {
                    cmbWorkspaceTypes.setBorder(cmbWorkspaceTypeValues.getBorder());
                    if (errorFieldsByRow.containsKey(row)) {
                        deleteComponent(errorFieldsByRow.get(row));
                    }
                }
                fireStateChanged();
            }
        });

        cmbWorkspaceTypeValues.addItemListener(itemEvent -> {
            if (itemEvent.getStateChange() == 1) {
                // when wsCB combo box value changes, wsTypesCB combo box is filled with all possible options
                String itemSelected = itemEvent.getItem().toString();
                hidePVCAndVCTComponents(parent);
                if (itemSelected.equals("Create new VolumeClaimTemplate")) {
                    changeNewVCTComponentsVisibility(parent, true);
                } else if (itemSelected.equals("Create new PVC")) {
                    changeNewPVCComponentsVisibility(parent, true);
                }
                updateWorkspaceModel(workspace, (Workspace.Kind) cmbWorkspaceTypes.getSelectedItem(), itemEvent.getItem().toString());
                fireStateChanged();
            }
        });
    }

    private void setCmbWorkspaceTypeValues(String workspaceName, Workspace.Kind kind, JComboBox cmbWorkspaceTypeValues, int row) {
        cmbWorkspaceTypeValues.removeAllItems();
        if (kind == null || kind == EMPTYDIR) {
            cmbWorkspaceTypeValues.setVisible(false);
            return;
        }

        List<String> items = getResources(kind);

        if (items.isEmpty() && kind != PVC) {
            // show message no resource exists for this type
            JLabel lblErrorText = new JLabel("There are no resources for this type in the cluster. Please select a different type.");
            lblErrorText.setForeground(Color.red);
            addComponent(lblErrorText, TIMES_PLAIN_10, MARGIN_TOP_35, ROW_DIMENSION_ERROR, 0, row, GridBagConstraints.PAGE_END);
            errorFieldsByRow.put(row, lblErrorText);
            cmbWorkspaceTypeValues.setVisible(false);
            return;
        }

        cmbWorkspaceTypeValues.setVisible(true);
        for (String item: items) {
            cmbWorkspaceTypeValues.addItem(item);
        }
        if (kind == PVC) {
            cmbWorkspaceTypeValues.insertItemAt("Create new PVC", 0);
            cmbWorkspaceTypeValues.insertItemAt("Create new VolumeClaimTemplate", 0);
        }

        String resource = model.getWorkspaces().get(workspaceName) == null ? null : model.getWorkspaces().get(workspaceName).getKind() == kind ? model.getWorkspaces().get(workspaceName).getResource() : null;
        if (resource != null) {
            cmbWorkspaceTypeValues.setSelectedItem(resource);
        } else {
            cmbWorkspaceTypeValues.setSelectedIndex(0);
        }
    }

    private void updateWorkspaceModel(String workspaceName, Workspace.Kind kind, String resource) {
        if (resource == null && kind != EMPTYDIR) {
            model.getWorkspaces().put(workspaceName, null);
        } else {
            Workspace workspace = new Workspace(workspaceName, kind, resource);
            model.getWorkspaces().put(workspaceName, workspace);
        }
    }

    private boolean isValid(JComboBox component) {
        if (!component.isVisible() || component.getSelectedIndex() == 0) return false;
        Workspace.Kind kind = (Workspace.Kind) component.getSelectedItem();
        List<String> resourcesByKind = getResources(kind);
        if (kind != EMPTYDIR && resourcesByKind.size() == 0) return false;
        return true;
    }

    private List<String> getResources(Workspace.Kind kind) {
        List<String> items = new ArrayList<>();
        if (kind == CONFIGMAP) {
            items = model.getConfigMaps();
        } else if(kind == SECRET) {
            items = model.getSecrets();
        } else if ( kind == PVC) {
            items = model.getPersistentVolumeClaims();
        }
        return items;
    }
}
