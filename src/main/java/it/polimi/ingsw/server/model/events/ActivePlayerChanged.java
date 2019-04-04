package it.polimi.ingsw.server.model.events;

import java.util.EventObject;

public class ActivePlayerChanged extends EventObject {
    /**
     *
     * @param source the source object
     */
    public ActivePlayerChanged(Object source) {
        super(source);
    }
}
