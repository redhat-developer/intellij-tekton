/*******************************************************************************
 *  Copyright (c) 2022 Red Hat, Inc.
 *  Distributed under license by Red Hat, Inc. All rights reserved.
 *  This program is made available under the terms of the
 *  Eclipse Public License v2.0 which accompanies this distribution,
 *  and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 *  Contributors:
 *  Red Hat, Inc.
 ******************************************************************************/
package com.redhat.devtools.intellij.tektoncd.ui.bundle;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Divider;
import com.intellij.ui.CollectionListModel;
import com.intellij.ui.OnePixelSplitter;
import com.intellij.ui.components.JBList;
import com.intellij.util.ui.JBUI;
import com.redhat.devtools.intellij.common.utils.ExecHelper;
import com.redhat.devtools.intellij.common.utils.UIHelper;
import com.redhat.devtools.intellij.tektoncd.tkn.Tkn;
import org.jetbrains.annotations.Nullable;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionListener;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static com.redhat.devtools.intellij.tektoncd.ui.UIConstants.RED;
import static com.redhat.devtools.intellij.tektoncd.ui.UIConstants.RED_BORDER_SHOW_ERROR;
import static com.redhat.devtools.intellij.tektoncd.ui.UIConstants.SEARCH_FIELD_BORDER_COLOR;

public abstract class BundleDialog extends DialogWrapper {

    protected Project project;
    protected Tkn tkn;
    protected JLabel warning, lblGeneralError;
    protected BundleDialog(@Nullable Project project, String title, Tkn tkn) {
        this(project, title, "Ok", tkn);
    }

    protected BundleDialog(@Nullable Project project, String title, String OkButton, Tkn tkn) {
        super(project, true);
        this.project = project;
        this.tkn = tkn;
        setTitle(title);
        setOKButtonText(OkButton);
        super.init();
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        final JPanel panel = new JPanel(new BorderLayout());
        panel.add(createDescriptionPanel(), BorderLayout.NORTH);
        panel.add(createSplitter(), BorderLayout.CENTER);
        panel.add(createErrorSouthPanel(), BorderLayout.SOUTH);
        panel.setPreferredSize(new Dimension(630, 400));
        return panel;
    }

    private JPanel createErrorSouthPanel() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBorder(JBUI.Borders.emptyTop(10));
        lblGeneralError = new JLabel("");
        lblGeneralError.setForeground(RED);
        lblGeneralError.setVisible(false);
        wrapper.add(lblGeneralError, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel createDescriptionPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        JLabel lbl = new JLabel(getTopDescriptionText(),
                AllIcons.General.ShowInfos,
                SwingConstants.LEFT
        );
        lbl.setToolTipText(getTopDescriptionTooltip());
        panel.add(lbl);
        warning = new JLabel("");
        warning.setBorder(JBUI.Borders.emptyTop(5));
        warning.setVisible(false);
        panel.add(warning);
        panel.setBorder(JBUI.Borders.emptyBottom(10));
        return panel;
    }

    private JComponent createSplitter() {
        OnePixelSplitter tabPanel = new OnePixelSplitter(false, 0.37F) {
            protected Divider createDivider() {
                Divider divider = super.createDivider();
                divider.setBackground(SEARCH_FIELD_BORDER_COLOR);
                return divider;
            }
        };
        tabPanel.setFirstComponent(buildLeftPanel());
        tabPanel.setSecondComponent(buildRightPanel());
        return tabPanel;
    }

    protected void enableLoadingState() {
        updateLoadingState(new Cursor(Cursor.WAIT_CURSOR));
    }

    protected void disableLoadingState() {
        updateLoadingState(Cursor.getDefaultCursor());
    }

    protected void showTimedErrorMessage(String error) {
        lblGeneralError.setText(error);
        lblGeneralError.setVisible(true);
        ExecHelper.submit(() -> {
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    UIHelper.executeInUI(() -> lblGeneralError.setVisible(false));
                }
            }, 30000);
        });
    }

    protected <T> JBList<T> initJBList(JBList<T> listPanel, List<T> values, String emptyText, Border border,
                                     ListCellRenderer<T> listCellRenderer, ListSelectionListener selectionListener) {
        listPanel.setModel(new CollectionListModel<>(values));
        listPanel.setEmptyText(emptyText);
        if (border != null) {
            listPanel.setBorder(border);
        }
        listPanel.setCellRenderer(listCellRenderer);
        listPanel.addListSelectionListener(selectionListener);
        return listPanel;
    }

    protected void showWarning(String text, JComponent failingComponent) {
        Border curBorder = null;
        if (failingComponent != null) {
            curBorder = failingComponent.getBorder();
            failingComponent.setBorder(RED_BORDER_SHOW_ERROR);
        }
        warning.setText(text);
        warning.setForeground(RED);
        warning.setVisible(true);
        warning.invalidate();

        Border finalCurBorder = curBorder;
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                warning.setVisible(false);
                if (failingComponent != null) {
                    failingComponent.setBorder(finalCurBorder);
                }
            }
        }, 5000);
    }

    protected abstract String getTopDescriptionText();
    protected abstract String getTopDescriptionTooltip();
    protected abstract JComponent buildLeftPanel();
    protected abstract JComponent buildRightPanel();
    protected abstract void updateLoadingState(Cursor cursor);
}
