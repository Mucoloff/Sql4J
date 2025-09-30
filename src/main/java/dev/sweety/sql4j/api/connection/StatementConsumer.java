package dev.sweety.sql4j.api.connection;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface StatementConsumer<T> {

    T accept(PreparedStatement statement) throws SQLException;
}