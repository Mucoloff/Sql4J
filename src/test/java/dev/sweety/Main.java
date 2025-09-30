// File: src/test/java/dev/sweety/Main.java
package dev.sweety;

import com.google.gson.Gson;
import dev.sweety.sql4j.api.SqlUtils;
import dev.sweety.sql4j.impl.connection.SQLiteConnection;
import dev.sweety.sql4j.impl.table.Table;
import dev.sweety.sql4j.impl.table.TableManager;
import dev.sweety.tables.Order;
import dev.sweety.tables.User;

import java.util.List;

public class Main {

    public static void main(String[] args) throws Exception {
        SqlUtils.DEBUG = true;

        Gson gson = new Gson().newBuilder().setPrettyPrinting().disableHtmlEscaping().create();

        SQLiteConnection connection = new SQLiteConnection("database");

        // Sincrono
        Table<User> userTable = TableManager.create(User.class, connection);
        Table<Order> orderTable = TableManager.create(Order.class, connection);

        User user = new User("UserSync");
        Order order = new Order("ProductSync", user);

        userTable.insert(user);
        orderTable.insert(order);

        List<User> usersSync = userTable.selectAll();
        List<Order> ordersSync = orderTable.selectAll();

        gson.toJson(usersSync, System.out);
        gson.toJson(ordersSync, System.out);


    }
}