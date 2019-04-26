package it.polimi.ingsw.server.model.exceptions;

/**
 * This exception should be thrown if the acquisition of an object violates the constraints imposed by the game
 *
 * @author Adriana Ferrari
 */
public class UnauthorizedExchangeException extends RuntimeException {
    /**
     * @author Adriana Ferrari
     * @param reason the reason why the exception has been thrown
     */
    public UnauthorizedExchangeException(String reason) {
        super(reason);
    }

    /**
     * @author Adriana Ferrari
     * @param reason the reason why the exception has been thrown
     * @param cause the previous exception
     */
    public UnauthorizedExchangeException(String reason, Throwable cause) {
        super(reason, cause);
    }
}
