package org.laba.gui;

import javax.swing.*;
import java.sql.SQLException;

public class LoginDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonLogin;
    private JTextField login_field;
    private JPasswordField password_field;
    private JButton buttonBack;

    public LoginDialog() {
        setContentPane(contentPane);
        pack();
        getRootPane().setDefaultButton(buttonLogin);
        setResizable(false);
        buttonLogin.addActionListener(e -> onLogin());
        buttonBack.addActionListener(e -> onBack());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setVisible(true);
    }

    private void onBack() {
        new StartDialog();
        dispose();
    }

    private void onLogin() {
        try {
            new MainWindow(login_field.getText(),new String(password_field.getPassword()));
            dispose();
        }catch (SQLException e){
            JOptionPane.showMessageDialog(null, "Ошибка входа: неправильный логин или пароль");
        }

    }
}
