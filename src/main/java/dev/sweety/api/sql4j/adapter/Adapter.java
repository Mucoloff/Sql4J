package dev.sweety.api.sql4j.adapter;

public interface Adapter<T> {

    String serialize(T value);
    T deserialize(String value);

}
