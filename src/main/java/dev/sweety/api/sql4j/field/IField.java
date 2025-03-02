package dev.sweety.api.sql4j.field;

import dev.sweety.api.sql4j.connection.SQLConnection;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.*;
import java.util.concurrent.CompletableFuture;

public interface IField {

    String name();

    Field field();

    SQLConnection connection();

    PrimaryKey primaryKey();

    ForeignKey foreignKey();

    String query();

    String defaultValue();

    default boolean isSupported() {
        Class<?> type = field().getType();

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
        return type == BigInteger.class;
    }

    default boolean autoIncrement() {
        return primaryKey() != null && primaryKey().autoIncrement();
    }

    default boolean hasPrimaryKey() {
        return primaryKey() != null;
    }

    <T> String get(T entity);

    <T> void set(T entity, Object value);

    <T> String serialize(T value) throws Exception;

    <T> Object deserialize(Object object) throws Exception;

    <T> CompletableFuture<String> getAsync(T entity);

    <T> CompletableFuture<Void> setAsync(T entity, Object value);

    <T> CompletableFuture<String> serializeAsync(T value);

    <T> CompletableFuture<Object> deserializeAsync(Object object) throws Exception;
}
