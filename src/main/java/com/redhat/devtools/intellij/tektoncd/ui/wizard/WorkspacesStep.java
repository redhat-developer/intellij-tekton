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

import com.google.common.base.Strings;
import com.intellij.openapi.editor.colors.ColorKey;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.util.Pair;
import com.intellij.ui.DocumentAdapter;
import com.redhat.devtools.intellij.tektoncd.tkn.component.field.Workspace;
import com.redhat.devtools.intellij.tektoncd.utils.model.actions.ActionToRunModel;
import org.jetbrains.annotations.NotNull;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import javax.swing.text.NumberFormatter;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_VCT;
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
import static com.redhat.devtools.intellij.tektoncd.ui.UIConstants.TIMES_PLAIN_12;
import static com.redhat.devtools.intellij.tektoncd.ui.UIConstants.TIMES_PLAIN_14;

public class WorkspacesStep extends BaseStep {
    private Map<String, JPanel> workspacePanelMapper;
    private JLabel lblErrorText;

    private static final String NEW_VCT_TEXT = "Create new VolumeClaimTemplate";
    private static final String NEW_PVC_TEXT = "Create new PVC";


    public WorkspacesStep(ActionToRunModel model) {
        super("Workspaces", model);
    }

    @Override
    public boolean isComplete() {
        boolean isComplete = model.getWorkspaces().values().stream().allMatch(workspace -> workspace != null);
        if (!isComplete) {
            workspacePanelMapper.entrySet().forEach(entry -> {
                JPanel panel = entry.getValue();
                JComboBox cmbWorkspaceTypes = (JComboBox) Arrays.stream(panel.getComponents())
                                                                .filter(component -> "cmbWorkspaceTypes".equals(component.getName())).findFirst().get();
                if (!isValid(cmbWorkspaceTypes)) {
                    cmbWorkspaceTypes.setBorder(RED_BORDER_SHOW_ERROR);
                    return;
                }
                Workspace.Kind kind = (Workspace.Kind) cmbWorkspaceTypes.getSelectedItem();
                if (kind == PVC) {
                    String workspaceName = entry.getKey();
                    JComboBox cmbWorkspaceTypeValues = (JComboBox) Arrays.stream(panel.getComponents())
                                                                        .filter(component -> "cmbWorkspaceTypeValues".equals(component.getName())).findFirst().get();
                    String valueSelected = cmbWorkspaceTypeValues.getSelectedItem().toString();
                    if (valueSelected.equals(NEW_PVC_TEXT)) {
                        JPanel newPVCNamePanel = (JPanel) Arrays.stream(panel.getComponents())
                                                                .filter(component -> "newPVCNamePanel".equals(component.getName())).findFirst().get();
                        JTextField newPVCNameTextField = (JTextField) Arrays.stream(newPVCNamePanel.getComponents())
                                                                        .filter(component -> "txtNameNewPVC".equals(component.getName())).findFirst().get();
                        String nameNewPVC = newPVCNameTextField.getText();
                        newPVCNameTextField.setBorder(nameNewPVC.isEmpty() ? RED_BORDER_SHOW_ERROR : NO_BORDER);

                        saveNewVolume(workspaceName, nameNewPVC, PVC, panel);
                    } else if (valueSelected.equals(NEW_VCT_TEXT)) {
                        saveNewVolume(workspaceName, workspaceName + "-vct", PVC, panel, true);
                    }
                }

            });
        }
        isComplete = model.getWorkspaces().values().stream().allMatch(workspace -> workspace != null);
        changeErrorTextVisibility(!isComplete);
        return isComplete;
    }

    private void saveNewVolume(String workspaceName, String name, Workspace.Kind kind, JPanel panel) {
        saveNewVolume(workspaceName, name, kind, panel, false);
    }

    private void saveNewVolume(String workspaceName, String name, Workspace.Kind kind, JPanel panel, boolean isVCT) {
        JPanel accessModePanel = (JPanel) Arrays.stream(panel.getComponents())
                                                .filter(component -> "accessModePanel".equals(component.getName())).findFirst().get();
        JComboBox accessModeComboBox = (JComboBox) Arrays.stream(accessModePanel.getComponents())
                                                        .filter(component -> "cmbAccessMode".equals(component.getName())).findFirst().get();
        JPanel sizePanel = (JPanel) Arrays.stream(panel.getComponents())
                                        .filter(component -> "sizePanel".equals(component.getName())).findFirst().get();
        JSpinner sizeSpinner = (JSpinner) Arrays.stream(sizePanel.getComponents()).filter(component -> "txtSize".equals(component.getName())).findFirst().get();
        JComboBox sizeUnitComboBox = (JComboBox) Arrays.stream(sizePanel.getComponents()).filter(component -> "cmbSizeMeasureUnit".equals(component.getName())).findFirst().get();

        boolean isNewItemFormValid = !name.isEmpty();
        String size = ((JSpinner.NumberEditor)sizeSpinner.getEditor()).getTextField().getText();
        if (size.isEmpty() || size.equals("0")) {
            sizeSpinner.setBorder(RED_BORDER_SHOW_ERROR);
            isNewItemFormValid = false;
        } else {
            sizeSpinner.setBorder(NO_BORDER);
        }
        if (!isNewItemFormValid) {
            return;
        }

        Map<String, String> values = new HashMap<>();
        values.put("name", name);
        if (isVCT) {
            values.put("type", KIND_VCT);
        }
        values.put("accessMode", ((Pair)accessModeComboBox.getSelectedItem()).getSecond().toString());
        values.put("size", size);
        values.put("unit", ((Pair)sizeUnitComboBox.getSelectedItem()).getSecond().toString());
        Workspace workspace = new Workspace(workspaceName, kind, "", values);
        model.getWorkspaces().put(workspaceName, workspace);
    }

    @Override
    public String getHelpId() {
        return "https://github.com/tektoncd/pipeline/blob/master/docs/workspaces.md";
    }

    public void setContent() {
        workspacePanelMapper = new HashMap<>();
        final int[] row = {1};

        lblErrorText = new JLabel("Please fill all fields to proceed.");
        lblErrorText.setForeground(Color.red);
        addComponent(lblErrorText, TIMES_PLAIN_12, BORDER_LABEL_NAME, ROW_DIMENSION_ERROR, 0, 0, GridBagConstraints.PAGE_START);
        changeErrorTextVisibility(false);

        model.getWorkspaces().entrySet().forEach(ws -> {
            String workspaceName = ws.getKey();
            Workspace workspace = ws.getValue();
            int innerPanelRow = 0;
            GridBagConstraints workspacePanelConstraint = new GridBagConstraints();
            JPanel panelWrapperWorkspace = new JPanel(new GridBagLayout());
            panelWrapperWorkspace.setBackground(backgroundTheme);

            JLabel lblWorkspaceName = createLabel(workspaceName, 11, "", null);
            addComponent(lblWorkspaceName, panelWrapperWorkspace, workspacePanelConstraint,  null, getBottomBorder(), ROW_DIMENSION, 0, innerPanelRow, GridBagConstraints.NORTH);
            innerPanelRow += 1;

            JComboBox cmbWorkspaceTypes = createCustomCombo("cmbWorkspaceTypes",
                   null,
                   "",
                    EMPTYDIR,
                    CONFIGMAP,
                    SECRET,
                    PVC);
            cmbWorkspaceTypes = (JComboBox) addComponent(cmbWorkspaceTypes, panelWrapperWorkspace, workspacePanelConstraint, TIMES_PLAIN_14, null, ROW_DIMENSION, 0, innerPanelRow, GridBagConstraints.NORTH);

            Workspace.Kind typeToBeSelected = workspace == null ? null : workspace.getKind();
            cmbWorkspaceTypes.setSelectedItem(typeToBeSelected != null ? typeToBeSelected : "");
            innerPanelRow += 1;

            JComboBox cmbWorkspaceTypeValues = createCustomCombo("cmbWorkspaceTypeValues", null);
            cmbWorkspaceTypeValues = (JComboBox) addComponent(cmbWorkspaceTypeValues, panelWrapperWorkspace, workspacePanelConstraint, TIMES_PLAIN_14, null, ROW_DIMENSION, 0, innerPanelRow, GridBagConstraints.NORTH);
            setCmbWorkspaceTypeValues(workspaceName, typeToBeSelected, cmbWorkspaceTypeValues, innerPanelRow - 1);
            addListeners(workspaceName, panelWrapperWorkspace, innerPanelRow - 1);
            innerPanelRow += 1;

            JLabel lblNewPVCName = createLabel("Name:", 9, "lblNameNewPVC", null);

            JTextField txtNewPVCName = new JTextField("");
            txtNewPVCName.setName("txtNameNewPVC");
            txtNewPVCName.getDocument().addDocumentListener(new DocumentAdapter() {
                @Override
                protected void textChanged(@NotNull DocumentEvent e) {
                    updateFormByTextFieldContent(workspaceName, txtNewPVCName, txtNewPVCName);
                }
            });
            JPanel newPVCNamePanel = createCompoundComponentAsPanel("newPVCNamePanel", lblNewPVCName, txtNewPVCName, null);
            addComponent(newPVCNamePanel, panelWrapperWorkspace, workspacePanelConstraint,null, NO_BORDER, ROW_DIMENSION, 0, innerPanelRow, GridBagConstraints.NORTH);
            newPVCNamePanel.setVisible(false);
            innerPanelRow += 1;

            JLabel lblNewVolumeAccessMode = createLabel("Mode:", 9, "lblAccessMode", null);

            JComboBox cmbNewVolumeAccessMode = createCustomCombo("cmbAccessMode",
                    getBasicComboBoxRenderer(),
                    Pair.create("Single User (RWO)", "ReadWriteOnce"),
                    Pair.create("Shared Access (RWX)", "ReadWriteMany"),
                    Pair.create("Read Only (ROX)", "ReadOnlyMany"));
            JPanel panelNewVolumeAccessMode = createCompoundComponentAsPanel("accessModePanel", lblNewVolumeAccessMode, cmbNewVolumeAccessMode, null);
            setDefaultValueInCmbWithPairs(workspace, cmbNewVolumeAccessMode, "accessMode");
            addComponent(panelNewVolumeAccessMode, panelWrapperWorkspace, workspacePanelConstraint,null, NO_BORDER, ROW_DIMENSION, 0, innerPanelRow, GridBagConstraints.NORTH);
            innerPanelRow += 1;

            JLabel lblNewVolumeSize = createLabel("Size:", 9, "lblSize", lblNewPVCName.getPreferredSize());

            String sizeDefaultValue = getVCTItemValue(workspace, "size");
            int sizeDefaultValueAsInteger = sizeDefaultValue.isEmpty() ?  0 : Integer.parseInt(sizeDefaultValue);
            JSpinner spinnerNewVolumeSize = new JSpinner(new SpinnerNumberModel(sizeDefaultValueAsInteger,0, Integer.MAX_VALUE,1));
            spinnerNewVolumeSize.setName("txtSize");
            spinnerNewVolumeSize.setEditor(new JSpinner.NumberEditor(spinnerNewVolumeSize, "#"));
            JTextField spinnerTextFieldNewVolumeSize = ((JSpinner.NumberEditor)spinnerNewVolumeSize.getEditor()).getTextField();
            spinnerTextFieldNewVolumeSize.addPropertyChangeListener(evt -> {
                if (evt.getPropertyName().equals("value")) {
                    updateFormByTextFieldContent(workspaceName, spinnerTextFieldNewVolumeSize, spinnerNewVolumeSize);
                }
            });
            ((NumberFormatter)((JFormattedTextField) spinnerTextFieldNewVolumeSize).getFormatter()).setAllowsInvalid(false);

            JComboBox cmbNewVolumeSizeMeasureUnit = createCustomCombo("cmbSizeMeasureUnit",
                    getBasicComboBoxRenderer(),
                    Pair.create("MB", "Mi"),
                    Pair.create("GB", "Gi"),
                    Pair.create("TB", "Ti"));
            setDefaultValueInCmbWithPairs(workspace, cmbNewVolumeSizeMeasureUnit, "unit");
            JPanel panelNewVolumeSize = createCompoundComponentAsPanel("sizePanel", lblNewVolumeSize, spinnerNewVolumeSize, cmbNewVolumeSizeMeasureUnit);
            addComponent(panelNewVolumeSize, panelWrapperWorkspace, workspacePanelConstraint,null, NO_BORDER, ROW_DIMENSION, 0, innerPanelRow, GridBagConstraints.NORTH);


            addComponent(panelWrapperWorkspace, null, NO_BORDER, null, 0, row[0], GridBagConstraints.NORTH);
            if (!isVCT(workspace)) {
                hidePVCAndVCTComponents(panelWrapperWorkspace);
            }
            workspacePanelMapper.put(workspaceName, panelWrapperWorkspace);
            row[0] += 1;
        });

    }

    private JLabel createLabel(String text, int pixel, String name, Dimension size) {
        JLabel label = new JLabel("<html><span style=\\\"font-family:serif;font-size:" + pixel + "px;font-weight:bold;\\\">" + text + "</span>   </html");
        if (!name.isEmpty()) {
            label.setName(name);
        }
        if (size != null) {
            label.setPreferredSize(size);
        }
        return label;
    }

    private JComboBox createCustomCombo(String name, ListCellRenderer cellRenderer, Object ...items) {
        JComboBox comboBox = new ComboBox();
        if (!name.isEmpty()) {
            comboBox.setName(name);
        }
        if (cellRenderer != null) {
            comboBox.setRenderer(cellRenderer);
        }
        if (items.length > 0) {
            Arrays.stream(items).forEach(item -> comboBox.addItem(item));
        }
        return comboBox;
    }

    private void updateFormByTextFieldContent(String workspaceName, JTextField textField, JComponent wrapper) {
        wrapper.setBorder(NO_BORDER);
        changeErrorTextVisibility(false);
        if (textField.getText().equals("0")) {
            updateWorkspaceModel(workspaceName, PVC, "");
        }
    }

    private ListCellRenderer getBasicComboBoxRenderer() {
        return new BasicComboBoxRenderer()
        {
            public Component getListCellRendererComponent(
                    JList list, Object value, int index, boolean isSelected, boolean cellHasFocus)
            {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Pair)
                {
                    Pair pair = (Pair)value;
                    setText(pair.getFirst().toString());
                }
                return this;
            }
        };
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
        changeComponentsVisibility(parent, Arrays.asList("newPVCNamePanel", "accessModePanel", "sizePanel"), visible);
    }

    private void changeNewVCTComponentsVisibility(JPanel parent, boolean visible) {
        changeComponentsVisibility(parent, Arrays.asList("accessModePanel", "sizePanel"), visible);

    }

    private void changeComponentsVisibility(JPanel parent, List<String> components, boolean visible) {
        Arrays.stream(parent.getComponents())
                .filter(component -> components.contains(component.getName()))
                .forEach(component -> {
                    component.setVisible(visible);
                });
    }

    private void changeErrorTextVisibility(boolean visible) {
        lblErrorText.setVisible(visible);
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
                String resource = cmbWorkspaceTypeValues.isVisible() && cmbWorkspaceTypeValues.getItemCount() > 0 ? cmbWorkspaceTypeValues.getSelectedItem().toString() : "";
                updateWorkspaceModel(workspace, kindSelected, resource);
                // reset error graphics if error occurred earlier
                if (isValid(cmbWorkspaceTypes)) {
                    changeErrorTextVisibility(false);
                    cmbWorkspaceTypes.setBorder(new JComboBox().getBorder());
                }
                fireStateChanged();
            }
        });

        cmbWorkspaceTypeValues.addItemListener(itemEvent -> {
            if (itemEvent.getStateChange() == 1) {
                changeErrorTextVisibility(false);
                // when wsCB combo box value changes, wsTypesCB combo box is filled with all possible options
                String itemSelected = itemEvent.getItem().toString();
                hidePVCAndVCTComponents(parent);
                if (itemSelected.equals(NEW_VCT_TEXT)) {
                    changeNewVCTComponentsVisibility(parent, true);
                    updateWorkspaceModel(workspace, PVC, "");
                } else if (itemSelected.equals(NEW_PVC_TEXT)) {
                    changeNewPVCComponentsVisibility(parent, true);
                    updateWorkspaceModel(workspace, PVC, "");
                } else {
                    updateWorkspaceModel(workspace, (Workspace.Kind) cmbWorkspaceTypes.getSelectedItem(), itemEvent.getItem().toString());
                }
                fireStateChanged();
            }
        });
    }

    private boolean isVCT(Workspace workspace) {
        return workspace != null
                && KIND_VCT.equals(workspace.getItems().get("type"));
    }

    private String getVCTItemValue(Workspace workspace, String key) {
        if (isVCT(workspace)) {
            return workspace.getItems().get(key);
        }
        return "";
    }

    private void setDefaultValueInCmbWithPairs(Workspace workspace, JComboBox combo, String key) {
        String value = getVCTItemValue(workspace, key);
        if (!value.isEmpty()) {
            int index = getCmbIndexFromValue(value, combo);
            combo.setSelectedIndex(index);
        }
    }

    private int getCmbIndexFromValue(String value, JComboBox combo) {
        int cont = 0;
        while (combo.getItemCount() > cont) {
            Pair pair = (Pair) combo.getItemAt(cont);
            if (pair.getSecond().equals(value)) {
                return cont;
            }
            cont++;
        }
        return -1;
    }

    private void setCmbWorkspaceTypeValues(String workspaceName, Workspace.Kind kind, JComboBox cmbWorkspaceTypeValues, int row) {
        cmbWorkspaceTypeValues.removeAllItems();
        if (kind == null || kind.equals(EMPTYDIR)) {
            cmbWorkspaceTypeValues.setVisible(false);
            return;
        }

        List<String> items = getResources(kind);

        if (items.isEmpty() && !kind.equals(PVC)) {
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
        if (kind.equals(PVC)) {
            cmbWorkspaceTypeValues.insertItemAt(NEW_PVC_TEXT, 0);
            cmbWorkspaceTypeValues.insertItemAt(NEW_VCT_TEXT, 0);
        }

        Workspace workspace = model.getWorkspaces().get(workspaceName);
        if (workspace != null) {
            String resource = workspace.getKind() == kind ? model.getWorkspaces().get(workspaceName).getResource() : "";
            if (Strings.isNullOrEmpty(resource)) {
                if (kind.equals(PVC)
                        && !workspace.getItems().isEmpty()
                        && KIND_VCT.equals(workspace.getItems().get("type"))) {
                    cmbWorkspaceTypeValues.setSelectedItem(NEW_VCT_TEXT);
                    return;
                }
            } else {
                cmbWorkspaceTypeValues.setSelectedItem(resource);
                return;
            }
        }
        cmbWorkspaceTypeValues.setSelectedIndex(0);
    }

    private void updateWorkspaceModel(String workspaceName, Workspace.Kind kind, String resource) {
        if (resource.isEmpty() && kind != EMPTYDIR) {
            model.getWorkspaces().put(workspaceName, null);
        } else {
            Workspace workspace = new Workspace(workspaceName, kind, resource);
            model.getWorkspaces().put(workspaceName, workspace);
        }
    }

    private boolean isValid(JComboBox component) {
        if (!component.isVisible() || component.getSelectedItem().toString().isEmpty()) return false;
        Workspace.Kind kind = (Workspace.Kind) component.getSelectedItem();
        if (kind == PVC || kind == EMPTYDIR) return true;
        List<String> resourcesByKind = getResources(kind);
        if (resourcesByKind.size() == 0) return false;
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
