package dev.sweety.table;

import dev.sweety.api.ITable;
import dev.sweety.api.SQLConnection;
import dev.sweety.fields.SqlField;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

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
        connection.executeQuery(sql);
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
}
