package dev.sweety;

import dev.sweety.connection.SQLiteConnection;
import dev.sweety.table.Table;
import dev.sweety.tables.User;
import dev.sweety.tables.Order;

import java.sql.SQLException;

public class Main {
    public static void main(String[] args) throws SQLException {

        Settings.DEBUG = true;

        SQLiteConnection sqLite = new SQLiteConnection("database");

        Table<User> users = Table.create(User.class, sqLite);
        Table<Order> orders = Table.create(Order.class, sqLite);

        User user = new User("user");
        users.insert(user);


        Order order = new Order("Product", user);
        orders.insert(order);

        System.out.println("users:");
        users.selectAll().forEach(System.out::println);

        System.out.println("orders");
        orders.selectAll().forEach(System.out::println);

        sqLite.close();
    }
}
