package dev.sweety.fields.adapters;

public interface Adapter<T> {
    String serialize(T value);

    T deserialize(String value);
}
