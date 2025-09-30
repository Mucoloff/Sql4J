package dev.sweety.sql4j.impl.connection;

import dev.sweety.sql4j.api.connection.SQLConnection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.Executor;

import static dev.sweety.sql4j.api.SqlUtils.EXECUTOR;

public class SQLiteConnection implements SQLConnection {
    private String database;
    private Connection connection;

    public SQLiteConnection(String database) {
        this.database = database;
    }

    @Override
    public Connection connect() throws SQLException {
        return this.connection = DriverManager.getConnection(url());
    }

    @Override
    public Connection connection() throws SQLException {
        if (connection == null || connection.isClosed()) return connect();
        return connection;
    }

    @Override
    public void close() throws SQLException {
        if (connection != null && !connection.isClosed()) connection.close();
    }

    @Override
    public Executor executor() {
        return EXECUTOR;
    }

    @Override
    public String url() {
        return "jdbc:sqlite:" + (this.database) + ".db";
    }

    @Override
    public String database() {
        return database;
    }

    @Override
    public void database(String database) {
        this.database = database;
    }
}