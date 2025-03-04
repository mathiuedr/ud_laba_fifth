package org.laba.gui;

import javax.swing.*;
import java.sql.SQLException;

public class StartDialog extends JFrame {
    private JPanel contentPane;
    private JButton buttonGuest;
    private JButton buttonAccount;

    public StartDialog() {
        super("Вход в базу данных");
        setContentPane(contentPane);
        setSize(300,100);
        setResizable(false);
        getRootPane().setDefaultButton(buttonGuest);
        setVisible(true);
        buttonGuest.addActionListener(e -> onGuest());

        buttonAccount.addActionListener(e -> onAccount());

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

    }

    private void onGuest() {
        try {
            new MainWindow();
            dispose();
        }
        catch (SQLException e){
            JOptionPane.showMessageDialog(null, e.getMessage());
        }

        // add your code here

    }

    private void onAccount() {
        // add your code here if necessary
        new LoginDialog();
        dispose();
    }


}
