package it.polimi.ingsw.server.model.events;

import it.polimi.ingsw.server.model.Player;

import java.util.EventObject;

public class PlayerDamaged extends EventObject {

    /**
     *
     * @param victim the source object
     */
    public PlayerDamaged(Player victim) {
        super(victim);
    }
}
