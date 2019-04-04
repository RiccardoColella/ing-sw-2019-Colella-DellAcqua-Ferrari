package it.polimi.ingsw.server.model.events;

import it.polimi.ingsw.server.model.Player;

import java.util.EventObject;

public class PlayerMoved extends EventObject {
    /**
     *
     * @param movedPlayer the source object
     */
    public PlayerMoved(Player movedPlayer) {
        super(movedPlayer);
    }
}
