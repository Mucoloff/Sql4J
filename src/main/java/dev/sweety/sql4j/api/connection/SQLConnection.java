package dev.sweety.sql4j.api.connection;

import dev.sweety.sql4j.api.SqlUtils;

import java.sql.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public interface SQLConnection {

    Connection connect() throws SQLException;

    Connection connection() throws SQLException;

    String url();

    Executor executor();

    String database();

    void database(String database);

    void close() throws SQLException;

    default <T> T execute(String query, StatementConsumer<T> function) {
        try (Connection connection = connection(); PreparedStatement statement = connection.prepareStatement(query)) {
            if (SqlUtils.DEBUG) System.out.println("query: " + query);
            return function.accept(statement);
        } catch (SQLException e) {
            SqlUtils.logger.log(System.Logger.Level.ERROR, "query: " + query);
            e.printStackTrace(System.err);
        }

        return null;
    }

    default <T> CompletableFuture<T> executeAsync(String query, StatementConsumer<T> function) {
        return CompletableFuture.supplyAsync(() -> execute(query, function), executor());
    }

    default ResultSet executeQuery(String query, Object... params) {
        try (Connection connection = connection(); PreparedStatement statement = connection.prepareStatement(query)) {
            if (SqlUtils.DEBUG) System.out.println("query: " + query);
            setParameters(statement, params);

            return statement.executeQuery();
        } catch (SQLException e) {
            SqlUtils.logger.log(System.Logger.Level.ERROR, "query: " + query + " params:" + Arrays.toString(params));
            e.printStackTrace(System.err);
        }

        return null;
    }

    default CompletableFuture<ResultSet> executeQueryAsync(String query, Object... params) {
        return CompletableFuture.supplyAsync(() -> executeQuery(query, params), executor());
    }

    default int executeUpdate(String query, Object... params) {
        try (Connection connection = connection(); PreparedStatement statement = connection.prepareStatement(query)) {
            if (SqlUtils.DEBUG) System.out.println("query: " + query);
            setParameters(statement, params);

            return statement.executeUpdate();
        } catch (SQLException e) {
            SqlUtils.logger.log(System.Logger.Level.ERROR, "query: " + query + " params:" + Arrays.toString(params));
            e.printStackTrace(System.err);
        }

        return -1;
    }

    default CompletableFuture<Integer> executeUpdateAsync(String query, Object... params) {
        return CompletableFuture.supplyAsync(() -> executeUpdate(query, params), executor());
    }

    default <T> T update(String query, StatementConsumer<T> function, Object... params) {
        try (Connection connection = connection(); PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            if (SqlUtils.DEBUG) System.out.println("query: " + query);

            setParameters(statement, params);
            statement.executeUpdate();

            return function.accept(statement);
        } catch (SQLException e) {
            SqlUtils.logger.log(System.Logger.Level.ERROR, "query: " + query);
            e.printStackTrace(System.err);
        }

        return null;
    }

    default <T> CompletableFuture<T> updateAsync(String query, StatementConsumer<T> function, Object... params) {
        return CompletableFuture.supplyAsync(() -> update(query, function, params), executor());
    }

    default void setParameters(PreparedStatement statement, Collection<Object> params) throws SQLException {
        int i = 1;
        for (Object param : params) {
            statement.setObject(i++, param);
        }
    }

    default void setParameters(PreparedStatement statement, Object[] params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            statement.setObject(i + 1, params[i]);
        }
    }

    default CompletableFuture<Void> executeAsync(String query) {
        return CompletableFuture.runAsync(() -> execute(query));
    }

    default void execute(String query) {
        try (Connection connection = connection(); Statement statement = connection.createStatement()) {
            if (SqlUtils.DEBUG) System.out.println("query: " + query);
            statement.execute(query);
        } catch (SQLException e) {

            SqlUtils.logger.log(System.Logger.Level.ERROR, "query: " + query);
            e.printStackTrace(System.err);
        }
    }
}