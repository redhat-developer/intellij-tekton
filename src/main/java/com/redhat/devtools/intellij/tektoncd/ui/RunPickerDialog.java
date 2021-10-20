package com.redhat.devtools.intellij.tektoncd.ui;

import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.Nullable;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.Component;
import java.util.List;

public class RunPickerDialog extends DialogWrapper {
    private JPanel contentPane;
    private JLabel selectLabel;
    private JComboBox resourcesCB;
    private String selected;

    public RunPickerDialog(Component parent, String action, String kind, List<String> resourceRuns) {
        super(null, parent, false, IdeModalityType.IDE);
        selectLabel.setText("Select " + kind + ":");
        setTitle(action);
        setOKButtonText(action);
        init();
        for (String runName: resourceRuns) {
            resourcesCB.addItem(runName);
        }
    }

    public static void main(String[] args) {
        RunPickerDialog dialog = new RunPickerDialog(null, "", "", null);
        dialog.pack();
        dialog.show();
        System.exit(0);
    }

    @Override
    protected void doOKAction() {
        selected = resourcesCB.getSelectedItem().toString();
        super.doOKAction();
    }

    public String getSelected() {
        return selected;
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return contentPane;
    }
}
