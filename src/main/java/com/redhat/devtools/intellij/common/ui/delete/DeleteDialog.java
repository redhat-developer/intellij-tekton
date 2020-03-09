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
