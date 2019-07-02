package it.polimi.ingsw.server.bootstrap.events.listeners;

import it.polimi.ingsw.server.bootstrap.events.ViewReconnected;

/**
 * A listener of the View reconnection event
 *
 * @author Carlo Dell'Acqua
 */
public interface ViewReconnectedListener {
    /**
     * Method called on view reconnection
     *
     * @param e the view reconnected event object
     */
    void onViewReconnected(ViewReconnected e);
}
