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
import com.intellij.ui.ColorUtil;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.JBUI;
import com.redhat.devtools.intellij.telemetry.ui.preferences.TelemetryConfigurable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

public class TektonTelemetryView {
    private static final String dataDescription =
            "Help Red Hat improve Tekton Pipelines by sending anonymous data."
                    + " You can enable or disable telemetry data in the <a href=\"\">preferences for Red Hat Telemetry</a>.";

    private final JPanel panel;

    public TektonTelemetryView() {
        panel = FormBuilder.createFormBuilder()
                .addComponent(createText(), 1)
                .addComponentFillVertically(new JPanel(), 0)
                .getPanel();
    }

    private JComponent createText() {
        return createCommentComponent(dataDescription, 70);
    }

    public JLabel createCommentComponent(@Nullable String commentText, int maxLineLength) {
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
        setComponentText(component, commentText, maxLineLength);
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

    private void setComponentText(JLabel component, String commentText, int maxLineLength) {
        String css = "<head><style type=\"text/css\">\n" +
                "a, a:link {color:#" + ColorUtil.toHex(JBUI.CurrentTheme.Link.linkColor()) + ";}\n" +
                "a:visited {color:#" + ColorUtil.toHex(JBUI.CurrentTheme.Link.linkVisitedColor()) + ";}\n" +
                "a:hover {color:#" + ColorUtil.toHex(JBUI.CurrentTheme.Link.linkHoverColor()) + ";}\n" +
                "a:active {color:#" + ColorUtil.toHex(JBUI.CurrentTheme.Link.linkPressedColor()) + ";}\n" +
                "</style>\n</head>";
        int width = component.getFontMetrics(component.getFont()).stringWidth(commentText.substring(0, maxLineLength));
        component.setText(String.format("<html>%s<body><div width=%d>%s</div></body></html>", css, width, commentText));
    }

    public JPanel getPanel() {
        return panel;
    }
}
