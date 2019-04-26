package it.polimi.ingsw.utils.function;
import it.polimi.ingsw.utils.function.exceptions.UnsafeConsumerException;

public interface UnsafeConsumer<T> {

    /**
     * Performs this operation on the given argument.
     *
     * @param t the input argument
     * @throws UnsafeConsumerException thrown if the consumer is interrupted by an internal exception
     */
    void accept(T t);
}
