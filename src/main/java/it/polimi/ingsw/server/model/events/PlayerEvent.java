package it.polimi.ingsw.server.model.events;

import it.polimi.ingsw.server.model.player.Player;

import java.util.EventObject;

public class PlayerEvent extends EventObject {

    public PlayerEvent(Player player) {
        super(player);
    }

    public Player getPlayer() {
        return (Player)source;
    }
}
