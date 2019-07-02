package it.polimi.ingsw.client;

/**
 * This class represents the initial configuration of the game client
 *
 * @author Carlo Dell'Acqua
 */
public class ClientConfig {
    /**
     * RMI listening port
     */
    private int rmiPort;

    /**
     * Socket listening port
     */
    private int socketPort;

    public int getRMIPort() {
        return rmiPort;
    }

    public int getSocketPort() {
        return socketPort;
    }
}
