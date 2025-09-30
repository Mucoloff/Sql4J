package dev.sweety;

import com.google.gson.Gson;
import dev.sweety.sql4j.impl.connection.SQLiteConnection;
import dev.sweety.sql4j.impl.table.Table;
import dev.sweety.sql4j.impl.table.TableManager;
import dev.sweety.tables.Order;
import dev.sweety.tables.User;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class MainAsync {

    public static void main(String[] args) throws Exception {

        Gson gson = new Gson().newBuilder().setPrettyPrinting().disableHtmlEscaping().create();

        SQLiteConnection connection = new SQLiteConnection("database.async");

        Table<User> userTable = TableManager.create(User.class, connection);
        Table<Order> orderTable = TableManager.create(Order.class, connection);

        User user = new User("UserSync");
        Order order = new Order("ProductSync", user);

        CompletableFuture<Void> insertUser = userTable.insertAsync(user);
        CompletableFuture<Void> allInserts = insertUser.thenRunAsync(() -> orderTable.insertAsync(order));

        allInserts.thenCompose(v -> {
            CompletableFuture<List<User>> usersFuture = userTable.selectAllAsync();
            CompletableFuture<List<Order>> ordersFuture = orderTable.selectAllAsync();

            return usersFuture.thenCombine(ordersFuture, (usersSync, ordersSync) -> {
                gson.toJson(usersSync, System.out);
                gson.toJson(ordersSync, System.out);
                return null;
            });
        }).join();
    }
}