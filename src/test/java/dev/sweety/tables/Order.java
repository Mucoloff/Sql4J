package dev.sweety.tables;

import dev.sweety.api.sql4j.table.Info;
import dev.sweety.api.sql4j.field.DataField;
import dev.sweety.api.sql4j.field.ForeignKey;
import dev.sweety.api.sql4j.field.PrimaryKey;

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