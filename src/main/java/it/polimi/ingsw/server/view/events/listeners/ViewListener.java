package it.polimi.ingsw.server.view.events.listeners;

import it.polimi.ingsw.server.view.events.ViewEvent;

import java.util.EventListener;

/**
 * Interface of a class that will react to the status changes of a view
 */
public interface ViewListener extends EventListener {

    /**
     * This method is called when a view disconnects
     * @param e the event corresponding to the view disconnection
     */
    void onViewDisconnected(ViewEvent e);

    /**
     * This method is called when a view is ready
     * @param e the event corresponding to the view being ready
     */
    void onViewReady(ViewEvent e);
}
