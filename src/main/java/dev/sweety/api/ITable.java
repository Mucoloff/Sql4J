package dev.sweety.api;

import dev.sweety.exception.SqlError;
import dev.sweety.fields.SqlField;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static dev.sweety.Settings.DEBUG;

public interface ITable<T> {

    Connection connection() throws SQLException;

    void execute(String sql);

    String name();

    Class<T> clazz();

    SqlField primaryKey();

    List<SqlField> sqlFields();

    default void insert(T obj) throws SqlError {
        StringBuilder query = new StringBuilder("INSERT INTO ");
        query.append(name()).append("(");

        for (SqlField field : sqlFields()) {
            if (field.autoIncrement()) continue;
            query.append(field.name()).append(", ");
        }

        query.setLength(query.length() - 2);
        query.append(") VALUES (");

        for (int i = 0; i < sqlFields().size(); i++) {
            SqlField field = sqlFields().get(i);
            if (field.autoIncrement()) continue;
            query.append("?, ");
        }

        query.setLength(query.length() - 2);
        query.append(");");

        try (Connection connection = connection(); PreparedStatement statement = connection.prepareStatement(query.toString(), Statement.RETURN_GENERATED_KEYS)) {

            int paramIndex = 1;
            for (SqlField field : sqlFields()) {
                if (field.autoIncrement()) continue;
                statement.setObject(paramIndex++, field.get(obj));
            }

            statement.executeUpdate();

            if (DEBUG) System.out.println("query: " + query);

            try (var rs = statement.getGeneratedKeys()) {
                if (rs.next()) {
                    Object generatedKey = rs.getObject(1);
                    for (SqlField field : sqlFields()) {
                        if (field.hasPrimaryKey()) {
                            field.accessible();
                            field.field().set(obj, generatedKey);
                            break;
                        }
                    }
                }
            }

        } catch (Exception e) {
            throw new SqlError(query.toString(), e);
        }
    }


    default List<T> selectAll() throws SqlError {
        return select("SELECT * FROM " + name());
    }

    default List<T> selectWhere(String filterQuery) {
        return select("SELECT * FROM " + name() + " WHERE " + filterQuery);
    }

    default List<T> select(Predicate<T>... predicates) {
        List<T> resultList = selectAll();

        Predicate<T> combinedPredicate = Arrays.stream(predicates).reduce(x -> true, Predicate::and);

        return resultList.stream()
                .filter(combinedPredicate)
                .toList();
    }

    default List<T> select(String query) throws SqlError {
        List<T> resultList = new ArrayList<>();

        try (Statement statement = connection().createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            while (resultSet.next()) {
                T obj = clazz().getDeclaredConstructor().newInstance();
                for (SqlField field : sqlFields()) {
                    field.set(obj, resultSet.getObject(field.name()));
                }
                resultList.add(obj);
            }

            if (DEBUG) System.out.println("query: " + query);
        } catch (Exception e) {
            throw new SqlError(query, e);
        }

        return resultList;
    }

    default void delete(T obj) throws SqlError {
        StringBuilder query = new StringBuilder("DELETE FROM ");
        query.append(name()).append(" WHERE ");

        Object primaryKeyValue = null;

        for (SqlField field : sqlFields()) {
            if (field.hasPrimaryKey()) {
                query.append(field.name()).append(" = ?");
                primaryKeyValue = field.get(obj);
                break;
            }
        }

        if (primaryKeyValue == null) throw new IllegalArgumentException("No primary key found for object.");

        try (Connection connection = connection();
             PreparedStatement statement = connection.prepareStatement(query.toString())) {

            statement.setObject(1, primaryKeyValue);
            statement.executeUpdate();

            if (DEBUG) System.out.println("query: " + query);
        } catch (Exception e) {
            throw new SqlError(query.toString(), e);
        }

    }

    default void deleteWhere(String filterQuery, Object... filterParams) throws SqlError {
        String query = "DELETE FROM " + name() + " WHERE " + filterQuery;

        try (Connection connection = connection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            for (int i = 0; i < filterParams.length; i++) {
                statement.setObject(i + 1, filterParams[i]);
            }

            statement.executeUpdate();

            if (DEBUG) System.out.println("query: " + query);
        } catch (Exception e) {
            throw new SqlError(query, e);
        }
    }

    default void update(T obj) throws SqlError {
        StringBuilder query = new StringBuilder("UPDATE ");
        query.append(name()).append(" SET ");

        Object primaryKeyValue = null;
        List<Object> fieldValues = new ArrayList<>();

        for (SqlField field : sqlFields()) {
            Object value = field.get(obj);
            if (field.hasPrimaryKey()) {
                primaryKeyValue = value;
            } else {
                query.append(field.name()).append(" = ?, ");
                fieldValues.add(value);
            }
        }

        query.setLength(query.length() - 2);
        query.append(" WHERE ");

        for (SqlField field : sqlFields()) {
            if (field.hasPrimaryKey()) {
                query.append(field.name()).append(" = ?");
                break;
            }
        }

        if (primaryKeyValue != null) {
            try (Connection connection = connection();
                 PreparedStatement statement = connection.prepareStatement(query.toString())) {

                for (int i = 0; i < fieldValues.size(); i++) {
                    statement.setObject(i + 1, fieldValues.get(i));
                }

                statement.setObject(fieldValues.size() + 1, primaryKeyValue);

                statement.executeUpdate();

                if (DEBUG) System.out.println("query: " + query);
            } catch (Exception e) {
                throw new SqlError(query.toString(), e);
            }
        } else {
            throw new IllegalArgumentException("No primary key found for the object.");
        }
    }

    default void drop() {
        execute("DROP TABLE " + name());
    }

    default void print() throws SqlError {
        StringBuilder text = new StringBuilder();
        text.append("Table: ").append(name()).append("\n");

        for (SqlField field : sqlFields()) {
            text.append("| ").append(field.name()).append(" ");
        }
        text.append("|\n");

        for (T record : selectAll()) {
            for (SqlField field : sqlFields()) {
                text.append("| ").append(field.get(record)).append(" ");
            }
            text.append("|\n");
        }

        System.out.println(text);
    }

    default CompletableFuture<Void> insertAsync(T obj) {
        //todo fix
        return CompletableFuture.runAsync(() -> insert(obj));
    }

    default CompletableFuture<List<T>> selectAllAsync() throws SqlError {
        return selectAsync("SELECT * FROM " + name());
    }

    default CompletableFuture<List<T>> selectWhereAsync(String filterQuery) {
        return selectAsync("SELECT * FROM " + name() + " WHERE " + filterQuery);
    }

    default CompletableFuture<List<T>> selectAsync(String query) {
        return CompletableFuture.supplyAsync(() -> select(query));
    }

    default CompletableFuture<Void> deleteAsync(T obj) {
        return CompletableFuture.runAsync(() -> delete(obj));
    }

    default CompletableFuture<Void> deleteWhereAsync(String filterQuery, Object... filterParams) {
        return CompletableFuture.runAsync(() -> deleteWhere(filterQuery, filterParams));
    }

    default CompletableFuture<Void> updateAsync(T obj) {
        return CompletableFuture.runAsync(() -> update(obj));
    }

    default CompletableFuture<Void> dropAsync() {
        return CompletableFuture.runAsync(this::drop);
    }

    default CompletableFuture<Void> executeAsync(String sql) {
        return CompletableFuture.runAsync(() -> execute(sql));
    }

    default CompletableFuture<Void> printAsync() {
        AtomicReference<String> result = new AtomicReference<>();
        CompletableFuture<Void> future = selectAllAsync().thenAcceptAsync(list -> {
            StringBuilder text = new StringBuilder();
            text.append("Table: ").append(name()).append("\n");

            for (SqlField field : sqlFields()) {
                text.append("| ").append(field.name()).append(" ");
            }
            text.append("|\n");

            for (T record : list) {
                for (SqlField field : sqlFields()) {
                    text.append("| ").append(field.get(record)).append(" ");
                }
                text.append("|\n");
            }

            result.set(text.toString());

        }).thenRun(() -> System.out.println(result.get()));

        future.join();

        return future;
    }

}
