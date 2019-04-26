package it.polimi.ingsw.server.model.exceptions;

/**
 * This exception should be thrown if the configuration file does not provide valid rules
 *
 * @author Adriana Ferrari
 */
public class IncoherentConfigurationException extends RuntimeException {
    /**
     * @author Adriana Ferrari
     * @param reason the reason why the exception has been thrown
     */
    public IncoherentConfigurationException(String reason) {
        super(reason);
    }

    /**
     * @author Adriana Ferrari
     * @param reason the reason why the exception has been thrown
     * @param cause the previous exception
     */
    public IncoherentConfigurationException(String reason, Throwable cause) {
        super(reason, cause);
    }
}
