package it.polimi.ingsw.utils.function;
import it.polimi.ingsw.utils.function.exceptions.UnsafeSupplierException;

public interface UnsafeSupplier<T> {


    /**
     * Gets a result.
     *
     * @return a result
     * @throws UnsafeSupplierException thrown if the supplier is interrupted by an internal exception
     */
    T get();
}
