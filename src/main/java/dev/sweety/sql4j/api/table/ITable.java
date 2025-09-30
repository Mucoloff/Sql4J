package dev.sweety.sql4j.api.table;

import dev.sweety.sql4j.api.connection.SQLConnection;
import dev.sweety.sql4j.api.field.IField;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

public interface ITable<T> {

    String name();

    Class<T> clazz();

    SQLConnection connection();

    IField primaryKey();

    void insert(T entity);

    CompletableFuture<Void> insertAsync(T entity);

    List<T> selectWhere(String filter, Object... params);

    CompletableFuture<List<T>> selectWhereAsync(String filter, Object... params);

    List<T> selectAll();

    CompletableFuture<List<T>> selectAllAsync();

    default List<T> select(Predicate<T>... predicates) {
        List<T> resultList = selectAll();

        Predicate<T> combinedPredicate = Arrays.stream(predicates).reduce(x -> true, Predicate::and);

        return resultList.stream()
                .filter(combinedPredicate)
                .toList();
    }

    default CompletableFuture<List<T>> selectAsync(Predicate<T>... predicates) {

        return selectAllAsync().thenApply(resultList -> {
            Predicate<T> combinedPredicate = Arrays.stream(predicates).reduce(x -> true, Predicate::and);

            return resultList.stream()
                    .filter(combinedPredicate)
                    .toList();
        });
    }

    void update(T entity);

    CompletableFuture<Void> updateAsync(T entity);

    void delete(T entity);

    CompletableFuture<Integer> deleteAsync(T entity);

    void delete(String query, Object... params);

    CompletableFuture<Integer> deleteAsync(String query, Object... params);

}
