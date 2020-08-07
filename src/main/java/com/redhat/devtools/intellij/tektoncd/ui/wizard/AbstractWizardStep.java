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

import com.intellij.ide.wizard.CommitStepException;
import com.intellij.ide.wizard.Step;
import com.intellij.ide.wizard.StepListener;
import com.intellij.openapi.Disposable;
import com.intellij.util.EventDispatcher;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public abstract class AbstractWizardStep implements Step, Disposable {

    protected enum CommitType {
        Prev, Next, Finish
    }

    @Nullable
    private String myTitle;

    public interface Listener extends StepListener {
        void doNextAction();
    }

    private final EventDispatcher<Listener> myEventDispatcher = EventDispatcher.create(Listener.class);

    public AbstractWizardStep(@Nullable final String title) {
        myTitle = title;
    }

    @Override
    public void _init() {
    }

    public final void _commitPrev() throws CommitStepException {
        commit(AbstractWizardStep.CommitType.Prev);
    }

    @Override
    public final void _commit(boolean finishChosen) throws CommitStepException {
        commit(finishChosen ? AbstractWizardStep.CommitType.Finish : AbstractWizardStep.CommitType.Next);
    }

    public void addStepListener(Listener listener) {
        myEventDispatcher.addListener(listener);
    }

    protected void setTitle(@Nullable final String title) {
        myTitle = title;
    }

    protected void fireStateChanged() {
        myEventDispatcher.getMulticaster().stateChanged();
    }

    protected void fireGoNext() {
        myEventDispatcher.getMulticaster().doNextAction();
    }

    @Override
    public Icon getIcon() {
        return null;
    }

    @NotNull
    public abstract Object getStepId();

    public abstract boolean isComplete();

    public abstract void commit(CommitType commitType) throws CommitStepException;

    @Nullable
    public String getTitle() {
        return myTitle;
    }

    @Override
    public void dispose() {
    }

    @Override
    @Nullable
    public abstract JComponent getPreferredFocusedComponent();

    @NonNls
    public String getHelpId() {
        return null;
    }

}
