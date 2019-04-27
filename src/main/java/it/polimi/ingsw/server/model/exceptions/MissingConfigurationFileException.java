package it.polimi.ingsw.server.model.exceptions;

/**
 * This exception should be thrown if the expected configuration file has not been found
 *
 * @author Carlo Dell'Acqua
 */
public class MissingConfigurationFileException extends RuntimeException {
    /**
     * @param reason the reason why the exception has been thrown
     */
    public MissingConfigurationFileException(String reason) {
        super(reason);
    }

    /**
     * @param reason the reason why the exception has been thrown
     * @param cause the previous exception
     */
    public MissingConfigurationFileException(String reason, Throwable cause) {
        super(reason, cause);
    }
}
