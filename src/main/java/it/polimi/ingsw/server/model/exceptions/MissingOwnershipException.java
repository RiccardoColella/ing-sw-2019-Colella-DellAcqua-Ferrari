package it.polimi.ingsw.server.model.exceptions;

/**
 * This exception should be thrown if a player uses a resource that does not belong to him
 *
 * @author Carlo Dell'Acqua
 */
public class MissingOwnershipException extends RuntimeException {
    /**
     * @param reason the reason why the exception has been thrown
     */
    public MissingOwnershipException(String reason) {
        super(reason);
    }

    /**
     * @param reason the reason why the exception has been thrown
     * @param cause the previous exception
     */
    public MissingOwnershipException(String reason, Throwable cause) {
        super(reason, cause);
    }
}
