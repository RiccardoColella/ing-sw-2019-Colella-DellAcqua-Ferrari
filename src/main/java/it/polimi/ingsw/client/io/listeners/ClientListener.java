package it.polimi.ingsw.client.io.listeners;

import it.polimi.ingsw.shared.events.networkevents.ClientEvent;

/**
 * Interface shared between client and server of a class that will react to the status changes of a client
 */
public interface ClientListener {
    /**
     * This method is used to notify a successful login
     * @param e the client event
     */
    void onLoginSuccess(ClientEvent e);

    /**
     * This method is used to notify a disconnection
     * @param e the client event
     */
    void onClientDisconnected(ClientEvent e);

}
