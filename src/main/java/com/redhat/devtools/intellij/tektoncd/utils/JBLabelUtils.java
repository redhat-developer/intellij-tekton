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
 package com.redhat.devtools.intellij.tektoncd.utils;

import com.intellij.ui.ColorUtil;
import com.intellij.util.ui.JBUI;

import javax.swing.JLabel;

public class JBLabelUtils {

    /**
     * Turns the given text to html that a JBLabel can display (with links).
     *
     * @param text the text to convert to Html
     * @param maxLineLength the maximum number of characters in a line
     * @param the component to retrieve the font metrics from
     *
     * @returns the text
     */
    public static String toStyledHtml(String text, int maxLineLength, JLabel component) {
        String css = "<head><style type=\"text/css\">\n" +
                "a, a:link {color:#" + ColorUtil.toHex(JBUI.CurrentTheme.Link.linkColor()) + ";}\n" +
                "a:visited {color:#" + ColorUtil.toHex(JBUI.CurrentTheme.Link.linkVisitedColor()) + ";}\n" +
                "a:hover {color:#" + ColorUtil.toHex(JBUI.CurrentTheme.Link.linkHoverColor()) + ";}\n" +
                "a:active {color:#" + ColorUtil.toHex(JBUI.CurrentTheme.Link.linkPressedColor()) + ";}\n" +
                "</style>\n</head>";
        int width = component.getFontMetrics(component.getFont()).stringWidth(text.substring(0, maxLineLength));
        return String.format("<html>%s<body><div width=%d>%s</div></body></html>", css, width, text);
    }

}
