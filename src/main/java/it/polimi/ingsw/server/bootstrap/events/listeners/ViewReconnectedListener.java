package it.polimi.ingsw.server.bootstrap.events.listeners;

import it.polimi.ingsw.server.bootstrap.events.ViewReconnected;

/**
 * A listener of the View reconnection event
 *
 * @author Carlo Dell'Acqua
 */
public interface ViewReconnectedListener {
    void onViewReconnected(ViewReconnected e);
}
