package dev.sweety.tables;

import dev.sweety.annotations.table.Info;
import dev.sweety.annotations.field.DataField;
import dev.sweety.annotations.field.ForeignKey;
import dev.sweety.annotations.field.PrimaryKey;

/**
 * @author mk$weety
 * Order represents an order in the system with a product and a user.
 */
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

    /**
     * Default constructor for Order.
     */
    public Order() {
    }

    public String product() {
        return product;
    }

    public User user() {
        return user;
    }

    /**
     * Constructs a new Order with the specified product and user.
     *
     * @param product the product name
     * @param user    the user who placed the order
     */
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