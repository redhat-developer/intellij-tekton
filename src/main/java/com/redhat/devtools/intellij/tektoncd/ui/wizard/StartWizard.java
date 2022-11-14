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

import com.intellij.icons.AllIcons;
import com.intellij.openapi.editor.colors.ColorKey;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.redhat.devtools.intellij.tektoncd.tkn.Tkn;
import com.redhat.devtools.intellij.tektoncd.tree.ParentableNode;
import com.redhat.devtools.intellij.tektoncd.utils.model.actions.ActionToRunModel;
import com.redhat.devtools.intellij.tektoncd.utils.model.actions.StartResourceModel;
import org.jetbrains.annotations.Nullable;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_CLUSTERTASK;
import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_PIPELINE;
import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_TASK;
import static com.redhat.devtools.intellij.tektoncd.ui.UIConstants.MARGIN_10;
import static com.redhat.devtools.intellij.tektoncd.ui.UIConstants.ROW_DIMENSION;
import static com.redhat.devtools.intellij.tektoncd.ui.UIConstants.TIMES_PLAIN_14;

public class StartWizard extends BaseWizard {

    private JPanel optionsPanel;
    private JTextField txtRunPrefixName;

    // options name
    private static final String PREFIX_NAME_RUN = "prefix_name_for_run";
    private static final String IMPORT_DATA_FROM_RUN = "import_data_from_run";

    public StartWizard(String title, ParentableNode element, @Nullable Project project, StartResourceModel model) {
        super(title, project, model);
        init();
        buildOptionsPanel(element);
    }

    private void buildOptionsPanel(ParentableNode element) {
        // if wizard requires an option panel
        List<String> optionsToDisplay = getOptionsToDisplay((StartResourceModel) model);
        if (!optionsToDisplay.isEmpty()) {
            JPanel innerOptionsPanel = getOptionsPanel(optionsToDisplay, (StartResourceModel) model, element);

            optionsPanel = new JPanel();
            optionsPanel.setBackground(backgroundTheme);
            optionsPanel.add(innerOptionsPanel);
            optionsPanel.setVisible(false);

            JLabel openOptionsLabel = new JLabel("Advanced Options");
            openOptionsLabel.setIcon(AllIcons.Actions.MoveDown);
            openOptionsLabel.addMouseListener(
                    new MouseAdapter() {
                        @Override
                        public void mouseClicked(MouseEvent e) {
                            if (optionsPanel.isVisible()) {
                                openOptionsLabel.setIcon(AllIcons.Actions.MoveDown);
                                optionsPanel.setVisible(false);
                            } else {
                                openOptionsLabel.setIcon(AllIcons.Actions.MoveUp);
                                optionsPanel.setVisible(true);
                            }
                        }
                    }
            );

            JPanel openOptionsPanel = new JPanel(new GridBagLayout());
            openOptionsPanel.setBackground(backgroundTheme);
            Border lineSeparatorBelow = new MatteBorder(0, 0, 1, 0, EditorColorsManager.getInstance().getGlobalScheme().getColor(ColorKey.find("SEPARATOR_BELOW_COLOR")));
            Border margin_5 = new EmptyBorder(5, 0, 5, 0);
            Border compoundBorderMargin = BorderFactory.createCompoundBorder(lineSeparatorBelow, margin_5);
            openOptionsPanel.setBorder(compoundBorderMargin);
            openOptionsPanel.add(openOptionsLabel);

            myHeaderPanel.setBackground(backgroundTheme);
            myHeaderPanel.add(optionsPanel, BorderLayout.LINE_START);
            myHeaderPanel.add(openOptionsPanel, BorderLayout.PAGE_END);
        }
    }

    private JPanel getOptionsPanel(List<String> optionsToDisplay, ActionToRunModel model, ParentableNode element) {
        JPanel innerOptionsPanel = new JPanel(new GridBagLayout());
        innerOptionsPanel.setBackground(backgroundTheme);
        innerOptionsPanel.setBorder(MARGIN_10);
        int row = 0;
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.anchor = GridBagConstraints.WEST;

        // set prefix for runs
        if (optionsToDisplay.contains(PREFIX_NAME_RUN)) {
            JLabel lblRunPrefixName = new JLabel("Prefix for the *Run name: ");
            lblRunPrefixName.setFont(TIMES_PLAIN_14);
            JLabel lblRunPrefixName_Help = new JLabel();
            lblRunPrefixName_Help.setIcon(AllIcons.General.Information);
            lblRunPrefixName_Help.setToolTipText("Specify a prefix for the *Run name (must be lowercase alphanumeric characters)");
            txtRunPrefixName = new JTextField();
            txtRunPrefixName.setPreferredSize(ROW_DIMENSION);

            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = row;
            innerOptionsPanel.add(lblRunPrefixName, gridBagConstraints);
            gridBagConstraints.gridx = 1;
            innerOptionsPanel.add(txtRunPrefixName, gridBagConstraints);
            gridBagConstraints.gridx = 2;
            innerOptionsPanel.add(lblRunPrefixName_Help, gridBagConstraints);
            row++;
        }

        // import data from *run
        if (optionsToDisplay.contains(IMPORT_DATA_FROM_RUN)) {
            JCheckBox chkImportRunData = new JCheckBox("Import data from run");
            chkImportRunData.setBackground(backgroundTheme);
            JLabel chkImportRunData_Help = new JLabel();
            chkImportRunData_Help.setIcon(AllIcons.General.Information);
            chkImportRunData_Help.setToolTipText("Fill all wizard inputs with the values taken from an old *run");
            JComboBox cmbPickRunToImportData = new ComboBox();
            cmbPickRunToImportData.setEnabled(false);
            cmbPickRunToImportData.setPreferredSize(ROW_DIMENSION);

            chkImportRunData.addItemListener(itemEvent -> {
                if (chkImportRunData.isSelected()) {
                    cmbPickRunToImportData.setEnabled(true);
                } else {
                    cmbPickRunToImportData.setEnabled(false);
                }
            });
            cmbPickRunToImportData.addItem("Please choose");
            ((StartResourceModel)model).getRuns().forEach(run -> cmbPickRunToImportData.addItem(run.getMetadata().getName()));

            cmbPickRunToImportData.addItemListener(itemEvent -> {
                // when combo box value change
                if (itemEvent.getStateChange() == 1) {
                    if (itemEvent.getItem().toString().equals("Please choose")) return;
                    Tkn tkncli = element.getRoot().getTkn();
                    String configuration = "";
                    String kind = model.getKind();
                    try {
                        if (kind.equalsIgnoreCase(KIND_PIPELINE)) {
                            configuration = tkncli.getPipelineRunYAML(element.getNamespace(), itemEvent.getItem().toString());
                        } else if (kind.equalsIgnoreCase(KIND_TASK) || kind.equalsIgnoreCase(KIND_CLUSTERTASK)) {
                            configuration = tkncli.getTaskRunYAML(element.getNamespace(), itemEvent.getItem().toString());
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (!configuration.isEmpty()) {
                        ((StartResourceModel)model).adaptsToRun(configuration);
                        refreshSteps();
                        updatePreview(model);
                    }
                }
            });

            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = row;
            innerOptionsPanel.add(chkImportRunData, gridBagConstraints);
            gridBagConstraints.gridx = 1;
            innerOptionsPanel.add(cmbPickRunToImportData, gridBagConstraints);
            gridBagConstraints.gridx = 2;
            innerOptionsPanel.add(chkImportRunData_Help, gridBagConstraints);
            row++;
        }

        return innerOptionsPanel;
    }

    private List<String> getOptionsToDisplay(StartResourceModel model) {
        List<String> optionsEnabled = new ArrayList<>();

        optionsEnabled.add(PREFIX_NAME_RUN);

        if (!model.getRuns().isEmpty()) {
            optionsEnabled.add(IMPORT_DATA_FROM_RUN);
        }
        return optionsEnabled;
    }

    public List<BaseStep> getSteps() {
        List<BaseStep> steps = new ArrayList<>();
        boolean hasParams = !model.getParams().isEmpty();
        boolean hasWorkspaces = !model.getWorkspaces().isEmpty();
        if (hasParams) {
            steps.add(buildStepWithListener(new ParametersStep(model)));
        }

        if (hasWorkspaces) {
            steps.add(buildStepWithListener(new WorkspacesStep(model)));
        }

        steps.add(buildStepWithListener(new AuthenticationStep(model)));

        return steps;
    }

    @Override
    public void doBeforeNextStep(int currentStep) { }

    public String getLastStepButtonText() {
        return "&Start";
    }

    public void calculateArgs() {
        // options panel
        String runPrefixName = txtRunPrefixName != null ? txtRunPrefixName.getText() : "";
        if (!runPrefixName.trim().isEmpty()) {
            ((StartResourceModel)model).setRunPrefixName(runPrefixName);
        }
    }
}
