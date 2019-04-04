package it.polimi.ingsw.server.model.exceptions;

/**
 * This exception is thrown when an Enum is taken as a parameter and it is either null when it is not expected, or it has an unrecognizable value
 */
public class UnknownEnumException extends Exception {
    /**
     * This constructor generates an Exception with only a string
     *
     * @param reason the reason why the exception has been thrown
     */
    public UnknownEnumException(String reason) {
        super(reason);
    }
}
