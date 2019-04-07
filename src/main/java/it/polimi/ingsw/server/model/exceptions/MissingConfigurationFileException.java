package it.polimi.ingsw.server.model.exceptions;

public class MissingConfigurationFileException extends RuntimeException {
    /**
     * This constructor generates an Exception with only a string
     *
     * @param reason the reason why the exception has been thrown
     */
    public MissingConfigurationFileException(String reason) {
        super(reason);
    }
}
