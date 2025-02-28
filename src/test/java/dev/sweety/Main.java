package dev.sweety;

import dev.sweety.connection.SQLiteConnection;
import dev.sweety.table.Table;
import dev.sweety.tables.User;
import dev.sweety.tables.Order;

import java.sql.SQLException;

public class Main {
    public static void main(String[] args) throws SQLException {

        Settings.DEBUG = true;

        SQLiteConnection connection = new SQLiteConnection("database");

        Table<User> users = Table.create(User.class, connection);
        Table<Order> orders = Table.create(Order.class, connection);

        User user = new User("User");
        Order order = new Order("Product", user);

        users.insert(user);
        orders.insert(order);

        users.print();
        orders.print();

        connection.close();
    }
}
