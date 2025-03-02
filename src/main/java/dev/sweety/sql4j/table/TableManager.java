package dev.sweety.sql4j.table;

import dev.sweety.api.sql4j.SqlUtils;
import dev.sweety.api.sql4j.connection.SQLConnection;
import dev.sweety.api.sql4j.table.Info;
import dev.sweety.sql4j.fields.SqlField;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class TableManager {

    private static final Map<Class<?>, Table<?>> tables = new HashMap<>();

    public static <T> Optional<Table<T>> get(Class<T> clazz) {
        // noinspection unchecked
        return Optional.ofNullable((Table<T>) tables.get(clazz));
    }

    public static <T> CompletableFuture<Table<T>> createAsync(Class<T> clazz, SQLConnection connection) {
        return CompletableFuture.supplyAsync(() -> create(clazz, connection),connection.executor());
    }

    public static <T> Table<T> create(Class<T> clazz, SQLConnection connection) {
        Optional<Table<T>> optional = get(clazz);
        if (optional.isPresent()) return optional.get();

        Info info = clazz.getAnnotation(Info.class);
        String name = info != null ? info.name() : clazz.getSimpleName();

        StringBuilder queryBuilder = new StringBuilder("CREATE TABLE IF NOT EXISTS ");
        queryBuilder.append(name).append("(");

        Field[] fields = clazz.getDeclaredFields();

        List<SqlField> sqlFields = new ArrayList<>();
        SqlField primaryKey = null;

        for (Field field : fields) {
            if (!SqlUtils.isSqlField(field)) continue;

            SqlField sqlField = SqlField.sqlField(field, connection);
            sqlFields.add(sqlField);

            if (sqlField.hasPrimaryKey())
                primaryKey = sqlField;

            queryBuilder.append(sqlField.query()).append(", ");
        }

        queryBuilder.setLength(queryBuilder.length() - 2);

        queryBuilder.append(");");

        Table<T> table = new Table<>(name, clazz, connection, primaryKey, sqlFields);
        tables.put(clazz, table);

        String query = queryBuilder.toString();
        connection.execute(query);
        return table;
    }

    public static <T> void drop(Class<T> clazz, SQLConnection connection) {
        Info info = clazz.getAnnotation(Info.class);
        String name = info != null ? info.name() : clazz.getSimpleName();

        connection.execute("DROP TABLE " + name);
    }

    public static <T> CompletableFuture<Void> dropAsync(Class<T> clazz, SQLConnection connection) {
        return CompletableFuture.runAsync(() -> drop(clazz, connection), connection.executor());
    }


}
