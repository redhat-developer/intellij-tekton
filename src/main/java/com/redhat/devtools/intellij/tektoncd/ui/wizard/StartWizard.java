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

import com.intellij.CommonBundle;
import com.intellij.icons.AllIcons;
import com.intellij.ide.BrowserUtil;
import com.intellij.ide.IdeBundle;
import com.intellij.ide.wizard.CommitStepException;
import com.intellij.ide.wizard.Step;
import com.intellij.openapi.application.ApplicationInfo;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.colors.ColorKey;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.ui.JBCardLayout;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.mac.TouchbarDataKeys;
import com.intellij.util.ui.UIUtil;
import com.intellij.util.ui.update.UiNotifyConnector;
import com.redhat.devtools.intellij.tektoncd.tkn.Tkn;
import com.redhat.devtools.intellij.tektoncd.tkn.component.field.Input;
import com.redhat.devtools.intellij.tektoncd.tkn.component.field.Output;
import com.redhat.devtools.intellij.tektoncd.tree.ParentableNode;
import com.redhat.devtools.intellij.tektoncd.utils.model.actions.StartResourceModel;
import com.redhat.devtools.intellij.tektoncd.utils.YAMLBuilder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import org.jetbrains.annotations.Nullable;


import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_CLUSTERTASK;
import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_PIPELINE;
import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_TASK;
import static com.redhat.devtools.intellij.tektoncd.ui.UIConstants.BLUE;
import static com.redhat.devtools.intellij.tektoncd.ui.UIConstants.MARGIN_10;
import static com.redhat.devtools.intellij.tektoncd.ui.UIConstants.NO_BORDER;
import static com.redhat.devtools.intellij.tektoncd.ui.UIConstants.ROW_DIMENSION;
import static com.redhat.devtools.intellij.tektoncd.ui.UIConstants.TIMES_PLAIN_14;

public class StartWizard extends DialogWrapper {

    private String myTitle;
    private final List<BaseStep> mySteps;
    private int myCurrentStep;
    private JButton myPreviousButton;
    private JButton myNextButton;
    private JButton myCancelButton;
    private JButton myHelpButton;
    protected JPanel myHeaderPanel;
    protected JPanel myContentPanel;
    protected JPanel myLeftPanel;
    protected JPanel myRightPanel;
    protected JPanel myFooterPanel;
    private List<JLabel> navigationList;
    private JPanel optionsPanel;
    private JPanel navigationPanel;
    private JPanel previewFooterPanel;
    private JTextArea previewTextArea;
    private Color backgroundTheme;
    private JTextField txtRunPrefixName;

    // options name
    private static final String PREFIX_NAME_RUN = "prefix_name_for_run";
    private static final String IMPORT_DATA_FROM_RUN = "import_data_from_run";

    private JBCardLayout.SwipeDirection myTransitionDirection = JBCardLayout.SwipeDirection.AUTO;

    public StartWizard(String title, ParentableNode element, @Nullable Project project, StartResourceModel model) {
        super(project, true);
        this.backgroundTheme = EditorColorsManager.getInstance().getGlobalScheme().getDefaultBackground();
        myTitle = title;
        buildStructure(model, element);
        myCurrentStep = 0;
        mySteps = getSteps(model);
        fill(model);
        init();
    }

    @Override
    protected void init() {
        super.init();
        updateStep();
    }

    private void fill(StartResourceModel model) {
        navigationList = new ArrayList<>();
        Box box = Box.createVerticalBox();
        navigationPanel.add(box);
        final int[] index = {0};
        mySteps.stream().forEach(step -> {
            JLabel currentStepLabel = new JLabel(step.getTitle());
            currentStepLabel.setForeground(EditorColorsManager.getInstance().getGlobalScheme().getColor(ColorKey.find("CARET_COLOR")));
            currentStepLabel.setBorder(new EmptyBorder(0, 0, 5, 0));

            box.add(currentStepLabel);
            // add steps in left sidebar
            navigationList.add(currentStepLabel);

            // add steps in cardLayout center panel
            myContentPanel.add(step.getComponent(), Integer.toString(index[0]));
            index[0]++;
        });
        navigationList.get(0).setForeground(BLUE);


        JCheckBox chk = new JCheckBox("Show Preview");
        chk.setBounds(100,100, 50,50);
        previewFooterPanel.add(chk);
        chk.addItemListener(itemEvent -> {
            if (chk.isSelected()) {
                myRightPanel.setVisible(true);
            } else {
                myRightPanel.setVisible(false);
            }
        });

        previewTextArea = new JTextArea();
        previewTextArea.setEditable(false);
        previewTextArea.setBorder(MARGIN_10);
        previewTextArea.setFont(TIMES_PLAIN_14);

        JPanel previewPanel = new JPanel();
        previewPanel.add(previewTextArea);

        JBScrollPane scroll = new JBScrollPane(previewPanel);
        scroll.setBorder(NO_BORDER);
        scroll.setBackground(backgroundTheme);

        myRightPanel.add(scroll);

        updatePreview(model);

    }

    private void buildStructure(StartResourceModel model, ParentableNode element) {
        myPreviousButton = new JButton(IdeBundle.message("button.wizard.previous"));
        myNextButton = new JButton(IdeBundle.message("button.wizard.next"));
        myCancelButton = new JButton(CommonBundle.getCancelButtonText());
        myHelpButton = new JButton(CommonBundle.getHelpButtonText());

        myHeaderPanel = new JPanel(new BorderLayout());
        myContentPanel = new JPanel(new JBCardLayout());
        myLeftPanel = new JPanel(new JBCardLayout());
        myRightPanel = new JPanel(new BorderLayout());
        myFooterPanel = new JPanel(new BorderLayout());

        // if wizard requires an option panel
        List<String> optionsToDisplay = getOptionsToDisplay(model);
        if (!optionsToDisplay.isEmpty()) {
            JPanel innerOptionsPanel = getOptionsPanel(optionsToDisplay, model, element);

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

        myContentPanel.setBackground(backgroundTheme);
        myContentPanel.setPreferredSize(new Dimension(550, 400));
        myLeftPanel.setBackground(backgroundTheme);
        myLeftPanel.setBorder(new MatteBorder(0, 0, 0, 1, EditorColorsManager.getInstance().getGlobalScheme().getColor(ColorKey.find("SEPARATOR_BELOW_COLOR"))));
        myRightPanel.setBackground(backgroundTheme);
        myRightPanel.setBorder(new MatteBorder(0, 1, 0, 0, EditorColorsManager.getInstance().getGlobalScheme().getColor(ColorKey.find("SEPARATOR_BELOW_COLOR"))));
        myRightPanel.setVisible(false);

        navigationPanel = new JPanel();
        navigationPanel.setBackground(backgroundTheme);
        navigationPanel.setBorder(new EmptyBorder(10, 15, 0, 15));

        myLeftPanel.add(navigationPanel);

        previewFooterPanel = new JPanel();
        if (SystemInfo.isMac) {
            myFooterPanel.add(previewFooterPanel, BorderLayout.LINE_START);
            previewFooterPanel.setLayout(new BoxLayout(previewFooterPanel, BoxLayout.X_AXIS));
        } else {
            myFooterPanel.add(previewFooterPanel, BorderLayout.LINE_START);
        }
    }

    private JPanel getOptionsPanel(List<String> optionsToDisplay, StartResourceModel model, ParentableNode element) {
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
            model.getRuns().forEach(run -> cmbPickRunToImportData.addItem(run.getName()));

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
                        model.adaptsToRun(configuration);
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

    private List<BaseStep> getSteps(StartResourceModel model) {
        List<BaseStep> steps = new ArrayList<>();
        boolean hasParams = !model.getParams().isEmpty();
        boolean hasInputResources = !model.getInputResources().isEmpty();
        boolean hasOutputResources = !model.getOutputResources().isEmpty();
        boolean hasWorkspaces = !model.getWorkspaces().isEmpty();
        if (hasParams) {
            steps.add(buildStepWithListener(new ParametersStep(model)));
        }

        if (hasInputResources) {
            steps.add(buildStepWithListener(new InputResourcesStep(model)));
        }

        if (hasOutputResources) {
            steps.add(buildStepWithListener(new OutputResourcesStep(model)));
        }

        if (hasWorkspaces) {
            steps.add(buildStepWithListener(new WorkspacesStep(model)));
        }

        steps.add(buildStepWithListener(new AuthenticationStep(model)));

        return steps;
    }

    private BaseStep buildStepWithListener(BaseStep step) {
        step.addStepListener(new BaseStep.Listener() {

            @Override
            public void stateChanged() {
                StartResourceModel model = getModelCurrentStep();
                updatePreview(model);
                updateButtons();
            }

            @Override
            public void doNextAction() {
                if (myNextButton.isEnabled()) {
                    StartWizard.this.doNextAction();
                }
            }
        });
        return step;
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        final JPanel panel = new JPanel(new BorderLayout());
        panel.add(myHeaderPanel, BorderLayout.PAGE_START);
        panel.add(myRightPanel, BorderLayout.LINE_END);
        panel.add(myContentPanel, BorderLayout.CENTER);
        panel.add(myLeftPanel, BorderLayout.LINE_START);
        return panel;
    }

    @Override
    protected JComponent createSouthPanel() {
        myFooterPanel.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));

        JPanel buttonPanel = new JPanel();

        if (SystemInfo.isMac) {
            myFooterPanel.add(buttonPanel, BorderLayout.EAST);
            buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));

            if (!UIUtil.isUnderDarcula()) {
                myHelpButton.putClientProperty("JButton.buttonType", "help");
            }
            if (UIUtil.isUnderAquaLookAndFeel()) {
                myHelpButton.setText("");
            }

            int index = 0;
            JPanel leftPanel = new JPanel();
            if (ApplicationInfo.contextHelpAvailable()) {
                leftPanel.add(myHelpButton);
                TouchbarDataKeys.putDialogButtonDescriptor(myHelpButton, index++);
            }
            leftPanel.add(myCancelButton);
            TouchbarDataKeys.putDialogButtonDescriptor(myCancelButton, index++);
            myFooterPanel.add(leftPanel, BorderLayout.WEST);

            if (mySteps.size() > 1) {
                buttonPanel.add(Box.createHorizontalStrut(5));
                buttonPanel.add(myPreviousButton);
                TouchbarDataKeys.putDialogButtonDescriptor(myPreviousButton, index++).setMainGroup(true);
            }
            buttonPanel.add(Box.createHorizontalStrut(5));
            buttonPanel.add(myNextButton);
            TouchbarDataKeys.putDialogButtonDescriptor(myNextButton, index++).setMainGroup(true).setDefault(true);
        }
        else {
            myFooterPanel.add(buttonPanel, BorderLayout.CENTER);
            GroupLayout layout = new GroupLayout(buttonPanel);
            buttonPanel.setLayout(layout);
            layout.setAutoCreateGaps(true);

            final GroupLayout.SequentialGroup hGroup = layout.createSequentialGroup();
            final GroupLayout.ParallelGroup vGroup = layout.createParallelGroup();
            final Collection<Component> buttons = new ArrayList<>(5);
            final boolean helpAvailable = ApplicationInfo.contextHelpAvailable();

            add(hGroup, vGroup, null, Box.createHorizontalGlue());
            if (mySteps.size() > 1) {
                add(hGroup, vGroup, buttons, myPreviousButton);
            }
            add(hGroup, vGroup, buttons, myNextButton, myCancelButton);
            if (helpAvailable) {
                add(hGroup, vGroup, buttons, myHelpButton);
            }

            layout.setHorizontalGroup(hGroup);
            layout.setVerticalGroup(vGroup);
            layout.linkSize(buttons.toArray(new Component[0]));
        }

        myPreviousButton.setEnabled(false);
        myPreviousButton.addActionListener(e -> doPreviousAction());
        myNextButton.addActionListener(e -> {
            if (isLastStep()) {
                // Commit data of current step and perform OK action
                final Step currentStep = mySteps.get(myCurrentStep);
                try {
                    currentStep._commit(true);
                    doOKAction();
                }
                catch (final CommitStepException exc) {
                    String message = exc.getMessage();
                    if (message != null) {
                        Messages.showErrorDialog(myContentPanel, message);
                    }
                }
            }
            else {
                doNextAction();
            }
        });

        myCancelButton.addActionListener(e -> doCancelAction());
        myHelpButton.addActionListener(e -> helpAction());

        return myFooterPanel;
    }

    private void add(final GroupLayout.Group hGroup,
                     final GroupLayout.Group vGroup,
                     @Nullable final Collection<? super Component> collection,
                     final Component... components) {
        for (Component component : components) {
            hGroup.addComponent(component);
            vGroup.addComponent(component);
            if (collection != null) collection.add(component);
        }
    }

    private void doPreviousAction() {
        int oldStep = myCurrentStep;
        myCurrentStep = getPreviousStep(myCurrentStep);
        setSelectItemNavigationList(oldStep, myCurrentStep);
        updateStep(JBCardLayout.SwipeDirection.BACKWARD);
    }

    private int getPreviousStep(int step) {
        if (--step < 0) {
            step = 0;
        }
        return step;
    }

    private void doNextAction() {
        if (!canGoNext()) return;

        if (isLastStep()) {
            doOKAction();
            return;
        }

        int oldStep = myCurrentStep;
        myCurrentStep = getNextStep(myCurrentStep);
        setSelectItemNavigationList(oldStep, myCurrentStep);
        updateStep(JBCardLayout.SwipeDirection.FORWARD);
    }

    private int getNextStep(int step) {
        final int stepCount = mySteps.size();
        if (++step >= stepCount) {
            step = stepCount - 1;
        }
        return step;
    }

    private void helpAction() {
        String helpURL = mySteps.get(myCurrentStep).getHelpId();
        BrowserUtil.browse(helpURL);
    }

    private void setSelectItemNavigationList(int oldStep, int newStep) {
        if (newStep < 0) {
            newStep = 0;
        }
        if (newStep >= navigationList.size()) {
            newStep = navigationList.size() - 1;
        }
        //navigationList.get(oldStep).setForeground(Color.black);
        navigationList.get(oldStep).setForeground(EditorColorsManager.getInstance().getGlobalScheme().getColor(ColorKey.find("CARET_COLOR")));
        navigationList.get(newStep).setForeground(BLUE);
    }

    private StartResourceModel getModelCurrentStep() {
        return (StartResourceModel) mySteps.get(myCurrentStep).getModel();
    }

    protected void updateStep() {
        if (mySteps.isEmpty()) {
            return;
        }

        showStepComponent(myCurrentStep);
        updateButtons();

        final BaseStep step = mySteps.get(myCurrentStep);
        JComponent component = step.getPreferredFocusedComponent();
        requestFocusTo(component != null ? component : myNextButton);

        String stepTitle = step.getTitle();
        setTitle(stepTitle != null ? myTitle + ": " + stepTitle : myTitle);
    }

    private void updateStep(JBCardLayout.SwipeDirection direction) {
        //it would be better to pass 'direction' to 'updateStep' as a parameter, but since that method is used and overridden in plugins
        // we cannot do it without breaking compatibility
        try {
            myTransitionDirection = direction;
            updateStep();
        }
        finally {
            myTransitionDirection = JBCardLayout.SwipeDirection.AUTO;
        }
    }

    private void requestFocusTo(final JComponent component) {
        UiNotifyConnector.doWhenFirstShown(component, () -> {
            final IdeFocusManager focusManager = IdeFocusManager.findInstanceByComponent(component);
            focusManager.requestFocus(component, false);
        });
    }

    private void showStepComponent(int step) {
        myContentPanel.revalidate();
        myContentPanel.repaint();
        ((JBCardLayout)myContentPanel.getLayout()).swipe(myContentPanel, Integer.toString(step), myTransitionDirection);
    }

    private void updateButtons() {
        boolean lastStep = isLastStep();
        updateButtons(lastStep, lastStep ? canFinish() : true, isFirstStep());
    }

    private void updateButtons(boolean lastStep, boolean canGoNext, boolean firstStep) {
        if (lastStep) {
            myNextButton.setText(UIUtil.removeMnemonic("&Start"));
            myNextButton.setMnemonic('F');
        }
        else {
            myNextButton.setText(UIUtil.removeMnemonic(IdeBundle.message("button.wizard.next")));
            myNextButton.setMnemonic('N');
        }

        myNextButton.setEnabled(canGoNext);

        if (myNextButton.isEnabled() && !ApplicationManager.getApplication().isUnitTestMode() && getRootPane() != null) {
            getRootPane().setDefaultButton(myNextButton);
        }

        myPreviousButton.setEnabled(!firstStep);
    }

    private boolean canGoNext() {
        return mySteps.get(myCurrentStep).isComplete();
    }

    private boolean isFirstStep() {
        return myCurrentStep == 0;
    }

    private boolean isLastStep() {
        return myCurrentStep == mySteps.size() - 1;
    }

    private boolean canFinish() {
        for (BaseStep step : mySteps) {
            if (!step.isComplete()) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected void dispose() {
        super.dispose();
        for (BaseStep step : mySteps) {
            Disposer.dispose(step);
        }
    }

    private void updatePreview(StartResourceModel model) {
        String preview = "";
        try {
            preview = YAMLBuilder.createPreview(model);
        } catch (IOException e) {
            //logger.warn("Error: " + e.getLocalizedMessage());
        }
        previewTextArea.setText(preview);
    }

    @Override
    protected void doOKAction() {
        calculateArgs();
        super.doOKAction();
    }

    private void calculateArgs() {
        Map<String, String> parameters = new HashMap<>();
        Map<String, String> inputResources = new HashMap<>();
        Map<String, String> outputResources = new HashMap<>();

        StartResourceModel model = getModelCurrentStep();
        for (Input input : model.getParams().values()) {
            String value = input.value() == null ? input.defaultValue().orElse("") : input.value();
            parameters.put(input.name(), value);
        }

        for (Input input: model.getInputResources().values()) {
            inputResources.put(input.name(), input.value());
        }

        for (Output output : model.getOutputResources().values()) {
            outputResources.put(output.name(), output.value());
        }

        /*model.setParameters(parameters);
        model.setInputResources(inputResources);
        model.setOutputResources(outputResources);*/

        // options panel
        String runPrefixName = txtRunPrefixName != null ? txtRunPrefixName.getText() : "";
        if (!runPrefixName.trim().isEmpty()) {
            model.setRunPrefixName(runPrefixName);
        }
    }

    private void refreshSteps() {
        this.mySteps.forEach(step -> step.refresh());
    }

}
