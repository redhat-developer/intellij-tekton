package com.redhat.devtools.intellij.tektoncd.ui.component;

import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
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
        selectLabel.setText("Select " + kind);
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
        // listener for when value in resources input combo box changes
        resourcesCB.addItemListener(itemEvent -> {
            if (itemEvent.getStateChange() == 1) {
                selected = itemEvent.getItem().toString();
            }
        });
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return contentPane;
    }
}
