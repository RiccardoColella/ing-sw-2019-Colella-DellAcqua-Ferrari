package it.polimi.ingsw.client.ui.gui.events;

import java.util.EventObject;

/**
 * Event class for when a notification gets closed
 *
 * @author Adriana Ferrari
 */
public class NotificationClosed extends EventObject {
    /**
     * Constructs a prototypical Event.
     *
     * @param source the object on which the Event initially occurred
     * @throws IllegalArgumentException if source is null
     */
    public NotificationClosed(Object source) {
        super(source);
    }
}
