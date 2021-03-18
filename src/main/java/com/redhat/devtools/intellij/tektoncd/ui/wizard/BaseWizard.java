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

import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.intellij.CommonBundle;
import com.intellij.ide.BrowserUtil;
import com.intellij.ide.IdeBundle;
import com.intellij.ide.wizard.CommitStepException;
import com.intellij.ide.wizard.Step;
import com.intellij.openapi.application.ApplicationInfo;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.colors.ColorKey;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.project.Project;
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
import com.redhat.devtools.intellij.tektoncd.utils.YAMLBuilder;
import com.redhat.devtools.intellij.tektoncd.utils.model.actions.ActionToRunModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import org.jetbrains.annotations.Nullable;


import static com.redhat.devtools.intellij.tektoncd.ui.UIConstants.BLUE;
import static com.redhat.devtools.intellij.tektoncd.ui.UIConstants.MARGIN_10;
import static com.redhat.devtools.intellij.tektoncd.ui.UIConstants.NO_BORDER;
import static com.redhat.devtools.intellij.tektoncd.ui.UIConstants.TIMES_PLAIN_14;

public abstract class BaseWizard extends DialogWrapper {

    private String myTitle;
    protected List<BaseStep> mySteps;
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
    private JPanel navigationPanel;
    private JPanel previewFooterPanel;
    private JTextArea previewTextArea;
    protected Color backgroundTheme;
    protected ActionToRunModel model;

    private JBCardLayout.SwipeDirection myTransitionDirection = JBCardLayout.SwipeDirection.AUTO;

    public BaseWizard(String title, @Nullable Project project, ActionToRunModel model) {
        super(project, true);
        this.model = model;
        this.backgroundTheme = EditorColorsManager.getInstance().getGlobalScheme().getDefaultBackground();
        myTitle = title;
        buildStructure();
        myCurrentStep = 0;
    }

    @Override
    protected void init() {
        mySteps = getSteps();
        fill();
        super.init();
        updateStep();
    }

    private void fill() {
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
        previewTextArea.setSize(new Dimension(300, 400));
        previewTextArea.setFont(TIMES_PLAIN_14);
        previewTextArea.setLineWrap(true);

        JPanel previewPanel = new JPanel();
        previewPanel.add(previewTextArea);

        JBScrollPane scroll = new JBScrollPane(previewPanel);
        scroll.setBorder(NO_BORDER);
        scroll.setBackground(backgroundTheme);

        myRightPanel.add(scroll);

        updatePreview(model);

    }

    private void buildStructure() {
        myPreviousButton = new JButton(IdeBundle.message("button.wizard.previous"));
        myNextButton = new JButton(IdeBundle.message("button.wizard.next"));
        myCancelButton = new JButton(CommonBundle.getCancelButtonText());
        myHelpButton = new JButton(CommonBundle.getHelpButtonText());

        myHeaderPanel = new JPanel(new BorderLayout());
        myContentPanel = new JPanel(new JBCardLayout());
        myLeftPanel = new JPanel(new JBCardLayout());
        myRightPanel = new JPanel(new BorderLayout());
        myFooterPanel = new JPanel(new BorderLayout());

        myContentPanel.setBackground(backgroundTheme);
        myContentPanel.setPreferredSize(new Dimension(600, 400));
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

    public abstract List<BaseStep> getSteps();

    protected BaseStep buildStepWithListener(BaseStep step) {
        step.addStepListener(new BaseStep.Listener() {

            @Override
            public void stateChanged() {
                updatePreview(model);
                updateButtons();
            }

            @Override
            public void doNextAction() {
                if (myNextButton.isEnabled()) {
                    BaseWizard.this.doNextAction();
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

        if (SystemInfo.isMac) {
            createFooterMac();
        } else {
            createFooter();
        }
        setFooterButtonsListener();

        return myFooterPanel;
    }

    private void setFooterButtonsListener() {
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
    }

    private void createFooterMac() {
        JPanel buttonPanel = new JPanel();
        myFooterPanel.add(buttonPanel, BorderLayout.EAST);
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));

        if (!UIUtil.isUnderDarcula()) {
            myHelpButton.putClientProperty("JButton.buttonType", "help");
        }

        int index = 0;
        if (ApplicationInfo.contextHelpAvailable()) {
            add(buttonPanel, myHelpButton, index++, false);
        }
        add(buttonPanel, myCancelButton, index++, false);

        if (mySteps.size() > 1) {
            add(buttonPanel, myPreviousButton, index++, false);
        }
        add(buttonPanel, myNextButton, index++, false);
    }

    private void createFooter() {
        JPanel buttonPanel = new JPanel();
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

    private void add(JPanel panel, JButton button, int index, boolean isDefault) {
        panel.add(Box.createHorizontalStrut(5));
        panel.add(button);
        TouchbarDataKeys.putDialogButtonDescriptor(button, index).setMainGroup(true).setDefault(isDefault);
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
        doBeforeNextStep(oldStep);
        myCurrentStep = getNextStep(myCurrentStep);
        setSelectItemNavigationList(oldStep, myCurrentStep);
        updateStep(JBCardLayout.SwipeDirection.FORWARD);
    }

    public abstract void doBeforeNextStep(int currentStep);

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
            myNextButton.setText(UIUtil.removeMnemonic(getLastStepButtonText()));
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


    protected void updatePreview(ActionToRunModel model) {
        String preview = "";
        try {
            preview = new YAMLMapper().writeValueAsString(YAMLBuilder.createRun(model));
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

    public abstract String getLastStepButtonText();
    public abstract void calculateArgs();

    protected void refreshSteps() {
        this.mySteps.forEach(step -> step.refresh());
    }

}

