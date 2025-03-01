package dev.sweety.api;

import java.sql.SQLException;

public interface IExecutor<T> {

    SQLConnection connection() throws SQLException;

    ITable<T> table();



}
