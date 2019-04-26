package it.polimi.ingsw.server;

/**
 * This class represents the initial configuration of the game server
 *
 * @author Carlo Dell'Acqua
 */
public class ServerConfig {
    private int maxParallelMatches;
    private int matchStartTimeout;
    private int clientAcceptTimeout;
    private int clientAnswerTimeout;
    private int minClients;
    private int maxClients;
    private int rmiPort;
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

    public int getClientAcceptTimeout() {
        return clientAcceptTimeout;
    }

    public void setClientAcceptTimeout(int clientAcceptTimeout) {
        this.clientAcceptTimeout = clientAcceptTimeout;
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
