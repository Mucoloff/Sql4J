package dev.sweety.sql4j.connection;

public class MariaDBConnection extends MySQLConnection {

    public MariaDBConnection(String host, int port, String database, String user, String password) {
        super(host, port, database, user, password);
    }

    @Override
    public String url() {
        return "jdbc:mariadb://" + host() + ":" + port() + "/" + database();
    }
}