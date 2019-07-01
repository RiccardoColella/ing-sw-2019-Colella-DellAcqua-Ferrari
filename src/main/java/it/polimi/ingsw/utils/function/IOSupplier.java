package it.polimi.ingsw.utils.function;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * A simple supplier interface that might throw an IOException during it's execution
 *
 * @param <T> the type of the item to supply
 *
 * @author Carlo Dell'Acqua
 */
public interface IOSupplier<T> {


    /**
     * Gets a result.
     *
     * @param timeout the maximum timeout
     * @param unit the measurement unit of the timeout
     * @return a result
     * @throws TimeoutException if the supplier couldn't get a message within the specified timeout
     * @throws IOException if the supplier couldn't read from the resource
     */
    T get(int timeout, TimeUnit unit) throws TimeoutException, IOException;
}
