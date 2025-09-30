package dev.sweety.tables;

import dev.sweety.sql4j.api.table.Info;
import dev.sweety.sql4j.api.field.DataField;
import dev.sweety.sql4j.api.field.PrimaryKey;

@Info(name = "users")
public class User {

    @DataField
    @PrimaryKey(autoIncrement = true)
    int id;

    @DataField
    String name;

    public User() {
    }

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