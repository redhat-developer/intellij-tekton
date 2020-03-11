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
package com.redhat.devtools.intellij.common.ui.delete;

import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class DeleteDialog extends DialogWrapper {
    private JPanel contentPane;
    private JLabel bodyDialog;

    public DeleteDialog(Component parent, String title, String message) {
        super(null, parent, false, IdeModalityType.IDE);
        init();
        setTitle(title);
        bodyDialog.setText(message);
    }

    public static void main(String[] args) {
        DeleteDialog dialog = new DeleteDialog(null, "", "");
        dialog.show();
        System.exit(0);
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return contentPane;
    }
}
