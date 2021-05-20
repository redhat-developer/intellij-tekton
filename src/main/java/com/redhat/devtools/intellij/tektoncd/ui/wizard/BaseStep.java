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
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.EventDispatcher;
import com.redhat.devtools.intellij.tektoncd.tkn.component.field.Input;
import com.redhat.devtools.intellij.tektoncd.utils.model.actions.ActionToRunModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


import static com.redhat.devtools.intellij.tektoncd.ui.UIConstants.NO_BORDER;

public abstract class BaseStep implements Step, Disposable {
    @Nullable
    private String title;
    protected ActionToRunModel model;
    protected JPanel mainPanel;
    protected JPanel contentPanel;
    protected GridBagLayout gridBagLayout;
    protected GridBagConstraints gridBagConstraints;
    protected Map<Integer, JLabel> errorFieldsByRow;
    protected Color backgroundTheme;
    protected Font editorFont;
    protected Font defaultLabelFont;

    public BaseStep(@Nullable String title, ActionToRunModel model) {
        this.title = title;
        this.model = model;
        this.errorFieldsByRow = new HashMap<>();
        this.backgroundTheme = EditorColorsManager.getInstance().getGlobalScheme().getDefaultBackground();
        this.editorFont = new Font(EditorColorsManager.getInstance().getSchemeForCurrentUITheme().getEditorFontName(), Font.PLAIN, EditorColorsManager.getInstance().getSchemeForCurrentUITheme().getEditorFontSize());
        this.defaultLabelFont = new JLabel().getFont();
        _init();
    }

    public ActionToRunModel getModel() {
        return this.model;
    }

    @Override
    public void _init() {
        // mainPanel is the panel displayed in the center of the wizard as it is
        this.mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(backgroundTheme);

        gridBagLayout = new GridBagLayout();
        gridBagConstraints = new GridBagConstraints();
        contentPanel = new JPanel(gridBagLayout);

        contentPanel.setBackground(backgroundTheme);
        contentPanel.setBorder(new EmptyBorder(0, 10, 10, 10));

        JBScrollPane scroll = new JBScrollPane(contentPanel);
        scroll.setBorder(NO_BORDER);
        scroll.setBackground(backgroundTheme);

        mainPanel.add(scroll);

        setContent();
        adjustContentPanel();
    }

    public interface Listener extends StepListener {
        void doNextAction();
    }

    private final EventDispatcher<BaseStep.Listener> myEventDispatcher = EventDispatcher.create(BaseStep.Listener.class);

    protected void fireStateChanged() {
        myEventDispatcher.getMulticaster().stateChanged();
    }

    public void addStepListener(BaseStep.Listener listener) {
        myEventDispatcher.addListener(listener);
    }

    @Override
    public void _commit(boolean finishChosen) throws CommitStepException {}

    @Override
    public Icon getIcon() {
        return null;
    }

    @Override
    public void dispose() {}

    @Override
    public JComponent getComponent() {
        return mainPanel;
    }

    @Nullable
    @Override
    public JComponent getPreferredFocusedComponent() {
        return mainPanel;
    }

    @Nullable
    public String getTitle() {
        return title;
    }

    protected void adjustContentPanel() {
        // this code allows content to stick to top left corner even when the window is resized manually
        // add an extra row consuming vertical extra space
        if (contentPanel == null) return;
        int nRows = contentPanel.getComponentCount();
        if (nRows == 0) return;
        gridBagLayout.rowHeights = new int[nRows + 1];
        gridBagLayout.rowWeights = new double[nRows + 1];
        gridBagLayout.rowWeights[nRows] = 1;
        // add an extra column consuming extra horizontal space
        gridBagLayout.columnWidths = new int[]{0, 0};
        gridBagLayout.columnWeights = new double[]{0, 1};
    }

    protected JComponent setComponentStyle(@NotNull JComponent component, Font font, Border border, Dimension preferredSize) {
        if (font != null) component.setFont(font);
        if (border != null) component.setBorder(border);
        if (preferredSize != null) component.setPreferredSize(preferredSize);
        return component;
    }

    protected JComponent addComponent(@NotNull JComponent component, Font font, Border border, Dimension preferredSize, @NotNull GridBagConstraints gridBagConstraints) {
        component = setComponentStyle(component, font, border, preferredSize);
        contentPanel.add(component, gridBagConstraints);
        return component;
    }

    protected JComponent addComponent(@NotNull JComponent component, Font font, Border border, Dimension preferredSize, @NotNull int col, @NotNull int row, @NotNull int anchor) {
        gridBagConstraints.gridx = col;
        gridBagConstraints.gridy = row;
        gridBagConstraints.anchor = anchor;
        return addComponent(component, font, border, preferredSize, gridBagConstraints);
    }

    protected void deleteComponent(@NotNull JComponent component) {
        if (Arrays.stream(contentPanel.getComponents()).anyMatch(comp -> comp.equals(component))) {
            contentPanel.remove(component);
            contentPanel.revalidate();
            contentPanel.repaint();
        }
    }

    protected void addTooltip(@NotNull JComponent component, String textToDisplay) {
        if (!textToDisplay.isEmpty()) {
            component.setToolTipText(textToDisplay);
        }
    }

    protected void setInputValue(List<Input> inputs, String inputName, String value) {
        for (Input input: inputs) {
            if (input.name().equals(inputName)) {
                input.setValue(value);
                break;
            }
        }
    }

    public void refresh() {
        contentPanel.removeAll();
        setContent();
        adjustContentPanel();
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    public abstract void setContent();
    public abstract boolean isComplete();
    public abstract String getHelpId();
}
