package dev.sweety.sql4j.api.adapter;

public interface Adapter<T> {

    String serialize(T value);
    T deserialize(String value);

}
