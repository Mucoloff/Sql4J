package dev.sweety.table.executor;

import dev.sweety.api.SQLConnection;
import dev.sweety.table.Table;

public class TableAsyncExecutor<T> extends TableExecutor<T> {

    public TableAsyncExecutor(Table<T> table, SQLConnection connection) {
        super(table, connection);
    }
}
