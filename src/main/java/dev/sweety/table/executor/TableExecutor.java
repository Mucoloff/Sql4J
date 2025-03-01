package dev.sweety.table.executor;

import dev.sweety.api.IExecutor;
import dev.sweety.api.SQLConnection;
import dev.sweety.exception.SqlError;
import dev.sweety.fields.SqlField;
import dev.sweety.table.Table;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;

import static dev.sweety.Settings.DEBUG;

public class TableExecutor<T> implements IExecutor<T> {
    private final Table<T> table;
    private final SQLConnection connection;

    public TableExecutor(Table<T> table, SQLConnection connection) {
        this.table = table;
        this.connection = connection;
    }

    @Override
    public Table<T> table() {
        return table;
    }

    @Override
    public SQLConnection connection() {
        return connection;
    }

    void insert(T obj) throws SqlError {
        StringBuilder query = new StringBuilder("INSERT INTO ");
        query.append(table.name()).append("(");

        for (SqlField field : table.sqlFields()) {
            if (field.autoIncrement()) continue;
            query.append(field.name()).append(", ");
        }

        query.setLength(query.length() - 2);
        query.append(") VALUES (");

        for (int i = 0; i < table.sqlFields().size(); i++) {
            SqlField field = table.sqlFields().get(i);
            if (field.autoIncrement()) continue;
            query.append("?, ");
        }

        query.setLength(query.length() - 2);
        query.append(");");

        try (Connection connection = connection().connection(); PreparedStatement statement = connection.prepareStatement(query.toString(), Statement.RETURN_GENERATED_KEYS)) {

            int paramIndex = 1;
            for (SqlField field : table.sqlFields()) {
                if (field.autoIncrement()) continue;
                statement.setObject(paramIndex++, field.get(obj));
            }

            statement.executeUpdate();

            if (DEBUG) System.out.println("query: " + query);

            try (var rs = statement.getGeneratedKeys()) {
                if (rs.next()) {
                    Object generatedKey = rs.getObject(1);
                    for (SqlField field : table.sqlFields()) {
                        if (field.hasPrimaryKey()) {
                            field.accessible();
                            field.field().set(obj, generatedKey);
                            break;
                        }
                    }
                }
            }

        } catch (Exception e) {
            throw new SqlError(query.toString(), e);
        }
    }
}
