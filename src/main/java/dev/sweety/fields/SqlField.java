package dev.sweety.fields;

import com.google.gson.Gson;
import dev.sweety.SqlUtils;
import dev.sweety.annotations.adapter.FieldAdapter;
import dev.sweety.annotations.field.ForeignKey;
import dev.sweety.annotations.field.PrimaryKey;
import dev.sweety.api.Adapter;
import dev.sweety.api.SQLConnection;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
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

    public void accessible() {
        field.setAccessible(true);
    }

    public <T> void set(T obj, Object value) throws SQLException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        accessible();
        field.set(obj, deserialize(value));
    }

    public <T> String get(T obj) {
        try {
            accessible();
            return serialize(field.get(obj));
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public static final Gson gson = new Gson().newBuilder().disableHtmlEscaping().create();

    public <T> String serialize(T value) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        if (value == null) return "null";

        FieldAdapter fieldAdapter = field.getAnnotation(FieldAdapter.class);
        if (fieldAdapter != null) {
            // noinspection unchecked
            Adapter<T> adapter = (Adapter<T>) fieldAdapter.adapter().getDeclaredConstructor().newInstance();
            return adapter.serialize(value);
        }

        if (foreignKey != null) {
            return SqlUtils.tables.get(field.getType()).primaryKey().get(value);
        }

        if (isSupported()) return String.valueOf(value);
        if (value instanceof Enum<?> e) return e.name();

        return gson.toJson(value);
    }

    private <T> Object deserialize(Object object) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException, SQLException {
        if (object == null) return null;

        String str = object.toString();

        FieldAdapter fieldAdapter = field.getAnnotation(FieldAdapter.class);
        if (fieldAdapter != null) {
            // noinspection unchecked
            Adapter<T> adapter = (Adapter<T>) fieldAdapter.adapter().getDeclaredConstructor().newInstance();
            return adapter.deserialize(str);
        }

        if (foreignKey != null) {
            return SqlUtils.tables.get(field.getType()).selectWhere(foreignKey.tableId() + " = " + str).getFirst();
        }

        if (isSupported()) return object;

        if (field.getType().isEnum()) {
            // noinspection unchecked
            return Enum.valueOf(((Class<Enum>) field.getType()), str);
        }

        return gson.fromJson(str, field.getType());
    }

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
