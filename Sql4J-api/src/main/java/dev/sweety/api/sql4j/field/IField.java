package dev.sweety.api.sql4j.field;

import dev.sweety.api.sql4j.connection.SQLConnection;

import java.lang.reflect.Field;

public interface IField {

    String name();

    Field field();

    SQLConnection connection();

    PrimaryKey primaryKey();

    ForeignKey foreignKey();

    String query();

    String defaultValue();

    boolean isSupported();

    default boolean autoIncrement() {
        return primaryKey() != null && primaryKey().autoIncrement();
    }

    default boolean hasPrimaryKey() {
        return primaryKey() != null;
    }

}
