package dev.sweety.sql4j.impl.table;

import dev.sweety.sql4j.api.connection.SQLConnection;
import dev.sweety.sql4j.api.table.ITable;
import dev.sweety.sql4j.impl.fields.SqlField;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public record Table<Entity>(String name, Class<Entity> clazz, SQLConnection connection,
                            SqlField primaryKey, List<SqlField> sqlFields) implements ITable<Entity> {


    @Override
    public void insert(Entity entity) {
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
    public CompletableFuture<Void> insertAsync(Entity entity) {
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
    public List<Entity> selectWhere(String filter, Object... params) {
        List<Entity> resultList = new ArrayList<>();

        try (Connection connection = this.connection.connection(); PreparedStatement statement = connection.prepareStatement("SELECT * FROM " + name() + " WHERE " + filter + ";")) {

            this.connection.setParameters(statement, params);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Entity obj = clazz().getDeclaredConstructor().newInstance();
                    for (SqlField field : sqlFields()) {
                        field.set(obj, resultSet.getObject(field.name()));
                    }
                    resultList.add(obj);
                }
            }


        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
        return resultList;
    }

    @Override
    public CompletableFuture<List<Entity>> selectWhereAsync(String filter, Object... params) {
        return CompletableFuture.supplyAsync(() -> {
            List<Entity> resultList = new ArrayList<>();

            CompletableFuture<Connection> connectionFuture = this.connection.connectAsync();

            try (Connection connection = connectionFuture.join()){
                try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM " + name() + " WHERE " + filter + ";")) {

                    this.connection.setParameters(statement, params);

                    try (ResultSet resultSet = statement.executeQuery()) {
                        while (resultSet.next()) {
                            Entity obj = clazz().getDeclaredConstructor().newInstance();
                            for (SqlField field : sqlFields()) {
                                field.set(obj, resultSet.getObject(field.name()));
                            }
                            resultList.add(obj);
                        }
                    }


                }
            } catch (Exception e) {
                e.printStackTrace(System.err);
            }
            return resultList;
        }, connection.executor());
    }

    @Override
    public List<Entity> selectAll() {
        List<Entity> resultList = new ArrayList<>();

        try (Connection connection = this.connection.connection();
             PreparedStatement ps = connection.prepareStatement("SELECT * from " + name() + ";");
             ResultSet rs = ps.executeQuery()) {

            List<Object[]> rows = new ArrayList<>();

            while (rs.next()) {
                Object[] values = new Object[sqlFields().size()];
                int i = 0;
                for (SqlField field : sqlFields()) {
                    values[i++] = rs.getObject(field.name());
                }
                rows.add(values);
            }

            // Ora rs e ps sono chiusi, ricostruisci le entity
            for (Object[] row : rows) {
                Entity obj = clazz().getDeclaredConstructor().newInstance();
                int i = 0;
                for (SqlField field : sqlFields()) {
                    field.set(obj, row[i++]);
                }
                resultList.add(obj);
            }

        } catch (Exception e) {
            e.printStackTrace(System.err);
        }

        return resultList;
    }


    @Override
    public CompletableFuture<List<Entity>> selectAllAsync() {
        return CompletableFuture.supplyAsync(() -> {
            List<Entity> resultList = new ArrayList<>();

            CompletableFuture<Connection> connectionFuture = this.connection.connectAsync();

            try (Connection connection = connectionFuture.join();
                 PreparedStatement ps = connection.prepareStatement("SELECT * from " + name() + ";");
                 ResultSet rs = ps.executeQuery()) {

                List<Object[]> rows = new ArrayList<>();

                while (rs.next()) {
                    Object[] values = new Object[sqlFields().size()];
                    int i = 0;
                    for (SqlField field : sqlFields()) {
                        values[i++] = rs.getObject(field.name());
                    }
                    rows.add(values);
                }

                // Ora rs e ps sono chiusi, ricostruisci le entity
                for (Object[] row : rows) {
                    Entity obj = clazz().getDeclaredConstructor().newInstance();
                    int i = 0;
                    for (SqlField field : sqlFields()) {
                        field.set(obj, row[i++]);
                    }
                    resultList.add(obj);
                }

            } catch (Exception e) {
                e.printStackTrace(System.err);
            }

            return resultList;
        }, connection.executor()); // usa pure il tuo executor
    }


    @Override
    public void update(Entity entity) {
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
    public CompletableFuture<Void> updateAsync(Entity entity) {
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
    public void delete(Entity entity) {
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
    public CompletableFuture<Integer> deleteAsync(Entity entity) {
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
