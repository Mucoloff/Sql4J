package dev.sweety.tables;

import dev.sweety.sql4j.api.table.Info;
import dev.sweety.sql4j.api.field.DataField;
import dev.sweety.sql4j.api.field.ForeignKey;
import dev.sweety.sql4j.api.field.PrimaryKey;

@Info(name = "orders")
public class Order {

    @DataField
    @PrimaryKey(autoIncrement = true)
    int orderId;

    @DataField
    String product;

    @DataField
    @ForeignKey
    User user;


    public Order() {
    }

    public String product() {
        return product;
    }

    public User user() {
        return user;
    }

    public Order(String product, User user) {
        this.product = product;
        this.user = user;
    }

    @Override
    public String toString() {
        return "Order{" +
                "orderId=" + orderId +
                ", product='" + product + '\'' +
                ", user=" + user +
                '}';
    }
}