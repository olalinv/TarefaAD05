package com.robottitto.model;

public class Configuration {
    private DbConnection dbConnection;
    private App app;

    public Configuration(DbConnection dbConnection, App app) {
        this.dbConnection = dbConnection;
        this.app = app;
    }

    public DbConnection getDbConnection() {
        return dbConnection;
    }

    public void setDbConnection(DbConnection dbConnection) {
        this.dbConnection = dbConnection;
    }

    public App getApp() {
        return app;
    }

    public void setApp(App app) {
        this.app = app;
    }
}
