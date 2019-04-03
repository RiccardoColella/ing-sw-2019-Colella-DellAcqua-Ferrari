package it.polimi.ingsw.server.model.exceptions;

public class TypeMismatchException extends Exception {

    /**
     * This constructor generates an Exception with only a string
     *
     * @param reason the reason why the exception has been thrown
     */
    public TypeMismatchException(String reason) {
        super(reason);
    }
}
