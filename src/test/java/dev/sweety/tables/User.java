package dev.sweety.tables;

import dev.sweety.table.Table;
import dev.sweety.table.fields.DataField;
import dev.sweety.table.fields.PrimaryKey;

/**
 * @author mk$weety
 * User represents a user in the system with an ID and a name.
 */
@Table.Info(name = "users")
public class User {

    @DataField
    @PrimaryKey(autoIncrement = true)
    int id;

    @DataField
    String name;

    /**
     * Default constructor for User.
     */
    public User() {
    }

    /**
     * Constructs a new User with the specified name.
     *
     * @param name the user's name
     */
    public User(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}