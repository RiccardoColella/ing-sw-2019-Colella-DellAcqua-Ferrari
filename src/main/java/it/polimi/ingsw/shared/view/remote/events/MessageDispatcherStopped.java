package it.polimi.ingsw.shared.view.remote.events;

import java.util.EventObject;

/**
 * Creates a MessageDispatcherStopped event to enable attached objects to manage the associated resources
 */
public class MessageDispatcherStopped extends EventObject {
    /**
     * Constructs a MessageDispatcherStopped event.
     *
     * @param source the object on which the Event initially occurred.
     */
    public MessageDispatcherStopped(Object source) {
        super(source);
    }
}
