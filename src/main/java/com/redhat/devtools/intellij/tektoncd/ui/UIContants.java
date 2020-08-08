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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;

public class UIContants {
    public static final Color RED = new Color(229,77,4);
    public static final Color LIGHT_GREY = new Color(212,212,212);


    public static final Dimension ROW_DIMENSION = new Dimension(400, 33);
    public static final Border BORDER_LABEL_NAME = new EmptyBorder(10, 0, 0, 0);
    public static final Font FONT_COMPONENT_VALUE = new Font("TimesRoman", Font.PLAIN, 14);
    public static final Border BORDER_COMPONENT_VALUE = new MatteBorder(1, 1, 2, 1, LIGHT_GREY);
    public static final Border RED_BORDER_SHOW_ERROR = new MatteBorder(1, 1, 2, 1, RED);
    public static final Border NO_BORDER = new EmptyBorder(0, 0, 0, 0);

}
