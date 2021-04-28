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
package com.redhat.devtools.intellij.tektoncd.ui;

import com.intellij.ui.Gray;
import com.intellij.ui.JBColor;
import com.intellij.util.ui.UIUtil;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;

public class UIConstants {
    public static final Color MAIN_BG_COLOR = JBColor.namedColor("Plugins.background", new JBColor(() -> JBColor.isBright() ? UIUtil.getListBackground() : new Color(0x313335)));
    public static final Color GRAY_COLOR = JBColor.namedColor("Label.infoForeground", new JBColor(Gray._120, Gray._135));
    public static final Color SEARCH_FIELD_BORDER_COLOR = JBColor.namedColor("Plugins.SearchField.borderColor", new JBColor(0xC5C5C5, 0x515151));
    public static final Color SEARCH_BG_COLOR = JBColor.namedColor("Plugins.SearchField.background", MAIN_BG_COLOR);
    public static final Color RED = new Color(229,77,4);
    public static final Color LIGHT_GREY = new Color(212,212,212);
    public static final Color BLUE = new Color(0, 102, 204);

    public static final Dimension ROW_DIMENSION_ERROR = new Dimension(400, 43);
    public static final Dimension ROW_DIMENSION = new Dimension(400, 33);

    public static final Font TIMES_PLAIN_10 = new Font(Font.DIALOG, Font.PLAIN, 10);
    public static final Font TIMES_PLAIN_12 = new Font(Font.DIALOG, Font.PLAIN, 12);
    public static final Font ROMAN_PLAIN_13 = new Font(Font.DIALOG, Font.PLAIN, 13);
    public static final Font TIMES_PLAIN_14 = new Font(Font.DIALOG, Font.PLAIN, 14);

    public static final Border BORDER_COMPONENT_VALUE = new MatteBorder(1, 1, 2, 1, LIGHT_GREY);
    public static final Border RED_BORDER_SHOW_ERROR = new MatteBorder(1, 1, 1, 1, RED);
    public static final Border BORDER_LABEL_NAME = new EmptyBorder(10, 0, 0, 0);
    public static final Border NO_BORDER = new EmptyBorder(0, 0, 0, 0);
    public static final Border MARGIN_10 = new EmptyBorder(10, 10, 10, 10);
    public static final Border MARGIN_BOTTOM_10 = new EmptyBorder(0, 0, 10, 0);
    public static final Border MARGIN_TOP_35 = new EmptyBorder(35, 0,0,0);

}
