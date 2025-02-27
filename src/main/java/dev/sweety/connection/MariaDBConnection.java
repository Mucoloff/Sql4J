package dev.sweety.connection;

/**
 * @author mk$weety
 * MariaDBConnection is a subclass of MySQLConnection that provides a connection URL specific to MariaDB.
 */
public class MariaDBConnection extends MySQLConnection {

    /**
     * Constructs a new MariaDBConnection.
     *
     * @param host     the database host
     * @param port     the database port
     * @param database the database name
     * @param user     the database user
     * @param password the database password
     */
    public MariaDBConnection(String host, int port, String database, String user, String password) {
        super(host, port, database, user, password);
    }

    /**
     * Returns the connection URL for MariaDB.
     *
     * @return the connection URL
     */
    @Override
    public String url() {
        return "jdbc:mariadb://" + host() + ":" + port() + "/" + database();
    }
}