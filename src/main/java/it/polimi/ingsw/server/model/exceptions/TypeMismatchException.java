package it.polimi.ingsw.server.model.exceptions;

/**
 * This exception should be thrown if there is a mismatch between the actual and expected types
 *
 * @author Carlo Dell'Acqua
 */
public class TypeMismatchException extends RuntimeException {
    /**
     * @author Carlo Dell'Acqua
     * @param reason the reason why the exception has been thrown
     */
    public TypeMismatchException(String reason) {
        super(reason);
    }

    /**
     * @author Carlo Dell'Acqua
     * @param reason the reason why the exception has been thrown
     * @param cause the previous exception
     */
    public TypeMismatchException(String reason, Throwable cause) {
        super(reason, cause);
    }
}
