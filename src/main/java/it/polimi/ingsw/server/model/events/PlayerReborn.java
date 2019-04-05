package it.polimi.ingsw.server.model.events;

import it.polimi.ingsw.server.model.player.Player;

import java.util.EventObject;

public class PlayerReborn extends EventObject {

    /**
     *
     * @param phoenix the source object
     */
    public PlayerReborn(Player phoenix) {
        super(phoenix);
    }

    public Player getPhoenix() {
        return (Player) this.getSource();
    }
}
