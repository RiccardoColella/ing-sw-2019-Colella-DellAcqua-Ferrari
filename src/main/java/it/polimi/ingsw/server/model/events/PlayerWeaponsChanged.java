package it.polimi.ingsw.server.model.events;

import it.polimi.ingsw.server.model.Player;

import java.util.EventObject;

public class PlayerWeaponsChanged extends EventObject {

    /**
     *
     * @param ownerPlayer the source object
     */
    public PlayerWeaponsChanged(Player ownerPlayer) {
        super(ownerPlayer);
    }
}
