package it.polimi.ingsw.server.model.exceptions;

public class IncoherentConfigurationException extends RuntimeException {
    /**
     * This constructor generates an Exception with only a string
     *
     * @param reason the reason why the exception has been thrown
     */
    public IncoherentConfigurationException(String reason) {
        super(reason);
    }
}
