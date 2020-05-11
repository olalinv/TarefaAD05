package com.robottitto.util;

import com.robottitto.model.Archive;
import com.robottitto.model.DbConnection;
import com.robottitto.model.Directory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Properties;

public class PostgreSQLUtils {

    private static final String JDBC_POSTGRE_SQL = "jdbc:postgresql://";
    private static final String TABLE_DIRECTORY = "directory";
    private static final String TABLE_ARCHIVE = "archive";
    private static final String EVENT_NAME = "new_archive";
    private static final String FUNCTION_NAME = "notify_new_archive";
    private static final String TRIGGER_NAME = "not_new_archive";

    private static Connection connection = null;
    private static Properties properties = new Properties();

    public static void connect(final DbConnection dbConnection) throws SQLException {
        properties.setProperty("user", dbConnection.getUser());
        properties.setProperty("password", dbConnection.getPassword());
        connection = DriverManager.getConnection(JDBC_POSTGRE_SQL + dbConnection.getAddress() + "/" + dbConnection.getName(), properties);
        createSchema();
    }

    private static void createSchema() throws SQLException {
        // Tables
        executeStatement("CREATE TABLE IF NOT EXISTS " + TABLE_DIRECTORY + " (id SERIAL PRIMARY KEY, name TEXT NOT NULL);");
        executeStatement("CREATE TABLE IF NOT EXISTS " + TABLE_ARCHIVE + " (id SERIAL PRIMARY KEY, name TEXT NOT NULL, directoryId INTEGER REFERENCES " + TABLE_DIRECTORY + "(id), binaryFile BYTEA NOT NULL);");
        // Functions
        executeStatement("CREATE OR REPLACE FUNCTION " + FUNCTION_NAME + "() RETURNS trigger AS $$ BEGIN PERFORM pg_notify('" + EVENT_NAME + "', NEW.id::text); RETURN NEW; END; $$ LANGUAGE plpgsql;");
        // Triggers
        executeStatement("DROP TRIGGER IF EXISTS " + TRIGGER_NAME + " ON " + TABLE_ARCHIVE + "; CREATE TRIGGER " + TRIGGER_NAME + " AFTER INSERT ON " + TABLE_ARCHIVE + " FOR EACH ROW EXECUTE PROCEDURE " + FUNCTION_NAME + "();");
    }

    private static void executeStatement(String sql) throws SQLException {
        CallableStatement statement = connection.prepareCall(sql);
        statement.execute();
        statement.close();
    }

    public static Connection getConnection() {
        return connection;
    }

    public static void setConnection(Connection connection) {
        PostgreSQLUtils.connection = connection;
    }

    public static ArrayList<Directory> getDirectories() throws SQLException {
        ArrayList<Directory> directories = new ArrayList<>();
        PreparedStatement statement = connection.prepareStatement("SELECT * FROM " + TABLE_DIRECTORY);
        ResultSet rs = statement.executeQuery();
        while (rs.next()) {
            Directory directory = new Directory(rs.getInt("id"), rs.getString("name"));
            directories.add(directory);
        }
        rs.close();
        statement.close();
        return directories;
    }

    public static ArrayList<Archive> getArchives() throws SQLException {
        ArrayList<Archive> archives = new ArrayList<>();
        PreparedStatement statement = connection.prepareStatement("SELECT * FROM " + TABLE_ARCHIVE);
        ResultSet rs = statement.executeQuery();
        while (rs.next()) {
            Archive archive = new Archive(rs.getInt("id"), rs.getString("name"), rs.getInt("directoryId"));
            archives.add(archive);
        }
        rs.close();
        statement.close();
        return archives;
    }

    public static boolean isArchiveInserted(String fileName, int directoryId, File resource) throws SQLException {
        boolean isInserted = false;
        String sql = "SELECT name, directoryId from " + TABLE_ARCHIVE;
        PreparedStatement statement = connection.prepareStatement(sql);
        ResultSet rs = statement.executeQuery();
        while (rs.next()) {
            if (fileName.equals(rs.getString(1)) && directoryId == rs.getInt(2)) {
                isInserted = true;
            }

        }
        rs.close();
        statement.close();
        return isInserted;
    }

    public static boolean isDirectoryInserted(String name) throws SQLException {
        boolean isInserted = false;
        PreparedStatement statement = connection.prepareStatement("SELECT name FROM " + TABLE_DIRECTORY);
        ResultSet rs = statement.executeQuery();
        while (rs.next()) {
            if (rs.getString("name").equals(name)) {
                isInserted = true;
            }
        }
        rs.close();
        statement.close();
        return isInserted;
    }

    public static void insertArchive(String fileName, int directoryId, File resource) throws SQLException, IOException {
        String sql = "INSERT INTO " + TABLE_ARCHIVE + "(name, directoryId, binaryFile) VALUES (?,?,?);";
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setString(1, fileName);
        statement.setInt(2, directoryId);
        FileInputStream fileInputStream = new FileInputStream(resource);
        statement.setBinaryStream(3, fileInputStream, (int) resource.length());
        statement.executeUpdate();
        statement.close();
        fileInputStream.close();
    }

    public static void insertDirectory(String directoryName) throws SQLException {
        String sql = "INSERT INTO " + TABLE_DIRECTORY + "(name) VALUES (?);";
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setString(1, directoryName);
        statement.executeUpdate();
        statement.close();
    }

    public static int getDirectoryId(String directoryName) throws SQLException {
        int id = 0;
        PreparedStatement statement = connection.prepareStatement("SELECT id FROM " + TABLE_DIRECTORY + " WHERE name = ?");
        statement.setString(1, directoryName);
        ResultSet rs = statement.executeQuery();
        if (rs.next()) {
            id = rs.getInt("id");
        }
        return id;
    }

    public static String getDirectoryName(int directoryId) throws SQLException {
        String name = "";
        PreparedStatement statement = connection.prepareStatement("SELECT name FROM " + TABLE_DIRECTORY + " WHERE id = ?");
        statement.setInt(1, directoryId);
        ResultSet rs = statement.executeQuery();
        if (rs.next()) {
            name = rs.getString("name");
        }
        return name;
    }

    public static byte[] getArchiveBinaryFile(int archiveId) throws SQLException {
        byte[] binaryFile = null;
        PreparedStatement statement = connection.prepareStatement("SELECT binaryFile FROM " + TABLE_ARCHIVE + " WHERE id = ?");
        statement.setInt(1, archiveId);
        ResultSet rs = statement.executeQuery();
        if (rs.next()) {
            binaryFile = rs.getBytes("binaryFile");
        }
        return binaryFile;
    }

    public static void listen(String eventName) throws SQLException {
        Statement statement = connection.createStatement();
        statement.execute("LISTEN " + eventName);
        statement.close();
    }

}
