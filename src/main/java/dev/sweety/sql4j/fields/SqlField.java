package dev.sweety.sql4j.fields;

import dev.sweety.api.sql4j.SqlUtils;
import dev.sweety.api.sql4j.adapter.Adapter;
import dev.sweety.api.sql4j.adapter.FieldAdapter;
import dev.sweety.api.sql4j.connection.SQLConnection;
import dev.sweety.api.sql4j.field.DataField;
import dev.sweety.api.sql4j.field.ForeignKey;
import dev.sweety.api.sql4j.field.IField;
import dev.sweety.api.sql4j.field.PrimaryKey;
import dev.sweety.sql4j.table.Table;
import dev.sweety.sql4j.table.TableManager;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static dev.sweety.api.sql4j.SqlUtils.gson;

public record SqlField(String name, Field field, SQLConnection connection, PrimaryKey primaryKey, ForeignKey foreignKey,
                       String query, String defaultValue) implements IField {

    public static SqlField sqlField(Field field, SQLConnection connection) {

        StringBuilder query = new StringBuilder();
        DataField info = field.getAnnotation(DataField.class);
        String name = info == null || info.name().isEmpty() ? field.getName() : info.name();

        query.append(name).append(" ").append(SqlUtils.getType(field));

        String defaultValue = null;

        if (info != null) {
            if (info.notNull()) query.append(" NOTNULL");
            if (info.unique()) query.append(" UNIQUE");
            if (!info.value().isEmpty() && !info.value().isBlank()) defaultValue = info.value();
        }

        PrimaryKey primaryKey = field.getAnnotation(PrimaryKey.class);
        if (primaryKey != null) {
            query.append(" PRIMARY KEY");
            if (primaryKey.autoIncrement()) query.append(" AUTOINCREMENT");
        }

        boolean hasForeignKey = false;
        String table = "", tableId = "";

        ForeignKey foreignKey = field.getAnnotation(ForeignKey.class);
        if (foreignKey != null) {
            hasForeignKey = true;
            table = foreignKey.table();
            tableId = foreignKey.tableId();
        }

        Class<?> type = field.getType();
        Optional<? extends Table<?>> opt = TableManager.get(type);

        if (opt.isPresent()) {
            hasForeignKey = true;
            Table<?> t = opt.get();
            if (table.isBlank()) table = t.name();
            if (tableId.isBlank()) tableId = t.primaryKey().name();
        }

        if (hasForeignKey) {
            query.append(", FOREIGN KEY (").append(name).append(") REFERENCES ")
                    .append(table)
                    .append("(")
                    .append(tableId)
                    .append(")");
        }

        ForeignKey newForeignKey = SqlUtils.getForeignKey(table, tableId, hasForeignKey);

        return new SqlField(name, field, connection, primaryKey, newForeignKey, query.toString(), defaultValue);
    }

    @Override
    public <T> String get(T entity) {
        field.setAccessible(true);
        try {
            return serialize(field.get(entity));
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
        return null;
    }

    @Override
    public <T> void set(T entity, Object value) {
        field.setAccessible(true);
        try {
            field.set(entity, deserialize(value));
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }

    @Override
    public <T> String serialize(T value) throws Exception {
        if (value == null) return "null";

        FieldAdapter fieldAdapter = field.getAnnotation(FieldAdapter.class);
        if (fieldAdapter != null) {
            // noinspection unchecked
            Adapter<T> adapter = (Adapter<T>) fieldAdapter.adapter().getDeclaredConstructor().newInstance();
            return adapter.serialize(value);
        }

        if (foreignKey != null) {
            Optional<? extends Table<?>> table = TableManager.get(field.getType());
            if (table.isPresent()) return table.get().primaryKey().get(value);
        }

        if (isSupported()) return String.valueOf(value);
        if (value instanceof Enum<?> e) return e.name();

        return gson.toJson(value);
    }

    @Override
    public <T> Object deserialize(Object object) throws Exception {
        if (object == null) return null;

        String str = object.toString();

        FieldAdapter fieldAdapter = field.getAnnotation(FieldAdapter.class);
        if (fieldAdapter != null) {
            // noinspection unchecked
            Adapter<T> adapter = (Adapter<T>) fieldAdapter.adapter().getDeclaredConstructor().newInstance();
            return adapter.deserialize(str);
        }

        if (foreignKey != null) {
            Optional<? extends Table<?>> table = TableManager.get(field.getType());
            if (table.isPresent()) return table.get().selectWhere(foreignKey.tableId() + " = ?", str).getFirst();
        }

        if (isSupported()) return object;

        if (field.getType().isEnum()) {
            // noinspection unchecked
            return Enum.valueOf(((Class<Enum>) field.getType()), str);
        }

        return gson.fromJson(str, field.getType());
    }

    @Override
    public <T> CompletableFuture<String> getAsync(T entity) {
        field.setAccessible(true);
        try {
            return serializeAsync(field.get(entity));
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public <T> CompletableFuture<Void> setAsync(T entity, Object value)  {
        field.setAccessible(true);
        try {
            return deserializeAsync(value).thenAccept(a -> {
                try {
                    field.set(entity, a);
                } catch (Exception e) {
                    e.printStackTrace(System.err);
                }
            });
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
        return null;
    }

    @Override
    public <T> CompletableFuture<String> serializeAsync(T value) {
        if (value == null) return CompletableFuture.completedFuture("null");

        FieldAdapter fieldAdapter = field.getAnnotation(FieldAdapter.class);
        if (fieldAdapter != null) {

            return CompletableFuture.supplyAsync(() -> {
                try {
                    // noinspection unchecked
                    Adapter<T> adapter = (Adapter<T>) fieldAdapter.adapter().getDeclaredConstructor().newInstance();
                    return adapter.serialize(value);
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                         NoSuchMethodException e) {
                    return "null|" + e.getMessage();
                }
            }, connection.executor());
        }

        if (foreignKey != null) {
            Optional<? extends Table<?>> table = TableManager.get(field.getType());
            if (table.isPresent()) return table.get().primaryKey().getAsync(value);
        }

        return CompletableFuture.supplyAsync(() -> {
            if (isSupported()) return String.valueOf(value);
            if (value instanceof Enum<?> e) return e.name();

            return gson.toJson(value);
        }, connection.executor());
    }

    @Override
    public <T> CompletableFuture<Object> deserializeAsync(Object object) throws Exception {
        if (object == null) return CompletableFuture.completedFuture(null);

        String str = object.toString();

        FieldAdapter fieldAdapter = field.getAnnotation(FieldAdapter.class);
        if (fieldAdapter != null) {
            return CompletableFuture.supplyAsync(() -> {
                try {
                    // noinspection unchecked
                    Adapter<T> adapter = (Adapter<T>) fieldAdapter.adapter().getDeclaredConstructor().newInstance();
                    return adapter.deserialize(str);
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                         NoSuchMethodException e) {
                    e.printStackTrace(System.err);
                }

                return null;

            }, connection.executor());
        }

        if (foreignKey != null) {
            Optional<? extends Table<?>> table = TableManager.get(field.getType());
            if (table.isPresent())
                return table.get().selectWhereAsync(foreignKey.tableId() + " = ?", str).thenApply(List::getFirst);
        }

        return CompletableFuture.supplyAsync(() -> {
            if (isSupported()) return object;

            if (field.getType().isEnum()) {
                // noinspection unchecked
                return Enum.valueOf(((Class<Enum>) field.getType()), str);
            }

            return gson.fromJson(str, field.getType());
        }, connection.executor());
    }
}
