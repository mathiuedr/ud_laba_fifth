package org.laba.gui;

import org.laba.database.DatabaseManager;

import javax.swing.*;
import java.sql.SQLException;
import java.util.HashMap;

public class CreateUserDialog extends JDialog {
    private JPanel contentPane;
    private JButton button_create;
    private JButton button_back;
    private JTextField login_input;
    private JPasswordField password_input;
    private JCheckBox checkbox_search;
    private JCheckBox checkbox_insert;
    private JCheckBox checkbox_update;
    private JCheckBox checkbox_delete;
    private JCheckBox checkbox_create_db;
    private JCheckBox checkbox_delete_table;
    private JCheckBox checkbox_clear_table;
    private JCheckBox checkbox_create_user;
    private final DatabaseManager dbm;
    public CreateUserDialog(DatabaseManager dbm) {
        setContentPane(contentPane);
        getRootPane().setDefaultButton(button_create);
        setVisible(true);
        pack();
        button_create.addActionListener(e -> onCreate());
        this.dbm = dbm;
        button_back.addActionListener(e -> onBack());


        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

    }

    private void onCreate() {
        HashMap<String,Boolean> rights = new HashMap<>();
        rights.put("search_books_by_author_name", checkbox_search.isSelected());
        rights.put("insert_book",checkbox_insert.isSelected());
        rights.put("create_db",checkbox_create_db.isSelected());
        rights.put("drop_db",checkbox_delete_table.isSelected());
        rights.put("clear_table",checkbox_clear_table.isSelected());
        rights.put("update_book",checkbox_update.isSelected());
        rights.put("delete_book_by_name",checkbox_delete.isSelected());
        rights.put("create_user", checkbox_create_user.isSelected());
        rights.put("grant_user_on_function", checkbox_create_user.isSelected());
        rights.put("grant_user_on_procedure", checkbox_create_user.isSelected());
        try {
            dbm.create_user(login_input.getText(),new String(password_input.getPassword()),rights);
        }catch (SQLException e){
            JOptionPane.showMessageDialog(null, e.getMessage());
        }

        dispose();
    }

    private void onBack() {
        dispose();
    }
}
