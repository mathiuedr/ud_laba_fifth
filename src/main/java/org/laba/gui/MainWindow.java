package org.laba.gui;

import org.laba.database.DatabaseManager;
import org.laba.database.errors.WrongFieldException;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.function.Function;

public class MainWindow extends JFrame {
    private JPanel panel1;
    private JTabbedPane tabbedPane1;
    private JButton buttonExit;
    private JTextField search_input;
    private JButton button_search;
    private JTextArea search_result;
    private JTextField insert_input;
    private JButton button_insert;
    private JTextField update_id_input;
    private JTextField update_input;
    private JButton button_update;
    private JTextField delete_input;
    private JButton button_delete;
    private JButton button_drop_table;
    private JButton button_create_table;
    private JButton button_clear_table;
    private JButton button_create_account;
    private DatabaseManager dbm;

    public MainWindow() throws SQLException {
        super("База данных с гостевым доступом");
        dbm = new DatabaseManager();
        init();
    }

    public MainWindow(String username,String password) throws SQLException {
        super("База данных с доступом пользователя "+ username);
        dbm = new DatabaseManager(username,password);
        init();
    }
    private void init(){
        setSize(1000, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setVisible(true);
        add(panel1);
        buttonExit.addActionListener(e -> onExit());
        button_search.addActionListener(e->handleExceptions(this::onSearch));
        button_insert.addActionListener(e->handleExceptions(this::onInsert));
        button_update.addActionListener(e->handleExceptions(this::onUpdate));
        button_delete.addActionListener(e->handleExceptions(this::onDelete));
        button_create_table.addActionListener(e->handleExceptions(this::onDatabaseCreate));
        button_drop_table.addActionListener(e->handleExceptions(this::onDatabaseDelete));
        button_clear_table.addActionListener(e->handleExceptions(this::onDatabaseClear));
        button_create_account.addActionListener(e->onUserCreate());
    }
    private void onExit(){
        new StartDialog();
        dispose();
    }

    @FunctionalInterface
    public interface RunnableWithException {
        void run() throws Exception;
    }
    public static void handleExceptions(RunnableWithException runnable) {
        try {
            runnable.run();
            JOptionPane.showMessageDialog(null, "Успех!", "Успех", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }
    private void onSearch() throws WrongFieldException, SQLException {
        search_result.setText("id isbn name description date\n"+dbm.search_books_by_author_name(search_input.getText()));
    }
    private void onInsert() throws WrongFieldException, SQLException {
        String[] args = insert_input.getText().split(", ");
        dbm.insert_book(args[0],args[1],args[2],args[3],args[4]);
    }
    private void onUpdate() throws WrongFieldException, SQLException {
        String[] args = update_input.getText().split(", ");

        dbm.update_book_by_id(Integer.parseInt(update_id_input.getText()),args[0],args[1],args[2],args[3],args[4]);
    }
    private void onDelete() throws WrongFieldException, SQLException {
        dbm.delete_book_by_name(delete_input.getText());
    }
    private void onDatabaseDelete() throws SQLException {
        dbm.drop_db();
    }
    private void onDatabaseCreate() throws SQLException {
        dbm.create_db();
    }
    private void onDatabaseClear() throws SQLException {
        dbm.clear_db();
    }
    private void onUserCreate(){
        new CreateUserDialog(dbm);
    }


}
