package it.polimi.ingsw.server.model.events;

import java.util.EventObject;

public class ActiveWeaponChanged extends EventObject {
    /**
     * @param source the source object
     */
    public ActiveWeaponChanged(Object source) {
        super(source);
    }
}
