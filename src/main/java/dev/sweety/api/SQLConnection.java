package dev.sweety.api;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import static dev.sweety.Settings.DEBUG;
import static dev.sweety.Settings.SHOW_ERROR_QUERIES;

/**
 * @author mk$weety
 * SQLConnection is an interface that defines methods for connecting to a SQL database and executing queries.
 */
public interface SQLConnection {

    /**
     * Connects to the SQL database.
     *
     * @return the database connection
     * @throws SQLException if a database access error occurs
     */
    Connection connect() throws SQLException;

    /**
     * Returns the current database connection, connecting if necessary.
     *
     * @return the database connection
     * @throws SQLException if a database access error occurs
     */
    Connection connection() throws SQLException;

    /**
     * Returns the connection URL.
     *
     * @return the connection URL
     */
    String url();

    /**
     * Returns the database name.
     *
     * @return the database name
     */
    String database();

    /**
     * Sets the database name.
     *
     * @param database the database name
     */
    void database(String database);

    /**
     * Closes the database connection.
     *
     * @throws SQLException if a database access error occurs
     */
    void close() throws SQLException;

    /**
     * Executes a SQL query.
     *
     * @param sql the SQL query to execute
     */
    default void execute(final String sql, Object... params) {
        try (Connection connection = connection(); PreparedStatement statement = connection.prepareStatement(sql)) {

            for (int i = 0; i < params.length; i++) {
                statement.setObject(i + 1, params[i]);
            }

            statement.execute();

            if (DEBUG) System.out.println("query: " + sql);
        } catch (Exception e) {
            if (SHOW_ERROR_QUERIES || DEBUG) System.err.println("query: " + sql);
            e.printStackTrace();
        }
    }

}