package dev.sweety.table;

import dev.sweety.api.sql4j.connection.SQLConnection;
import dev.sweety.api.sql4j.table.ITable;
import dev.sweety.fields.SqlField;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public record Table<T>(String name, Class<T> clazz, SQLConnection connection,
                       SqlField primaryKey, List<SqlField> sqlFields) implements ITable<T> {


    @Override
    public void insert(T entity) {
        StringBuilder query = new StringBuilder("INSERT INTO ");
        query.append(name()).append("(");

        for (SqlField field : sqlFields()) {
            if (field.autoIncrement()) continue;
            query.append(field.name()).append(", ");
        }

        query.setLength(query.length() - 2);
        query.append(") VALUES (");

        List<Object> params = new ArrayList<>();
        for (int i = 0; i < sqlFields().size(); i++) {
            SqlField field = sqlFields().get(i);
            if (field.autoIncrement()) continue;
            params.add(field.get(entity));
            query.append("?, ");
        }

        query.setLength(query.length() - 2);
        query.append(");");


        connection().update(query.toString(), statement -> {
            try (var rs = statement.getGeneratedKeys()) {
                if (rs.next()) {
                    Object generatedKey = rs.getObject(1);
                    for (SqlField field : sqlFields()) {
                        if (field.hasPrimaryKey()) {
                            field.set(entity, generatedKey);
                            break;
                        }
                    }
                }
                return null;
            }
        }, params.toArray(Object[]::new));
    }

    @Override
    public CompletableFuture<Void> insertAsync(T entity) {
        StringBuilder query = new StringBuilder("INSERT INTO ");
        query.append(name()).append("(");

        for (SqlField field : sqlFields()) {
            if (field.autoIncrement()) continue;
            query.append(field.name()).append(", ");
        }

        query.setLength(query.length() - 2);
        query.append(") VALUES (");

        List<Object> params = new ArrayList<>();
        for (int i = 0; i < sqlFields().size(); i++) {
            SqlField field = sqlFields().get(i);
            if (field.autoIncrement()) continue;
            params.add(field.getAsync(entity).join());
            query.append("?, ");
        }

        query.setLength(query.length() - 2);
        query.append(");");


        return connection().updateAsync(query.toString(), statement -> {
            try (var rs = statement.getGeneratedKeys()) {
                if (rs.next()) {
                    Object generatedKey = rs.getObject(1);
                    for (SqlField field : sqlFields()) {
                        if (field.hasPrimaryKey()) {
                            field.setAsync(entity, generatedKey);
                            break;
                        }
                    }
                }
                return null;
            }
        }, params.toArray(Object[]::new));
    }

    @Override
    public List<T> select(String query, Object... params) {
        List<T> resultList = new ArrayList<>();

        try (ResultSet resultSet = connection.executeQuery(query, params)) {

            while (resultSet.next()) {
                T obj = clazz().getDeclaredConstructor().newInstance();
                for (SqlField field : sqlFields()) {
                    field.set(obj, resultSet.getObject(field.name()));
                }
                resultList.add(obj);
            }

        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
        return resultList;
    }

    @Override
    public CompletableFuture<List<T>> selectAsync(String query, Object... params) {


        return connection.executeQueryAsync(query, params).thenApply(resultSet -> {
            List<T> resultList = new ArrayList<>();

            try {
                while (resultSet.next()) {
                    T obj = clazz().getDeclaredConstructor().newInstance();
                    for (SqlField field : sqlFields()) {
                        field.setAsync(obj, resultSet.getObject(field.name()));
                    }
                    resultList.add(obj);
                }

            } catch (Exception e) {
                e.printStackTrace(System.err);
            }
            return resultList;
        });


    }

    @Override
    public List<T> selectWhere(String filter, Object... params) {
        return select("SELECT * FROM " + name() + " WHERE " + filter, params);
    }

    @Override
    public CompletableFuture<List<T>> selectWhereAsync(String filter, Object... params) {
        return selectAsync("SELECT * FROM " + name() + " WHERE " + filter, params);
    }

    @Override
    public List<T> selectAll() {
        return select("SELECT * FROM " + name());
    }

    @Override
    public CompletableFuture<List<T>> selectAllAsync() {
        return selectAsync("SELECT * FROM " + name());
    }

    @Override
    public void update(T entity) {
        StringBuilder query = new StringBuilder("UPDATE ");
        query.append(name()).append(" SET ");

        Object primaryKeyValue = null;
        List<Object> fieldValues = new ArrayList<>();

        for (SqlField field : sqlFields()) {
            Object value = field.get(entity);
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

        if (primaryKeyValue == null)
            throw new IllegalArgumentException("No primary key found for the object.");

        Object o = primaryKeyValue;
        connection.execute(query.toString(), statement -> {

            connection.setParameters(statement, fieldValues);

            statement.setObject(fieldValues.size() + 1, o);

            statement.executeUpdate();
            return null;
        });


    }

    @Override
    public CompletableFuture<Void> updateAsync(T entity) {
        StringBuilder query = new StringBuilder("UPDATE ");
        query.append(name()).append(" SET ");

        Object primaryKeyValue = null;
        List<Object> fieldValues = new ArrayList<>();

        for (SqlField field : sqlFields()) {
            Object value = field.get(entity);
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

        if (primaryKeyValue == null)
            throw new IllegalArgumentException("No primary key found for the object.");

        Object o = primaryKeyValue;
        return connection.executeAsync(query.toString(), statement -> {

            connection.setParameters(statement, fieldValues);

            statement.setObject(fieldValues.size() + 1, o);

            statement.executeUpdate();
            return null;
        });
    }

    @Override
    public void delete(T entity){
        StringBuilder query = new StringBuilder("DELETE FROM ");
        query.append(name()).append(" WHERE ");

        Object primaryKeyValue = null;

        for (SqlField field : sqlFields()) {
            if (field.hasPrimaryKey()) {
                query.append(field.name()).append(" = ?");
                primaryKeyValue = field.get(entity);
                break;
            }
        }

        if (primaryKeyValue == null) throw new IllegalArgumentException("No primary key found for object.");

        connection.executeUpdate(query.toString(), primaryKeyValue);
    }

    @Override
    public CompletableFuture<Integer> deleteAsync(T entity){
        StringBuilder query = new StringBuilder("DELETE FROM ");
        query.append(name()).append(" WHERE ");

        Object primaryKeyValue = null;

        for (SqlField field : sqlFields()) {
            if (field.hasPrimaryKey()) {
                query.append(field.name()).append(" = ?");
                primaryKeyValue = field.get(entity);
                break;
            }
        }

        if (primaryKeyValue == null) throw new IllegalArgumentException("No primary key found for object.");

        return connection.executeUpdateAsync(query.toString(), primaryKeyValue);
    }

    @Override
    public void delete(String filter, Object... params) {
        connection.executeUpdate("DELETE FROM " + name() + " WHERE " + filter, params);
    }

    @Override
    public CompletableFuture<Integer> deleteAsync(String filter, Object... params) {
        return connection.executeUpdateAsync("DELETE FROM " + name() + " WHERE " + filter, params);
    }


}
