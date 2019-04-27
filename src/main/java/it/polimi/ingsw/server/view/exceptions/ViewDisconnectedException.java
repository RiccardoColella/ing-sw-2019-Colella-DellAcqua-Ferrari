package it.polimi.ingsw.server.view.exceptions;

/**
 * This exception should be thrown if a client view disconnects (logically) from the server
 *
 * @author Carlo Dell'Acqua
 */
public class ViewDisconnectedException extends RuntimeException {
    /**
     * @param reason the reason why the exception has been thrown
     */
    public ViewDisconnectedException(String reason) {
        super(reason);
    }

    /**
     * @param reason the reason why the exception has been thrown
     * @param cause the previous exception
     */
    public ViewDisconnectedException(String reason, Throwable cause) {
        super(reason, cause);
    }
}
