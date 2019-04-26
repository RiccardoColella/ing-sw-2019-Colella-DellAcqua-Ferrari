package it.polimi.ingsw.shared.view.remote.events;

import java.util.EventObject;

public class MessageDispatcherStopped extends EventObject {
    /**
     * Constructs a MessageDispatcherStopped Event.
     *
     * @param source The object on which the Event initially occurred.
     */
    public MessageDispatcherStopped(Object source) {
        super(source);
    }
}
