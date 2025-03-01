package dev.sweety.connection;

import dev.sweety.api.SQLConnection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.Executor;

import static dev.sweety.SqlUtils.EXECUTOR;

/**
 * @author mk$weety
 * MySQLConnection provides methods to connect to a MySQL database.
 */

public class MySQLConnection implements SQLConnection {
    private String host;
    private int port;
    private String database;
    private String user;
    private String password;
    private Connection connection;

    /**
     * Constructs a new MySQLConnection.
     *
     * @param host     the database host
     * @param port     the database port
     * @param database the database name
     * @param user     the database user
     * @param password the database password
     */
    public MySQLConnection(String host, int port, String database, String user, String password) {
        this.host = host;
        this.port = port;
        this.database = database;
        this.user = user;
        this.password = password;
    }

    /**
     * Connects to the MySQL database.
     *
     * @return the database connection
     * @throws SQLException if a database access error occurs
     */
    @Override
    public Connection connect() throws SQLException {
        return this.connection = DriverManager.getConnection(url(), user, password);
    }

    /**
     * Returns the current database connection, connecting if necessary.
     *
     * @return the database connection
     * @throws SQLException if a database access error occurs
     */
    @Override
    public Connection connection() throws SQLException {
        if (connection == null || connection.isClosed()) return connect();
        return connection;
    }

    /**
     * Returns the connection URL for MySQL.
     *
     * @return the connection URL
     */
    @Override
    public String url() {
        return "jdbc:mysql://" + host + ":" + port + "/" + database;
    }

    @Override
    public Executor executor() {
        return EXECUTOR;
    }

    @Override
    public void close() throws SQLException {
        if (connection != null && !connection.isClosed()) connection.close();
    }

    @Override
    public String database() {
        return database;
    }

    @Override
    public void database(String database) {
        this.database = database;
    }

    public String host() {
        return host;
    }

    public int port() {
        return port;
    }

    public String user() {
        return user;
    }

    public String password() {
        return password;
    }

    public void host(String host) {
        this.host = host;
    }

    public void port(int port) {
        this.port = port;
    }

    public void user(String user) {
        this.user = user;
    }

    public void password(String password) {
        this.password = password;
    }
}
