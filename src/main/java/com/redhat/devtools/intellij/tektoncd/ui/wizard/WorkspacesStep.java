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
import com.redhat.devtools.intellij.tektoncd.utils.StartResourceModel;
import java.awt.GridBagConstraints;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

import static com.redhat.devtools.intellij.tektoncd.tkn.component.field.Workspace.Kind.CONFIGMAP;
import static com.redhat.devtools.intellij.tektoncd.tkn.component.field.Workspace.Kind.PVC;
import static com.redhat.devtools.intellij.tektoncd.tkn.component.field.Workspace.Kind.SECRET;
import static com.redhat.devtools.intellij.tektoncd.ui.UIConstants.BORDER_COMPONENT_VALUE;
import static com.redhat.devtools.intellij.tektoncd.ui.UIConstants.BORDER_LABEL_NAME;
import static com.redhat.devtools.intellij.tektoncd.ui.UIConstants.FONT_COMPONENT_VALUE;
import static com.redhat.devtools.intellij.tektoncd.ui.UIConstants.RED_BORDER_SHOW_ERROR;
import static com.redhat.devtools.intellij.tektoncd.ui.UIConstants.ROW_DIMENSION;

public class WorkspacesStep extends BaseStep {

    List<JComboBox> cmbsWorkspaceTypes;

    public WorkspacesStep(StartResourceModel model) {
        super("Workspaces", model);
    }

    @Override
    public boolean isComplete() {
        boolean isComplete = model.getWorkspaces().values().stream().allMatch(workspace -> workspace != null);
        if (!isComplete) {
            cmbsWorkspaceTypes.stream().forEach(cmb -> {
                if (cmb.isVisible() && cmb.getSelectedIndex() == 0) {
                    cmb.setBorder(RED_BORDER_SHOW_ERROR);
                }
            });
        }
        return isComplete;
    }

    @Override
    public String getHelpId() {
        return "https://github.com/tektoncd/pipeline/blob/master/docs/workspaces.md";
    }

    public void setContent(StartResourceModel model) {
        cmbsWorkspaceTypes = new ArrayList<>();
        final int[] row = {0};

        model.getWorkspaces().keySet().forEach(workspaceName -> {
            JLabel lblNameWorkspace = new JLabel("<html><span style=\\\"font-family:serif;font-size:10px;font-weight:bold;\\\">" + workspaceName + "</span></html");
            addComponent(lblNameWorkspace,  null, BORDER_LABEL_NAME, ROW_DIMENSION, 0, row[0], GridBagConstraints.NORTH);
            row[0] += 1;

            JComboBox cmbWorkspaceTypes = new JComboBox();
            Border compoundBorderBottomMargin = BorderFactory.createCompoundBorder(new EmptyBorder(0, 0, 3, 0), BORDER_COMPONENT_VALUE);
            cmbWorkspaceTypes = (JComboBox) addComponent(cmbWorkspaceTypes, FONT_COMPONENT_VALUE, compoundBorderBottomMargin, ROW_DIMENSION, 0, row[0], GridBagConstraints.NORTH);

            cmbWorkspaceTypes.addItem("");
            cmbWorkspaceTypes.addItem(Workspace.Kind.EMPTYDIR);
            cmbWorkspaceTypes.addItem(CONFIGMAP);
            cmbWorkspaceTypes.addItem(Workspace.Kind.SECRET);
            cmbWorkspaceTypes.addItem(Workspace.Kind.PVC);
            Workspace.Kind typeToBeSelected = workspaceName == null ? null : model.getWorkspaces().get(workspaceName) == null ? null : model.getWorkspaces().get(workspaceName).getKind();
            if (typeToBeSelected != null) {
                cmbWorkspaceTypes.setSelectedItem(typeToBeSelected);
            } else {
                cmbWorkspaceTypes.setSelectedItem("");
            }
            cmbsWorkspaceTypes.add(cmbWorkspaceTypes);
            row[0] += 1;

            JComboBox cmbWorkspaceTypeValues = new JComboBox();
            Border compoundBorderTopMargin = BorderFactory.createCompoundBorder(new EmptyBorder(3, 0, 0, 0), BORDER_COMPONENT_VALUE);
            cmbWorkspaceTypeValues = (JComboBox) addComponent(cmbWorkspaceTypeValues, FONT_COMPONENT_VALUE, compoundBorderTopMargin, ROW_DIMENSION, 0, row[0], GridBagConstraints.NORTH);
            setCmbWorkspaceTypeValues(workspaceName, typeToBeSelected, cmbWorkspaceTypeValues);
            addListeners(workspaceName, cmbWorkspaceTypes, cmbWorkspaceTypeValues);
            row[0] += 1;
        });
    }

    private void addListeners(String workspace, JComboBox cmbWorkspaceTypes, JComboBox cmbWorkspaceTypeValues) {
        cmbWorkspaceTypes.addItemListener(itemEvent -> {
            if (itemEvent.getStateChange() == 1) {
                // when cmbWorkspaceTypes combo box value changes, a type (secret, emptyDir, pvcs ..) is chosen and cmbWorkspaceTypeValues combo box is filled with all existing resources of that kind
                Workspace.Kind kindSelected = cmbWorkspaceTypes.getSelectedItem().equals("") ? null : (Workspace.Kind) cmbWorkspaceTypes.getSelectedItem();
                setCmbWorkspaceTypeValues(workspace, kindSelected, cmbWorkspaceTypeValues);
                String resource = cmbWorkspaceTypeValues.isVisible() && cmbWorkspaceTypeValues.getItemCount() > 0 ? cmbWorkspaceTypeValues.getSelectedItem().toString() : null;
                updateWorkspaceModel(workspace, kindSelected, resource);
                // reset border if an error occured previously and the border is red
                cmbWorkspaceTypes.setBorder(BORDER_COMPONENT_VALUE);
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

    private void setCmbWorkspaceTypeValues(String workspaceName, Workspace.Kind kind, JComboBox cmbWorkspaceTypeValues) {
        cmbWorkspaceTypeValues.removeAllItems();
        if (kind == null) {
            cmbWorkspaceTypeValues.setVisible(false);
            return;
        }

        List<String> items = new ArrayList<>();
        if (kind == CONFIGMAP) {
            items = model.getConfigMaps();
        } else if(kind == SECRET) {
            items = model.getSecrets();
        } else if ( kind == PVC) {
            items = model.getPersistenceVolumeClaims();
        }

        if (items.isEmpty()) {
            // show message no resource exists for this type
            //noResFoundLbl.setVisible(true);
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
        if (resource == null && kind != Workspace.Kind.EMPTYDIR) {
            model.getWorkspaces().put(workspaceName, null);
        } else {
            Workspace workspace = new Workspace(workspaceName, kind, resource);
            model.getWorkspaces().put(workspaceName, workspace);
        }
    }
}
