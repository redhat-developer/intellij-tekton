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

import com.redhat.devtools.intellij.tektoncd.tkn.component.field.Workspace;
import com.redhat.devtools.intellij.tektoncd.utils.model.actions.ActionToRunModel;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.border.Border;


import static com.redhat.devtools.intellij.tektoncd.tkn.component.field.Workspace.Kind.CONFIGMAP;
import static com.redhat.devtools.intellij.tektoncd.tkn.component.field.Workspace.Kind.EMPTYDIR;
import static com.redhat.devtools.intellij.tektoncd.tkn.component.field.Workspace.Kind.PVC;
import static com.redhat.devtools.intellij.tektoncd.tkn.component.field.Workspace.Kind.SECRET;
import static com.redhat.devtools.intellij.tektoncd.ui.UIConstants.BORDER_LABEL_NAME;
import static com.redhat.devtools.intellij.tektoncd.ui.UIConstants.MARGIN_TOP_35;
import static com.redhat.devtools.intellij.tektoncd.ui.UIConstants.RED_BORDER_SHOW_ERROR;
import static com.redhat.devtools.intellij.tektoncd.ui.UIConstants.ROW_DIMENSION;
import static com.redhat.devtools.intellij.tektoncd.ui.UIConstants.ROW_DIMENSION_ERROR;
import static com.redhat.devtools.intellij.tektoncd.ui.UIConstants.TIMES_PLAIN_10;
import static com.redhat.devtools.intellij.tektoncd.ui.UIConstants.TIMES_PLAIN_14;

public class WorkspacesStep extends BaseStep {

    List<JComboBox> cmbsWorkspaceTypes;

    public WorkspacesStep(ActionToRunModel model) {
        super("Workspaces", model);
    }

    @Override
    public boolean isComplete() {
        boolean isComplete = model.getWorkspaces().values().stream().allMatch(workspace -> workspace != null);
        if (!isComplete) {
            final int[] row = {1};
            cmbsWorkspaceTypes.stream().forEach(cmb -> {
                if (!isValid(cmb)) {
                    cmb.setBorder(RED_BORDER_SHOW_ERROR);
                    JLabel lblErrorText = new JLabel("Please select a value.");
                    lblErrorText.setForeground(Color.red);
                    addComponent(lblErrorText, TIMES_PLAIN_10, MARGIN_TOP_35, ROW_DIMENSION_ERROR, 0, row[0], GridBagConstraints.PAGE_END);
                    errorFieldsByRow.put(row[0], lblErrorText);
                    lblErrorText.setEnabled(true);
                }
                row[0] += 3;
            });
        }
        return isComplete;
    }

    @Override
    public String getHelpId() {
        return "https://github.com/tektoncd/pipeline/blob/master/docs/workspaces.md";
    }

    public void setContent() {
        cmbsWorkspaceTypes = new ArrayList<>();
        final int[] row = {0};

        model.getWorkspaces().keySet().forEach(workspaceName -> {
            JLabel lblNameWorkspace = new JLabel("<html><span style=\\\"font-family:serif;font-size:10px;font-weight:bold;\\\">" + workspaceName + "</span></html");
            addComponent(lblNameWorkspace,  null, BORDER_LABEL_NAME, ROW_DIMENSION, 0, row[0], GridBagConstraints.NORTH);
            row[0] += 1;

            JComboBox cmbWorkspaceTypes = new JComboBox();
            cmbWorkspaceTypes = (JComboBox) addComponent(cmbWorkspaceTypes, TIMES_PLAIN_14, null, ROW_DIMENSION, 0, row[0], GridBagConstraints.NORTH);

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
            cmbsWorkspaceTypes.add(cmbWorkspaceTypes);
            row[0] += 1;

            JComboBox cmbWorkspaceTypeValues = new JComboBox();
            cmbWorkspaceTypeValues = (JComboBox) addComponent(cmbWorkspaceTypeValues, TIMES_PLAIN_14, null, ROW_DIMENSION, 0, row[0], GridBagConstraints.NORTH);
            setCmbWorkspaceTypeValues(workspaceName, typeToBeSelected, cmbWorkspaceTypeValues, row[0] - 1);
            addListeners(workspaceName, cmbWorkspaceTypes, cmbWorkspaceTypeValues, cmbWorkspaceTypeValues.getBorder(), row[0] - 1);
            row[0] += 1;
        });
    }

    private void addListeners(String workspace, JComboBox cmbWorkspaceTypes, JComboBox cmbWorkspaceTypeValues, Border defaultBorder, int row) {
        cmbWorkspaceTypes.addItemListener(itemEvent -> {
            if (itemEvent.getStateChange() == 1) {
                // when cmbWorkspaceTypes combo box ssvalue changes, a type (secret, emptyDir, pvcs ..) is chosen and cmbWorkspaceTypeValues combo box is filled with all existing resources of that kind
                Workspace.Kind kindSelected = cmbWorkspaceTypes.getSelectedItem().equals("") ? null : (Workspace.Kind) cmbWorkspaceTypes.getSelectedItem();
                setCmbWorkspaceTypeValues(workspace, kindSelected, cmbWorkspaceTypeValues, row);
                String resource = cmbWorkspaceTypeValues.isVisible() && cmbWorkspaceTypeValues.getItemCount() > 0 ? cmbWorkspaceTypeValues.getSelectedItem().toString() : null;
                updateWorkspaceModel(workspace, kindSelected, resource);
                // reset error graphics if error occurred earlier
                if (isValid(cmbWorkspaceTypes)) {
                    cmbWorkspaceTypes.setBorder(defaultBorder);
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

        if (items.isEmpty()) {
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
