package org.laba;

import org.laba.database.DatabaseInitializer;
import org.laba.gui.StartDialog;

import javax.swing.*;

public class App {
    public static void main(String[] args) {
        DatabaseInitializer.init();
        SwingUtilities.invokeLater(StartDialog::new);
    }
}