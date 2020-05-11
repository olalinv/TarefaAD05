package com.robottitto.util;

import com.robottitto.Main;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NewFileChecker extends Thread {

    private Connection connection;

    public NewFileChecker(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void run() {
        try {
            while (true) {
                System.out.println("Comprobando se hai novos arquivos ou directorios...");
                Main.syncAppDirectoryToDb(Main.appDirectory, Main.root);
                Thread.sleep(2500);
            }
        } catch (InterruptedException | SQLException | IOException ex) {
            Logger.getLogger(NewFileChecker.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
