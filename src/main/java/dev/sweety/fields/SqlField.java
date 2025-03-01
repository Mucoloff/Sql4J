package dev.sweety.fields;

import com.google.gson.Gson;
import dev.sweety.connection.SQLConnection;
import dev.sweety.fields.adapters.Adapter;
import dev.sweety.fields.annotations.*;
import dev.sweety.table.Table;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.*;

/**
 * @author mk$weety
 * SqlField represents a field in a SQL table with its associated metadata and methods for serialization and deserialization.
 */
public record SqlField(String name, PrimaryKey primaryKey, ForeignKey foreignKey, String query,
                       String defaultValue,
                       Field field, SQLConnection connection) {

    /**
     * Creates a SqlField from a Java Field.
     *
     * @param field      the Java Field
     * @param connection
     * @return the SqlField
     */
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

            if (Table.tables.containsKey(type)) {
                Table<?> t = Table.tables.get(type);
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

    /**
     * Sets the field to be accessible.
     */
    public void accessible() {
        field.setAccessible(true);
    }

    /**
     * Sets the value of the field from a ResultSet.
     *
     * @param obj the object to set the field value on
     * @param rs  the ResultSet
     * @param <T> the type of the object
     * @throws SQLException           if a database access error occurs
     * @throws IllegalAccessException if the field is not accessible
     */
    public <T> void set(T obj, ResultSet rs) throws Exception {
        accessible();
        field.set(obj, deserialize(rs.getObject(name)));
    }

    /**
     * Gets the value of the field as a string.
     *
     * @param obj the object to get the field value from
     * @param <T> the type of the object
     * @return the field value as a string
     */
    public <T> String get(T obj) {
        try {
            accessible();
            return serialize(field.get(obj));
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public static final Gson gson = new Gson().newBuilder().disableHtmlEscaping().create();

    /**
     * Serializes the field value to a string.
     *
     * @param value the field value
     * @return the serialized value
     */
    public <T> String serialize(T value) throws Exception {
        if (value == null) return "null";

        FieldAdapter fieldAdapter = field.getAnnotation(FieldAdapter.class);
        if (fieldAdapter != null) {
            // noinspection unchecked
            Adapter<T> adapter = (Adapter<T>) fieldAdapter.adapter().getDeclaredConstructor().newInstance();
            return adapter.serialize(value);
        }

        if (foreignKey != null) {
            String s = Table.tables.get(field.getType()).primaryKey().get(value);
            System.out.println("sssssssssss: " + s);
            return s;
        }

        if (isSupported()) return String.valueOf(value);
        if (value instanceof Enum<?> e) return e.name();

        return gson.toJson(value);
    }

    /**
     * Deserializes the field value from a string.
     *
     * @param object the serialized value
     * @return the deserialized value
     */
    private <T> Object deserialize(Object object) throws Exception {
        if (object == null) return null;

        String str = object.toString();

        FieldAdapter fieldAdapter = field.getAnnotation(FieldAdapter.class);
        if (fieldAdapter != null) {
            // noinspection unchecked
            Adapter<T> adapter = (Adapter<T>) fieldAdapter.adapter().getDeclaredConstructor().newInstance();
            return adapter.deserialize(str);
        }

        if (foreignKey != null) {
            return Table.tables.get(field.getType()).selectWhere(foreignKey.tableId() + " = " + str).getFirst();
        }

        if (isSupported()) return object;

        if (field.getType().isEnum()) {
            // noinspection unchecked
            return Enum.valueOf(((Class<Enum>) field.getType()), str);
        }

        return gson.fromJson(str, field.getType());
    }

    /**
     * Gets the SQL type of the field.
     *
     * @param field the Java Field
     * @return the SQL type
     */
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

    /**
     * Checks if the field type is supported.
     *
     * @return true if the field type is supported, false otherwise
     */
    public boolean isSupported() {
        Class<?> type = field.getType();

        if (type.isPrimitive()) return true;

        if (type == String.class) return true;
        if (type == int.class || type == Integer.class) return true;
        if (type == long.class || type == Long.class) return true;
        if (type == double.class || type == Double.class) return true;
        if (type == float.class || type == Float.class) return true;
        if (type == boolean.class || type == Boolean.class) return true;
        if (type == byte.class || type == Byte.class) return true;
        if (type == short.class || type == Short.class) return true;
        if (type == char.class || type == Character.class) return true;

        if (type == Date.class) return true;
        if (type == Time.class) return true;
        if (type == Timestamp.class) return true;
        if (type == Blob.class) return true;
        if (type == Clob.class) return true;

        if (type == BigDecimal.class) return true;
        if (type == BigInteger.class) return true;

        return false;
    }

    public boolean autoIncrement() {
        return primaryKey != null && primaryKey.autoIncrement();
    }

    public boolean hasPrimaryKey() {
        return primaryKey != null;
    }
}
