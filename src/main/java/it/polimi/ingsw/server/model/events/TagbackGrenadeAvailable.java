package it.polimi.ingsw.server.model.events;

import it.polimi.ingsw.server.model.Player;

import java.util.EventObject;

public class TagbackGrenadeAvailable extends EventObject {

    /**
     *
     * @param ownerPlayer the source object
     */
    public TagbackGrenadeAvailable(Player ownerPlayer) {
        super(ownerPlayer);
    }
}
