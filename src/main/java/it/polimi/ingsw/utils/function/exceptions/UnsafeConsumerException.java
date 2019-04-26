package it.polimi.ingsw.utils.function.exceptions;

/**
 * This exception should be thrown if the consumer can't accept the value due to internal errors
 *
 * @author Carlo Dell'Acqua
 */
public class UnsafeConsumerException extends RuntimeException {
    /**
     * @author Carlo Dell'Acqua
     * @param reason the reason why the exception has been thrown
     */
    public UnsafeConsumerException(String reason) {
        super(reason);
    }

    /**
     * @author Carlo Dell'Acqua
     * @param reason the reason why the exception has been thrown
     * @param cause the previous exception
     */
    public UnsafeConsumerException(String reason, Throwable cause) {
        super(reason, cause);
    }
}
