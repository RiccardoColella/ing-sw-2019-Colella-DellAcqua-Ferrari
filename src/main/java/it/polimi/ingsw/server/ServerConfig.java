package it.polimi.ingsw.server;

/**
 * This class represents the initial configuration of the game server
 */
public class ServerConfig {
    private int maxParallelMatches;
    private int matchStartTimeout;
    private int clientAcceptTimeout;
    private int minClients;
    private int maxClients;

    public ServerConfig(int maxParallelMatches, int matchStartTimeout, int clientAcceptTimeout, int minClients, int maxClients) {
        this.maxParallelMatches = maxParallelMatches;
        this.matchStartTimeout = matchStartTimeout;
        this.clientAcceptTimeout = clientAcceptTimeout;
        this.minClients = minClients;
        this.maxClients = maxClients;
    }

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

    public int getRMIPort() {
        return 0;
    }

    public int getSocketPort() {
        return 0;
    }
}
