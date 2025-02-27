package dev.sweety.connection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * @author mk$weety
 * SQLiteConnection provides methods to connect to a SQLite database.
 */
public class SQLiteConnection implements SQLConnection {
    private String database;
    private Connection connection;

    /**
     * Constructs a new SQLiteConnection.
     *
     * @param database the database name
     */
    public SQLiteConnection(String database) {
        this.database = database;
    }

    /**
     * Connects to the SQLite database.
     *
     * @return the database connection
     * @throws SQLException if a database access error occurs
     */
    @Override
    public Connection connect() throws SQLException {
        return this.connection = DriverManager.getConnection(url());
    }

    /**
     * Returns the current database connection, connecting if necessary.
     *
     * @return the database connection
     * @throws SQLException if a database access error occurs
     */
    @Override
    public Connection connection() throws SQLException {
        if (connection == null) return connect();
        return connection;
    }

    /**
     * Returns the connection URL for SQLite.
     *
     * @return the connection URL
     */
    @Override
    public String url() {
        return "jdbc:sqlite:" + (this.database) + ".db";
    }

    /**
     * Returns the database name.
     *
     * @return the database name
     */
    @Override
    public String database() {
        return database;
    }

    /**
     * Sets the database name.
     *
     * @param database the database name
     */
    @Override
    public void database(String database) {
        this.database = database;
    }
}