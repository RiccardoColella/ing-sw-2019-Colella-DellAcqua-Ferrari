package it.polimi.ingsw.server.model.exceptions;

/**
 * This exception is thrown when the acquisition of an object violates the constraints imposed by the game
 */
public class UnauthorizedGrabException extends Exception {
    /**
     * This constructor generates an Exception with only a string
     *
     * @param reason the reason why the exception has been thrown
     */
    public UnauthorizedGrabException(String reason) {
        super(reason);
    }
}
