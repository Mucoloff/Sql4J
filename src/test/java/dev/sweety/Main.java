package dev.sweety;

import dev.sweety.api.sql4j.SqlUtils;
import dev.sweety.sql4j.connection.SQLiteConnection;
import dev.sweety.sql4j.table.Table;
import dev.sweety.sql4j.table.TableManager;
import dev.sweety.tables.Order;
import dev.sweety.tables.User;

public class Main {

    public static void main(String[] args) {

        SqlUtils.DEBUG = true;

        SQLiteConnection connection = new SQLiteConnection("database");

        Table<User> userTable = TableManager.create(User.class, connection);
        Table<Order> orderTable = TableManager.create(Order.class, connection);

        User user = new User("User");
        Order order = new Order("Product", user);


        userTable.insert(user);
        orderTable.insert(order);



    }
}