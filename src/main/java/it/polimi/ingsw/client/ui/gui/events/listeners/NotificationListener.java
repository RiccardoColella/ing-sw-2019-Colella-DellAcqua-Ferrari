package it.polimi.ingsw.client.ui.gui.events.listeners;

import it.polimi.ingsw.client.ui.gui.events.NotificationClosed;

/**
 * Interface for the listeners of the GUI notification panes
 *
 * @author Adriana Ferrari
 */
public interface NotificationListener {

    /**
     * A notification has been closed and the listener can act accordingly
     *
     * @param e the NotificationClosed event
     */
    void onNotificationClosed(NotificationClosed e);
}
