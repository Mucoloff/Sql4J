package dev.sweety.tables;

import dev.sweety.table.Table;
import dev.sweety.fields.DataField;
import dev.sweety.fields.ForeignKey;
import dev.sweety.fields.PrimaryKey;

/**
 * @author mk$weety
 * Order represents an order in the system with a product and a user.
 */
@Table.Info(name = "orders")
public class Order {

    @DataField
    @PrimaryKey(autoIncrement = true)
    int orderId;

    @DataField
    String product;

    @DataField
    @ForeignKey(tableId = "id")
    User user;

    /**
     * Default constructor for Order.
     */
    public Order() {
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