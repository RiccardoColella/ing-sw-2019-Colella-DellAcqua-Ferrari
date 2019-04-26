package it.polimi.ingsw.utils.function;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public interface TimeoutSupplier<T> {


    /**
     * Gets a result.
     *
     * @return a result
     * @throws TimeoutException thrown if the supplier is interrupted by an internal exception
     * @throws IOException
     */
    T get(int timeout, TimeUnit unit) throws TimeoutException, IOException;
}
