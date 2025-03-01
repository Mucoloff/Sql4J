package dev.sweety.table;

import dev.sweety.annotations.adapter.FieldAdapter;
import dev.sweety.annotations.adapter.SqlType;
import dev.sweety.annotations.field.DataField;
import dev.sweety.annotations.field.ForeignKey;
import dev.sweety.annotations.field.PrimaryKey;
import dev.sweety.annotations.table.Info;
import dev.sweety.api.ITable;
import dev.sweety.api.SQLConnection;
import dev.sweety.fields.SqlField;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

import static dev.sweety.Settings.DEBUG;
import static dev.sweety.Settings.SHOW_ERROR_QUERIES;

/**
 * @param <T> the type of the records in the table
 * @author mk$weety
 * Table represents a SQL table with methods for creating, inserting, selecting, updating, and deleting records.
 */
public class Table<T> implements ITable<T> {

    private final String name;
    private final List<SqlField> sqlFields;
    private final Class<T> clazz;
    private final SQLConnection connection;
    private final SqlField primaryKey;

    public Table(String name, List<SqlField> sqlFields, Class<T> clazz, SqlField primaryKey, SQLConnection connection) {
        this.name = name;
        this.sqlFields = sqlFields;
        this.clazz = clazz;
        this.primaryKey = primaryKey;
        this.connection = connection;
    }

    @Override
    public Connection connection() throws SQLException {
        return connection.connection();
    }

    @Override
    public void execute(String sql) {
        connection.execute(sql);
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public Class<T> clazz() {
        return clazz;
    }

    @Override
    public SqlField primaryKey() {
        return primaryKey;
    }

    @Override
    public List<SqlField> sqlFields() {
        return sqlFields;
    }

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

            SqlField sqlField = SqlField.getFromField(field, connection);
            sqlFields.add(sqlField);

            if (sqlField.hasPrimaryKey())
                primaryKey = sqlField;

            query.append(sqlField.query()).append(", ");
        }
        query.setLength(query.length() - 2);
        query.append(");");

        connection.execute(query.toString());

        Table<T> table = new Table<>(name, sqlFields, clazz, primaryKey, connection);
        tables.put(clazz, table);
        return table;
    }

    public static <T> void drop(Class<T> clazz, SQLConnection connection) {

        Info info = clazz.getAnnotation(Info.class);
        String name = tables.containsKey(clazz) ? tables.get(clazz).name() : info != null ? info.name() : clazz.getSimpleName();

        connection.execute("DROP TABLE " + name);
    }

    private static boolean isSqlField(Field field) {
        return field.isAnnotationPresent(DataField.class) ||
                field.isAnnotationPresent(ForeignKey.class) ||
                field.isAnnotationPresent(PrimaryKey.class) ||
                field.isAnnotationPresent(SqlType.class) ||
                field.isAnnotationPresent(FieldAdapter.class);
    }
}
