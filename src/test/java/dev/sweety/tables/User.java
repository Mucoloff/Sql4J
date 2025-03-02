package dev.sweety.tables;

import dev.sweety.api.sql4j.table.Info;
import dev.sweety.api.sql4j.field.DataField;
import dev.sweety.api.sql4j.field.PrimaryKey;

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