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
import com.intellij.ide.BrowserUtil;
import com.intellij.ide.IdeBundle;
import com.intellij.ide.wizard.CommitStepException;
import com.intellij.ide.wizard.Step;
import com.intellij.ide.wizard.StepAdapter;
import com.intellij.ide.wizard.StepListener;
import com.intellij.openapi.application.ApplicationInfo;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.ui.JBCardLayout;
import com.intellij.ui.mac.TouchbarDataKeys;
import com.intellij.util.ui.UIUtil;
import com.intellij.util.ui.update.UiNotifyConnector;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
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
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractWizard<T extends Step> extends DialogWrapper {
    private static final Logger LOG = Logger.getInstance("com.redhat.devtools.intellij.tektoncd.ui.wizard.AbstractWizard");

    protected int myCurrentStep;
    protected final ArrayList<T> mySteps;
    private JButton myPreviousButton;
    private JButton myNextButton;
    private JButton myCancelButton;
    private JButton myHelpButton;
    protected JPanel myHeaderPanel;
    protected JPanel myContentPanel;
    protected JPanel myLeftPanel;
    protected JPanel myRightPanel;
    protected JPanel myFooterPanel;
    private Component myCurrentStepComponent;
    private JBCardLayout.SwipeDirection myTransitionDirection = JBCardLayout.SwipeDirection.AUTO;
    private final Map<Component, String> myComponentToIdMap = new HashMap<>();
    private final StepListener myStepListener = () -> updateStep();

    public AbstractWizard(final String title, final Component dialogParent) {
        super(dialogParent, true);
        mySteps = new ArrayList<>();
        initWizard(title);
    }

    public AbstractWizard(final String title, @Nullable final Project project) {
        super(project, true);
        mySteps = new ArrayList<>();
        initWizard(title);
    }

    private void initWizard(final String title) {
        setTitle(title);
        myCurrentStep = 0;

        myPreviousButton = new JButton(IdeBundle.message("button.wizard.previous"));
        myNextButton = new JButton(IdeBundle.message("button.wizard.next"));
        myCancelButton = new JButton(CommonBundle.getCancelButtonText());
        myHelpButton = new JButton(CommonBundle.getHelpButtonText());

        myHeaderPanel = new JPanel(new BorderLayout());
        myContentPanel = new JPanel(new JBCardLayout());
        myContentPanel.setPreferredSize(new Dimension(500, 300));
        myLeftPanel = new JPanel(new JBCardLayout());
        myRightPanel = new JPanel(new JBCardLayout());
        myFooterPanel = new JPanel(new BorderLayout());
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
                LOG.assertTrue(currentStep != null);
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

    public JPanel getContentComponent() {
        return myContentPanel;
    }

    private static void add(final GroupLayout.Group hGroup,
                            final GroupLayout.Group vGroup,
                            @Nullable final Collection<? super Component> collection,
                            final Component... components) {
        for (Component component : components) {
            hGroup.addComponent(component);
            vGroup.addComponent(component);
            if (collection != null) collection.add(component);
        }
    }

    @Override
    protected JComponent createCenterPanel() {
        final JPanel panel = new JPanel(new BorderLayout());
        panel.add(myHeaderPanel, BorderLayout.PAGE_START);
        panel.add(myRightPanel, BorderLayout.LINE_END);
        panel.add(myContentPanel, BorderLayout.CENTER);
        panel.add(myLeftPanel, BorderLayout.LINE_START);
        return panel;
    }

    protected void setLeftPanel(JComponent component) {
        myLeftPanel.add(component);
    }

    protected void setRightPanel(JComponent component) {
        myRightPanel.add(component);
    }

    public int getCurrentStep() {
        return myCurrentStep;
    }

    public T getCurrentStepObject() {
        return mySteps.get(myCurrentStep);
    }

    public void addSteps(List<T> steps) {
        for (T step : steps) {
            addStep(step);
        }
    }

    public void addStep(@NotNull final T step) {
        addStep(step, mySteps.size());
    }

    public void addStep(@NotNull final T step, int index) {
        mySteps.add(index, step);

        if (step instanceof StepAdapter) {
            ((StepAdapter)step).registerStepListener(myStepListener);
        }
        // card layout is used
        final Component component = step.getComponent();
        if (component != null) {
            addStepComponent(component);
        }
    }

    @Override
    protected void init() {
        super.init();
        updateStep();
    }


    protected String addStepComponent(final Component component) {
        String id = myComponentToIdMap.get(component);
        if (id == null) {
            id = Integer.toString(myComponentToIdMap.size());
            myComponentToIdMap.put(component, id);
            myContentPanel.add(component, id);
        }
        return id;
    }

    private void showStepComponent(final Component component) {
        String id = myComponentToIdMap.get(component);
        if (id == null) {
            id = addStepComponent(component);
            myContentPanel.revalidate();
            myContentPanel.repaint();
        }
        ((JBCardLayout)myContentPanel.getLayout()).swipe(myContentPanel, id, myTransitionDirection);
    }

    protected void doPreviousAction() {
        // Commit data of current step
        final Step currentStep = mySteps.get(myCurrentStep);
        LOG.assertTrue(currentStep != null);
        try {
            currentStep._commit(false);
        }
        catch (final CommitStepException exc) {
            Messages.showErrorDialog(
                    myContentPanel,
                    exc.getMessage()
            );
            return;
        }

        myCurrentStep = getPreviousStep(myCurrentStep);
        updateStep(JBCardLayout.SwipeDirection.BACKWARD);
    }

    protected final void updateStep(JBCardLayout.SwipeDirection direction) {
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

    protected void doNextAction() {
        // Commit data of current step
        final Step currentStep = mySteps.get(myCurrentStep);
        LOG.assertTrue(currentStep != null);
        LOG.assertTrue(!isLastStep(), "steps: " + mySteps + " current: " + currentStep);
        try {
            currentStep._commit(false);
        }
        catch (final CommitStepException exc) {
            Messages.showErrorDialog(
                    myContentPanel,
                    exc.getMessage()
            );
            return;
        }

        myCurrentStep = getNextStep(myCurrentStep);
        updateStep(JBCardLayout.SwipeDirection.FORWARD);
    }

    /**
     * override this to provide alternate step order
     * @param step index
     * @return the next step's index
     */
    protected int getNextStep(int step) {

        final int stepCount = mySteps.size();
        if (++step >= stepCount) {
            step = stepCount - 1;
        }
        return step;
    }

    protected final int getNextStep() {
        return getNextStep(getCurrentStep());
    }

    protected T getNextStepObject() {
        int step = getNextStep();
        return mySteps.get(step);
    }

    /**
     * override this to provide alternate step order
     * @param step index
     * @return the previous step's index
     */
    protected int getPreviousStep(int step) {
        if (--step < 0) {
            step = 0;
        }
        return step;
    }

    protected void updateStep() {
        if (mySteps.isEmpty()) {
            return;
        }

        final Step step = mySteps.get(myCurrentStep);
        LOG.assertTrue(step != null);
        step._init();
        myCurrentStepComponent = step.getComponent();
        LOG.assertTrue(myCurrentStepComponent != null);
        showStepComponent(myCurrentStepComponent);

        updateButtons();

        JComponent component = mySteps.get(getCurrentStep()).getPreferredFocusedComponent();
        requestFocusTo(component != null ? component : myNextButton);
    }

    private static void requestFocusTo(final JComponent component) {
        UiNotifyConnector.doWhenFirstShown(component, () -> {
            final IdeFocusManager focusManager = IdeFocusManager.findInstanceByComponent(component);
            focusManager.requestFocus(component, false);
        });
    }

    @Nullable
    @Override
    public JComponent getPreferredFocusedComponent() {
        JComponent component = getCurrentStepObject().getPreferredFocusedComponent();
        return component == null ? super.getPreferredFocusedComponent() : component;
    }

    protected boolean canGoNext() {
        return true;
    }

    protected boolean canFinish() {
        return isLastStep() && canGoNext();
    }

    protected void updateButtons() {
        boolean lastStep = isLastStep();
        updateButtons(lastStep, lastStep ? canFinish() : canGoNext(), isFirstStep());
    }

    public void updateButtons(boolean lastStep, boolean canGoNext, boolean firstStep) {
        if (lastStep) {
            if (mySteps.size() > 1) {
                myNextButton.setText(UIUtil.removeMnemonic(IdeBundle.message("button.finish")));
                myNextButton.setMnemonic('F');
            }
            else {
                myNextButton.setText(IdeBundle.message("button.ok"));
            }
            myNextButton.setEnabled(canGoNext);
        }
        else {
            myNextButton.setText(UIUtil.removeMnemonic(IdeBundle.message("button.wizard.next")));
            myNextButton.setMnemonic('N');
            myNextButton.setEnabled(canGoNext);
        }

        if (myNextButton.isEnabled() && !ApplicationManager.getApplication().isUnitTestMode() && getRootPane() != null) {
            getRootPane().setDefaultButton(myNextButton);
        }

        myPreviousButton.setEnabled(!firstStep);
    }

    protected boolean isFirstStep() {
        return myCurrentStep == 0;
    }

    protected boolean isLastStep() {
        return myCurrentStep == mySteps.size() - 1;
    }

    protected JButton getNextButton() {
        return myNextButton;
    }

    protected JButton getPreviousButton() {
        return myPreviousButton;
    }

    protected JButton getHelpButton() {
        return myHelpButton;
    }

    public JButton getCancelButton() {
        return myCancelButton;
    }

    /**
     * @deprecated unused
     */
    @Deprecated
    protected JButton getFinishButton() {
        return new JButton();
    }

    public Component getCurrentStepComponent() {
        return myCurrentStepComponent;
    }

    protected void helpAction() {
        BrowserUtil.browse(getHelpID());
    }

    @Nullable
    @NonNls
    protected abstract String getHelpID();
}

