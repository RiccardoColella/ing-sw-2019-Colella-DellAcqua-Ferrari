package it.polimi.ingsw.server.model.events;

import it.polimi.ingsw.server.model.Player;

import java.util.EventObject;

public class PlayerDied extends EventObject {

    private Player victim;
    private Player killer;
    private boolean wasOverkilled;

    /**
     *
     * @param victim the source object
     * @param killer the player who stroke the deadshot
     * @param wasOverkilled true if the player got an extra shot after the deadshot
     */
    public PlayerDied(Player victim, Player killer, boolean wasOverkilled) {
        super(victim);
        this.killer = killer;
        this.victim = victim;
        this.wasOverkilled = wasOverkilled;
    }

    public Player getVictim() {
        return victim;
    }

    public Player getKiller() {
        return killer;
    }

    public boolean wasOverkilled() {
        return wasOverkilled;
    }
}
