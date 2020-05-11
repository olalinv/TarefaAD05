package com.robottitto.util;

import com.robottitto.Main;
import org.postgresql.PGConnection;
import org.postgresql.PGNotification;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class NewFileListener extends Thread {

    private Connection connection;
    private PGConnection pgConnection;

    public NewFileListener(Connection connection) throws SQLException {
        this.connection = connection;
        pgConnection = connection.unwrap(PGConnection.class);
        PostgreSQLUtils.listen("new_archive");
        System.out.println("Escoitando notificacións de novos arquivos...");
    }

    @Override
    public void run() {
        try {
            PreparedStatement statement = connection.prepareStatement("SELECT name, directoryId, binaryFile FROM archive WHERE id=?;");
            while (true) {
                PGNotification[] notifications = pgConnection.getNotifications();
                if (notifications != null) {
                    for (int i = 0; i < notifications.length; i++) {
                        int id = Integer.parseInt(notifications[i].getParameter());
                        statement.setInt(1, id);
                        ResultSet rs = statement.executeQuery();
                        rs.next();
                        System.out.println("Recibiuse a notificación do novo arquivo " + rs.getString(1));
                        Main.syncDbToAppDirectory(Main.appDirectory, Main.root);
                        rs.close();
                    }
                }
                Thread.sleep(2500);
            }
        } catch (SQLException | InterruptedException | IOException e) {
            e.printStackTrace();
        }

    }

}
