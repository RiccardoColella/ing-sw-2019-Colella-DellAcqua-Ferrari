package it.polimi.ingsw.utils.function;

import java.io.IOException;

/**
 * A simple consumer that might throw an IOException during it's execution
 *
 * @author Carlo Dell'Acqua
 * @param <T> the type of the item to consume
 */
public interface IOConsumer<T> {

    /**
     * Performs this operation on the given argument.
     *
     * @param t the input argument
     * @throws IOException if the consumer couldn't write result
     */
    void accept(T t) throws IOException;
}
