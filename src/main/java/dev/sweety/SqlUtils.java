package dev.sweety;

import dev.sweety.annotations.adapter.FieldAdapter;
import dev.sweety.annotations.adapter.SqlType;
import dev.sweety.annotations.field.DataField;
import dev.sweety.annotations.field.ForeignKey;
import dev.sweety.annotations.field.PrimaryKey;
import dev.sweety.annotations.table.Info;
import dev.sweety.api.SQLConnection;
import dev.sweety.fields.SqlField;
import dev.sweety.table.Table;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class SqlUtils {

    public static final Executor EXECUTOR = Executors.newCachedThreadPool();

    public static final Map<Class<?>, Table<?>> tables = new HashMap<>();

    public static <T> CompletableFuture<Table<T>> createAsync(Class<T> clazz, SQLConnection connection) {
        return CompletableFuture.supplyAsync(() -> create(clazz, connection));
    }

    public static <T> CompletableFuture<Void> dropAsync(Class<T> clazz, SQLConnection connection) {
        return CompletableFuture.runAsync(() -> drop(clazz, connection));
    }

    public static <T> Table<T> create(Class<T> clazz, SQLConnection connection) {
        if (tables.containsKey(clazz))
            // noinspection unchecked
            return (Table<T>) tables.get(clazz);

        Info info = clazz.getAnnotation(Info.class);
        String name = info != null ? info.name() : clazz.getSimpleName();

        StringBuilder query = new StringBuilder("CREATE TABLE IF NOT EXISTS ");
        query.append(name).append("(");

        Field[] fields = clazz.getDeclaredFields();

        List<SqlField> sqlFields = new ArrayList<>();
        SqlField primaryKey = null;

        for (Field field : fields) {
            if (!isSqlField(field)) continue;

            SqlField sqlField = getFromField(field, connection);
            sqlFields.add(sqlField);

            if (sqlField.hasPrimaryKey())
                primaryKey = sqlField;

            query.append(sqlField.query()).append(", ");
        }

        query.setLength(query.length() - 2);
        query.append(");");

        connection.executeQuery(query.toString());

        Table<T> table = new Table<>(name, sqlFields, clazz, primaryKey, connection);
        tables.put(clazz, table);
        return table;
    }

    public static <T> void drop(Class<T> clazz, SQLConnection connection) {

        Info info = clazz.getAnnotation(Info.class);
        String name = tables.containsKey(clazz) ? tables.get(clazz).name() : info != null ? info.name() : clazz.getSimpleName();

        connection.executeQuery("DROP TABLE " + name);
    }

    private static boolean isSqlField(Field field) {
        return field.isAnnotationPresent(DataField.class) ||
                field.isAnnotationPresent(ForeignKey.class) ||
                field.isAnnotationPresent(PrimaryKey.class) ||
                field.isAnnotationPresent(SqlType.class) ||
                field.isAnnotationPresent(FieldAdapter.class);
    }

    public static SqlField getFromField(Field field, SQLConnection connection) {
        StringBuilder query = new StringBuilder();

        DataField info = field.getAnnotation(DataField.class);
        String name = info == null || info.name().isEmpty() ? field.getName() : info.name();

        query.append(name).append(" ").append(getType(field));

        String value = null;

        if (info != null) {
            if (info.notNull()) query.append(" NOTNULL");
            if (info.unique()) query.append(" UNIQUE");
            if (!info.value().isEmpty() && !info.value().isBlank()) value = info.value();

        }

        PrimaryKey primaryKey = field.getAnnotation(PrimaryKey.class);
        if (primaryKey != null) {
            query.append(" PRIMARY KEY");
            if (primaryKey.autoIncrement()) {
                query.append(" AUTOINCREMENT");
            }
        }

        boolean hasForeignKey = false;
        String table = "", tableId = "";

        ForeignKey foreignKey = field.getAnnotation(ForeignKey.class);
        Class<?> type = field.getType();
        if (foreignKey != null) {
            hasForeignKey = true;

            table = foreignKey.table();
            tableId = foreignKey.tableId();

            if (tables.containsKey(type)) {
                Table<?> t = tables.get(type);
                if (table.isBlank()) table = t.name();
                if (tableId.isBlank()) tableId = t.primaryKey().name();
            }
        }


        if (hasForeignKey) {
            query.append(", FOREIGN KEY (").append(name).append(") REFERENCES ")
                    .append(table)
                    .append("(")
                    .append(tableId)
                    .append(")");
        }

        ForeignKey newForeignKey = getNewForeignKey(table, tableId, hasForeignKey);

        return new SqlField(name, primaryKey, newForeignKey, query.toString(), value, field, connection);
    }

    private static ForeignKey getNewForeignKey(final String table, final String tableId, boolean hasForeignKey) {
        return !hasForeignKey ? null : new ForeignKey() {

            @Override
            public Class<? extends Annotation> annotationType() {
                return ForeignKey.class;
            }

            @Override
            public String table() {
                return table;
            }

            @Override
            public String tableId() {
                return tableId;
            }
        };
    }

    private static String getType(Field field) {
        Class<?> type = field.getType();

        SqlType sqlType = field.getAnnotation(SqlType.class);

        if (sqlType != null) return sqlType.type();

        if (type == String.class) return "TEXT";
        if (type == int.class || type == Integer.class) return "INTEGER";
        if (type == long.class || type == Long.class) return "BIGINT";
        if (type == double.class || type == Double.class) return "DOUBLE";
        if (type == float.class || type == Float.class) return "FLOAT";
        if (type == boolean.class || type == Boolean.class) return "BOOLEAN";
        if (type == byte.class || type == Byte.class) return "TINYINT";
        if (type == short.class || type == Short.class) return "SMALLINT";
        if (type == char.class || type == Character.class) return "CHAR";

        if (type == Date.class) return "DATE";
        if (type == Time.class) return "TIME";
        if (type == Timestamp.class) return "TIMESTAMP";
        if (type == Blob.class) return "BLOB";
        if (type == Clob.class) return "CLOB";

        if (type == BigDecimal.class) return "DECIMAL";
        if (type == BigInteger.class) return "NUMERIC";

        return "TEXT";
    }

}
