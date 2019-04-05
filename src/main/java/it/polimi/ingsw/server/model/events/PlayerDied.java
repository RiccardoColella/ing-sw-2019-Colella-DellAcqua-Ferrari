package it.polimi.ingsw.server.model.events;

import it.polimi.ingsw.server.model.player.Player;

import java.util.EventObject;

public class PlayerDied extends EventObject {

    private final Player killer;

    /**
     *
     * @param victim the source object
     * @param killer the player who stroke the deadshot
     */
    public PlayerDied(Player victim, Player killer) {
        super(victim);
        this.killer = killer;
    }

    public Player getVictim() {
        return (Player) this.getSource();
    }

    public Player getKiller() {
        return killer;
    }
}
