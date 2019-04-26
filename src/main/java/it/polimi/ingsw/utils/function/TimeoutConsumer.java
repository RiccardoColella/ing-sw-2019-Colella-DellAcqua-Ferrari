package it.polimi.ingsw.utils.function;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public interface TimeoutConsumer<T> {

    /**
     * Performs this operation on the given argument.
     *
     * @param t the input argument
     * @throws TimeoutException thrown if the consumer is interrupted by an internal exception
     * @throws IOException
     */
    void accept(T t) throws IOException;
}
