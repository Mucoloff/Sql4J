package dev.sweety.sql4j.api;

import com.google.gson.Gson;
import dev.sweety.sql4j.api.adapter.FieldAdapter;
import dev.sweety.sql4j.api.adapter.SqlType;
import dev.sweety.sql4j.api.field.DataField;
import dev.sweety.sql4j.api.field.ForeignKey;
import dev.sweety.sql4j.api.field.PrimaryKey;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.*;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class SqlUtils {

    public static boolean DEBUG = false;

    public static final Executor EXECUTOR = Executors.newCachedThreadPool();
    public static final Gson gson = new Gson().newBuilder().disableHtmlEscaping().create();

    public static final System.Logger logger = System.getLogger("Sql4J");

    public static boolean isSqlField(Field field) {
        return field.isAnnotationPresent(DataField.class) ||
                field.isAnnotationPresent(ForeignKey.class) ||
                field.isAnnotationPresent(PrimaryKey.class) ||
                field.isAnnotationPresent(SqlType.class) ||
                field.isAnnotationPresent(FieldAdapter.class);
    }

    public static ForeignKey getForeignKey(final String table, final String tableId, boolean hasForeignKey) {
        return !hasForeignKey ? null : new ForeignKey() {

            @Override
            public Class<? extends Annotation> annotationType() {
                return ForeignKey.class;
            }

            @Override
            public String table() {
                return table;
            }

            @Override
            public String tableId() {
                return tableId;
            }
        };
    }

    public static String getType(Field field) {
        Class<?> type = field.getType();

        SqlType sqlType = field.getAnnotation(SqlType.class);

        if (sqlType != null) return sqlType.type();

        if (type == String.class) return "TEXT";
        if (type == int.class || type == Integer.class) return "INTEGER";
        if (type == long.class || type == Long.class) return "BIGINT";
        if (type == double.class || type == Double.class) return "DOUBLE";
        if (type == float.class || type == Float.class) return "FLOAT";
        if (type == boolean.class || type == Boolean.class) return "BOOLEAN";
        if (type == byte.class || type == Byte.class) return "TINYINT";
        if (type == short.class || type == Short.class) return "SMALLINT";
        if (type == char.class || type == Character.class) return "CHAR";

        if (type == Date.class) return "DATE";
        if (type == Time.class) return "TIME";
        if (type == Timestamp.class) return "TIMESTAMP";
        if (type == Blob.class) return "BLOB";
        if (type == Clob.class) return "CLOB";

        if (type == BigDecimal.class) return "DECIMAL";
        if (type == BigInteger.class) return "NUMERIC";

        return "TEXT";
    }

}
