package dev.sweety.tables;

import dev.sweety.annotations.table.Info;
import dev.sweety.annotations.field.DataField;
import dev.sweety.annotations.field.PrimaryKey;

/**
 * @author mk$weety
 * User represents a user in the system with an ID and a name.
 */
@Info(name = "users")
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