package dev.sweety.exception;

import static dev.sweety.Settings.DEBUG;
import static dev.sweety.Settings.SHOW_ERROR_QUERIES;

public class SqlError extends RuntimeException {

    private final String sql;
    public SqlError(String sql, Exception e) {
        super(e);
        this.sql = sql;
    }

    @Override
    public String getMessage() {
        String message = super.getMessage();
        if (SHOW_ERROR_QUERIES || DEBUG) message += "\n" + sql;
        return message;
    }

    @Override
    public void printStackTrace() {
        if (SHOW_ERROR_QUERIES || DEBUG) System.err.println(sql);
        super.printStackTrace();
    }
}
