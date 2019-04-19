package it.polimi.ingsw.server.controller.exceptions;

public class ViewDisconnectedException extends RuntimeException {
    /**
     * This constructor generates an Exception with only a string
     *
     * @param reason the reason why the exception has been thrown
     */
    public ViewDisconnectedException(String reason) {
        super(reason);
    }
}
