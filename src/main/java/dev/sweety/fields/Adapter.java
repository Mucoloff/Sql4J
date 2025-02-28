package dev.sweety.fields;

public interface Adapter<T> {
    String serialize(T value);

    T deserialize(String value);
}
