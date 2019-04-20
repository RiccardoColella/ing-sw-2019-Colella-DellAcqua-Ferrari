package it.polimi.ingsw.utils;

public interface TriConsumer<T, U, V> {
    void apply(T t, U u, V v);
}
