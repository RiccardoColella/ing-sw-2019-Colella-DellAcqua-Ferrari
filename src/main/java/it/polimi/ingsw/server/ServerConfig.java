package it.polimi.ingsw.server;

/**
 * This class represents the initial configuration of the game server
 *
 * @author Carlo Dell'Acqua
 */
public class ServerConfig {
    /**
     * Maximum number of parallel matches
     */
    private int maxParallelMatches;

    /**
     * Time to wait before starting a match with at least the minimum amount of participants
     */
    private int matchStartTimeout;

    /**
     * The maximum time to wait before considering the client disconnected
     */
    private int clientAnswerTimeout;

    /**
     * The minimum amount of participants needed for a match
     */
    private int minClients;

    /**
     * The maximum amount of participants that a match can support
     */
    private int maxClients;

    /**
     * RMI listening port
     */
    private int rmiPort;

    /**
     * Socket listening port
     */
    private int socketPort;

    public int getMaxParallelMatches() {
        return maxParallelMatches;
    }

    public void setMaxParallelMatches(int maxParallelMatches) {
        this.maxParallelMatches = maxParallelMatches;
    }

    public int getMatchStartTimeout() {
        return matchStartTimeout;
    }

    public void setMatchStartTimeout(int matchStartTimeout) {
        this.matchStartTimeout = matchStartTimeout;
    }

    public int getMinClients() {
        return minClients;
    }

    public void setMinClients(int minClients) {
        this.minClients = minClients;
    }

    public int getMaxClients() {
        return maxClients;
    }

    public void setMaxClients(int maxClients) {
        this.maxClients = maxClients;
    }

    public int getClientAnswerTimeout() {
        return clientAnswerTimeout;
    }

    public void setClientAnswerTimeout(int clientAnswerTimeout) {
        this.clientAnswerTimeout = clientAnswerTimeout;
    }

    public int getRMIPort() {
        return rmiPort;
    }

    public void setRMIPort(int rmiPort) {
        this.rmiPort = rmiPort;
    }

    public int getSocketPort() {
        return socketPort;
    }

    public void setSocketPort(int socketPort) {
        this.socketPort = socketPort;
    }
}
