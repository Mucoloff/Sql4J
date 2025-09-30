package dev.sweety;

import dev.sweety.sql4j.api.SqlUtils;
import dev.sweety.sql4j.api.UUIDv8Hybrid;
import dev.sweety.sql4j.impl.connection.SQLiteConnection;
import dev.sweety.sql4j.impl.table.Table;
import dev.sweety.sql4j.impl.table.TableManager;
import dev.sweety.tables.Order;
import dev.sweety.tables.User;

import java.time.ZoneId;
import java.util.Date;
import java.util.UUID;

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