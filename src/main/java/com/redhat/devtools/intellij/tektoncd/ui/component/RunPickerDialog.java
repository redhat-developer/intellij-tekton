package com.redhat.devtools.intellij.tektoncd.ui.component;

import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class RunPickerDialog extends DialogWrapper {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JLabel selectLabel;
    private JComboBox resourcesCB;
    private String selected;

    public RunPickerDialog(Component parent, String kind, List<String> resourceRuns) {
        super(null, parent, false, IdeModalityType.IDE);
        selectLabel.setText("Select " + kind + ":");
        setTitle("Show Logs");
        init();
        for (String runName: resourceRuns) {
            resourcesCB.addItem(runName);
        }
        registerListeners();

    }

    public static void main(String[] args) {
        RunPickerDialog dialog = new RunPickerDialog(null, "", null);
        dialog.pack();
        dialog.show();
        System.exit(0);
    }

    public String getSelected() {
        return selected;
    }

    private void registerListeners() {
        buttonOK.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                selected = resourcesCB.getSelectedItem().toString();
                RunPickerDialog.super.close(0, true);
            }
        });

        buttonCancel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                RunPickerDialog.super.close(1, false);
            }
        });
    }

    @NotNull
    @Override
    protected Action[] createActions() {
        return new Action[]{};
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return contentPane;
    }
}
