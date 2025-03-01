package dev.sweety.api;

import java.sql.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

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

    Executor executor();

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

    default <T> T execute(String query, StatementConsumer<T> function) {
        try (PreparedStatement statement = connection().prepareStatement(query)) {
            return function.accept(statement);
        } catch (SQLException e) {
            e.printStackTrace(System.err);
        }

        return null;
    }

    default <T> CompletableFuture<T> executeAsync(String query, StatementConsumer<T> function) {
        return CompletableFuture.supplyAsync(() -> execute(query, function), executor());
    }

    default ResultSet executeQuery(String query, Object... params) {
        try (PreparedStatement statement = connection().prepareStatement(query)) {
            setParameters(statement, params);

            return statement.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace(System.err);
        }

        return null;
    }

    default CompletableFuture<ResultSet> executeQueryAsync(String query, Object... params) {
        return CompletableFuture.supplyAsync(() -> executeQuery(query, params), executor());
    }

    default int executeUpdate(String query, Object... params) {
        try (PreparedStatement statement = connection().prepareStatement(query)) {
            setParameters(statement, params);

            return statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace(System.err);
        }

        return -1;
    }

    default CompletableFuture<Integer> executeUpdateAsync(String query, Object... params) {
        return CompletableFuture.supplyAsync(() -> executeUpdate(query, params), executor());
    }

    private void setParameters(PreparedStatement statement, Object[] params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            statement.setObject(i + 1, params[i]);
        }
    }

}