/*******************************************************************************
 * Copyright (c) 2021 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc.
 ******************************************************************************/
package com.redhat.devtools.intellij.tektoncd.settings;

import com.intellij.ide.DataManager;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ex.Settings;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.FormBuilder;
import com.redhat.devtools.intellij.telemetry.ui.preferences.TelemetryConfigurable;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import static com.redhat.devtools.intellij.tektoncd.utils.JBLabelUtils.toStyledHtml;

public class SettingsView {
    private final JPanel myMainPanel;
    private final JBCheckBox chkDisplayPipelineRunResultAsNotification = new JBCheckBox("Display PipelineRuns result as notification");
    private final JBCheckBox chkEnableDeleteAllRelatedResourcesAsDefault = new JBCheckBox("Enable delete all related resource as default");
    private final JBCheckBox chkShowStartWizardWithNoInputs = new JBCheckBox("Show start wizard when a pipeline/task have no inputs (this allows to set up service accounts)");
    private final JBCheckBox chkDisplayLogsInEditor = new JBCheckBox("Show logs in text editor");

    public SettingsView() {
        myMainPanel = FormBuilder.createFormBuilder()
                .addComponent(chkDisplayPipelineRunResultAsNotification, 1)
                .addComponent(chkEnableDeleteAllRelatedResourcesAsDefault, 1)
                .addComponent(chkShowStartWizardWithNoInputs, 1)
                .addComponent(chkDisplayLogsInEditor, 1)
                .addComponent(createTelemetryComponent(), 1)
                .addComponentFillVertically(new JPanel(), 0)
                .getPanel();
    }

    public JPanel getPanel() {
        return myMainPanel;
    }

    public boolean getDisplayPipelineRunResultAsNotification() {
        return chkDisplayPipelineRunResultAsNotification.isSelected();
    }

    public void setDisplayPipelineRunResultAsNotification(boolean newStatus) {
        chkDisplayPipelineRunResultAsNotification.setSelected(newStatus);
    }

    public boolean getEnableDeleteAllRelatedResourcesAsDefault() {
        return chkEnableDeleteAllRelatedResourcesAsDefault.isSelected();
    }

    public void setEnableDeleteAllRelatedResourcesAsDefault(boolean newStatus) {
        chkEnableDeleteAllRelatedResourcesAsDefault.setSelected(newStatus);
    }

    public boolean getShowStartWizardWithNoInputs() {
        return chkShowStartWizardWithNoInputs.isSelected();
    }

    public void setShowStartWizardWithNoInputs(boolean newStatus) {
        chkShowStartWizardWithNoInputs.setSelected(newStatus);
    }

    public boolean getDisplayLogsInEditor() {
        return chkDisplayLogsInEditor.isSelected();
    }

    public void setDisplayLogsInEditor(boolean newStatus) {
        chkDisplayLogsInEditor.setSelected(newStatus);
    }

    private JComponent createText() {
        return createTelemetryComponent();
    }

    public JLabel createTelemetryComponent() {
        JLabel component = new JBLabel("") {
            @Override
            protected HyperlinkListener createHyperlinkListener() {
                return (event) -> {
                    if (HyperlinkEvent.EventType.ACTIVATED == event.getEventType()) openTelemetryPreferences();
                };
            }
        }
                .setCopyable(true)
                .setAllowAutoWrapping(true);
        component.setVerticalTextPosition(SwingConstants.TOP);
        component.setFocusable(false);
        component.setText(toStyledHtml(
                "<br/>Help Red Hat improve Tekton Pipelines by sending anonymous usage data."
                        + " You can enable or disable it in the <a href=\"\">preferences for Red Hat Telemetry</a>."
                , 70,
                component));
        return component;
    }

    private void openTelemetryPreferences() {
        Settings allSettings = Settings.KEY.getData(DataManager.getInstance().getDataContext(getPanel()));
        if (allSettings != null) {
            final Configurable configurable = allSettings.find(TelemetryConfigurable.ID);
            if (configurable != null) {
                allSettings.select(configurable);
            }
        }
    }
}
