package it.polimi.ingsw.server.model.events;

import it.polimi.ingsw.server.model.player.Player;

import java.util.EventObject;

/**
 * A generic player event
 */
public class PlayerEvent extends EventObject {

    /**
     * Constructs a player event
     *
     * @param player the player who is firing this event
     */
    public PlayerEvent(Player player) {
        super(player);
    }

    /**
     * @return the player who is firing this event
     */
    public Player getPlayer() {
        return (Player)source;
    }
}
