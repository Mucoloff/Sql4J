package dev.sweety.api;

public interface Adapter<T> {
    String serialize(T value);

    T deserialize(String value);
}
