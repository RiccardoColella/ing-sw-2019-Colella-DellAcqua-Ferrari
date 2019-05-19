package it.polimi.ingsw.client.io.listeners;

import it.polimi.ingsw.shared.events.networkevents.ClientEvent;

public interface ClientListener {
    void onLoginSuccess(ClientEvent e);

    void onClientDisconnected(ClientEvent e);

}
