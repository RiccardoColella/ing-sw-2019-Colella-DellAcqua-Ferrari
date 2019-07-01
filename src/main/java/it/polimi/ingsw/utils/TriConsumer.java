package it.polimi.ingsw.utils;

/**
 * A generic consumer of three items
 *
 * @param <T> the type of the first item
 * @param <U> the type of the second item
 * @param <V> the type of the third item
 *
 * @author Adriana Ferrari
 */
public interface TriConsumer<T, U, V> {
    /**
     * Consumes three items
     *
     * @param t the first item to consume
     * @param u the second item to consume
     * @param v the third item to consume
     */
    void accept(T t, U u, V v);
}
