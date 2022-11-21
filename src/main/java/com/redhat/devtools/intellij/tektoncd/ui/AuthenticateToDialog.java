/*******************************************************************************
 *  Copyright (c) 2022 Red Hat, Inc.
 *  Distributed under license by Red Hat, Inc. All rights reserved.
 *  This program is made available under the terms of the
 *  Eclipse Public License v2.0 which accompanies this distribution,
 *  and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 *  Contributors:
 *  Red Hat, Inc.
 ******************************************************************************/
package com.redhat.devtools.intellij.tektoncd.ui;

import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBRadioButton;
import com.redhat.devtools.intellij.tektoncd.tkn.Authenticator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionListener;

import static com.redhat.devtools.intellij.tektoncd.ui.UIConstants.ROW_DIMENSION;

public class AuthenticateToDialog extends DialogWrapper {
    private JBRadioButton userPswButton, tokenButton;
    private JTextField txtToken, txtUsername, txtPassword;
    private JCheckBox chkSkipTls;


    public AuthenticateToDialog(Component parent, String imageName) {
        super(null, parent, false, DialogWrapper.IdeModalityType.IDE);
        String registry = getRegistryFromImage(imageName);
        setTitle("Authenticate to " + registry);
        init();
    }

    private String getRegistryFromImage(String image) {
        int iSlash = image.indexOf("/");
        return image.substring(0, iSlash);
    }

    public static void main(String[] args) {
        AuthenticateToDialog dialog = new AuthenticateToDialog(null, "");
        dialog.pack();
        dialog.show();
        System.exit(0);
    }

    public Authenticator getAuthenticator() {
        if (!isOK()) {
            return null;
        }
        boolean isSkipTls = chkSkipTls.isSelected();
        if (tokenButton.isSelected()) {
            return new Authenticator(txtToken.getText(), isSkipTls);
        }
        return new Authenticator(txtUsername.getText(), txtPassword.getText(), isSkipTls);
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        final JPanel panel = new JPanel(new BorderLayout());
        panel.add(buildContainer(), BorderLayout.CENTER);
        panel.setMinimumSize(new Dimension(500, 150));
        return panel;
    }

    private void updateOKAction() {
        boolean isOkEnabled = (tokenButton.isSelected() && !txtToken.getText().isEmpty())
                                || (!txtUsername.getText().isEmpty() && !txtPassword.getText().isEmpty());
        setOKActionEnabled(isOkEnabled);
    }

    private JPanel buildContainer() {
        JPanel panel = new JPanel(new BorderLayout());

        CardLayout cl = new CardLayout();
        JPanel wrapper = new JPanel(cl);

        JPanel userPswPanel = new JPanel();
        userPswPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;

        addLblField("Username:  ", userPswPanel, c, 0);
        txtUsername = new JTextField("");
        addTxtField(txtUsername, userPswPanel, c, 0);

        addLblField("Password:  ", userPswPanel, c, 1);
        txtPassword = new JTextField("");
        addTxtField(txtPassword, userPswPanel, c, 1);

        wrapper.add(userPswPanel, "USER");

        JPanel tokenPanel = new JPanel();
        tokenPanel.setLayout(new GridBagLayout());

        addLblField("Token:  ", tokenPanel, c, 0);
        txtToken = new JTextField("");
        addTxtField(txtToken, tokenPanel, c, 0);

        wrapper.add(tokenPanel, "TKN");

        userPswButton = createRadioButton("Username/Password", e -> {
            if (!userPswPanel.isVisible()) {
                cl.show(wrapper, "USER");
            }
        });
        userPswButton.setSelected(true);
        tokenButton = createRadioButton("Token", e -> {
            if (!tokenPanel.isVisible()) {
                cl.show(wrapper, "TKN");
            }
        });

        ButtonGroup group = new ButtonGroup();
        group.add(userPswButton);
        group.add(tokenButton);

        JPanel header = new JPanel(new FlowLayout());
        header.add(userPswButton);
        header.add(tokenButton);

        chkSkipTls = new JBCheckBox("Skip TLS check when connecting to the registry");

        panel.add(header, BorderLayout.NORTH);
        panel.add(wrapper, BorderLayout.CENTER);
        panel.add(chkSkipTls, BorderLayout.SOUTH);
        return panel;
    }

    private JBRadioButton createRadioButton(String text, ActionListener listener) {
        JBRadioButton radioButton = new JBRadioButton(text);
        radioButton.addActionListener(listener);
        return radioButton;
    }

    private void addLblField(String text, JPanel panel, GridBagConstraints c, int gridY) {
        JLabel label = new JLabel(text);
        addComponent(label, panel, c, 0, gridY);
    }

    private void addTxtField(JTextField txtField, JPanel panel, GridBagConstraints c, int gridY) {
        txtField.setPreferredSize(ROW_DIMENSION);
        txtField.getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(@NotNull DocumentEvent e) {
                updateOKAction();
            }
        });
        addComponent(txtField, panel, c, 1, gridY);
    }

    private void addComponent(JComponent component, JPanel panel, GridBagConstraints c, int gridX, int gridY) {
        c.gridx = gridX;
        c.gridy = gridY;
        panel.add(component, c);
    }
}

