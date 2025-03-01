package dev.sweety.table;

import dev.sweety.connection.SQLConnection;
import dev.sweety.fields.annotations.*;
import dev.sweety.fields.SqlField;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.function.Predicate;

import static dev.sweety.Settings.DEBUG;
import static dev.sweety.Settings.SHOW_ERROR_QUERIES;

/**
 * @param <T> the type of the records in the table
 * @author mk$weety
 * Table represents a SQL table with methods for creating, inserting, selecting, updating, and deleting records.
 */
public class Table<T> {

    private final String name;
    private final List<SqlField> sqlFields;
    private final Class<T> clazz;
    private final SQLConnection connection;
    private final SqlField primaryKey;

    public static final Map<Class<?>, Table<?>> tables = new HashMap<>();


    /**
     * Constructs a new Table.
     *
     * @param name       the table name
     * @param sqlFields  the list of SqlFields
     * @param clazz      the class of the records
     * @param primaryKey the primaryKey
     * @param connection the SQL connection
     */

    public Table(String name, List<SqlField> sqlFields, Class<T> clazz, SqlField primaryKey, SQLConnection connection) {
        this.name = name;
        this.sqlFields = sqlFields;
        this.clazz = clazz;
        this.primaryKey = primaryKey;
        this.connection = connection;
    }

    /**
     * Returns the current database connection.
     *
     * @return the database connection
     * @throws SQLException if a database access error occurs
     */
    public Connection connection() throws SQLException {
        return connection.connection();
    }

    /**
     * Creates a new Table from a class and a SQL connection.
     *
     * @param clazz      the class of the records
     * @param connection the SQL connection
     * @param <T>        the type of the records
     * @return the created Table
     */
    public static <T> Table<T> create(Class<T> clazz, SQLConnection connection) {
        if (tables.containsKey(clazz))
            // noinspection unchecked
            return (Table<T>) tables.get(clazz);

        Info info = clazz.getAnnotation(Info.class);
        String name = info != null ? info.name() : clazz.getSimpleName();

        StringBuilder query = new StringBuilder("CREATE TABLE IF NOT EXISTS ");
        query.append(name).append("(");

        Field[] fields = clazz.getDeclaredFields();

        List<SqlField> sqlFields = new ArrayList<>();
        SqlField primaryKey = null;


        for (Field field : fields) {
            //todo
            if (!isSqlField(field)) continue;
            SqlField sqlField = SqlField.getFromField(field, connection);
            sqlFields.add(sqlField);
            if (sqlField.hasPrimaryKey())
                primaryKey = sqlField;

            query.append(sqlField.query()).append(", ");
        }
        query.setLength(query.length() - 2);
        query.append(");");

        connection.execute(query.toString());

        Table<T> table = new Table<>(name, sqlFields, clazz, primaryKey, connection);
        tables.put(clazz, table);
        return table;

    }

    private static boolean isSqlField(Field field) {
        return field.isAnnotationPresent(DataField.class)
        ||
                field.isAnnotationPresent(ForeignKey.class) ||
                field.isAnnotationPresent(PrimaryKey.class) ||
                field.isAnnotationPresent(SqlType.class) ||
                field.isAnnotationPresent(FieldAdapter.class);
    }

    /**
     * Inserts records into the table.
     *
     * @param obj the record to insert
     */
    public final void insert(T obj) {
        StringBuilder query = new StringBuilder("INSERT INTO ");
        query.append(name).append("(");

        for (SqlField field : sqlFields) {
            if (field.autoIncrement()) continue;
            query.append(field.name()).append(", ");
        }

        query.setLength(query.length() - 2);
        query.append(") VALUES (");

        for (SqlField field : sqlFields) {
            if (field.autoIncrement()) continue;
            query.append("'").append(field.get(obj)).append("',");
        }
        query.setLength(query.length() - 1);

        query.append(");");

        String sql = query.toString();

        try (Statement stmt = connection().createStatement()) {
            // Esegui la query e recupera le chiavi generate
            stmt.execute(sql, Statement.RETURN_GENERATED_KEYS);

            if (DEBUG) System.out.println("query: " + sql);

            // Ottieni la chiave primaria generata dal database
            try (var rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    Object generatedKey = rs.getObject(1); // Recupera la prima chiave generata
                    for (SqlField field : sqlFields) {
                        if (field.hasPrimaryKey()) {
                            field.accessible();
                            field.field().set(obj, generatedKey); // Imposta la chiave primaria nell'oggetto
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            if (SHOW_ERROR_QUERIES || DEBUG) System.err.println("query: " + sql);
            e.printStackTrace();
        }
    }



    /**
     * Selects all records from the table.
     *
     * @return the list of records
     */
    public List<T> selectAll() {
        String query = "SELECT * FROM " + name;
        return select(query);
    }

    /**
     * Selects records from the table with a filter.
     *
     * @param filter the filter
     * @return the list of records
     */
    public List<T> selectWhere(String filter) {
        String query = "SELECT * FROM " + name + " WHERE " + filter;
        return select(query);
    }


    /**
     * Selects records from the table with predicates.
     *
     * @param predicates the list of predicates
     * @return the list of records
     */
    @SafeVarargs
    public final List<T> select(Predicate<T>... predicates) {
        List<T> resultList = selectAll();

        Predicate<T> combinedPredicate = Arrays.stream(predicates)
                .reduce(x -> true, Predicate::and);

        return resultList.stream()
                .filter(combinedPredicate)
                .toList();
    }


    private List<T> select(String query) {
        List<T> resultList = new ArrayList<>();

        try (Statement stmt = connection().createStatement();
             var rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                T obj = clazz.getDeclaredConstructor().newInstance();
                for (SqlField field : sqlFields) {
                    field.set(obj, rs);
                }
                resultList.add(obj);
            }

            if (DEBUG) System.out.println("query: " + query);
        } catch (Exception e) {
            if (SHOW_ERROR_QUERIES || DEBUG) System.err.println("query: " + query);
            e.printStackTrace();
        }

        return resultList;
    }

    /**
     * Deletes a record from the table.
     *
     * @param obj the record to delete
     */
    public void delete(T obj) {
        StringBuilder query = new StringBuilder("DELETE FROM ");
        query.append(name).append(" WHERE ");

        for (SqlField field : sqlFields) {
            if (field.hasPrimaryKey()) {
                query.append(field.name()).append(" = '").append(field.get(obj)).append("';");
                break;
            }
        }

        connection.execute(query.toString());
    }

    /**
     * Deletes records from the table with a filter.
     *
     * @param filter the filter
     */
    public void deleteWhere(String filter) {
        String query = "DELETE FROM " + name + " WHERE " + filter;
        connection.execute(query);
    }

    /**
     * Updates a record in the table.
     *
     * @param obj the record to update
     * @throws SQLException           if a database access error occurs
     * @throws IllegalAccessException if the field is not accessible
     */
    public void update(T obj) throws SQLException, IllegalAccessException {
        StringBuilder query = new StringBuilder("UPDATE ");
        query.append(name).append(" SET ");

        String primaryKeyValue = null;

        for (SqlField field : sqlFields) {
            String o = field.get(obj);
            if (field.hasPrimaryKey()) {
                primaryKeyValue = o;
            } else {
                query.append(field.name()).append(" = '").append(o).append("', ");
            }
        }

        query.setLength(query.length() - 2);
        query.append(" WHERE ");

        for (SqlField field : sqlFields) {
            if (field.hasPrimaryKey()) {
                query.append(field.name()).append(" = '").append(primaryKeyValue).append("';");
                break;
            }
        }

        connection.execute(query.toString());
    }

    public static <T> void drop(Class<T> clazz, SQLConnection connection) {

        Info info = clazz.getAnnotation(Info.class);
        String name = tables.containsKey(clazz) ? tables.get(clazz).name() : info != null ? info.name() : clazz.getSimpleName();

        connection.execute("DROP TABLE " + name);
    }

    public void drop() {
        connection.execute("DROP TABLE " + name);
    }

    public void print() {
        StringBuilder text = new StringBuilder();
        text.append("Table: ").append(name).append("\n");

        for (SqlField field : sqlFields) {
            text.append("| ").append(field.name()).append(" ");
        }
        text.append("|\n");

        for (T record : selectAll()) {
            for (SqlField field : sqlFields) {
                text.append("| ").append(field.get(record)).append(" ");
            }
            text.append("|\n");
        }

        System.out.println(text);
    }

    public String name() {
        return name;
    }

    public Class<T> clazz() {
        return clazz;
    }

    public SqlField primaryKey() {
        return primaryKey;
    }


    /**
     * Info is an annotation used to define metadata for a table.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface Info {
        /**
         * The name of the table.
         *
         * @return the table name
         */
        String name();
    }


}
