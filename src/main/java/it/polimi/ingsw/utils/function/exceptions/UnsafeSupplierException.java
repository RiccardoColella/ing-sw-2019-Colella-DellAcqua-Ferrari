package it.polimi.ingsw.utils.function.exceptions;

/**
 * This exception should be thrown if the supplier can't provide a valid result due to internal errors
 *
 * @author Carlo Dell'Acqua
 */
public class UnsafeSupplierException extends RuntimeException {
    /**
     * @author Carlo Dell'Acqua
     * @param reason the reason why the exception has been thrown
     */
    public UnsafeSupplierException(String reason) {
        super(reason);
    }

    /**
     * @author Carlo Dell'Acqua
     * @param reason the reason why the exception has been thrown
     * @param cause the previous exception
     */
    public UnsafeSupplierException(String reason, Throwable cause) {
        super(reason, cause);
    }
}
